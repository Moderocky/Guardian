package com.moderocky.guardian.api;

import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.logic.shape.Vertex;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CuboidalZone extends Zone implements Parent<CuboidalChild> {

    private GuardianAPI api;

    private BoundingBox boundingBox;
    private Location location;
    private UUID owner;

    private String name = null;
    private String description = null;

    private final @NotNull List<CuboidalChild> children = new ArrayList<>();

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
        if (zone instanceof PolyhedralZone) return overlaps((PolyhedralZone) zone);
        else return super.overlaps(zone);
    }
    public boolean overlaps(PolyhedralZone zone) {
        for (Vertex vertex : zone.getPolyhedron().getVertices()) {
            if (getBoundingBox().contains(vertex.toVector())) return true;
        }
        return false;
    }

    public boolean overlaps(CuboidalZone zone) {
        return zone.getBoundingBox().overlaps(boundingBox);
    }

    public boolean eclipses(CuboidalZone zone) {
        return overlaps(zone) && boundingBox.contains(zone.getBoundingBox());
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
        if (hasChildren()) {
            for (CuboidalChild child : getChildren()) {
                api.displayBox(child.getBoundingBox().clone().expand(0.05, 0.05, 0.05), getWorld(), Particle.VILLAGER_HAPPY, null);
            }
        }
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
        section.set("name", name);
        section.set("desc", description == null ? null : Arrays.asList(description.split("\n")));
        saveChildren(section);
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
        name = section.getString("name");
        List<String> desc = section.getStringList("desc");
        if (desc.isEmpty()) description = null;
        else description = String.join(System.lineSeparator(), desc);
        loadChildren(section);
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
    public @NotNull List<CuboidalChild> getChildren() {
        return children;
    }

    @Override
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    @Override
    public void addChild(@NotNull CuboidalChild child) {
        if (!children.contains(child)) children.add(child);
    }

    @Override
    public void removeChild(@NotNull CuboidalChild child) {
        children.remove(child);
    }

    @Override
    public void clearChildren() {
        children.clear();
    }
}
