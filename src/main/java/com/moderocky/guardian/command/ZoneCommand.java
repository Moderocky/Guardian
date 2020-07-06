package com.moderocky.guardian.command;

import com.google.common.base.Ascii;
import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.api.CuboidalZone;
import com.moderocky.guardian.api.GuardianAPI;
import com.moderocky.guardian.api.PolyhedralZone;
import com.moderocky.guardian.api.Zone;
import com.moderocky.guardian.command.argument.ArgFlag;
import com.moderocky.guardian.command.argument.ArgZone;
import com.moderocky.guardian.config.GuardianConfig;
import com.moderocky.guardian.util.Messenger;
import com.moderocky.mask.api.MagicList;
import com.moderocky.mask.command.*;
import com.moderocky.mask.template.WrappedCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ZoneCommand extends Commander<CommandSender> implements WrappedCommand {

    public static ZoneCommand command;
    private final @NotNull GuardianConfig config = Guardian.getInstance().getGuardianConfig();
    private final @NotNull Messenger messenger = Guardian.getMessenger();
    private final @NotNull GuardianAPI api = Guardian.getApi();

    public ZoneCommand() {
        super();
        command = this;
    }

    @Override
    public @NotNull CommandImpl create() {
        return command("zone")
                .arg("add",
                        desc("Allow a player to access a zone."),
                        sender -> messenger.sendMessage("/zone add <zone_id> <player/uuid>", sender),
                        arg(
                                desc("Allow a player to access a zone."),
                                (sender, input) -> {
                                    String id = (String) input[0];
                                    Zone zone = api.getZone(id);
                                    if (zone == null) {
                                        messenger.sendMessage("No zone with the id '" + id + "' can be found.", sender);
                                        return;
                                    }
                                    if (!zone.canEdit(sender instanceof Player ? ((Player) sender).getUniqueId() : UUID.randomUUID())) {
                                        messenger.sendMessage("You may not edit this zone.", sender);
                                        return;
                                    }
                                    OfflinePlayer player = (OfflinePlayer) input[1];
                                    if (zone.isAllowed(player.getUniqueId())) {
                                        messenger.sendMessage("This player was already added.", sender);
                                    } else {
                                        zone.addPlayer(player.getUniqueId());
                                        messenger.sendMessage("Added a new player to this zone.", sender);
                                    }
                                    api.updateCache();
                                },
                                new ArgZone(),
                                new ArgPlayer()
                        )
                )
                .arg("create",
                        desc("Create a new zone.\nUse the wand to select corners."),
                        sender -> messenger.sendMessage("/zone create <zone_id>", sender),
                        arg(
                                desc("Create a new zone.\nUse the wand to select corners."),
                                (sender, input) -> {
                                    if (!(sender instanceof Player)) return;
                                    Player player = (Player) sender;
                                    Location l1 = api.getWandPosition(player, 1);
                                    Location l2 = api.getWandPosition(player, 2);
                                    if (l1 == null || l2 == null) {
                                        messenger.sendMessage("Please set zone corners using a zone wand first.", player);
                                        return;
                                    }
                                    if (l1.getWorld() != l2.getWorld() || l1.distanceSquared(l2) > (config.maxZoneDiameter * config.maxZoneDiameter)) {
                                        messenger.sendMessage("The selected area is larger than " + config.maxZoneDiameter + "×" + config.maxZoneDiameter + " blocks in diameter.", player);
                                        return;
                                    }
                                    @SuppressWarnings("all")
                                    String id = input[0].toString();
                                    if (api.getZone(id) != null) {
                                        messenger.sendMessage("A zone with the id '" + id + "' already exists.", sender);
                                        return;
                                    }
                                    CuboidalZone zone = CuboidalZone.createZone(player, id, l1, l2);
                                    if (!player.isOp() && !api.canCreateZone(zone, player)) {
                                        messenger.sendMessage("This zone conflicts with others that you are unable to edit or override.", player);
                                        return;
                                    }
                                    api.registerZone(zone);
                                    api.scheduleSave();
                                    api.updateCache();
                                    messenger.sendMessage("A cuboidal zone with the id '" + id + "' has been created.", sender);
                                },
                                new ArgString().setLabel("zone_id")
                        )
                )
                .arg("createpoly",
                        desc("Create a polyhedral zone.\nUse the poly-wand to select points."),
                        sender -> messenger.sendMessage("/zone createpoly <zone_id>", sender),
                        arg(
                                desc("Create a polyhedral zone.\nUse the poly-wand to select points."),
                                (sender, input) -> {
                                    if (!(sender instanceof Player)) return;
                                    Player player = (Player) sender;
                                    List<Location> locations = api.getPolywandPositions(player);
                                    if (locations.size() < 4) {
                                        messenger.sendMessage("Please set at least 4 zone vertices using a zone polywand first.", player);
                                        return;
                                    }
                                    World world = locations.get(0).getWorld();
                                    for (Location location : locations) {
                                        if (location.getWorld() != world || location.distanceSquared(locations.get(0)) > (config.maxZoneDiameter * config.maxZoneDiameter)) {
                                            messenger.sendMessage("The selected area is larger than " + config.maxZoneDiameter + "×" + config.maxZoneDiameter + " blocks in diameter.", player);
                                            return;
                                        }
                                    }
                                    String id = input[0].toString();
                                    if (api.getZone(id) != null) {
                                        messenger.sendMessage("A zone with the id '" + id + "' already exists.", sender);
                                        return;
                                    }
                                    PolyhedralZone zone = PolyhedralZone.createZone(player, id, locations.toArray(new Location[0]));
                                    if (!player.isOp() && !api.canCreateZone(zone, player)) {
                                        messenger.sendMessage("This zone conflicts with others that you are unable to edit or override.", player);
                                        return;
                                    }
                                    api.registerZone(zone);
                                    api.scheduleSave();
                                    api.updateCache();
                                    messenger.sendMessage("A polyhedral zone with the id '" + id + "' has been created.", sender);
                                },
                                new ArgString().setLabel("zone_id")
                        )
                )
                .arg("delete",
                        desc("Delete a zone."),
                        sender -> messenger.sendMessage("/zone delete <zone_id>", sender),
                        arg(
                                desc("Delete a zone."),
                                (sender, input) -> {
                                    String id = input[0].toString();
                                    Zone zone = api.getZone(id);
                                    if (zone == null) {
                                        messenger.sendMessage("No zone with the id '" + id + "' can be found.", sender);
                                        return;
                                    }
                                    if (!zone.canEdit(sender instanceof Player ? ((Player) sender).getUniqueId() : UUID.randomUUID())) {
                                        messenger.sendMessage("You may not edit this zone.", sender);
                                        return;
                                    }
                                    api.removeZone(zone);
                                    api.scheduleSave();
                                    api.updateCache();
                                    messenger.sendMessage("A zone with the id '" + id + "' has been removed.", sender);
                                },
                                new ArgZone()
                        )
                )
                .arg(new String[]{"desc", "description"}, arg(desc("Set the description of a zone."),
                        (sender, input) -> {
                            String id = input[0].toString();
                            Zone zone = api.getZone(id);
                            if (zone == null) {
                                messenger.sendMessage("No zone with the id '" + id + "' can be found.", sender);
                                return;
                            }
                            if (!zone.canEdit(sender instanceof Player ? ((Player) sender).getUniqueId() : UUID.randomUUID())) {
                                messenger.sendMessage("You may not edit this zone.", sender);
                                return;
                            }
                            zone.setDescription(input[1] != null ? input[1].toString() : null);
                            api.scheduleSave();
                            api.updateCache();
                            messenger.sendMessage("This zone's description was updated.", sender);
                        },
                        new ArgZone(),
                        new ArgStringFinal().setRequired(false).setLabel("text"))
                )
                .arg("info",
                        sender -> {
                            if (!(sender instanceof Player)) return;
                            List<Zone> zones = api.getZones(((Player) sender).getLocation().getBlock().getLocation());
                            if (zones.isEmpty()) {
                                messenger.sendMessage("There are no zones at your location.", sender);
                            } else {
                                List<String> names = new ArrayList<>();
                                zones.forEach(zone -> names.add(zone.getKey().toString()));
                                messenger.sendMessage(new ComponentBuilder("Zones at your location:")
                                        .append(System.lineSeparator())
                                        .append(messenger.getBullets(new MagicList<>(names)
                                                .collect(string -> new ComponentBuilder(string)
                                                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/zone info " + string))
                                                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Click to view.")))
                                                        .create()).toArray(new BaseComponent[0][0])
                                        ))
                                        .create(), sender);
                            }
                        },
                        arg(
                                (sender, input) -> {
                                    if (input[0] == null) {
                                        if (!(sender instanceof Player)) return;
                                        List<Zone> zones = api.getZones(((Player) sender).getLocation().getBlock().getLocation());
                                        if (zones.isEmpty()) {
                                            messenger.sendMessage("There are no zones at your location.", sender);
                                        } else {
                                            List<String> names = new ArrayList<>();
                                            zones.forEach(zone -> names.add(zone.getKey().toString()));
                                            messenger.sendMessage(new ComponentBuilder("Zones at your location:")
                                                    .append(System.lineSeparator())
                                                    .append(messenger.getBullets(names.toArray(new String[0])))
                                                    .create(), sender);
                                        }
                                        return;
                                    }
                                    String id = input[0].toString();
                                    Zone zone = api.getZone(id);
                                    if (zone == null) {
                                        messenger.sendMessage("No zone with the id '" + id + "' can be found.", sender);
                                        return;
                                    }
                                    List<String> players = new ArrayList<>();
                                    for (UUID uuid : zone.getAllowedPlayers()) {
                                        players.add(Bukkit.getOfflinePlayer(uuid).getName());
                                    }
                                    List<String> flags = new ArrayList<>();
                                    for (String flag : zone.getFlags()) {
                                        flags.add(convertCase(flag));
                                    }
                                    BaseComponent[] desc = zone.getDescription() == null ? new ComponentBuilder("").create() : new ComponentBuilder(System.lineSeparator()).append(TextComponent.fromLegacyText(zone.getDescription(), ChatColor.GRAY)).create();
                                    messenger.sendMessage(new ComponentBuilder("Zone Info: ")
                                            .color(ChatColor.GRAY)
                                            .append(zone.getName())
                                            .color(ChatColor.RESET)
                                            .append(desc)
                                            .append(System.lineSeparator())
                                            .reset()
                                            .append(System.lineSeparator())
                                            .append("Allowed Players:")
                                            .color(ChatColor.WHITE)
                                            .append(System.lineSeparator())
                                            .append(players.isEmpty() ? "(None)" : String.join(", ", players))
                                            .color(Messenger.color("#29ffdb", '7'))
                                            .append(System.lineSeparator())
                                            .reset()
                                            .append(System.lineSeparator())
                                            .append("Active Flags:")
                                            .color(ChatColor.WHITE)
                                            .append(System.lineSeparator())
                                            .append(flags.isEmpty() ? "(None)" : String.join(", ", flags))
                                            .color(Messenger.color("#29ffdb", '7'))
                                            .create(), sender);
                                },
                                new ArgZone().setRequired(false)
                        )
                )
                .arg("list",
                        desc("List all zones, or all zones that a specific player can access."),
                        sender -> {
                            if (api.getZoneKeys().isEmpty()) {
                                messenger.sendMessage("No zones have been defined.", sender);
                            } else {
                                ComponentBuilder builder = new ComponentBuilder("Defined Zones:");
                                for (NamespacedKey key : api.getZoneKeys()) {
                                    builder
                                            .append(System.lineSeparator())
                                            .reset()
                                            .append(" - ")
                                            .color(Messenger.color("#f28900", '8'))
                                            .append(convertCase(key.getKey()))
                                            .color(Messenger.color("#29ffdb", '7'))
                                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/zone info " + key.toString()));
                                }
                                messenger.sendMessage(builder.create(), sender);
                            }
                        },
                        arg(
                                desc("List all zones, or all zones that a specific player can access."),
                                (sender, input) -> {
                                    List<Zone> zones = api.getZones();
                                    zones.removeIf(zone -> !zone.canEdit(((OfflinePlayer) input[0]).getUniqueId()));
                                    if (zones.isEmpty()) {
                                        messenger.sendMessage("No zones have been defined.", sender);
                                    } else {
                                        List<NamespacedKey> keys = new ArrayList<>();
                                        for (Zone zone : zones) {
                                            keys.add(zone.getKey());
                                        }
                                        ComponentBuilder builder = new ComponentBuilder("Zones Editable by " + ((OfflinePlayer) input[0]).getName() + ":");
                                        for (NamespacedKey key : keys) {
                                            builder
                                                    .append(System.lineSeparator())
                                                    .reset()
                                                    .append(" - ")
                                                    .color(Messenger.color("#f28900", '8'))
                                                    .append(convertCase(key.getKey()))
                                                    .color(Messenger.color("#29ffdb", '7'))
                                                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/zone info " + key.toString()));
                                        }
                                        messenger.sendMessage(builder.create(), sender);
                                    }
                                },
                                new ArgOfflinePlayer()
                        )
                )
                .arg("remove",
                        desc("Revoke a player's access to a zone."),
                        sender -> messenger.sendMessage("/zone remove <zone_id> <player/uuid>", sender),
                        arg(
                                desc("Revoke a player's access to a zone."),
                                (sender, input) -> {
                                    String id = (String) input[0];
                                    Zone zone = api.getZone(id);
                                    if (zone == null) {
                                        messenger.sendMessage("No zone with the id '" + id + "' can be found.", sender);
                                        return;
                                    }
                                    if (!zone.canEdit(sender instanceof Player ? ((Player) sender).getUniqueId() : UUID.randomUUID())) {
                                        messenger.sendMessage("You may not edit this zone.", sender);
                                        return;
                                    }
                                    OfflinePlayer player = (OfflinePlayer) input[1];
                                    if (!zone.isAllowed(player.getUniqueId())) {
                                        messenger.sendMessage("This player was not added.", sender);
                                    } else {
                                        zone.removePlayer(player.getUniqueId());
                                        messenger.sendMessage("Removed a player from this zone.", sender);
                                    }
                                    api.updateCache();
                                },
                                new ArgZone(),
                                new ArgPlayer()
                        )
                )
                .arg("show",
                        desc("Display the spatial bounds of a zone."),
                        sender -> {
                            if (!(sender instanceof Player)) return;
                            List<Zone> zones = api.getZones(((Player) sender).getLocation().getBlock().getLocation());
                            if (zones.isEmpty()) {
                                messenger.sendMessage("No zones can be found at your location.", sender);
                                return;
                            }
                            zones.forEach(zone -> {
                                if (zone instanceof CuboidalZone)
                                    ((CuboidalZone) zone).showBounds();
                                else if (zone instanceof PolyhedralZone)
                                    ((PolyhedralZone) zone).showBounds();
                            });
                        },
                        arg(
                                desc("Display the spatial bounds of a zone."),
                                (sender, input) -> {
                                    String id = input[0].toString();
                                    Zone zone = api.getZone(id);
                                    if (zone == null) {
                                        messenger.sendMessage("No zone with the id '" + id + "' can be found.", sender);
                                        return;
                                    }
                                    if (zone instanceof CuboidalZone)
                                        ((CuboidalZone) zone).showBounds();
                                    else if (zone instanceof PolyhedralZone)
                                        ((PolyhedralZone) zone).showBounds();
                                },
                                new ArgZone()
                        )
                )
                .arg("teleport",
                        arg(
                                desc("Teleport to a zone."),
                                (sender, input) -> {
                                    if (!(sender instanceof Player)) return;
                                    if (!sender.hasPermission("guardian.command.teleport")) {
                                        messenger.sendMessage("You do not have permission for this action.", sender);
                                        return;
                                    }
                                    Player player = (Player) sender;
                                    String id = input[0].toString();
                                    Zone zone = api.getZone(id);
                                    if (zone == null) {
                                        messenger.sendMessage("No zone with the id '" + id + "' can be found.", sender);
                                        return;
                                    }
                                    player.teleport(zone.getLocation());
                                    messenger.sendMessage("Teleporting to '" + zone.getName() + "'.", sender);
                                },
                                new ArgZone()
                        )
                )
                .arg("toggle",
                        desc("Enable/disable certain actions within a zone.\nView all flags? /guardian flags"),
                        sender -> messenger.sendMessage("/zone toggle <zone_id> <flag>", sender),
                        arg(
                                desc("Enable/disable certain actions within a zone.\nView all flags? /guardian flags"),
                                (sender, input) -> {
                                    String id = input[0].toString();
                                    String flag = input[1].toString();
                                    Zone zone = api.getZone(id);
                                    if (zone == null) {
                                        messenger.sendMessage("No zone with the id '" + id + "' can be found.", sender);
                                        return;
                                    }
                                    if (!api.isProtectionFlag(flag)) {
                                        messenger.sendMessage("No flag with the id '" + id + "' is registered.", sender);
                                        return;
                                    }
                                    if (!zone.canEdit(sender instanceof Player ? ((Player) sender).getUniqueId() : UUID.randomUUID())) {
                                        messenger.sendMessage("You may not edit this zone.", sender);
                                        return;
                                    }
                                    if (!sender.hasPermission("guardian.flag." + id)) {
                                        messenger.sendMessage("You do not have permission to toggle this flag.", sender);
                                        return;
                                    }
                                    if (zone.hasFlag(flag)) {
                                        zone.removeFlag(flag);
                                        messenger.sendMessage("Deactivated this flag for the specified zone.", sender);
                                    } else {
                                        zone.addFlag(flag);
                                        messenger.sendMessage(new ComponentBuilder("Activated this flag for the specified zone. ")
                                                .append("(Flags are blocking - active flags override default behaviour.)")
                                                .color(ChatColor.GRAY)
                                                .create(), sender);
                                    }
                                    api.updateCache();
                                },
                                new ArgZone(),
                                new ArgFlag()
                        )
                );
    }

    @Override
    public @NotNull CommandSingleAction<CommandSender> getDefault() {
        return sender -> messenger.sendMessage(api.getCommandHelpMessage(this), sender);
    }

    private String convertCase(String string) {
        String[] words = string.split("_");
        List<String> list = new ArrayList<>();
        for (String word : words) {
            list.add((word.isEmpty())
                    ? word
                    : Ascii.toUpperCase(word.charAt(0)) + Ascii.toLowerCase(word.substring(1)));
        }
        return String.join(" ", list);
    }


    @Override
    public @NotNull List<String> getAliases() {
        return Arrays.asList("gz", "gzone", "guardianzone", "region");
    }

    @Override
    public @NotNull String getUsage() {
        return "/zone help";
    }

    @Override
    public @NotNull String getDescription() {
        return "Used for managing protection zones.";
    }

    @Override
    public @Nullable String getPermission() {
        return "guardian.command.zone";
    }

    @Override
    public @Nullable String getPermissionMessage() {
        return null;
    }

    @Override
    public @Nullable List<String> getCompletions(int i) {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return execute(sender, args);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return api.getTabCompletions(this, args);
    }

}
