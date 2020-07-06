package com.moderocky.guardian.command;

import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.api.GuardianAPI;
import com.moderocky.guardian.config.GuardianConfig;
import com.moderocky.guardian.util.Messenger;
import com.moderocky.mask.command.ArgInteger;
import com.moderocky.mask.command.Commander;
import com.moderocky.mask.template.WrappedCommand;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WandCommand extends Commander<Player> implements WrappedCommand {

    private final @NotNull GuardianConfig config = Guardian.getInstance().getGuardianConfig();
    private final @NotNull Messenger messenger = Guardian.getMessenger();
    private final @NotNull GuardianAPI api = Guardian.getApi();

    @Override
    public @NotNull String getUsage() {
        return "/" + getCommand() + " [pos <int>]";
    }

    @Override
    public @NotNull String getDescription() {
        return "Obtain a guardian wand.";
    }

    @Override
    public @Nullable String getPermission() {
        return "guardian.command." + getCommand();
    }

    @Override
    public @Nullable String getPermissionMessage() {
        return null;
    }

    @Override
    public @NotNull CommandImpl create() {
        return command("wand", "gwand", "guardianwand")
                .arg(new String[]{"pos", "position"}, arg(
                        desc("Set the specified wand position to your location."),
                        (player, input) -> {
                            ItemStack item = player.getInventory().getItemInMainHand();
                            if (api.isWand(item)) {
                                api.setWandPosition(player, (int) input[0], player.getLocation());
                            }
                        }, new ArgInteger().setLabel("1/2"))
                );
    }

    @Override
    public @NotNull CommandSingleAction<Player> getDefault() {
        return player -> {
            PlayerInventory inventory = player.getInventory();
            if (inventory.getItemInMainHand().getType() == Material.AIR)
                inventory.setItemInMainHand(config.getWand());
            else
                inventory.addItem(config.getWand());
            messenger.sendMessage("You have received a wand. Use this to select points!", player);
        };
    }

    @Override
    public @NotNull String getCommand() {
        return "wand";
    }

    @Override
    public @Nullable List<String> getCompletions(int i) {
        return i == 1 ? Collections.singletonList("pos") : i == 2 ? Arrays.asList("1", "2") : null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return false;
        return execute((Player) sender, args);
    }
}
