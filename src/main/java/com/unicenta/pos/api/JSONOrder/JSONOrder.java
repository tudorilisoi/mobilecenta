// JSONOrder.java

package com.unicenta.pos.api.JSONOrder;

import java.util.*;

import com.fasterxml.jackson.annotation.*;

public class JSONOrder {
    private String placeID;
    private String userID;
    private List<JSONLine> lines;

    @JsonProperty("placeID")
    public String getPlaceID() {
        return placeID;
    }
    @JsonProperty("placeID")
    public void setPlaceID(String value) {
        this.placeID = value;
    }

    @JsonProperty("userID")
    public String getuserID() {
        return userID;
    }
    @JsonProperty("userID")
    public void setuserID(String value) {
        this.userID = value;
    }

    @JsonProperty("lines")
    public List<JSONLine> getLines() {
        return lines;
    }

    @JsonProperty("lines")
    public void setLines(List<JSONLine> value) {
        this.lines = value;
    }
}
