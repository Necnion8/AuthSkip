package com.gmail.necnionch.myplugin.authskip.bungee;

import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TemporaryWhitelist {
    private AuthSkip pl;
    private Map<String, ScheduledTask> list = new HashMap<>();

    public TemporaryWhitelist(AuthSkip pl) {
        this.pl = pl;
    }


    public void put(String name) {
        reset(name);
        list.put(name, pl.getProxy().getScheduler().schedule(pl, () -> list.remove(name), 1, TimeUnit.MINUTES));
    }

    public void reset(String name) {
        ScheduledTask task = list.remove(name);
        if (task != null)
            task.cancel();
    }

    public boolean contains(String name) {
        return list.containsKey(name);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public Set<String> getWhitelistedNames() {
        return list.keySet();
    }



}
