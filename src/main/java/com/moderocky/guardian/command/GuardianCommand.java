package com.moderocky.guardian.command;

import com.moderocky.mask.template.WrappedCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class GuardianCommand implements WrappedCommand {
    @Override
    public @NotNull List<String> getAliases() {
        return Arrays.asList("g", "guard");
    }

    @Override
    public @NotNull String getUsage() {
        return "/guardian";
    }

    @Override
    public @NotNull String getDescription() {
        return "The main command for Guardian.";
    }

    @Override
    public @Nullable String getPermission() {
        return "guardian.command.guardian";
    }

    @Override
    public @Nullable String getPermissionMessage() {
        return null;
    }

    @Override
    public @NotNull String getCommand() {
        return "guardian";
    }

    @Override
    public @Nullable List<String> getCompletions(int i) {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 1) {

        }
        return true;
    }
}
