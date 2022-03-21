package com.gmail.necnionch.myplugin.authskip.bungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PlayerHandshakeEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;


public final class AuthSkip extends Plugin implements Listener {
    private AccountConfig accountConfig = new AccountConfig(this);
    private TemporaryWhitelist tempWhitelist = new TemporaryWhitelist(this);

    @Override
    public void onEnable() {
        accountConfig.load();

        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getPluginManager().registerCommand(this, new MainCommand(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public AccountConfig getAccounts() {
        return accountConfig;
    }

    public TemporaryWhitelist getTempList() {
        return tempWhitelist;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PreLoginEvent event) {
        if (event.isCancelled() || event.getCancelReasonComponents() != null)
            return;
        if (!event.getConnection().isOnlineMode())
            return;

        String playerName = event.getConnection().getName();
        if (playerName == null)
            return;
        String whitelistedAddress = accountConfig.get(playerName);
        String connectedAddress = event.getConnection().getAddress().getHostString();

        if (whitelistedAddress == null) {
            if (tempWhitelist.contains(playerName)) {
                getLogger().info("Add list!");
                getAccounts().put(playerName, connectedAddress);
                getAccounts().save();
                whitelistedAddress = connectedAddress;
            } else {
                return;
            }
        }
        tempWhitelist.reset(playerName);

        if (whitelistedAddress.equalsIgnoreCase(event.getConnection().getAddress().getHostString())) {
            getLogger().info("Allowed unauthenticated player \"" + playerName + "\" to join");
            event.getConnection().setOnlineMode(false);

        } else {
            getLogger().warning("Unauthenticated player \"" + playerName + "\" does not match the registered address!");
            getLogger().warning("required: " + whitelistedAddress + ", request: " + event.getConnection().getAddress().getHostString());

            event.setCancelReason(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',
                    "&cサーバーに参加できません\n&7管理者にお問い合わせください。"
            )));
            event.setCancelled(true);
        }

    }

}
