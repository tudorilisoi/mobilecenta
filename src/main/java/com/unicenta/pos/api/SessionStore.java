package com.unicenta.pos.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.HashMap;

public class SessionStore {
    private String secret = null;
    private HashMap<String, String> tokenStore = new HashMap();

    //TODO implement checking
    //device id-:last request seq
    private HashMap<String, Long> requestSeqStore = new HashMap();
    private Algorithm algorithmHS;
    private JWTVerifier verifier;
    private static SessionStore instance;

    private SessionStore(String secret) {
        this.secret = secret;
        algorithmHS = Algorithm.HMAC256(secret);
        verifier = JWT.require(algorithmHS)
//                .withIssuer("auth0")
                .build();
    }

    public void storeRequestSequence(String clientID, long seq) {
        requestSeqStore.put(clientID, seq);
    }

    public boolean verifyRequestSequence(String clientID, long seq) {
        Long lastReqTime = requestSeqStore.get(clientID);
        if (lastReqTime == null) {
            System.out.println("*** INIT *** " + clientID);
            lastReqTime = System.currentTimeMillis() - 3000;
        }
        if (seq > lastReqTime) {
            return true;
        }
        return false;
    }

    public static SessionStore instance(String secret) {
        if (instance == null) {
            instance = new SessionStore(secret);
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
        String existingToken = tokenStore.get(userID);
        if (existingToken != null) {
            return existingToken;
        }
        String token = generateToken(userID);
        tokenStore.put(userID, token);
        return token;
    }

    public String generateToken(String userID) {
        String token = JWT.create()
                .withClaim("sub", userID)
                .sign(algorithmHS);
        return token;
    }

}
