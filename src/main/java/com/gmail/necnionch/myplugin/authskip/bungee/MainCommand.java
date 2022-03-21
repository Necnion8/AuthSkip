package com.gmail.necnionch.myplugin.authskip.bungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
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
            Map<String, String> addresses = pl.getAccounts().getNameAndAddresses();
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

        } else {
            sender.sendMessage(new ComponentBuilder("/authskip <add/remove/list> [player]").color(ChatColor.RED).create());
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Stream.of("add", "remove", "list")
                    .filter(e -> e.startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            Set<String> entries = new HashSet<>(pl.getTempList().getWhitelistedNames());
            entries.addAll(pl.getAccounts().getNameAndAddresses().keySet());
            return entries.stream()
                    .filter(e -> e.startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
