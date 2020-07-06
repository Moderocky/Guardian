package com.moderocky.guardian.util;

import com.moderocky.guardian.Guardian;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.jetbrains.annotations.NotNull;

public class RGBMessenger implements Messenger {

    private @NotNull
    final BaseComponent[] prefix;

    public RGBMessenger(@NotNull Guardian guardian) {
        prefix = new ComponentBuilder("")
                .color(Messenger.color("d9f7ff", 'f'))
                .append("‹")
                .color(ChatColor.of("#f28900"))
                .append("Guardian")
                .color(ChatColor.of("#1ce8d4"))
                .append("›")
                .color(ChatColor.of("#f28900"))
                .append(" ")
                .color(Messenger.color("d9f7ff", 'f'))
                .create();
    }

    @Override
    public BaseComponent[] getPrefix() {
        return prefix;
    }

}
