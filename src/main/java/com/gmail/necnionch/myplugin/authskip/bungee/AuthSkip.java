package com.gmail.necnionch.myplugin.authskip.bungee;

import com.gmail.necnionch.myplugin.authskip.bungee.commands.MainCommand;
import com.gmail.necnionch.myplugin.authskip.bungee.config.Accounts;
import com.gmail.necnionch.myplugin.authskip.bungee.util.SkinData;
import com.gmail.necnionch.myplugin.authskip.bungee.util.SkinManager;
import com.gmail.necnionch.myplugin.authskip.bungee.util.TemporaryWhitelist;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.Executor;


public final class AuthSkip extends Plugin implements Listener {
    private final SkinManager skinManager = new SkinManager(this);
    private final Accounts accounts = new Accounts(this);
    private final TemporaryWhitelist tempWhitelist = new TemporaryWhitelist(this);
    private final Executor asyncExecutor = command -> getProxy().getScheduler().runAsync(AuthSkip.this, command);

    @Override
    public void onEnable() {
        accounts.load();

        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getPluginManager().registerCommand(this, new MainCommand(this));
    }


    public SkinManager getSkinManager() {
        return skinManager;
    }

    public Accounts getAccounts() {
        return accounts;
    }

    public TemporaryWhitelist getTempList() {
        return tempWhitelist;
    }

    public Executor getExecutor() {
        return asyncExecutor;
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(PreLoginEvent event) {
        if (event.isCancelled() || event.getCancelReasonComponents() != null)
            return;

        String playerName = event.getConnection().getName();
        if (playerName == null)
            return;

        String address = event.getConnection().getAddress().getHostString();
        Accounts.Entry account = accounts.get(playerName);

        if (account == null) {
            // pre add check
            if (tempWhitelist.contains(playerName)) {
                getLogger().info("Add list!");
                account = getAccounts().put(playerName, address);
                // allowed
            } else {
                return;  // unknown offline name
            }
        }
        tempWhitelist.reset(playerName);

        // whitelist check
        if (!account.getAddresses().contains(address)) {
            getLogger().warning("Unauthenticated player \"" + playerName + "\" does not match the registered address!");
            getLogger().warning("Connection by " + address);

            event.setCancelReason(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',
                    "&cサーバーに参加できません\n&7管理者にお問い合わせください。"
            )));
            event.setCancelled(true);
            return;
        }

        try {
            processPreLogin(event, account);
        } catch (Throwable e) {
            e.printStackTrace();
            event.setCancelReason(new ComponentBuilder()
                    .append("参加処理中に内部エラーが発生しました\n").color(ChatColor.RED)
                    .append("管理者にお問い合わせください。").color(ChatColor.GRAY)
                    .create()
            );
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(LoginEvent event) {
        String playerName = event.getConnection().getName();
        Accounts.Entry account = accounts.get(playerName);
        if (playerName == null || account == null)
            return;

        event.registerIntent(this);
        getProxy().getScheduler().runAsync(this, () -> {
            try {
                processLogin(event, account);
            } catch (Throwable e) {
                e.printStackTrace();
                event.setCancelReason(new ComponentBuilder()
                        .append("参加処理中に内部エラーが発生しました\n").color(ChatColor.RED)
                        .append("管理者にお問い合わせください。").color(ChatColor.GRAY)
                        .create()
                );
                event.setCancelled(true);
            }
            event.completeIntent(this);
        });
    }


    private void processPreLogin(PreLoginEvent event, Accounts.Entry account) {
        getLogger().info("Allowed unauthenticated player \"" + account.getName() + "\" to join");

        // set offline
        event.getConnection().setOnlineMode(false);

        // set uuid
        UUID customId = account.getCustomUniqueId();
        if (customId != null) {
            getLogger().info("Apply custom uuid set: " + customId);
            event.getConnection().setUniqueId(customId);
        }

    }

    private void processLogin(LoginEvent event, Accounts.Entry account) throws Throwable {
        // apply skin
        String skinFileName = account.getSkinFileName();
        if (skinFileName != null) {
            try {
                SkinData skinData = loadSkin(skinFileName);
                if (skinData != null) {
                    getLogger().info("Apply custom skin: " + skinFileName);
                    applySkin(((InitialHandler) event.getConnection()), skinData);
                }
            } catch (Throwable e) {
                getLogger().warning("Failed to apply skin: " + e.getMessage());
            }
        }
    }


    private void applySkin(InitialHandler initial, SkinData skinData) throws ReflectiveOperationException {
        LoginResult.Property property = new LoginResult.Property("textures", skinData.getValue(), skinData.getSignature());
        LoginResult loginResult = new LoginResult(null, null, new LoginResult.Property[]{property});

        Field loginProfile = initial.getClass().getDeclaredField("loginProfile");
        loginProfile.setAccessible(true);
        loginProfile.set(initial, loginResult);
    }


    public SkinData loadSkin(String fileName) {
        File file = new File(getDataFolder(), "skins/" + fileName);
        if (file.isFile()) {
            try {
                return SkinData.loadFrom(file);
            } catch (IOException e) {
                getLogger().warning("Failed to load skin file: " + e.getMessage());
            }
        }
        return null;
    }

    public boolean saveSkin(String fileName, SkinData skin) {
        File file = new File(getDataFolder(), "skins/" + fileName);
        if (file.isFile())
            return false;

        try {
            skin.saveTo(file);
            return true;
        } catch (IOException e) {
            getLogger().warning("Failed to save skin file: " + e.getMessage());
        }
        return false;
    }


}
