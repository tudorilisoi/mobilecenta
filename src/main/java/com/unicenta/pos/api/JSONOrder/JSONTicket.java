// JSONOrder.java

package com.unicenta.pos.api.JSONOrder;

import java.util.*;

import com.fasterxml.jackson.annotation.*;

public class JSONTicket {
    public static String OP_UPDATE = "UPDATE";
    public static String OP_PAY_AND_CLOSE = "PAY_AND_CLOSE";

    private String placeID;
    private String userID;
    private String operation;

    private Date date;
    private List<JSONLine> lines;

    @JsonProperty("operation")
    public String getOperation() {
        return operation;
    }

    @JsonProperty("operation")
    public void setOperation(String value) {
        this.operation = value;
    }

    @JsonProperty("date")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    public Date getDate() {
        return date;
    }

    @JsonProperty("date")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    public void setDate(Date value) {
        this.date = value;
    }

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
