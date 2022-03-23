package com.gmail.necnionch.myplugin.authskip.bungee.commands;

import com.gmail.necnionch.myplugin.authskip.bungee.AuthSkip;
import com.gmail.necnionch.myplugin.authskip.bungee.config.Accounts;
import com.gmail.necnionch.myplugin.authskip.bungee.util.SkinData;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainCommand extends Command implements TabExecutor {
    private AuthSkip pl;

    public MainCommand(AuthSkip pl) {
        super("authskip", "authskip.command.authskip");
        this.pl = pl;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length >= 1 && "add".equalsIgnoreCase(args[0])) {
            if (args.length >= 3) {
                try {
                    //noinspection ResultOfMethodCallIgnored
                    InetAddress.getAllByName(args[2]);
                } catch (UnknownHostException e) {
                    sender.sendMessage(new ComponentBuilder("無効なアドレスです。").color(ChatColor.RED).create());
                    return;
                }
                pl.getAccounts().put(args[1], args[2]);
                pl.getAccounts().save();
                sender.sendMessage(new ComponentBuilder("プレイヤー ")
                        .append(args[1]).color(ChatColor.YELLOW)
                        .append(" のオフラインモード接続を").color(ChatColor.WHITE)
                        .append("許可").color(ChatColor.GREEN)
                        .append("しました。").color(ChatColor.WHITE)
                        .create());

            } else if (args.length == 2) {
                pl.getTempList().put(args[1]);
                sender.sendMessage(new ComponentBuilder("プレイヤー名 ")
                        .append(args[1]).color(ChatColor.YELLOW)
                        .append(" のオフラインモード接続を一時的に許可します。").color(ChatColor.WHITE)
                        .append("(1分間 有効)").color(ChatColor.GOLD)
                        .create());
            } else {
                sender.sendMessage(new ComponentBuilder("/authskip add (playerName) [address]").color(ChatColor.RED).create());
            }

        } else if (args.length >= 2 && "remove".equalsIgnoreCase(args[0])) {
            if (pl.getAccounts().remove(args[1]) != null) {
                pl.getAccounts().save();
                sender.sendMessage(new ComponentBuilder("プレイヤー ")
                        .append(args[1]).color(ChatColor.YELLOW)
                        .append(" のオフラインモード接続を").color(ChatColor.WHITE)
                        .append("ブロック").color(ChatColor.RED)
                        .append("しました。").color(ChatColor.WHITE)
                        .create());

            } else if (pl.getTempList().contains(args[1])) {
                sender.sendMessage(new ComponentBuilder("プレイヤー ")
                        .append(args[1]).color(ChatColor.YELLOW)
                        .append(" のオフラインモード接続を").color(ChatColor.WHITE)
                        .append("ブロック").color(ChatColor.RED)
                        .append("しました。").color(ChatColor.WHITE)
                        .create());

            } else {
                sender.sendMessage(new ComponentBuilder("プレイヤー ")
                        .append(args[1]).color(ChatColor.YELLOW)
                        .append(" のオフラインモード接続は許可されていません。").color(ChatColor.WHITE)
                        .create());
            }
            pl.getTempList().reset(args[1]);

        } else if (args.length >= 1 && "list".equalsIgnoreCase(args[0])) {
            Map<String, String> addresses = pl.getAccounts().getEntries().stream()
                    .filter(e -> !e.getAddresses().isEmpty())
                    .collect(Collectors.toMap(Accounts.Entry::getName, e -> Lists.newArrayList(e.getAddresses()).get(0)));

            if (addresses.isEmpty() && pl.getTempList().isEmpty()) {
                sender.sendMessage(new ComponentBuilder("許可されたプレイヤーはありません。").color(ChatColor.RED).create());
                return;
            }

            if (!addresses.isEmpty()) {
                ComponentBuilder b = new ComponentBuilder("許可されたプレイヤー:\n  ").color(ChatColor.GOLD);
                List<Map.Entry<String, String>> list = new ArrayList<>(addresses.entrySet());
                while (!list.isEmpty()) {
                    Map.Entry<String, String> e = list.remove(0);
                    b.append(e.getKey()).color(ChatColor.WHITE);
                    b.append("(" + e.getValue() + ")").color(ChatColor.GRAY);

                    if (!list.isEmpty())
                        b.append(", ");
                }
                sender.sendMessage(b.create());
            }

            if (!pl.getTempList().isEmpty()) {
                ComponentBuilder b = new ComponentBuilder("一時的に許可されるプレイヤー:\n  ").color(ChatColor.YELLOW);
                List<String> list = new ArrayList<>(pl.getTempList().getWhitelistedNames());
                while (!list.isEmpty()) {
                    String e = list.remove(0);
                    b.append(e).color(ChatColor.WHITE);

                    if (!list.isEmpty())
                        b.append(", ").color(ChatColor.GRAY);
                }
                sender.sendMessage(b.create());
            }

        } else if (args.length >= 1 && "reload".equalsIgnoreCase(args[0])) {
            pl.getAccounts().load();
            sender.sendMessage(new ComponentBuilder("設定ファイルを再読み込みしました").color(ChatColor.GREEN).create());

        } else if (args.length >= 1 && "setMe".equalsIgnoreCase(args[0]) && sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            Accounts.Entry offline = pl.getAccounts().get(player.getName());
            if (offline != null) {
                sender.sendMessage(new ComponentBuilder("既にオフラインモード接続に設定されています").color(ChatColor.RED).create());
                return;
            }

            String address = player.getPendingConnection().getAddress().getHostString();
            Accounts.Entry account = pl.getAccounts().put(player.getName(), address);
            account.setCustomUniqueId(player.getUniqueId());

            SkinData localSkin = pl.getSkinManager().getByPlayer(player);
            String skinName = player.getUniqueId() + ".skin";
            boolean applySkin = false;
            if (pl.saveSkin(skinName, localSkin)) {
                account.setSkinFileName(skinName);
                applySkin = true;
            }

            account.save();
            sender.sendMessage(new ComponentBuilder("オフラインモード接続に設定しました").color(ChatColor.GOLD)
                            .append(" (オンラインUUID" + ((applySkin) ? "、スキン適用)" : ")")).color(ChatColor.GRAY).create());
            sender.sendMessage(new ComponentBuilder("再接続することで接続が変更されます").color(ChatColor.GRAY).create());

        } else if (args.length >= 2 && "generateSkin".equalsIgnoreCase(args[0]) && sender instanceof ProxiedPlayer) {
            UUID uuid;
            try {
                uuid = UUID.fromString(args[1]);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(new ComponentBuilder("UUIDを指定してください").color(ChatColor.RED).create());
                return;
            }

            pl.getSkinManager().fetchFromMojangProfile(uuid).whenComplete((skin, error) -> {
                if (error != null || skin == null) {
                    sender.sendMessage(new ComponentBuilder("スキンを取得できませんでした").color(ChatColor.RED).create());

                } else if (pl.saveSkin(uuid + ".skin", skin)) {
                    sender.sendMessage(new ComponentBuilder("スキンを取得しました: " + uuid).color(ChatColor.GREEN).create());

                } else {
                    sender.sendMessage(new ComponentBuilder("スキンを取得しましたが、保存に失敗しました: " + uuid).color(ChatColor.RED).create());
                }
            });

        } else {
            sender.sendMessage(new ComponentBuilder("/authskip <add/remove/list/reload/setMe/generateSkin> [player]").color(ChatColor.RED).create());
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Stream.of("add", "remove", "list", "reload", "setme")
                    .filter(e -> e.startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            Set<String> entries = new HashSet<>(pl.getTempList().getWhitelistedNames());
            entries.addAll(pl.getAccounts().getEntries().stream().map(Accounts.Entry::getName).collect(Collectors.toSet()));
            return entries.stream()
                    .filter(e -> e.startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
