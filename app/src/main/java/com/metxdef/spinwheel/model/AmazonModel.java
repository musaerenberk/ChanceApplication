package com.metxdef.spinwheel.model;

public class AmazonModel {

    private int id;
    private String amazonCode;

    public AmazonModel() {

    }

    public AmazonModel(int id, String amazonCode) {
        this.id = id;
        this.amazonCode = amazonCode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAmazonCode() {
        return amazonCode;
    }

    public void setAmazonCode(String amazonCode) {
        this.amazonCode = amazonCode;
    }
}
