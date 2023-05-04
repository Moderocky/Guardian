package com.moderocky.guardian.command;

import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.api.CuboidalZone;
import com.moderocky.guardian.api.GuardianAPI;
import com.moderocky.guardian.api.PolyhedralZone;
import com.moderocky.guardian.api.Zone;
import com.moderocky.guardian.config.GuardianConfig;
import mx.kenzie.centurion.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

public class ZoneCommand extends MinecraftCommand {
    private final GuardianConfig config = Guardian.getInstance().getGuardianConfig();
    private final GuardianAPI api = Guardian.getApi();

    public ZoneCommand() {
        super("Used for managing protection zones.");
    }

    @Override
    public MinecraftBehaviour create() {
        return command("zone", "gzone", "guardianzone")
            .arg("add", ZoneArgument.ZONE, PLAYER, this::add)
            .arg("remove", ZoneArgument.ZONE, PLAYER, this::remove)
            .arg("create", "poly", Arguments.STRING.labelled("id").described("The zone ID."), this::createPoly)
            .arg("create", Arguments.STRING.labelled("id").described("The zone ID."), this::create)
            .arg("delete", ZoneArgument.ZONE, this::delete)
            .arg("describe", ZoneArgument.ZONE, Arguments.GREEDY_STRING, this::describe)
            .arg("info", ZoneArgument.ZONE.asOptional(), this::info)
            .arg("list", PLAYER.asOptional(), this::list)
            .arg("show", ZoneArgument.ZONE.asOptional(), this::show)
            .arg("teleport", ZoneArgument.ZONE, this::teleport)
            .permission("guardian.command.teleport")
            .arg("toggle", ZoneArgument.ZONE, FlagArgument.FLAG, this::toggle);
    }

    private Result teleport(CommandSender sender, Arguments arguments) {
        final Zone zone = arguments.get(0);
        final ColorProfile profile = this.getProfile();
        if (!(sender instanceof Player player)) return CommandResult.LAPSE;
        player.teleport(zone.getLocation());
        //<editor-fold desc="Message" defaultstate="collapsed">
        sender.sendMessage(Component.textOfChildren(
            text("Teleporting to '", profile.dark()),
            text(zone.getName(), profile.highlight()),
            text("'.'", profile.dark())
        ));
        //</editor-fold>
        return CommandResult.PASSED;
    }

    private Result add(CommandSender sender, Arguments arguments) {
        final Zone zone = arguments.get(0);
        final Player target = arguments.get(1);
        final ColorProfile profile = this.getProfile();
        if (sender instanceof Player player && !zone.canEdit(player.getUniqueId())) {
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("You cannot edit this zone.", profile.dark())
            ));
            //</editor-fold>
        } else if (zone.isAllowed(target.getUniqueId())) {
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("This player was already added.", profile.dark())
            ));
            //</editor-fold>
        } else {
            zone.addPlayer(target.getUniqueId());
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("Added a new player to this zone.", profile.dark())
            ));
            //</editor-fold>
            this.api.updateCache();
        }
        return CommandResult.PASSED;
    }

    private Result remove(CommandSender sender, Arguments arguments) {
        final Zone zone = arguments.get(0);
        final Player target = arguments.get(1);
        final ColorProfile profile = this.getProfile();
        if (sender instanceof Player player && !zone.canEdit(player.getUniqueId())) {
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("You cannot edit this zone.", profile.dark())
            ));
            //</editor-fold>
        } else if (!zone.isAllowed(target.getUniqueId())) {
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("This player was not added.", profile.dark())
            ));
            //</editor-fold>
        } else {
            zone.removePlayer(target.getUniqueId());
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("Removed a player from this zone.", profile.dark())
            ));
            //</editor-fold>
            this.api.updateCache();
        }
        return CommandResult.PASSED;
    }

    private Result create(CommandSender sender, Arguments arguments) {
        if (!(sender instanceof Player player)) return CommandResult.LAPSE;
        final String id = arguments.get(0);
        if (id.equals("poly")) return CommandResult.WRONG_INPUT;
        final Location l1 = api.getWandPosition(player, 1), l2 = api.getWandPosition(player, 2);
        final ColorProfile profile = this.getProfile();
        if (l1 == null || l2 == null) {
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("Please set zone corners using a zone wand first.", profile.dark())
            ));
            //</editor-fold>
            return CommandResult.PASSED;
        } else if (l1.getWorld() != l2.getWorld() || l1.distanceSquared(l2) > (config.maxZoneDiameter * config.maxZoneDiameter)) {
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("The selected area is larger than ", profile.dark()),
                text(config.maxZoneDiameter, profile.highlight()),
                text("×", profile.pop()),
                text(config.maxZoneDiameter, profile.highlight()),
                text(" blocks in diameter.", profile.dark())
            ));
            //</editor-fold>
            return CommandResult.PASSED;
        } else if (api.getZone(id) != null) {
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("A zone with the id '", profile.dark()),
                text(id, profile.highlight()),
                text("' already exists.", profile.dark())
            ));
            //</editor-fold>
            return CommandResult.PASSED;
        }
        final CuboidalZone zone = CuboidalZone.createZone(player, id, l1, l2);
        if (!player.isOp() && !api.canCreateZone(zone, player)) {
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("This zone conflicts with others that you are unable to edit or override.", profile.dark())
            ));
            //</editor-fold>
            return CommandResult.PASSED;
        }
        this.api.registerZone(zone);
        this.api.scheduleSave();
        this.api.updateCache();
        //<editor-fold desc="Message" defaultstate="collapsed">
        sender.sendMessage(Component.textOfChildren(
            text("A cuboidal zone with the id '", profile.dark()),
            text(id, profile.highlight()),
            text("' has been created.", profile.dark())
        ));
        //</editor-fold>
        return CommandResult.PASSED;
    }

    private Result createPoly(CommandSender sender, Arguments arguments) {
        if (!(sender instanceof Player player)) return CommandResult.LAPSE;
        final String id = arguments.get(0);
        if (id.length() < 1) return CommandResult.WRONG_INPUT;
        final List<Location> locations = api.getPolywandPositions(player);
        final ColorProfile profile = this.getProfile();
        if (locations.size() < 4) {
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("Please set at least 4 zone vertices using a zone polywand first.", profile.dark())
            ));
            //</editor-fold>
            return CommandResult.PASSED;
        }
        final World world = locations.get(0).getWorld();
        for (Location location : locations) {
            if (location.getWorld() != world || location.distanceSquared(locations.get(0)) > (config.maxZoneDiameter * config.maxZoneDiameter)) {
                //<editor-fold desc="Message" defaultstate="collapsed">
                sender.sendMessage(Component.textOfChildren(
                    text("The selected area is larger than ", profile.dark()),
                    text(config.maxZoneDiameter, profile.highlight()),
                    text("×", profile.pop()),
                    text(config.maxZoneDiameter, profile.highlight()),
                    text(" blocks in diameter.", profile.dark())
                ));
                //</editor-fold>
                return CommandResult.PASSED;
            }
        }
        if (api.getZone(id) != null) {
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("A zone with the id '", profile.dark()),
                text(id, profile.highlight()),
                text("' already exists.", profile.dark())
            ));
            //</editor-fold>
            return CommandResult.PASSED;
        }
        final PolyhedralZone zone = PolyhedralZone.createZone(player, id, locations.toArray(new Location[0]));
        if (!player.isOp() && !api.canCreateZone(zone, player)) {
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("This zone conflicts with others that you are unable to edit or override.", profile.dark())
            ));
            //</editor-fold>
            return CommandResult.PASSED;
        }
        this.api.registerZone(zone);
        this.api.scheduleSave();
        this.api.updateCache();
        //<editor-fold desc="Message" defaultstate="collapsed">
        sender.sendMessage(Component.textOfChildren(
            text("A polyhedral zone with the id '", profile.dark()),
            text(id, profile.highlight()),
            text("' has been created.", profile.dark())
        ));
        //</editor-fold>
        return CommandResult.PASSED;
    }

    private Result delete(CommandSender sender, Arguments arguments) {
        final Zone zone = arguments.get(0);
        final ColorProfile profile = this.getProfile();
        if (sender instanceof Player player && !zone.canEdit(player.getUniqueId())) {
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("You cannot edit this zone.", profile.dark())
            ));
            //</editor-fold>
        } else {
            this.api.removeZone(zone);
            this.api.scheduleSave();
            this.api.updateCache();
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("The zone '", profile.dark()),
                text(zone.getName(), profile.highlight()),
                text("' has been removed.", profile.dark())
            ));
            //</editor-fold>
        }
        return CommandResult.PASSED;
    }

    private Result describe(CommandSender sender, Arguments arguments) {
        final Zone zone = arguments.get(0);
        final String text = arguments.get(1);
        final ColorProfile profile = this.getProfile();
        if (sender instanceof Player player && !zone.canEdit(player.getUniqueId())) {
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("You cannot edit this zone.", profile.dark())
            ));
            //</editor-fold>
        } else {
            zone.setDescription(text);
            this.api.scheduleSave();
            this.api.updateCache();
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("This zone's description was updated.", profile.dark())
            ));
            //</editor-fold>
        }
        return CommandResult.PASSED;
    }

    private Result info(CommandSender sender, Arguments arguments) {
        final Zone zone = arguments.get(0);
        final ColorProfile profile = this.getProfile();
        if (zone == null && sender instanceof Player player) {
            final List<Zone> zones = api.getZones(player.getLocation().getBlock().getLocation());
            if (zones.isEmpty()) {
                //<editor-fold desc="Message" defaultstate="collapsed">
                sender.sendMessage(Component.textOfChildren(
                    text("There are no zones at your location.", profile.dark())
                ));
                //</editor-fold>
                return CommandResult.PASSED;
            }
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("Zones at your location:", profile.dark())
            ));
            for (Zone thing : zones) {
                sender.sendMessage(Component.textOfChildren(
                    text(" - ", profile.pop()),
                    text(thing.getKey().getKey(), profile.highlight())
                ));
            }
            //</editor-fold>
            return CommandResult.PASSED;
        } else if (zone == null) return CommandResult.LAPSE;
        final List<String> players = new ArrayList<>();
        for (UUID uuid : zone.getAllowedPlayers()) players.add(Bukkit.getOfflinePlayer(uuid).getName());
        final List<String> flags = new ArrayList<>();
        for (String flag : zone.getFlags()) flags.add(convertCase(flag));
        //<editor-fold desc="Message" defaultstate="collapsed">
        final String description = zone.getDescription();
        sender.sendMessage(Component.textOfChildren(
            text("Zone Info for ", profile.dark()),
            text(zone.getName(), profile.highlight()),
            Component.newline(),
            text(description == null ? "No description." : description, profile.light()),
            Component.newline(),
            Component.newline(),
            text("Allowed Players:", profile.dark()),
            Component.newline(),
            text(players.isEmpty() ? "(None)" : String.join(", ", players), profile.highlight()),
            Component.newline(),
            Component.newline(),
            text("Active Flags:", profile.dark()),
            Component.newline(),
            text(flags.isEmpty() ? "(None)" : String.join(", ", flags), profile.highlight()),
            Component.newline()
        ));
        //</editor-fold>
        return CommandResult.PASSED;
    }

    private Result list(CommandSender sender, Arguments arguments) {
        final ColorProfile profile = this.getProfile();
        final Player player = arguments.get(0);
        if (api.getZoneKeys().isEmpty()) {
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("No zones are defined.", profile.dark())
            ));
            //</editor-fold>
        } else if (player == null) {
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("List of Zones:", profile.dark())
            ));
            for (Zone zone : api.getZones()) {
                sender.sendMessage(Component.textOfChildren(
                    text(" - ", profile.pop()),
                    text(this.convertCase(zone.getKey().getKey()), profile.highlight())
                ).clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/zone info " + zone.getKey().getKey())));
            }
            //</editor-fold>
        } else {
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("List of Zones accessible by ", profile.dark()),
                text(player.getName(), profile.highlight()),
                text(":", profile.dark())
            ));
            for (Zone zone : api.getZones()) {
                if (!zone.canEdit(player.getUniqueId())) continue;
                sender.sendMessage(Component.textOfChildren(
                    text(" - ", profile.pop()),
                    text(this.convertCase(zone.getKey().getKey()), profile.highlight())
                ).clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/zone info " + zone.getKey().getKey())));
            }
            //</editor-fold>
        }
        return CommandResult.PASSED;
    }

    private Result show(CommandSender sender, Arguments arguments) {
        final Zone zone = arguments.get(0);
        final ColorProfile profile = this.getProfile();
        if (zone == null && sender instanceof Player player) {
            final List<Zone> zones = api.getZones(player.getLocation().getBlock().getLocation());
            if (zones.isEmpty()) {
                //<editor-fold desc="Message" defaultstate="collapsed">
                sender.sendMessage(Component.textOfChildren(
                    text("There are no zones at your location.", profile.dark())
                ));
                //</editor-fold>
            } else zones.get(0).showBounds();
            return CommandResult.PASSED;
        } else if (zone == null) return CommandResult.LAPSE;
        zone.showBounds();
        return CommandResult.PASSED;
    }

    private Result toggle(CommandSender sender, Arguments arguments) {
        final Zone zone = arguments.get(0);
        final String flag = arguments.get(1);
        final ColorProfile profile = this.getProfile();
        if (sender instanceof Player player && !zone.canEdit(player.getUniqueId())) {
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("You cannot edit this zone.", profile.dark())
            ));
            //</editor-fold>
        } else if (!sender.hasPermission("guardian.flag." + flag)) {
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("You do not have permission to toggle this flag.", profile.dark())
            ));
            //</editor-fold>
        } else if (zone.hasFlag(flag)) {
            zone.removeFlag(flag);
            this.api.updateCache();
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("Deactivated ", profile.dark()),
                text(this.convertCase(flag), profile.highlight()),
                text(" for ", profile.dark()),
                text(zone.getName(), profile.highlight()),
                text(".", profile.dark())
            ));
            //</editor-fold>
        } else {
            zone.addFlag(flag);
            this.api.updateCache();
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("Activated ", profile.dark()),
                text(this.convertCase(flag), profile.highlight()),
                text(" for ", profile.dark()),
                text(zone.getName(), profile.highlight()),
                text(".", profile.dark())
            ));
            //</editor-fold>
        }
        return CommandResult.PASSED;
    }

    private String convertCase(String string) {
        return Zone.convertCase(string);
    }

}
