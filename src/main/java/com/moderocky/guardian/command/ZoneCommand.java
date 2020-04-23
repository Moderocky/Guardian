package com.moderocky.guardian.command;

import com.google.common.base.Ascii;
import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.api.CuboidalZone;
import com.moderocky.guardian.api.GuardianAPI;
import com.moderocky.guardian.api.Zone;
import com.moderocky.guardian.config.GuardianConfig;
import com.moderocky.guardian.util.Messenger;
import com.moderocky.mask.template.WrappedCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ZoneCommand implements WrappedCommand {

    private final @NotNull GuardianConfig config = Guardian.getInstance().getGuardianConfig();
    private final @NotNull Messenger messenger = Guardian.getMessenger();
    private final @NotNull GuardianAPI api = Guardian.getApi();

    @Override
    public @NotNull List<String> getAliases() {
        return Arrays.asList("gz", "gzone", "guardianzone", "region");
    }

    @Override
    public @NotNull String getUsage() {
        return "/zone <create/delete> [id]";
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
    public @NotNull String getCommand() {
        return "zone";
    }

    @Override
    public @Nullable List<String> getCompletions(int i) {
        if (i == 1)
            return Arrays.asList("list", "create", "delete", "info", "teleport", "show", "toggle", "add", "remove");
        return null;
    }

    @Override
    public @Nullable List<String> getCompletions(int i, @NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (i == 1) {
            return Arrays.asList("list", "create", "delete", "info", "teleport", "show", "toggle", "add", "remove");
        } else if (i == 2 && (args[0].equalsIgnoreCase("delete") ||
                args[0].equalsIgnoreCase("toggle") ||
                args[0].equalsIgnoreCase("info") ||
                args[0].equalsIgnoreCase("teleport") ||
                args[0].equalsIgnoreCase("show") ||
                args[0].equalsIgnoreCase("add") ||
                args[0].equalsIgnoreCase("remove"))) {
            List<String> list = new ArrayList<>();
            api.getZoneKeys().forEach(key -> list.add(key.getKey()));
            return list;
        } else if (i == 3 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))) {
            List<String> list = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(player -> list.add(player.getName()));
            return list;
        } else if (i == 3 && (args[0].equalsIgnoreCase("toggle"))) {
            List<String> list = new ArrayList<>(api.getFlags());
            list.removeIf(string -> !sender.hasPermission("guardian.flag." + string));
            return list;
        }
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("create")) {
                if (!(sender instanceof Player)) return false;
                Player player = (Player) sender;
                if (args.length < 2) {
                    messenger.sendMessage("/zone create <zone_id>", sender);
                    return true;
                }
                Location l1 = api.getWandPosition(player, 1);
                Location l2 = api.getWandPosition(player, 2);
                if (l1 == null || l2 == null) {
                    messenger.sendMessage("Please set zone corners using a zone wand first.", player);
                    return true;
                }
                if (l1.getWorld() != l2.getWorld() || l1.distance(l2) > config.maxZoneDiameter) {
                    messenger.sendMessage("The selected area is larger than " + config.maxZoneDiameter + "×" + config.maxZoneDiameter + " blocks in diameter.", player);
                    return true;
                }
                String id = args[1];
                if (api.getZone(id) != null) {
                    messenger.sendMessage("A zone with the id '" + id + "' already exists.", sender);
                    return true;
                }
                CuboidalZone zone = CuboidalZone.createZone(player, id, l1, l2);
                if (!player.isOp() && !api.canCreateZone(zone, player)) {
                    messenger.sendMessage("This zone conflicts with others that you are unable to edit or override.", player);
                    return true;
                }
                api.registerZone(zone);
                api.scheduleSave();
                api.updateCache();
                messenger.sendMessage("A zone with the id '" + id + "' has been created.", sender);
            } else if (args[0].equalsIgnoreCase("delete")) {
                if (args.length < 2) {
                    messenger.sendMessage("/zone delete <zone_id>", sender);
                    return true;
                }
                String id = args[1];
                Zone zone = api.getZone(id);
                if (zone == null) {
                    messenger.sendMessage("No zone with the id '" + id + "' can be found.", sender);
                    return true;
                }
                api.removeZone(zone);
                api.scheduleSave();
                api.updateCache();
                messenger.sendMessage("A zone with the id '" + id + "' has been removed.", sender);
            } else if (args[0].equalsIgnoreCase("teleport") || args[0].equalsIgnoreCase("tp")) {
                if (!(sender instanceof Player)) return false;
                Player player = (Player) sender;
                if (args.length < 2) {
                    messenger.sendMessage("/zone teleport <zone_id>", sender);
                    return true;
                }
                String id = args[1];
                Zone zone = api.getZone(id);
                if (zone == null) {
                    messenger.sendMessage("No zone with the id '" + id + "' can be found.", sender);
                    return true;
                }
                player.teleport(zone.getLocation());
                messenger.sendMessage("Teleporting to '" + zone.getName() + "'.", sender);
            } else if (args[0].equalsIgnoreCase("toggle")) {
                if (args.length < 3) {
                    messenger.sendMessage("/zone toggle <zone_id> <flag>", sender);
                    return true;
                }
                String id = args[1];
                String flag = args[2];
                Zone zone = api.getZone(id);
                if (zone == null) {
                    messenger.sendMessage("No zone with the id '" + id + "' can be found.", sender);
                    return true;
                }
                if (!api.isFlag(flag)) {
                    messenger.sendMessage("No flag with the id '" + id + "' is registered.", sender);
                    return true;
                }
                if (!zone.canEdit(sender instanceof Player ? ((Player) sender).getUniqueId() : UUID.randomUUID())) {
                    messenger.sendMessage("You may not edit this zone.", sender);
                    return true;
                }
                if (!sender.hasPermission("guardian.flag." + id)) {
                    messenger.sendMessage("You do not have permission to toggle this flag.", sender);
                    return true;
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
            } else if (args[0].equalsIgnoreCase("info")) {
                if (args.length < 2) {
                    messenger.sendMessage("/zone info <zone_id>", sender);
                    return true;
                }
                String id = args[1];
                Zone zone = api.getZone(id);
                if (zone == null) {
                    messenger.sendMessage("No zone with the id '" + id + "' can be found.", sender);
                    return true;
                }
                List<String> players = new ArrayList<>();
                for (UUID uuid : zone.getAllowedPlayers()) {
                    players.add(Bukkit.getOfflinePlayer(uuid).getName());
                }
                List<String> flags = new ArrayList<>();
                for (String flag : zone.getFlags()) {
                    flags.add(convertCase(flag));
                }
                BaseComponent[] desc = zone.getDescription() == null ? new ComponentBuilder("").create() : new ComponentBuilder(System.lineSeparator()).append(TextComponent.fromLegacyText(getDescription(), ChatColor.GRAY)).create();
                messenger.sendMessage(new ComponentBuilder("Zone Info: ")
                        .color(ChatColor.GRAY)
                        .append(zone.getName())
                        .color(ChatColor.LIGHT_PURPLE)
                        .append(desc)
                        .append(System.lineSeparator())
                        .reset()
                        .append(System.lineSeparator())
                        .append("Allowed Players:")
                        .color(ChatColor.WHITE)
                        .append(System.lineSeparator())
                        .append(String.join(", ", players))
                        .color(ChatColor.GRAY)
                        .append(System.lineSeparator())
                        .reset()
                        .append(System.lineSeparator())
                        .append("Active Flags:")
                        .color(ChatColor.WHITE)
                        .append(System.lineSeparator())
                        .append(String.join(", ", flags))
                        .color(ChatColor.GRAY)
                        .create(), sender);
            } else if (args[0].equalsIgnoreCase("show")) {
                if (args.length < 2) {
                    messenger.sendMessage("/zone show <zone_id>", sender);
                    return true;
                }
                String id = args[1];
                Zone zone = api.getZone(id);
                if (zone == null) {
                    messenger.sendMessage("No zone with the id '" + id + "' can be found.", sender);
                    return true;
                }
                if (zone instanceof CuboidalZone)
                    ((CuboidalZone) zone).showBounds();
            } else if (args[0].equalsIgnoreCase("list")) {
                if (api.getZoneKeys().isEmpty()) {
                    messenger.sendMessage("No zones have been defined.", sender);
                    return true;
                }
                ComponentBuilder builder = new ComponentBuilder("Defined Zones:");
                for (NamespacedKey key : api.getZoneKeys()) {
                    builder
                            .append(System.lineSeparator())
                            .reset()
                            .append(" - ")
                            .color(ChatColor.DARK_GRAY)
                            .append(convertCase(key.getKey()))
                            .color(ChatColor.GRAY)
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/zone info " + key.toString()));
                }
                messenger.sendMessage(builder.create(), sender);
            } else if (args[0].equalsIgnoreCase("add")) {
                if (args.length < 3) {
                    messenger.sendMessage("/zone add <zone_id> <player>", sender);
                    return true;
                }
                String id = args[1];
                String playerId = args[2];
                Zone zone = api.getZone(id);
                if (zone == null) {
                    messenger.sendMessage("No zone with the id '" + id + "' can be found.", sender);
                    return true;
                }
                if (!zone.canEdit(sender instanceof Player ? ((Player) sender).getUniqueId() : UUID.randomUUID())) {
                    messenger.sendMessage("You may not edit this zone.", sender);
                    return true;
                }
                OfflinePlayer player = Bukkit.getPlayer(playerId);
                if (player == null) {
                    try {
                        player = Bukkit.getOfflinePlayer(UUID.fromString(playerId));
                    } catch (Exception e) {
                        messenger.sendMessage("No player by the name/uuid '" + playerId + "' can be found. Please use UUIDs for offline players.", sender);
                        return true;
                    }
                }
                if (zone.isAllowed(player.getUniqueId())) {
                    messenger.sendMessage("This player was already added.", sender);
                } else {
                    zone.addPlayer(player.getUniqueId());
                    messenger.sendMessage("Added a new player to this zone.", sender);
                }
                api.updateCache();
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (args.length < 3) {
                    messenger.sendMessage("/zone remove <zone_id> <player>", sender);
                    return true;
                }
                String id = args[1];
                String playerId = args[2];
                Zone zone = api.getZone(id);
                if (zone == null) {
                    messenger.sendMessage("No zone with the id '" + id + "' can be found.", sender);
                    return true;
                }
                if (!zone.canEdit(sender instanceof Player ? ((Player) sender).getUniqueId() : UUID.randomUUID())) {
                    messenger.sendMessage("You may not edit this zone.", sender);
                    return true;
                }
                OfflinePlayer player = Bukkit.getPlayer(playerId);
                if (player == null) {
                    try {
                        player = Bukkit.getOfflinePlayer(UUID.fromString(playerId));
                    } catch (Exception e) {
                        messenger.sendMessage("No player by the name/uuid '" + playerId + "' can be found. Please use UUIDs for offline players.", sender);
                        return true;
                    }
                }
                if (!zone.isAllowed(player.getUniqueId())) {
                    messenger.sendMessage("This player was not added.", sender);
                } else {
                    zone.removePlayer(player.getUniqueId());
                    messenger.sendMessage("Removed a player from this zone.", sender);
                }
                api.updateCache();
            }
        } else {
            messenger.sendMessage(new ComponentBuilder("Zones Help:")
                    .append(messenger.getBullets(
                            "/zone list",
                            "/zone info <zone_id>",
                            "/zone show <zone_id>",
                            "/zone create <zone_id>",
                            "/zone delete <zone_id>",
                            "/zone toggle <zone_id> <flag>",
                            "/zone add <zone_id> <player/uuid>",
                            "/zone remove <zone_id> <player/uuid>"
                    ))
                    .create(), sender);
        }
        return true;
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

}
