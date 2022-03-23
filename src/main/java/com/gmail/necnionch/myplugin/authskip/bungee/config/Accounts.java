package com.gmail.necnionch.myplugin.authskip.bungee.config;

import com.gmail.necnionch.myplugin.authskip.common.BungeeConfigDriver;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.io.File;
import java.util.*;


public class Accounts extends BungeeConfigDriver {
    private final Plugin plugin;
    private final Map<String, Entry> entries = Maps.newConcurrentMap();

    public Accounts(Plugin plugin) {
        super(plugin, "accounts.yml", "accounts.yml");
        this.plugin = plugin;

        setHeaderBody(new String[] {
                "",
                "SKIN FILE",
                "  Supported: SkinsRestorer .skin File",
                "  Convert From PNG: https://riflowth.github.io/SkinFile-Generator/",
                ""
        });
    }

    public Entry get(String name) {
        return entries.get(name);
    }

    public void put(Entry entry) {
        entries.put(entry.getName(), entry);
        entry.save();
    }

    public Entry put(String playerName, String address) {
        Entry entry = new Entry(playerName, Sets.newHashSet(address), null, null);
        entries.put(playerName, entry);
        entry.save();
        return entry;
    }

    public Entry remove(String name) {
        Entry entry = entries.remove(name);
        if (entry != null) {
            config.set("accounts." + name, null);
            save();
        }
        return entry;
    }

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(Lists.newArrayList(entries.values()));
    }

    @Override
    public boolean onLoaded(Configuration config) {
        entries.clear();

        if (super.onLoaded(config)) {

            // convert v1
            if (config.contains("name")) {
                for (String name : config.getSection("name").getKeys()) {
                    String address = config.getString("name." + name);
                    if (address != null && !address.isEmpty()) {
                        Entry entry = new Entry(name, Sets.newHashSet(address), null, null);
                        entries.put(name, entry);
                        entry.save(false);
                    }
                }
                config.set("name", null);
                save();
            }

            if (config.contains("accounts")) {
                for (String name : config.getSection("accounts").getKeys()) {
                    Configuration section = config.getSection("accounts." + name);
                    entries.put(name, load(name, section));
                }
            }
            return true;
        }
        return false;
    }

    public class Entry {
        private final String name;
        private final Set<String> addresses;
        private UUID customUniqueId;
        private String skinFileName;

        public Entry(String name, Set<String> addresses, UUID customUniqueId, String skinFileName) {
            this.name = name;
            this.addresses = addresses;
            this.customUniqueId = customUniqueId;
            this.skinFileName = skinFileName;
        }

        public String getName() {
            return name;
        }

        public Set<String> getAddresses() {
            return addresses;
        }

        public UUID getCustomUniqueId() {
            return customUniqueId;
        }

        public String getSkinFileName() {
            return skinFileName;
        }

        public void setCustomUniqueId(UUID customUniqueId) {
            this.customUniqueId = customUniqueId;
        }

        public void setSkinFileName(String skinFileName) {
            this.skinFileName = skinFileName;
        }


        public void save(boolean write) {
            Map<String, Object> data = Maps.newHashMap();
            data.put("addresses", Lists.newArrayList(addresses));
            data.put("custom-uuid", (customUniqueId != null) ? customUniqueId.toString() : null);
            data.put("skin-file", skinFileName);

            config.set("accounts." + name, data);

            if (write)
                Accounts.this.save();
        }

        public void save() {
            save(true);
        }

    }


    private Entry load(String name, Configuration config) {
        UUID uuid;
        try {
            uuid = UUID.fromString(config.getString("custom-uuid", ""));
        } catch (IllegalArgumentException e) {
            uuid = null;
        }
        File parent = new File(plugin.getDataFolder(), "skins");
        if (config.contains("skin-file")) {
            File skinFile = new File(parent, config.getString("skin-file"));
            if (!skinFile.isFile()) {
                plugin.getLogger().warning("Not exists skin file: " + skinFile);
            }
        }
        return new Entry(name, Sets.newHashSet(config.getStringList("addresses")), uuid, config.getString("skin-file"));
    }

}
