// Converter.java

// To use this code, add the following Maven dependency to your project:
//
//     com.fasterxml.jackson.core : jackson-databind : 2.9.0
//
// Import this package:
//
//     import com.unicenta.pos.api.JSONOrder.Converter;
//
// Then you can deserialize a JSON string with
//
//     JSONOrder data = Converter.fromJsonString(jsonString);

package com.unicenta.pos.api.JSONOrder;

import java.util.*;
import java.io.IOException;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.JsonProcessingException;

public class Converter {
    // Serialize/deserialize helpers

    public static JSONOrder fromJsonString(String json) throws IOException {
        return getObjectReader().readValue(json);
    }

    public static String toJsonString(JSONOrder obj) throws JsonProcessingException {
        return getObjectWriter().writeValueAsString(obj);
    }

    private static ObjectReader reader;
    private static ObjectWriter writer;

    private static void instantiateMapper() {
        ObjectMapper mapper = new ObjectMapper();
        reader = mapper.reader(JSONOrder.class);
        writer = mapper.writerFor(JSONOrder.class);
    }

    private static ObjectReader getObjectReader() {
        if (reader == null) instantiateMapper();
        return reader;
    }

    private static ObjectWriter getObjectWriter() {
        if (writer == null) instantiateMapper();
        return writer;
    }
}



