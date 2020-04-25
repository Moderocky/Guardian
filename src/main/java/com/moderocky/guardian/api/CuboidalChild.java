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

import java.util.UUID;

public class CuboidalChild extends CuboidalZone implements Child<CuboidalZone> {

    private GuardianAPI api;

    private BoundingBox boundingBox;
    private Location location;
    private UUID owner;

    private CuboidalZone parent;

    private String name = null;
    private String description = null;

    public CuboidalChild(@NotNull NamespacedKey id) {
        super(id);
        throw new IllegalArgumentException("This constructor may not be used!");
    }

    public CuboidalChild(@NotNull String id, @NotNull BoundingBox boundingBox, @NotNull World world) {
        super(Guardian.getNamespacedKey(id));
        throw new IllegalArgumentException("This constructor may not be used!");
    }

    public CuboidalChild(@NotNull String id, @NotNull BoundingBox boundingBox, @NotNull CuboidalZone parent) {
        super(Guardian.getNamespacedKey(id));
        this.boundingBox = boundingBox;
        this.location = boundingBox.getCenter().toLocation(parent.getWorld());
        api = Guardian.getApi();
    }

    public CuboidalChild(@NotNull NamespacedKey id, @NotNull ConfigurationSection section) {
        super(id, section);
    }

    public static CuboidalChild createZone(Player player, String id, CuboidalZone parent, Location l1, Location l2) {
        BoundingBox boundingBox = BoundingBox.of(l1, l2);
        CuboidalChild zone = new CuboidalChild(id, boundingBox, parent);
        zone.setOwner(player.getUniqueId());
        return zone;
    }

    public static CuboidalChild createZone(String id, CuboidalZone parent, Location l1, Location l2) {
        BoundingBox boundingBox = BoundingBox.of(l1, l2);
        CuboidalChild zone = new CuboidalChild(id, boundingBox, parent);
        zone.setOwner(null);
        return zone;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    @Override
    public <Z extends Zone> boolean overlaps(Z zone) {
        if (zone instanceof CuboidalChild) return overlaps((CuboidalChild) zone);
        else return super.overlaps(zone);
    }

    public boolean overlaps(CuboidalChild cuboidalZone) {
        return cuboidalZone.getBoundingBox().overlaps(boundingBox);
    }

    public boolean eclipses(CuboidalChild cuboidalZone) {
        return overlaps(cuboidalZone) && boundingBox.contains(cuboidalZone.getBoundingBox());
    }

    public @Nullable UUID getOwner() {
        return parent.getOwner();
    }

    public void setOwner(@Nullable UUID owner) {
        parent.setOwner(owner);
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
        super.save(section);
    }

    @Override
    public void load(@NotNull ConfigurationSection section) {
        super.load(section);
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

    @Override
    public void setParent(@NotNull CuboidalZone parent) {
        this.parent = parent;
    }

    @Override
    public CuboidalZone getParent() {
        return parent;
    }
}
