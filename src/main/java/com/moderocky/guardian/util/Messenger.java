package com.moderocky.guardian.util;

import com.moderocky.guardian.Guardian;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Messenger {

    private final @NotNull Guardian guardian;
    private @NotNull
    final BaseComponent[] prefix = new ComponentBuilder("")
            .color(ChatColor.WHITE)
            .append("‹")
            .color(ChatColor.DARK_GRAY)
            .append("Guardian")
            .color(ChatColor.DARK_AQUA)
            .append("›")
            .color(ChatColor.DARK_GRAY)
            .append(" ")
            .color(ChatColor.WHITE)
            .create();

    public Messenger(@NotNull Guardian guardian) {
        this.guardian = guardian;
    }

    public void sendMessage(String text, CommandSender... players) {
        BaseComponent[] components = new ComponentBuilder("")
                .append(getPrefix())
                .append(TextComponent.fromLegacyText(text))
                .create();
        for (CommandSender player : players) {
            player.sendMessage(components);
        }
    }

    public void sendMessage(BaseComponent[] text, CommandSender... players) {
        BaseComponent[] components = new ComponentBuilder("")
                .append(getPrefix())
                .append(text)
                .create();
        for (CommandSender player : players) {
            player.sendMessage(components);
        }
    }

    public BaseComponent[] getBullets(String... texts) {
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

    public BaseComponent[] getBullets(BaseComponent[]... texts) {
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

    public BaseComponent[] getPrefix() {
        return prefix;
    }

}
