package com.gmail.necnionch.myplugin.authskip.bungee.util;

import com.gmail.necnionch.myplugin.authskip.bungee.AuthSkip;
import com.gmail.necnionch.myplugin.authskip.bungee.util.responses.MojangProfileResponse;
import com.gmail.necnionch.myplugin.authskip.bungee.util.responses.PropertyResponse;
import com.google.gson.Gson;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SkinManager {
    private final AuthSkip plugin;
    public SkinManager(AuthSkip plugin) {
        this.plugin = plugin;
    }


    public SkinData getByPlayer(ProxiedPlayer player) {
        LoginResult loginProfile = ((InitialHandler) player.getPendingConnection()).getLoginProfile();
        if (loginProfile == null)
            return null;

        LoginResult.Property[] properties = loginProfile.getProperties();
        if (properties.length >= 1) {
            LoginResult.Property property = properties[0];
            String value = property.getValue();
            String signature = property.getSignature();
            return new SkinData(value, signature);
        }
        return null;
    }

    public CompletableFuture<SkinData> fetchFromMojangProfile(UUID profile) {
        return CompletableFuture.supplyAsync(() -> getProfileMojang(profile), plugin.getExecutor());
    }


    private SkinData getProfileMojang(UUID profile) {
        try {
            String output = readURL("https://sessionserver.mojang.com/session/minecraft/profile/%uuid%?unsigned=false".replace("%uuid%", profile.toString()));
            MojangProfileResponse obj = (new Gson()).fromJson(output, MojangProfileResponse.class);
            if (obj.getProperties() != null) {
                PropertyResponse property = obj.getProperties()[0];
                if (!property.getValue().isEmpty() && !property.getSignature().isEmpty())
                    return new SkinData(property.getValue(), property.getSignature());
            }
        } catch (Exception ignored) {

        }
        return null;
    }

    private String readURL(String url) throws IOException {
        HttpURLConnection con = (HttpURLConnection)(new URL(url)).openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "AuthSkipPlugin");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        con.setDoOutput(true);

        try (InputStream is = con.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            return br.lines().collect(Collectors.joining());
        }
    }

}
