// Line.java

package com.unicenta.pos.api.JSONOrder;

import java.util.*;

import com.fasterxml.jackson.annotation.*;

public class Line {


    private String id;
    private String productID;
    private double multiplier;
    private double price;
    private double um;
    private Boolean updated;

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("updated")
    public Boolean getUpdated() {
        return updated;
    }

    @JsonProperty("updated")
    public void setUpdated(Boolean updated) {
        this.updated = updated;
    }


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

    @JsonProperty("um")
    public double getUm() {
        return um;
    }

    @JsonProperty("um")
    public void setUm(double um) {
        this.um = um;
    }


}
