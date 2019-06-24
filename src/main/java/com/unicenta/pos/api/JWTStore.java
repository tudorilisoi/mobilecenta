package com.unicenta.pos.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.HashMap;

public class JWTStore {
    private String secret = null;
    private HashMap<String, String> store = new HashMap();
    private Algorithm algorithmHS;
    private JWTVerifier verifier;
    private static JWTStore instance;

    private JWTStore(String secret) {
        this.secret = secret;
        algorithmHS = Algorithm.HMAC256(secret);
        verifier = JWT.require(algorithmHS)
//                .withIssuer("auth0")
                .build();
    }

    public static JWTStore instance(String secret) {
        if (instance == null) {
            instance = new JWTStore(secret);
        }
        return instance;
    }

    public DecodedJWT decodeToken(String token) {
        try {
            DecodedJWT jwt = verifier.verify(token);
            return jwt;
        } catch (JWTVerificationException exception) {
            //Invalid signature/claims
            return null;
        }
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
