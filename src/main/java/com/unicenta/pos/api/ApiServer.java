/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unicenta.pos.api;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.AppProperties;
import com.openbravo.pos.forms.AppUser;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.JRootApp;
import com.openbravo.pos.sales.DataLogicReceipts;
import com.openbravo.pos.sales.TaxesLogic;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.TaxInfo;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import com.unicenta.pos.api.JSONOrder.Converter;
import com.openbravo.pos.util.AltEncrypter;
import com.openbravo.pos.util.Hashcypher;
import com.unicenta.pos.api.JSONOrder.JSONOrder;
import com.unicenta.pos.api.JSONOrder.Line;
import spark.Request;
import spark.Response;
import spark.Spark;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static spark.Spark.*;


/**
 * @author tudor
 */
public class ApiServer {

    private static final Logger logger = Logger.getLogger("ApiServer");

    private JRootApp app;
    private boolean running;
    private DSL DSL;
    private TicketDSL ticketDSL;
    private Cache cacheProducts = null;
    private Cache cacheFloors = null;
    private Cache cacheTickets = null;
    private Cache cacheImages = null;
    private JWTStore jwtStore = JWTStore.instance("123456");

    // encryption settings
    // since there is no easy way to run HTTPS on a LAN server
    // we use a shared private key


    private static String AESKey = "a disturbing secret";
    private static boolean useEncryption = true; //set to false in dev mode for easier debugging

    public ApiServer(JRootApp app) {
        this.running = false;
        this.app = app;

        DSL = (DSL) app.getBean("com.unicenta.pos.api.DSL");
        DataLogicReceipts receiptsLogic = (DataLogicReceipts) app.getBean("com.openbravo.pos.sales.DataLogicReceipts");
        DSL.setReceiptsLogic(receiptsLogic);

        DataLogicSales salesLogic = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
        DSL.setSalesLogic(salesLogic);
        TaxesLogic taxesLogic;
        try {
            taxesLogic = new TaxesLogic(salesLogic.getTaxList().list());
            DSL.setTaxesLogic(taxesLogic);
        } catch (BasicException e) {
            e.printStackTrace();
//            TODO bail out
        }

        ticketDSL = (TicketDSL) app.getBean("com.unicenta.pos.api.TicketDSL");
        ticketDSL.setReceiptsLogic(receiptsLogic);


        ticketDSL.setApp(app);


        cacheProducts = makeCache("routeProducts", 500);
        cacheFloors = makeCache("routeFloors", 500);
        cacheTickets = makeCache("routeSharedtickets", 0);
        cacheImages = makeCache("routeDBImage", 500);

        //Spark: log HTTP 500 expceptions
        Spark.exception(Exception.class, (exception, request, response) -> {
            exception.printStackTrace();
        });

    }

    private static JSONPayload createJSONPayload() {
        return new JSONPayload(useEncryption);
    }

    public static String encrypt(String payload) {
        if (!useEncryption) {
            return payload;
        }
        return AES256Cryptor.encrypt(payload, AESKey);
    }

    public static String decrypt(String payload) {
        if (!useEncryption) {
            return payload;
        }
        return AES256Cryptor.decrypt(payload, AESKey);
    }

    private Cache makeCache(String routeMethod, Integer size) {
        return CacheBuilder.newBuilder()
                .maximumSize(size)
//                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<HashMap, Object>() {
                            @Override
                            public Object load(HashMap params) throws Exception {
                                JSONPayload ret = createJSONPayload();
                                ret.setStatus("OK");
                                JsonElement data = null;
                                switch (routeMethod) {
                                    case "routeFloors":
                                        data = routeFloors(params);
                                        break;
                                    case "routeProducts":
                                        data = routeProducts(params);
                                        break;
                                    case "routeSharedtickets":
                                        data = routeSharedtickets(params);
                                        break;
                                    case "routeDBImage":
                                        return routeDBImage(params);

                                }
                                ret.setData(data);
                                return ret.getString();
                            }
                        }
                );
    }

    private JsonObject createGSON() {
        Gson gson = new Gson();
        JsonObject root = gson.fromJson("{}", JsonObject.class).getAsJsonObject();
        return root;
    }

    private JsonElement routeSharedtickets(Map params) throws BasicException {

        try {
            HashMap d = new HashMap();
            d.put("sharedtickets", DSL.listSharedTickets());
            d.put("_comment", "PLACEID:sharedticket map");
            Gson b = new GsonBuilder().serializeNulls().create();
            return b.toJsonTree(d);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
            throw new BasicException(e.getMessage(), e);
        }
    }


    private JsonElement routeUsers(Map params) throws BasicException {

        HashMap d = new HashMap();

        d.put("users", DSL.listUsers());
        d.put("roles", DSL.listRoles());

        Gson b = new GsonBuilder().serializeNulls().create();
        return b.toJsonTree(d);
    }

    private JsonElement routeFloors(Map params) throws BasicException {

        HashMap d = new HashMap();

        d.put("floors", DSL.listFloors());
        d.put("places", DSL.listPlaces());

        Gson b = new GsonBuilder().serializeNulls().create();
        return b.toJsonTree(d);
    }

    private JsonElement routeProducts(Map params) throws BasicException {

        HashMap d = new HashMap();
        d.put("tax_categories", DSL.listTaxCategories());
        d.put("taxes", DSL.listTaxes());
        d.put("categories", DSL.listProductCategories());
        d.put("products", DSL.listProducts());
        d.put("floors", DSL.listFloors());
        d.put("places", DSL.listPlaces());

        Gson b = new GsonBuilder().serializeNulls().create();
        return b.toJsonTree(d);
    }

    private JsonElement routeUpdateTicket(Map params) throws BasicException, IOException {

        //TODO!! parse req body, move this method into DSL,
        // cycle through lines and replace them  in the shared ticket

        HashMap d = new HashMap();

        JSONOrder order = Converter.fromJsonString(params.get("data").toString());
        String placeID = order.getPlaceID();
        TicketInfo ticketInfo = DSL.getTicketInfo(placeID);
        String userID = (String) params.get("userID");
        AppUser user = DSL.getAppUserByID(userID);
        logger.info("ORDER: " + Converter.toJsonString(order));
        logger.info("JWT User: " + userID);
        //TODO check locked status and user/role

        ticketInfo.setUser(user.getUserInfo());

        List<TicketLineInfo> lines = new ArrayList<>();
        NumberFormat nf = DecimalFormat.getInstance(Locale.getDefault());
        for (Line l : order.getLines()) {
            ProductInfoExt productInfo = DSL.salesLogic.getProductInfo(l.getProductID());
            productInfo.setName(
                    productInfo.getName()
                            + (l.getUm() == 1.0 ? "" : " (" + nf.format(l.getUm()) + ")"));
            TaxInfo tax = DSL.taxesLogic.getTaxInfo(
                    productInfo.getTaxCategoryID(),
                    ticketInfo.getCustomer()
            );
            TicketLineInfo line = new TicketLineInfo(
                    productInfo,
                    l.getMultiplier(),
                    l.getPrice(),
                    tax,
                    (Properties) (productInfo.getProperties().clone())
            );
            lines.add(line);
        }

        ticketInfo.setLines(lines);
        try {
            DSL.receiptsLogic.updateSharedTicket(placeID, ticketInfo, 0);

            //NOTE use 2nd arg null to unlock
            DSL.receiptsLogic.lockSharedTicket(placeID, "locked");
//            DSL.salesLogic.saveTicket(ticketInfo, app.getInventoryLocation());
        } catch (BasicException e) {
//            TODO!! return HTTP 500
            e.printStackTrace();
        }
        d.put("ticket", ticketInfo);

        Gson b = new GsonBuilder().serializeNulls().create();
        return b.toJsonTree(d);
    }

    HashMap routeDBImage(Map params) throws BasicException {
        HashMap ret = new HashMap();
        Object record = null;
        String hash = null;
        try {

            //currently "small" or "full"
            String size = (String) params.get("size");

            if (size.equals("small")) {

                record = DSL.getDBImageThumbnail(
                        (String) params.get("tableName"),
                        (String) params.get("pk")
                );
            } else {
                record = DSL.getDBImageBytes(
                        (String) params.get("tableName"),
                        (String) params.get("pk")
                );

            }
            if (record != null) {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] bytes = (byte[]) record;
                byte[] md5Arr = md.digest(bytes);
                String md5Str = new BigInteger(1, md5Arr).toString(16);
                hash = md5Str;
            }

            ret.put("hash", hash);
            ret.put("bytesA", record);
            return ret;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
            throw new BasicException(e.getMessage(), e);
        }
    }

    // this is a helper to quickly determine if the keys are current
    // if the decoded header is null this means there is a keys mismatch
    // TODO prevent replay attacks by rotating the key every request per originating device/host
    private void middlewareVerifyAESKey() {
        before((request, response) -> {
            boolean encrypted = isRequestEncrypted(request);
            if (!encrypted) {
                return;
            }
            String verifyHeader = request.headers("X-AES-Verify");
            String decodedPayload = null;
            try {
                decodedPayload = AES256Cryptor.decrypt(verifyHeader, AESKey);

            } catch (Exception e) {
            }
            if (decodedPayload == null) {
                logger.warning("Invalid X-AES-Verify header: " + verifyHeader);
                JSONPayload ret = new JSONPayload(false);
                ret.setErrorMessage("BAD_AES_KEY");
                halt(401, ret.getString());
            }
        });
    }

    private void middleWareJWTAuth() {
        before((request, response) -> {
            String whiteList = "^\\/(dbimage|authenticate)\\/(.+)?";
            if (
                    request.pathInfo().equals("/users") ||
                            request.pathInfo().matches(whiteList)
            ) {
                return;
            }
//            if(true){
//
//            return;
//            }

            JSONPayload ret = new JSONPayload(isRequestEncrypted(request));

            String authHeader = getAuthTokenFromHeader(request);

            // if auth goes wrong do not encrypt the response
            // not really necessary since errorMessage is always unencrypted
            if (authHeader != null) {
                DecodedJWT decodedJWT = jwtStore.decodeToken(authHeader);
                if (decodedJWT == null) {
                    logger.warning("Invalid JWT TOKEN " + authHeader);
                    ret.setErrorMessage("TOKEN_INVALID");
                    halt(401, ret.getString());
                } else {

                    String userID = decodedJWT.getClaim("sub").asString();
                    request.attribute("JWT_USER_ID", userID);
                    logger.warning("JWT AUTH OK, USER ID: " + userID);
                }

            } else {
                logger.warning("Missing JWT TOKEN " + authHeader);
                ret.setErrorMessage("TOKEN_NOT_FOUND");
                halt(401, ret.getString());
            }
        });
    }

    // Enables CORS on requests. This method is an initialization method and should be called once.
    // @see https://sparktutorials.github.io/2016/05/01/cors.html
    private static void middlewareEnableCORS(final String origin, final String methods, final String headers) {

        options("/*", (request, response) -> {

            // hide Jetty version
            response.header("server", "Mobilecenta API");

            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        before((request, response) -> {
            response.header("server", "Mobilecenta API");
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Request-Method", methods);
            response.header("Access-Control-Allow-Headers", headers);
            // Note: this may or may not be necessary in your particular application
            response.type("application/json");
        });
    }


    private String getAuthTokenFromHeader(Request request) {

        String authHeader = request.headers("Authorization");
        logger.warning("Authorization header is " + authHeader);
        boolean encrypted = isRequestEncrypted(request);

        if (encrypted) {
            String decodedPayload = AES256Cryptor.decrypt(authHeader, AESKey);
            logger.warning("Decoded Authorization header is " + decodedPayload);
            return decodedPayload;
        }
        return authHeader;
    }

    private boolean isRequestEncrypted(Request request) {
        String encryptionHeader = request.headers("X-AES-Encrypted");
        return "true".equals(encryptionHeader);
    }

    /**
     * Gets (and decrypts if necessary) the data member of the request JSON body
     *
     * <p>
     * HTTP request body should follow the {encrypted:true|false, data:{...}} pattern
     * </p>
     *
     * @param request a sparkjava request object
     * @return
     */
    private JsonObject getRequestBodyData(Request request) {
        JsonObject body = new Gson().fromJson(request.body(), JsonObject.class);
        boolean encrypted = isRequestEncrypted(request);
        if (encrypted) {
            String payload = body.get("data").getAsString();
            logger.warning("Encrypted data: " + payload);
            String decodedPayload = AES256Cryptor.decrypt(payload, AESKey);
            JsonObject data = new Gson().fromJson(decodedPayload, JsonObject.class);
            return data;
        }
        return body.get("data").getAsJsonObject();
    }

    private String routeAuthenticate(Request request, Response response) {
        JsonObject data = getRequestBodyData(request);
        String ID = data.get("id").getAsString();
        //TODO wrap this in try-catch
        String password = new String(data.get("password").getAsString());

        AppUser user = DSL.getAppUserByID(ID);
        JSONPayload ret = createJSONPayload();
        ret.setStatus("ERROR");
        if (user == null) {
            response.status(404);
            ret.setErrorMessage("NO_SUCH_USER_ID");
            return ret.getString();
        }
        if (!Hashcypher.authenticate(password, user.getPassword())) {
            response.status(400);
            ret.setErrorMessage("BAD_PASSWORD");
            return ret.getString();
        }

        HashMap retObj = new HashMap();
        retObj.put("authToken", jwtStore.getToken(ID));
        Gson b = new GsonBuilder().serializeNulls().create();

        ret.setStatus("OK");
        ret.setData(b.toJsonTree(retObj));
        return ret.getString();
    }


    public int start() {

        //TODO move this to JpanelConfigMobileCenta
        ArrayList<String> s = NetworkInfo.getAllAddresses();
        logger.warning(s.toString());

        File configFile = app.getProperties().getConfigFile();
        AppProperties props = app.getProperties();

        //TODO read mobilecenta.aes_secret_keys instead
        String aesKey = props.getProperty("mobilecenta.aes_secret_keys");
        if (aesKey != null && aesKey.startsWith("crypt:")) {
            AltEncrypter cypher = new AltEncrypter("cipherkey");
            aesKey = cypher.decrypt(aesKey.substring(6));
        }
        logger.warning("KEY " + aesKey);
        AES256Cryptor.setKeysStr(aesKey);
        AESKey = aesKey;

        // TODO check if IP address exists!
        // TODO memorize network if name and check if the IP has changed!
        // NOTE if it does not spark will bail out with exit code 100
        String ipAddressStr = props.getProperty("mobilecenta.server_ip_address");
        if (ipAddressStr != null) {
            logger.warning("API SERVER IP " + ipAddressStr);
            ipAddress(ipAddressStr);
        }


        String portStr = props.getProperty("mobilecenta.server_port");
        if (portStr == null) {
            portStr = "7777";
        }
        logger.warning("API SERVER PORT " + portStr);
        port(Integer.parseInt(portStr));

        running = true;

        before((request, response) -> {
            String method = request.requestMethod();
            String url = request.url();
            logger.info(method + " " + url);
        });

        middlewareEnableCORS("*",
                "GET,POST,PUT,DELETE,PATCH,OPTIONS",
                "Content-type,X-Requested-With"
        );
        middlewareVerifyAESKey();
        middleWareJWTAuth();


        // NOTE test with
        // curl  -X POST localhost:7777/authenticate/ -H "Content-Type: application/json; charset=utf8" --data '{"id":"0", "password":"123"}'

        //NOTE when changing pass in Unicenta mind the keyb switching from numbers to letters

        post("/authenticate/", this::routeAuthenticate);

        get("/dbimage/:tableName/:pk/:size/", (request, response) -> {
            HashMap params = new HashMap(); //params, not used here
            params.put("tableName", request.params(":tableName"));
            params.put("pk", request.params(":pk"));
            params.put("size", request.params(":size"));
            logger.info(params.toString());
//            logger.info(request.headers().toString());
//            Object bytesA = routeDBImage(params);
            HashMap data = (HashMap) cacheImages.get(params);
            Object bytesA = data.get("bytesA");
            String hash = (String) data.get("hash");
            Object status = "";
            if (bytesA != null) {
                response.type("image/png");
                HttpServletResponse raw = response.raw();
//            response.header("Content-Disposition", "attachment; filename=image.jpg");
//            response.type("application/force-download");
                try {
                    /*
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    byte[] md5Arr = md.digest(bytes);
                    String md5Str = new BigInteger(1, md5Arr).toString(16);
                    */
                    byte[] bytes = (byte[]) bytesA;
                    logger.info("User-agent ETag: " + request.headers("If-None-Match"));
                    String clientETag = request.headers("If-None-Match");
                    if (hash.equals(clientETag)) {
                        response.status(304);
                        logger.info("NOT MODIFIED");
                        return "NOT MODIFIED";
                    }
                    response.header("ETag", hash);
                    raw.getOutputStream().write(bytes);
                    raw.getOutputStream().flush();
                    raw.getOutputStream().close();

                    return raw;
                } catch (Exception e) {
                    e.printStackTrace();
                    status = "ERROR";
                }
            }
            if ("ERROR".equals(status)) {
                response.status(500);
            } else {
                response.status(404);
            }
            return "ERROR";
        });

        get("/tickets", (request, response) -> {

            // TODO move this to a POST handler for tickets
            // EventHub.post(EventHub.API_ORIGINATED_CHANGE);
            response.header("Content-Encoding", "gzip");
            HashMap params = new HashMap(); //params, not used here

            return (String) cacheTickets.get(params);
        });

        get("/users", (request, response) -> {
            JSONPayload ret = createJSONPayload();
            ret.setStatus("OK");
            HashMap params = new HashMap(); //params, not used here
            ret.setData(routeUsers(params));
            response.header("Content-Encoding", "gzip");

            return ret.getString();
        });

        get("/floors", (request, response) -> {
            response.header("Content-Encoding", "gzip");
            HashMap params = new HashMap(); //params, not used here
            return (String) cacheFloors.get(params);
        });


        get("/products", (request, response) -> {
            response.header("Content-Encoding", "gzip");
            HashMap params = new HashMap(); //params, not used here
            Object ret = cacheProducts.get(params);
            return ret;
        });

        //TODO complete this
        get("/ticket/:placeID", (request, response) -> {
            String placeID = request.params(":placeID");
//            TicketDSL t = TicketDSL.getInstance();
            response.header("Content-Encoding", "gzip");
            HashMap params = new HashMap(); //params, not used here

            JSONPayload ret = createJSONPayload();
            ret.setStatus("OK");

            TicketInfo ti = ticketDSL.getTicketByPlaceID(placeID);
//            HashMap d = new HashMap();
//            d.put("ticket", ti);

            Gson b = new GsonBuilder().serializeNulls().create();
            ret.setData(b.toJsonTree(ti));

            return ret.getString();
        });

        post("/ticket/:placeID", (request, response) -> {
            String placeID = request.params(":placeID");
//            TicketDSL t = TicketDSL.getInstance();
            response.header("Content-Encoding", "gzip");
            HashMap params = new HashMap(); //params, not used here

            JSONPayload ret = createJSONPayload();
            ret.setStatus("OK");

            logger.log(Level.INFO, request.body());
            params.put("data", getRequestBodyData(request));
            params.put("userID", request.attribute("JWT_USER_ID"));

            JsonElement resp = routeUpdateTicket(params);
            ret.setData(resp);

            return ret.getString();
        });

        //TODO complete this
        /*post("/ticket/:placeID", (request, response) -> {

            String placeID = request.params(":placeID");
//            TicketDSL t = TicketDSL.getInstance();
            response.header("Content-Encoding", "gzip");
            HashMap params = new HashMap(); //params, not used here

            JSONPayload ret = createJSONPayload();
            ret.setStatus("OK");

            TicketInfo ti = ticketDSL.getTicketByPlaceID(placeID);
//            HashMap d = new HashMap();
//            d.put("ticket", ti);

            JsonObject ticketLines = new Gson().fromJson(request.body(), JsonObject.class);

            Gson b = new GsonBuilder().serializeNulls().create();
            ret.setData(b.toJsonTree(ti));

            return ret.getString();
        });*/


        return 0;
    }

    public int stop() {
        return 0;
    }
}

class JSONPayload {
    String status;
    String errorMessage = null;
    JsonElement data;
    boolean encrypt = true;

    public void setEncryption(boolean encrypt) {
        this.encrypt = encrypt;
    }

    public JSONPayload(boolean encrypt) {
        this.encrypt = encrypt;
    }

    public String getString() {
        HashMap d = new HashMap();

        Object dataValue;
        if (data == null) {
            dataValue = null;
        } else if (data.isJsonNull()) {
            dataValue = null;
        } else {
            dataValue = encrypt ? ApiServer.encrypt(data.toString()) : data;
        }

        d.put("encrypted", encrypt);
        d.put("data", dataValue);
        d.put("status", status);
        d.put("errorMessage", errorMessage);
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting().create();
        return gson.toJson(d);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setErrorMessage(String message) {
        this.errorMessage = message;
        this.setEncryption(false);
    }

    public JsonElement getData() {
        return data;
    }

    public void setData(JsonElement data) {
        this.data = data;
    }

}