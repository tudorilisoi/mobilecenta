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
import com.openbravo.pos.forms.DataLogicSystem;
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
import com.unicenta.pos.api.JSONOrder.JSONTicket;
import com.unicenta.pos.api.JSONOrder.JSONLine;
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
    private String lastError;
    private DSL DSL;
    private TicketDSL ticketDSL;
    private Cache cacheProducts = null;
    private Cache cacheFloors = null;
    private Cache cacheImages = null;

    //TODO make this use the stored password
    private SessionStore sessionStore;

    // encryption settings
    // since there is no easy way to run HTTPS on a LAN server
    // we use a shared private key
    private static String AESKey = "a disturbing secret";
    private static boolean useEncryption = true; //set to false in dev mode for easier debugging

    public String getConfigParam(String prop) {
        File configFile = app.getProperties().getConfigFile();
        AppProperties props = app.getProperties();

        String value = props.getProperty(prop);
        if (value != null && value.startsWith("crypt:")) {
            AltEncrypter cypher = new AltEncrypter("cipherkey");
            value = cypher.decrypt(value.substring(6));
        }
        return value;
    }

    public ApiServer(JRootApp app) {
        this.running = false;
        this.app = app;

        DSL = (DSL) app.getBean("com.unicenta.pos.api.DSL");

        DataLogicSystem systemLogic = (DataLogicSystem) app.getBean("com.openbravo.pos.forms.DataLogicSystem");
        DSL.setSystemLogic(systemLogic);

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
        cacheImages = makeCache("routeDBImage", 500);

        //Spark: log HTTP 500 expceptions
        Spark.exception(Exception.class, (exception, request, response) -> {
            exception.printStackTrace();
        });

    }

    public boolean isRunning() {
        return running;
    }

    public String lastErrorMessage() {
        return lastError;
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
//                                    case "routeSharedtickets":
//                                        data = routeSharedtickets(params);
//                                        break;
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
            Gson b = new GsonBuilder()
                    .setPrettyPrinting()
                    .serializeNulls().create();
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

        String userID = (String) params.get("userID");
        AppUser user = DSL.getAppUserByID(userID);

        //TODO!! parse req body, move this method into DSL,
        // cycle through lines and replace them  in the shared ticket
        HashMap d = new HashMap();

        JSONTicket ticketFromRequest = Converter.fromJsonString(params.get("data").toString());
        logger.info("ORDER: " + Converter.toJsonString(ticketFromRequest));
        logger.info(String.format("JWT User: #%s name: %s", userID, user.getUserInfo().getName()));
        //TODO check locked status and user/role

        String placeID = ticketFromRequest.getPlaceID();

        boolean isNew = false;
        TicketInfo ticketInfo = DSL.getTicketInfo(placeID);

        if (ticketInfo == null) {
            isNew = true;
            ticketInfo = new TicketInfo();
            ticketInfo.setUser(user.getUserInfo());
        }

//        logger.info("existing lines: " + ticketInfo.getLines().toString());

        List<TicketLineInfo> lines = new ArrayList<>();
        List<TicketLineInfo> existingLines = ticketInfo.getLines();

        NumberFormat nf = DecimalFormat.getInstance(Locale.getDefault());
        for (JSONLine linefromRq : ticketFromRequest.getLines()) {
            //TODO put received um into a property
            ProductInfoExt productInfo = DSL.salesLogic.getProductInfo(linefromRq.getProductID());
            productInfo.setName(productInfo.getName()
                    //+ (linefromRq.getUm() == 1.0 ? "" : " (" + nf.format(linefromRq.getUm()) + ")")
            );

            TaxInfo tax = DSL.taxesLogic.getTaxInfo(
                    productInfo.getTaxCategoryID(),
                    ticketInfo.getCustomer()
            );
            logger.info("JSON line " + linefromRq.toString());
            TicketLineInfo line = DSL.findLineByUUID(
                    existingLines, linefromRq.getMobilecentaUUID()
            );
            if (line == null) {
                line = new TicketLineInfo(
                        productInfo,
                        linefromRq.getMultiply(),
                        linefromRq.getPrice(),
                        tax,
                        (Properties) (productInfo.getProperties().clone())
                );

            } else {
                logger.info("Found line " + linefromRq.getMobilecentaUUID());
                line.setMultiply(linefromRq.getMultiply());
            }

            line.setTicketUpdated("", (String) linefromRq.getattributes().get("ticket.updated"));
            line.setUpdated(linefromRq.getUpdated());

            lines.add(line);
        }

        //TODO remove this comment, debug
        ticketInfo.setLines(lines);

        //assigns ticketID and index to all lines
        ticketInfo.refreshLines();
        ticketDSL.restDB.setWaiterNameInTableById(user.getUserInfo().getName(), placeID);
        ticketDSL.restDB.setOccupied(ticketInfo.getId());
        ticketDSL.restDB.setGuestsInTable(
                ticketDSL.restDB.getGuestsInTable(ticketInfo.getId()), ticketInfo.getId()
        );
        ticketInfo.setDate(new Date());


        try {

            //print to kitchen test
            TicketOps ops = new TicketOps(app, DSL, ticketInfo, user, placeID);
            ops.printToKitchen();

            if (isNew) {
                DSL.receiptsLogic.insertSharedTicket(placeID, ticketInfo, 0);
            } else {
                DSL.receiptsLogic.updateSharedTicket(placeID, ticketInfo, 0);
            }

            if (ticketFromRequest.getOperation().equals(JSONTicket.OP_PAY_AND_CLOSE)) {
                logger.info("PAY_AND_CLOSE");
                ops.closeTicket(ticketInfo, placeID);
            }

            // NOTE use 2nd arg null to unlock
            // DSL.receiptsLogic.lockSharedTicket(placeID, "locked");
            // DSL.salesLogic.saveTicket(ticketInfo, app.getInventoryLocation());


        } catch (BasicException e) {
//            TODO!! return HTTP 500
            e.printStackTrace();
        }

        d.put("ticket", DSL.getTicketData(placeID));

        Gson b = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();

        logger.info("PLACE ID: " + placeID);
        logger.info(b.toJson(d));

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
//                logger.info("REQUEST SEQUENCE:" + decodedPayload);
                JsonObject requestInfo = new Gson().fromJson(decodedPayload, JsonObject.class);
                String clientID = requestInfo.get("clientID").getAsString();
                Long sequence = requestInfo.get("sequence").getAsLong();

                boolean success = sessionStore.verifyRequestSequence(
                        clientID, sequence
                );
//                logger.info(String.format("SEQ VERIFY: %s", success));
                if (!success) {
                    logger.warning("REPLAY_ATTEMPT");
                    decodedPayload = null;
                } else {
                    sessionStore.storeRequestSequence(clientID, sequence);
                }

            } catch (Exception e) {
                logger.warning(e.getMessage());
                e.printStackTrace();
            }
            if (decodedPayload == null) {
                logger.warning("Invalid X-AES-Verify header: " + verifyHeader);
                JSONPayload ret = new JSONPayload(false);
                ret.setErrorMessage("BAD_AES_KEY");
                halt(412, ret.getString());
            }
        });
    }

    private static void middlewareEnableGzipIfNeeded() {

        before((spark.Request request, spark.Response response) -> {

            String accept = request.headers("Accept-Encoding");
            if (accept == null) {
                return;
            }

            String blackList = "^\\/(dbimage)\\/(.+)?";
            if (request.pathInfo().matches(blackList)) {
                return;
            }

            String[] tokens = accept.split(",");
            if (Arrays.stream(tokens).map(String::trim).anyMatch(s -> s.equalsIgnoreCase("gzip"))) {
                logger.info("GZIP ENABLED");
                response.header("Content-Encoding", "gzip");
            }
        });
    }

    private void middleWareJWTAuth() {
        before((request, response) -> {

            String whiteList = "^\\/(dbimage|authenticate)\\/(.+)?";
            if (request.pathInfo().equals("/users")
                    || request.pathInfo().matches(whiteList)) {
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
            if (authHeader == null) {
                logger.warning("Missing JWT TOKEN " + authHeader);
                ret.setErrorMessage("TOKEN_NOT_FOUND");
                halt(401, ret.getString());
            }
            DecodedJWT decodedJWT = sessionStore.decodeToken(authHeader);
            if (decodedJWT == null) {
                logger.warning("Invalid JWT TOKEN " + authHeader);
                ret.setErrorMessage("TOKEN_INVALID");
                halt(401, ret.getString());
            } else {

                String userID = decodedJWT.getClaim("sub").asString();
                request.attribute("JWT_USER_ID", userID);
                logger.warning("JWT AUTH OK, USER ID: " + userID);
                // TODO check if the user is blocked/inactive

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
     * HTTP request body should follow the {encrypted:true|false, data:{...}}
     * pattern
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
//            logger.warning("Encrypted data: " + payload);
            String decodedPayload = AES256Cryptor.decrypt(payload, AESKey);
            JsonObject data = new Gson().fromJson(decodedPayload, JsonObject.class);
            return data;
        }
        return body.get("data").getAsJsonObject();
    }

    private String routeAuthRefreshToken(Request request, Response response) {
        JSONPayload ret = createJSONPayload();
        HashMap retObj = new HashMap();
        logger.info("REFRESH TOKEN for user #" + request.attribute("JWT_USER_ID"));
        sessionStore.clearToken(request.attribute("JWT_USER_ID"));
        retObj.put("authToken", sessionStore.getToken(
                request.attribute("JWT_USER_ID")
        ));
        retObj.put("userID", request.attribute("JWT_USER_ID"));
        Gson b = new GsonBuilder().serializeNulls().create();

        ret.setStatus("OK");
        ret.setData(b.toJsonTree(retObj));
        return ret.getString();
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
            response.status(401);
            ret.setErrorMessage("NO_SUCH_USER_ID");
            return ret.getString();
        }
        if (!Hashcypher.authenticate(password, user.getPassword())) {
            response.status(401);
            ret.setErrorMessage("BAD_PASSWORD");
            return ret.getString();
        }

        HashMap retObj = new HashMap();
        retObj.put("authToken", sessionStore.getToken(ID));
        Gson b = new GsonBuilder().serializeNulls().create();

        ret.setStatus("OK");
        ret.setData(b.toJsonTree(retObj));
        return ret.getString();
    }

    /*@throws BindException*/
    public int start() {
        String aesPassword = getConfigParam("mobilecenta.aes_password");
        String aesKey = getConfigParam("mobilecenta.aes_secret_keys");
        String ipAddressStr = getConfigParam("mobilecenta.server_ip_address");
        String portStr = getConfigParam("mobilecenta.server_port");
        if (portStr == null) {
            portStr = "7777";
        }
        if (aesPassword == null || aesKey == null || ipAddressStr == null) {
            //missing config
            return 2;
        }

        sessionStore = SessionStore.instance(aesPassword);

        initExceptionHandler((e) -> {
            logger.warning("Sparkjava exception");
            logger.warning(e.getMessage());
            lastError = e.getMessage();
            running = false;
        });

        //TODO move this to JpanelConfigMobileCenta
        ArrayList<String> s = NetworkInfo.getAllAddresses();
        logger.warning(s.toString());

        //TODO read mobilecenta.aes_secret_keys instead
        logger.warning("AES KEY " + aesKey);
        AES256Cryptor.setKeysStr(aesKey);
        AESKey = aesKey;

        // TODO check if IP address exists!
        // TODO memorize network if name and check if the IP has changed!
        // NOTE if it does not spark will bail out with exit code 100
        if (ipAddressStr != null) {
            logger.warning("API SERVER IP " + ipAddressStr);
            ipAddress(ipAddressStr);
        }

        logger.warning("API SERVER PORT " + portStr);
        port(Integer.parseInt(portStr));

        //err handler
        internalServerError((req, res) -> {
            res.type("application/json");
            return "{\"message\":\"Server error\"}";
        });
        // Using Route
        notFound((req, res) -> {
            res.type("application/json");
            return "{\"message\":\"Route not found\"}";
        });

        //Request logging
        before((request, response) -> {
            String method = request.requestMethod();
            String url = request.url();
            logger.info(method + " " + url);
            logger.info("REQ HEADERS:\n ");
            for (String headerName : request.headers()) {
                logger.info(headerName + ": " + request.headers(headerName));
            }
        });

        middlewareEnableCORS("*",
                "GET,POST,PUT,DELETE,PATCH,OPTIONS",
                "Content-type,X-Requested-With,Accept"
        );
        middlewareEnableGzipIfNeeded();
        middlewareVerifyAESKey();
        middleWareJWTAuth();

        // NOTE test with
        // curl  -X POST localhost:7777/authenticate/ -H "Content-Type: application/json; charset=utf8" --data '{"id":"0", "password":"123"}'
        //NOTE when changing pass in Unicenta mind the keyb switching from numbers to letters
        post("/authenticate/", this::routeAuthenticate);
        post("/refreshtoken/", this::routeAuthRefreshToken);

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
            HashMap params = new HashMap(); //params, not used here

            JSONPayload ret = createJSONPayload();
            ret.setStatus("OK");
            ret.setData(routeSharedtickets(params));
            return ret.getString();
        });

        get("/users", (request, response) -> {
            JSONPayload ret = createJSONPayload();
            ret.setStatus("OK");
            HashMap params = new HashMap(); //params, not used here
            ret.setData(routeUsers(params));

            return ret.getString();
        });

        get("/floors", (request, response) -> {
            HashMap params = new HashMap(); //params, not used here
            return (String) cacheFloors.get(params);
        });

        get("/products", (request, response) -> {
            HashMap params = new HashMap(); //params, not used here
            Object ret = cacheProducts.get(params);
            return ret;
        });

        //TODO complete this
        get("/ticket/:placeID", (request, response) -> {
            String placeID = request.params(":placeID");
//            TicketDSL t = TicketDSL.getInstance();
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

        post("/ticket/:placeID", "application/json", (request, response) -> {
            String placeID = request.params(":placeID");
//            TicketDSL t = TicketDSL.getInstance();
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

        post("/refreshtoken", "application/json", (request, response) -> {

            HashMap params = new HashMap();

            JSONPayload ret = createJSONPayload();
            ret.setStatus("OK");

            logger.log(Level.INFO, request.body());

            params.put("userID", request.attribute("JWT_USER_ID"));

            Gson b = new GsonBuilder()
                    .setPrettyPrinting()
                    .serializeNulls()
                    .create();
            ret.setData(b.toJsonTree(params));

            return ret.getString();
        });

        awaitInitialization();
        running = true;
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
