// Line.java

package com.unicenta.pos.api.JSONOrder;

import java.util.*;

import com.fasterxml.jackson.annotation.*;

public class JSONLine {


    private String id;
    private String mobilecentaUUID;
    private String productID;
    private String name;
    private double multiply;
    private double price;
    private double priceTax;
    private double um;
    private Boolean updated;
    private Map<String, Object> attributes;

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("mobilecenta.uuid")
    public String getMobilecentaUUID() {
        return mobilecentaUUID;
    }

    @JsonProperty("mobilecenta.uuid")
    public void setMobilecentaUUID(String id) {
        this.mobilecentaUUID = id;
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

    @JsonProperty("name")
    public String getname() {
        return name;
    }

    @JsonProperty("name")
    public void setname(String value) {
        this.name = value;
    }

    @JsonProperty("multiply")
    public double getMultiply() {
        return multiply;
    }

    @JsonProperty("multiply")
    public void setMultiply(double value) {
        this.multiply = value;
    }

    @JsonProperty("price")
    public double getPrice() {
        return price;
    }

    @JsonProperty("price")
    public void setPrice(double value) {
        this.price = value;
    }

    @JsonProperty("priceTax")
    public double getPriceTax() {
        return priceTax;
    }

    @JsonProperty("priceTax")
    public void setPriceTax(double value) {
        this.priceTax = value;
    }


    @JsonProperty("um")
    public double getUm() {
        return um;
    }

    @JsonProperty("um")
    public void setUm(double um) {
        this.um = um;
    }

    @JsonProperty("attributes")

    public Map<String, Object> getattributes() {
        return attributes;
    }

    @JsonProperty("attributes")
    public void setattributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }


}
