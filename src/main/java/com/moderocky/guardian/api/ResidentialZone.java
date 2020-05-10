package com.moderocky.guardian.api;

import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.logic.handler.LogicUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ResidentialZone extends Zone {

    private GuardianAPI api;

    private BoundingBox boundingBox;
    private Location location;
    private UUID owner;

    private Location homePos;

    private String name = null;
    private String description = null;

    public ResidentialZone(@NotNull NamespacedKey id) {
        super(id);
        throw new IllegalArgumentException("This constructor may not be used!");
    }

    public ResidentialZone(@NotNull String id, @NotNull BoundingBox boundingBox, @NotNull World world) {
        super(Guardian.getNamespacedKey(id));
        this.boundingBox = boundingBox;
        this.location = boundingBox.getCenter().toLocation(world);
        this.homePos = location;
        api = Guardian.getApi();
    }

    public ResidentialZone(@NotNull NamespacedKey id, @NotNull ConfigurationSection section) {
        super(id, section);
    }

    public static ResidentialZone createZone(Player player, String id, Location l1, Location l2) {
        BoundingBox boundingBox = BoundingBox.of(l1, l2);
        ResidentialZone zone = new ResidentialZone(id, boundingBox, l1.getWorld());
        zone.setOwner(player.getUniqueId());
        return zone;
    }

    public static ResidentialZone createZone(String id, Location l1, Location l2) {
        BoundingBox boundingBox = BoundingBox.of(l1, l2);
        ResidentialZone zone = new ResidentialZone(id, boundingBox, l1.getWorld());
        zone.setOwner(null);
        return zone;
    }

    @Override
    public @NotNull BoundingBox getBoundingBox() {
        return boundingBox;
    }

    @Override
    public <Z extends Zone> boolean overlaps(Z zone) {
        if (zone instanceof ResidentialZone) return overlaps((ResidentialZone) zone);
        else return super.overlaps(zone);
    }

    public boolean overlaps(ResidentialZone cuboidalZone) {
        return cuboidalZone.getBoundingBox().overlaps(boundingBox);
    }

    public boolean eclipses(ResidentialZone cuboidalZone) {
        return overlaps(cuboidalZone) && boundingBox.contains(cuboidalZone.getBoundingBox());
    }

    public @Nullable UUID getOwner() {
        return owner;
    }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
        if (owner != null && !isAllowed(owner)) addPlayer(owner);
    }

    @Override
    public int getWeight() {
        return 1;
    }

    @Override
    public double getRadius() {
        return (boundingBox.getMax().distance(boundingBox.getMin()) / 2) + 2;
    }

    @Override
    public boolean isInside(@NotNull Location location) {
        return location.getWorld() == getWorld() && boundingBox.clone().expand(0.1, 0.1, 0.1).contains(location.toVector());
    }

    @Override
    public boolean canEdit(@NotNull UUID player) {
        return player.equals(getOwner()) || Bukkit.getOfflinePlayer(player).isOp();
    }

    @Override
    public @NotNull List<Block> getBlocks() {
        return LogicUtils.getBlocks(getBoundingBox(), getWorld());
    }

    @Override
    public @NotNull World getWorld() {
        return location.getWorld();
    }

    @Override
    public @NotNull Location getLocation() {
        return location;
    }

    public void showBounds() {
        api.displayBox(getBoundingBox().clone().expand(0.05, 0.05, 0.05), getWorld(), null);
    }

    @Override
    public void save(@NotNull ConfigurationSection section) {
        api = Guardian.getApi();
        World world = getWorld();
        Location l1 = boundingBox.getMin().toLocation(world);
        Location l2 = boundingBox.getMax().toLocation(world);
        section.set("location", api.serialisePosition(location));
        section.set("home_pos", api.serialisePosition(homePos));
        section.set("min", api.serialisePosition(l1));
        section.set("max", api.serialisePosition(l2));
        section.set("flags", getFlags());
        List<String> players = new ArrayList<>();
        getAllowedPlayers().forEach(uuid -> players.add(uuid.toString()));
        section.set("players", players);
        section.set("owner", getOwner() != null ? getOwner().toString() : null);
        section.set("name", name);
        section.set("desc", description == null ? null : Arrays.asList(description.split("\n")));
    }

    @Override
    public void load(@NotNull ConfigurationSection section) {
        api = Guardian.getApi();
        this.location = api.deserialisePosition(section.getString("location"));
        this.homePos = api.deserialisePosition(section.getString("home_pos"));
        Location l1 = api.deserialisePosition(section.getString("min"));
        Location l2 = api.deserialisePosition(section.getString("max"));
        this.boundingBox = BoundingBox.of(l1, l2);
        List<String> list = section.getStringList("flags");
        for (String string : list) {
            addFlag(string);
        }
        List<String> plist = section.getStringList("players");
        for (String string : plist) {
            addPlayer(UUID.fromString(string));
        }
        String string = section.getString("owner");
        if (string != null) setOwner(UUID.fromString(string));
        else setOwner(null);
        name = section.getString("name");
        List<String> desc = section.getStringList("desc");
        if (desc.isEmpty()) description = null;
        else description = String.join(System.lineSeparator(), desc);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String s) {
        this.description = s;
    }

    @Override
    public String getName() {
        return name == null ? super.getName() : name;
    }

    @Override
    public void setName(String s) {
        this.name = s;
    }

}
