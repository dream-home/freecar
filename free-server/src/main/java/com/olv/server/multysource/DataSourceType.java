package com.olv.server.multysource;

public enum DataSourceType {

    Master("master"),

    Slave("slave");

    private DataSourceType(String name) {
        this.name = name;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
