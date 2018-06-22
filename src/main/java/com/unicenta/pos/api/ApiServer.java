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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
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
    private DSL dsl;
    private Cache cacheProducts = null;
    private Cache cacheFloors = null;

    public ApiServer(JRootApp _app) {
        this.running = false;
        app = _app;
        dsl = (DSL) app.getBean("com.unicenta.pos.api.DSL");
        dsl.setReceiptsLogic(
                (DataLogicReceipts) app.getBean("com.openbravo.pos.sales.DataLogicReceipts")
        );

        cacheProducts = makeCache("productsRoute");
        cacheFloors = makeCache("floorsRoute");
    }

    private Cache makeCache(String routeMethod) {
        return CacheBuilder.newBuilder()
                .maximumSize(1)
//                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<HashMap, String>() {
                            @Override
                            public String load(HashMap params) throws Exception {
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
            d.put("sharedtickets", dsl.listSharedTickets());
            d.put("_comment", "PLACEID=>sharedticket map");
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

        d.put("users", dsl.listUsers());
        d.put("roles", dsl.listRoles());

        Gson b = new GsonBuilder().serializeNulls().create();
        return b.toJsonTree(d);
    }

    private JsonElement floorsRoute(Map params) throws BasicException {

        HashMap d = new HashMap();

        d.put("floors", dsl.listFloors());
        d.put("places", dsl.listPlaces());

        Gson b = new GsonBuilder().serializeNulls().create();
        return b.toJsonTree(d);
    }

    private JsonElement productsRoute(Map params) throws BasicException {

        HashMap d = new HashMap();

        d.put("taxes", dsl.listTaxes());
        d.put("categories", dsl.listProductCategories());
        d.put("products", dsl.listProducts());

        Gson b = new GsonBuilder().serializeNulls().create();
        return b.toJsonTree(d);
    }

    Object dbimageRoute(Map params) throws BasicException {
        try {

            //currently "small" or "full"
            String size = (String) params.get("size");

            if (size.equals("small")) {

                Object record = dsl.getDBImageThumbnail(
                        (String) params.get("tableName"),
                        (String) params.get("pk")
                );
                return record;
            } else {
                Object record = dsl.getDBImageBytes(
                        (String) params.get("tableName"),
                        (String) params.get("pk")
                );
                return record;

            }
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
            Object bytesA = dbimageRoute(params);
            Object status = "";
            if (bytesA != null) {
                response.type("image/png");
                HttpServletResponse raw = response.raw();
//            response.header("Content-Disposition", "attachment; filename=image.jpg");
//            response.type("application/force-download");
                try {
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    byte[] bytes = (byte[]) bytesA;
                    byte[] md5Arr = md.digest(bytes);
                    String md5Str = new BigInteger(1, md5Arr).toString(16);
                    logger.info("User-agent ETag: " + request.headers("If-None-Match"));
                    String clientETag = request.headers("If-None-Match");
                    if (md5Str.equals(clientETag)) {
                        response.status(304);
                        logger.info("NOT MODIFIED");
                        return "NOT MODIFIED";
                    }
                    response.header("ETag", md5Str);
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
            EventHub.post(EventHub.API_ORIGINATED_CHANGE);
            JSONPayload ret = new JSONPayload();
            ret.setStatus("OK");
            HashMap params = new HashMap(); //params, not used here
            ret.setData(sharedticketsRoute(params));
            response.header("Content-Encoding", "gzip");
            return ret.getString();
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
            return cacheFloors.get(params);
        });


        get("/products", (request, response) -> {
            response.header("Content-Encoding", "gzip");
            HashMap params = new HashMap(); //params, not used here
            return cacheProducts.get(params);
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