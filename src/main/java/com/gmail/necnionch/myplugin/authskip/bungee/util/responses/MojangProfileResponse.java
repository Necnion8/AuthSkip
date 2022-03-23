package com.gmail.necnionch.myplugin.authskip.bungee.util.responses;

public class MojangProfileResponse {
    private String id;
    private String name;
    private PropertyResponse[] properties;


    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public PropertyResponse[] getProperties() {
        return this.properties;
    }
}
