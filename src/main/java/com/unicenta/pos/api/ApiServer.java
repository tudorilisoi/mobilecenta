/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unicenta.pos.api;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.AppUser;
import com.openbravo.pos.forms.JRootApp;
import com.openbravo.pos.sales.DataLogicReceipts;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.util.Hashcypher;
import spark.Request;
import spark.Response;
import spark.Spark;

import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
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
    // TODO make a barcode generator to easily scan it in the mobile app

    private static String AESKey = "a disturbing secret";
    private static boolean useEncryption = false; //set to false in dev mode for easier debugging

    public ApiServer(JRootApp app) {
        this.running = false;
        this.app = app;
        DataLogicReceipts receiptsLogic = (DataLogicReceipts) app.getBean("com.openbravo.pos.sales.DataLogicReceipts");

        DSL = (DSL) app.getBean("com.unicenta.pos.api.DSL");
        DSL.setReceiptsLogic(receiptsLogic);

        /*try {

            String payload = "Tudor was here";
            String enc = null;
            String dec = null;
            enc = AES256Cryptor.encrypt(payload, "secret");
            dec = AES256Cryptor.decrypt("U2FsdGVkX1+30P+7lZfHufktcX020h5KgOjVf6WlgA4=", "secret");

            logger.warning(String.format("enc: %s dec: %s", enc, dec));


        } catch (Exception e) {
            e.printStackTrace();
        }*/

        ticketDSL = (TicketDSL) app.getBean("com.unicenta.pos.api.TicketDSL");
        ticketDSL.setReceiptsLogic(receiptsLogic);
        ticketDSL.setApp(app);

        cacheProducts = makeCache("productsRoute", 500);
        cacheFloors = makeCache("floorsRoute", 500);
        cacheTickets = makeCache("sharedticketsRoute", 0);
        cacheImages = makeCache("dbimageRoute", 500);

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
                                    case "floorsRoute":
                                        data = floorsRoute(params);
                                        break;
                                    case "productsRoute":
                                        data = productsRoute(params);
                                        break;
                                    case "sharedticketsRoute":
                                        data = sharedticketsRoute(params);
                                        break;
                                    case "dbimageRoute":
                                        return dbimageRoute(params);

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

    private JsonElement sharedticketsRoute(Map params) throws BasicException {

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


    private JsonElement usersRoute(Map params) throws BasicException {

        HashMap d = new HashMap();

        d.put("users", DSL.listUsers());
        d.put("roles", DSL.listRoles());

        Gson b = new GsonBuilder().serializeNulls().create();
        return b.toJsonTree(d);
    }

    private JsonElement floorsRoute(Map params) throws BasicException {

        HashMap d = new HashMap();

        d.put("floors", DSL.listFloors());
        d.put("places", DSL.listPlaces());

        Gson b = new GsonBuilder().serializeNulls().create();
        return b.toJsonTree(d);
    }

    private JsonElement productsRoute(Map params) throws BasicException {

        HashMap d = new HashMap();

        d.put("taxes", DSL.listTaxes());
        d.put("categories", DSL.listProductCategories());
        d.put("products", DSL.listProducts());

        Gson b = new GsonBuilder().serializeNulls().create();
        return b.toJsonTree(d);
    }

    HashMap dbimageRoute(Map params) throws BasicException {
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


    // Enables CORS on requests. This method is an initialization method and should be called once.
    // @see https://sparktutorials.github.io/2016/05/01/cors.html
    private static void enableCORS(final String origin, final String methods, final String headers) {

        options("/*", (request, response) -> {

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
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Request-Method", methods);
            response.header("Access-Control-Allow-Headers", headers);
            // Note: this may or may not be necessary in your particular application
            response.type("application/json");
        });
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
    private JsonObject getPostData(Request request) {
        JsonObject body = new Gson().fromJson(request.body(), JsonObject.class);
        boolean _encrypted = body.get("encrypted").getAsBoolean();

        if (_encrypted) {
            String payload = body.get("data").toString();
            String decodedPayload = decrypt(payload);
            JsonObject data = new Gson().fromJson(decodedPayload, JsonObject.class);
            return data;
        }
        return body.get("data").getAsJsonObject();
    }

    private String authenticateRoute(Request request, Response response) {
        JsonObject data = getPostData(request);
        String ID = data.get("id").getAsString();
        //TODO wrap this in try-catch
        String password = new String(data.get("password").getAsString());

        AppUser user = DSL.getAppUserByID(ID);
        JSONPayload ret = createJSONPayload();
        ret.setStatus("ERROR");
        if (user == null) {
            response.status(404);
            ret.setErrorMessage("NO SUCH USER ID");
            return ret.getString();
        }
        if (!Hashcypher.authenticate(password, user.getPassword())) {
            response.status(400);
            ret.setErrorMessage("BAD PASSWORD");
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
        port(7777);
        running = true;

        enableCORS("*",
                "GET,POST,PUT,DELETE,PATCH,OPTIONS",
                "Content-type,X-Requested-With"
        );

        // NOTE test with
        // curl  -X POST localhost:7777/authenticate/ -H "Content-Type: application/json; charset=utf8" --data '{"id":"0", "password":"123"}'

        //NOTE when changing pass in Unicenta mind the keyb switching from numbers to letters

        post("/authenticate/", this::authenticateRoute);

        get("/dbimage/:tableName/:pk/:size/", (request, response) -> {
            HashMap params = new HashMap(); //params, not used here
            params.put("tableName", request.params(":tableName"));
            params.put("pk", request.params(":pk"));
            params.put("size", request.params(":size"));
            logger.info(params.toString());
//            logger.info(request.headers().toString());
//            Object bytesA = dbimageRoute(params);
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
            ret.setData(usersRoute(params));
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
            return (String) cacheProducts.get(params);
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

        //TODO complete this
        post("/ticket/:placeID", (request, response) -> {

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
        });


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
    }

    public JsonElement getData() {
        return data;
    }

    public void setData(JsonElement data) {
        this.data = data;
    }

}