package com.moderocky.guardian.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;

public interface Messenger {
    default void sendMessage(String text, CommandSender... players) {
        BaseComponent[] components = new ComponentBuilder("")
                .append(getPrefix())
                .append(TextComponent.fromLegacyText(text))
                .create();
        for (CommandSender player : players) {
            player.spigot().sendMessage(components);
        }
    }

    default void sendMessage(BaseComponent[] text, CommandSender... players) {
        BaseComponent[] components = new ComponentBuilder("")
                .append(getPrefix())
                .append(text)
                .create();
        for (CommandSender player : players) {
            player.spigot().sendMessage(components);
        }
    }

    default BaseComponent[] getBullets(String... texts) {
        ComponentBuilder builder = new ComponentBuilder("").reset();
        boolean first = true;
        for (String text : texts) {
            builder
                    .append(!first ? System.lineSeparator() : "")
                    .reset()
                    .append(" - ")
                    .color(ChatColor.DARK_GRAY)
                    .append("")
                    .color(ChatColor.RESET)
                    .append(text);
            first = false;
        }
        return builder.append("").reset().create();
    }

    default BaseComponent[] getBullets(BaseComponent[]... texts) {
        ComponentBuilder builder = new ComponentBuilder("").reset();
        boolean first = true;
        for (BaseComponent[] text : texts) {
            builder
                    .append(!first ? System.lineSeparator() : "")
                    .reset()
                    .append(" - ")
                    .color(ChatColor.DARK_GRAY)
                    .append("")
                    .color(ChatColor.RESET)
                    .append(text);
            first = false;
        }
        return builder.append("").reset().create();
    }

    BaseComponent[] getPrefix();
}
