package com.moderocky.guardian.util;

import com.moderocky.guardian.Guardian;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.jetbrains.annotations.NotNull;

public class LegacyMessenger implements Messenger {

    private @NotNull final BaseComponent[] prefix;

    public LegacyMessenger(@NotNull Guardian guardian) {
        prefix = new ComponentBuilder("")
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
    }

    @Override
    public BaseComponent[] getPrefix() {
        return prefix;
    }

}
