package com.gmail.necnionch.myplugin.authskip.bungee.util;

import java.io.*;

public class SkinData {

    private String value;
    private String signature;

    public SkinData(String value, String signature) {
        this.value = value;
        this.signature = signature;

    }

    public String getValue() {
        return value;
    }

    public String getSignature() {
        return signature;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }


    public static SkinData loadFrom(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String value = br.readLine();
            String signature = br.readLine();
            if (value != null && !value.isEmpty() && signature != null && !signature.isEmpty())
                return new SkinData(value, signature);
            throw new IllegalArgumentException("invalid data");
        }
    }

    public void saveTo(File file) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
            bw.write(value + "\n");
            bw.write(signature + "\n");
        }
    }

}
