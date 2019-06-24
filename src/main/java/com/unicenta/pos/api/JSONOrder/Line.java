// Line.java

package com.unicenta.pos.api.JSONOrder;

import java.util.*;

import com.fasterxml.jackson.annotation.*;

public class Line {
    private String productID;
    private double multiplier;
    private double price;

    @JsonProperty("productID")
    public String getProductID() {
        return productID;
    }

    @JsonProperty("productID")
    public void setProductID(String value) {
        this.productID = value;
    }

    @JsonProperty("multiplier")
    public double getMultiplier() {
        return multiplier;
    }

    @JsonProperty("multiplier")
    public void setMultiplier(double value) {
        this.multiplier = value;
    }

    @JsonProperty("price")
    public double getPrice() {
        return price;
    }

    @JsonProperty("price")
    public void setPrice(double value) {
        this.price = value;
    }
}
