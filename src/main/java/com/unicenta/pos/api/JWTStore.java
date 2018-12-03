package com.unicenta.pos.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.HashMap;

public class JWTStore {
    private String secret = null;
    private HashMap<String, String> store = new HashMap();
    private Algorithm algorithmHS;
    private static JWTStore instance;

    private JWTStore(String secret) {
        this.secret = secret;
        algorithmHS = Algorithm.HMAC256(secret);
    }

    public static JWTStore instance(String secret) {
        if (instance == null) {
            instance = new JWTStore(secret);
        }
        return instance;
    }

    public String getToken(String userID) {
        String existingToken = store.get(userID);
        if (existingToken != null) {
            return existingToken;
        }
        String token = generateToken(userID);
        store.put(userID, token);
        return token;
    }

    public String generateToken(String userID) {
        String token = JWT.create()
                .withClaim("sub", userID)
                .sign(algorithmHS);
        return token;
    }

}
