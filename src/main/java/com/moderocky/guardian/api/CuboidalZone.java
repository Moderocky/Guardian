package com.moderocky.guardian.api;

import com.moderocky.guardian.Guardian;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CuboidalZone extends Zone {

    private GuardianAPI api;

    private BoundingBox boundingBox;
    private Location location;
    private UUID owner;

    public CuboidalZone(@NotNull NamespacedKey id) {
        super(id);
        throw new IllegalArgumentException("This constructor may not be used!");
    }

    public CuboidalZone(@NotNull String id, @NotNull BoundingBox boundingBox, @NotNull World world) {
        super(Guardian.getNamespacedKey(id));
        this.boundingBox = boundingBox;
        this.location = boundingBox.getCenter().toLocation(world);
        api = Guardian.getApi();
    }

    public CuboidalZone(@NotNull NamespacedKey id, @NotNull ConfigurationSection section) {
        super(id, section);
    }

    public static CuboidalZone createZone(Player player, String id, Location l1, Location l2) {
        BoundingBox boundingBox = BoundingBox.of(l1, l2);
        CuboidalZone zone = new CuboidalZone(id, boundingBox, l1.getWorld());
        zone.setOwner(player.getUniqueId());
        return zone;
    }

    public static CuboidalZone createZone(String id, Location l1, Location l2) {
        BoundingBox boundingBox = BoundingBox.of(l1, l2);
        CuboidalZone zone = new CuboidalZone(id, boundingBox, l1.getWorld());
        zone.setOwner(null);
        return zone;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    @Override
    public <Z extends Zone> boolean overlaps(Z zone) {
        if (zone instanceof CuboidalZone) return overlaps((CuboidalZone) zone);
        else return super.overlaps(zone);
    }

    public boolean overlaps(CuboidalZone cuboidalZone) {
        return cuboidalZone.getBoundingBox().overlaps(boundingBox);
    }

    public boolean eclipses(CuboidalZone cuboidalZone) {
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
        section.set("location", api.serialisePosition(getLocation()));
        section.set("min", api.serialisePosition(l1));
        section.set("max", api.serialisePosition(l2));
        section.set("flags", getFlags());
        List<String> players = new ArrayList<>();
        getAllowedPlayers().forEach(uuid -> players.add(uuid.toString()));
        section.set("players", players);
        section.set("owner", getOwner() != null ? getOwner().toString() : null);
    }

    @Override
    public void load(@NotNull ConfigurationSection section) {
        api = Guardian.getApi();
        this.location = api.deserialisePosition(section.getString("location"));
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
    }

}
