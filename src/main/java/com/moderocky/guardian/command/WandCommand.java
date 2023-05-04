package com.moderocky.guardian.command;

import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.api.GuardianAPI;
import com.moderocky.guardian.config.GuardianConfig;
import mx.kenzie.centurion.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import static net.kyori.adventure.text.Component.text;

public class WandCommand extends MinecraftCommand {

    private final GuardianConfig config = Guardian.getInstance().getGuardianConfig();
    private final GuardianAPI api = Guardian.getApi();

    public WandCommand() {
        super("Obtain a guardian wand.");
    }

    @Override
    public MinecraftBehaviour create() {
        return command("wand", "gwand", "guardianwand")
            .arg("pos", Arguments.INTEGER, this::setPosition)
            .arg("get", this::getWand);
    }

    private Result setPosition(CommandSender sender, Arguments arguments) {
        if (!(sender instanceof Player player)) return CommandResult.LAPSE;
        final PlayerInventory inventory = player.getInventory();
        final ItemStack item = inventory.getItemInMainHand();
        if (api.isWand(item)) api.setWandPosition(player, arguments.get(0), player.getLocation(), BlockFace.UP);
        return CommandResult.PASSED;
    }

    private Result getWand(CommandSender sender, Arguments arguments) {
        if (!(sender instanceof Player player)) return CommandResult.LAPSE;
        final PlayerInventory inventory = player.getInventory();
        if (inventory.getItemInMainHand().getType() == Material.AIR)
            inventory.setItemInMainHand(config.getWand());
        else inventory.addItem(config.getWand());
        //<editor-fold desc="Message" defaultstate="collapsed">
        final ColorProfile profile = this.getProfile();
        sender.sendMessage(Component.textOfChildren(
            text("Received a wand.", profile.dark())
        ));
        //</editor-fold>
        return CommandResult.PASSED;
    }

}
