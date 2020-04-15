package com.moderocky.guardian.command;

import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.config.GuardianConfig;
import com.moderocky.guardian.util.Messenger;
import com.moderocky.mask.template.WrappedCommand;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class WandCommand implements WrappedCommand {

    private final @NotNull GuardianConfig config = Guardian.getInstance().getGuardianConfig();
    private final @NotNull Messenger messenger = Guardian.getMessenger();

    @Override
    public @NotNull List<String> getAliases() {
        return Arrays.asList("gwand", "guardianwand");
    }

    @Override
    public @NotNull String getUsage() {
        return "/wand";
    }

    @Override
    public @NotNull String getDescription() {
        return "Obtain a guardian wand.";
    }

    @Override
    public @Nullable String getPermission() {
        return "guardian.command.wand";
    }

    @Override
    public @Nullable String getPermissionMessage() {
        return null;
    }

    @Override
    public @NotNull String getCommand() {
        return "wand";
    }

    @Override
    public @Nullable List<String> getCompletions(int i) {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        PlayerInventory inventory = player.getInventory();
        if (inventory.getItemInMainHand().getType() == Material.AIR)
            inventory.setItemInMainHand(config.getWand());
        else
            inventory.addItem(config.getWand());
        messenger.sendMessage("You have received a wand. Use this to select points!", player);
        return true;
    }
}
