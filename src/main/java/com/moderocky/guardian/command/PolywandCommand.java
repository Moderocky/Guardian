package com.moderocky.guardian.command;

import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.config.GuardianConfig;
import mx.kenzie.centurion.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import static net.kyori.adventure.text.Component.text;

public class PolywandCommand extends MinecraftCommand {

    private final GuardianConfig config = Guardian.getInstance().getGuardianConfig();

    public PolywandCommand() {
        super("Obtain a guardian poly-wand.");
    }

    @Override
    public MinecraftBehaviour create() {
        return command("polywand", "gpwand", "guardianpolywand")
            .arg("get", this::getWand);
    }

    private Result getWand(CommandSender sender, Arguments arguments) {
        if (!(sender instanceof Player player)) return CommandResult.LAPSE;
        final PlayerInventory inventory = player.getInventory();
        if (inventory.getItemInMainHand().getType() == Material.AIR)
            inventory.setItemInMainHand(config.getPolywand());
        else inventory.addItem(config.getPolywand());
        //<editor-fold desc="Message" defaultstate="collapsed">
        final ColorProfile profile = this.getProfile();
        sender.sendMessage(Component.textOfChildren(
            text("Received a polywand.", profile.dark()),
            Component.newline(),
            text("Left-click to add a vertex, right-click to clear vertices.", profile.dark())
        ));
        //</editor-fold>
        return CommandResult.PASSED;
    }

}
