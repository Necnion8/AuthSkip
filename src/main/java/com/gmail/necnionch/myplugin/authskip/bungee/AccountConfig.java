package com.gmail.necnionch.myplugin.authskip.bungee;

import com.gmail.necnionch.myplugin.authskip.common.BungeeConfigDriver;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;


public class AccountConfig extends BungeeConfigDriver {
    public AccountConfig(Plugin plugin) {
        super(plugin, "accounts.yml", "empty.yml");
    }


    public void put(String name, String address) {
        config.set("name." + name, address);
    }

    public String get(String name) {
        String s = config.getString("name." + name, "");
        return s.isEmpty() ? null : s;
    }

    public String remove(String name) {
        try {
            return get(name);
        } finally {
            config.set("name." + name, null);
        }
    }

    public Map<String, String> getNameAndAddresses() {
        Map<String, String> addresses = new HashMap<>();
        for (String key : config.getSection("name").getKeys()) {
            addresses.put(key, config.getString("name." + key));
        }
        return addresses;
    }

}
