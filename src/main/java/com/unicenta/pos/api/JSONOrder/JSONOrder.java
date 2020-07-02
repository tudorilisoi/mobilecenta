// JSONOrder.java

package com.unicenta.pos.api.JSONOrder;

import java.util.*;

import com.fasterxml.jackson.annotation.*;

public class JSONOrder {
    private String placeID;
    private List<Line> lines;

    @JsonProperty("placeID")
    public String getPlaceID() {
        return placeID;
    }

    @JsonProperty("placeID")
    public void setPlaceID(String value) {
        this.placeID = value;
    }

    @JsonProperty("lines")
    public List<Line> getLines() {
        return lines;
    }

    @JsonProperty("lines")
    public void setLines(List<Line> value) {
        this.lines = value;
    }
}
