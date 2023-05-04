package com.moderocky.guardian.command;

import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.api.GuardianAPI;
import com.moderocky.guardian.config.GuardianConfig;
import mx.kenzie.centurion.*;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import static net.kyori.adventure.text.Component.text;

public class GuardianCommand extends MinecraftCommand {

    public static GuardianCommand command;
    private final GuardianConfig config = Guardian.getInstance().getGuardianConfig();
    private final GuardianAPI api = Guardian.getApi();

    protected GuardianCommand() {
        super("The main command for Guardian.");
    }

    @Override
    public MinecraftBehaviour create() {
        return command("guardian", "g", "guard")
            .arg("about", this::about)
            .arg("flags", this::flags)
            .arg("reload", this::reload)
            .permission("guardian.command.reload");
    }

    private Result flags(CommandSender sender, Arguments arguments) {
        //<editor-fold desc="Message" defaultstate="collapsed">
        final ColorProfile profile = this.getProfile();
        sender.sendMessage(Component.textOfChildren(
            text("List of Flags:", profile.dark())
        ));
        for (String flag : api.getProtectionFlags()) {
            sender.sendMessage(Component.textOfChildren(
                text(" - ", profile.pop()),
                text(flag, profile.highlight())
            ));
        }
        //</editor-fold>
        return CommandResult.PASSED;
    }

    private Result about(CommandSender sender, Arguments arguments) {
        //<editor-fold desc="Message" defaultstate="collapsed">
        final ColorProfile profile = this.getProfile();
        sender.sendMessage(Component.textOfChildren(
            text("Guardian", profile.dark()),
            Component.newline(),
            text("A lightweight zones/regions system, designed to be easy-to-use and easy for developers to access.", profile.light())
        ));
        //</editor-fold>
        return CommandResult.PASSED;
    }

    private Result reload(CommandSender sender, Arguments arguments) {
        this.config.reload();
        //<editor-fold desc="Message" defaultstate="collapsed">
        final ColorProfile profile = this.getProfile();
        sender.sendMessage(Component.textOfChildren(
            text("Guardian config reloaded.", profile.dark())
        ));
        //</editor-fold>
        return CommandResult.PASSED;
    }

}
