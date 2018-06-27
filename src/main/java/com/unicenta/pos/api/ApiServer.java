/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unicenta.pos.api;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.JRootApp;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static spark.Spark.*;

import com.google.gson.*;

import java.security.MessageDigest;
import java.math.*;

import com.openbravo.pos.sales.DataLogicReceipts;

import javax.servlet.http.HttpServletResponse;


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

    public ApiServer(JRootApp _app) {
        this.running = false;
        app = _app;
        DSL = (DSL) app.getBean("com.unicenta.pos.api.DSL");
        ticketDSL = (TicketDSL) app.getBean("com.unicenta.pos.api.TicketDSL");
        DataLogicReceipts DLReceipts = (DataLogicReceipts) app.getBean("com.openbravo.pos.sales.DataLogicReceipts");
        DSL.setReceiptsLogic(DLReceipts);
        ticketDSL.setReceiptsLogic(DLReceipts);

        cacheProducts = makeCache("productsRoute", 500);
        cacheFloors = makeCache("floorsRoute", 500);
        cacheTickets = makeCache("sharedticketsRoute", 0);
        cacheImages = makeCache("dbimageRoute", 500);
    }

    private Cache makeCache(String routeMethod, Integer size) {
        return CacheBuilder.newBuilder()
                .maximumSize(size)
//                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<HashMap, Object>() {
                            @Override
                            public Object load(HashMap params) throws Exception {
                                JSONPayload ret = new JSONPayload();
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

    public int start() {
        port(7777);
        running = true;

        enableCORS("*",
                "GET,POST,PUT,DELETE,PATCH,OPTIONS",
                "Content-type,X-Requested-With"
        );


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
            JSONPayload ret = new JSONPayload();
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

        post("/ticket/:placeID", (request, response) -> {
            TicketDSL t = TicketDSL.getInstance();
            response.header("Content-Encoding", "gzip");
            HashMap params = new HashMap(); //params, not used here

            JSONPayload ret = new JSONPayload();
            ret.setStatus("OK");

            HashMap d = new HashMap();
            d.put("ticket", t.getTicketByPlaceID());

            Gson b = new GsonBuilder().serializeNulls().create();
            ret.setData(b.toJsonTree(d));

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
    String errorMessage = "";
    JsonElement data;

    public String getString() {
        HashMap d = new HashMap();
        d.put("data", data);
        d.put("status", status);
        d.put("errorMessage", errorMessage);
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting().create();
        return gson.toJson(d).toString();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public JsonElement getData() {
        return data;
    }

    public void setData(JsonElement data) {
        this.data = data;
    }

}