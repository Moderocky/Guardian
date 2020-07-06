package com.moderocky.guardian.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.logic.handler.LogicUtils;
import com.moderocky.guardian.logic.shape.Vertex;
import com.moderocky.mask.api.MagicList;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public class CuboidalZone extends Zone implements Parent<CuboidalChild> {

    private final @NotNull List<CuboidalChild> children = new ArrayList<>();
    private GuardianAPI api;
    private BoundingBox boundingBox;
    private Location location;
    private UUID owner;
    private String name = null;
    private String description = null;

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

    public CuboidalZone(@NotNull NamespacedKey id, @NotNull JsonObject object) {
        super(id, object);
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

    @Override
    public @NotNull BoundingBox getBoundingBox() {
        return BoundingBox.of(boundingBox.getMin(), boundingBox.getMax().add(new Vector(1, 1, 1)));
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
        BoundingBox box = BoundingBox.of(boundingBox.getMin(), boundingBox.getMax().add(new Vector(1, 1, 1)));
        return location.getWorld() == getWorld() && box.expand(0.1, 0.1, 0.1).contains(location.toVector());
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
        if (hasChildren()) {
            for (CuboidalChild child : getChildren()) {
                api.displayBox(child.getBoundingBox().clone().expand(0.05, 0.05, 0.05), getWorld(), Particle.VILLAGER_HAPPY, null);
            }
        }
    }

    @Override
    public void save(final @NotNull JsonObject object) {
        api = Guardian.getApi();
        World world = getWorld();
        Location l1 = boundingBox.getMin().toLocation(world);
        Location l2 = boundingBox.getMax().toLocation(world);
        object.addProperty("location", api.serialisePosition(getLocation()));
        object.addProperty("min", api.serialisePosition(l1));
        object.addProperty("max", api.serialisePosition(l2));
        JsonArray flags = new JsonArray();
        for (String flag : getFlags()) {
            flags.add(flag);
        }
        object.add("flags", flags);
        JsonArray players = new JsonArray();
        getAllowedPlayers().forEach(uuid -> players.add(uuid.toString()));
        object.add("players", players);
        object.addProperty("owner", getOwner() != null ? getOwner().toString() : null);
        object.addProperty("name", name);
        object.addProperty("description", description);
        saveChildren(object);
    }

    @Override
    public void load(final @NotNull JsonObject object) {
        api = Guardian.getApi();
        this.location = api.deserialisePosition(object.get("location").getAsString());
        Location l1 = api.deserialisePosition(object.get("min").getAsString());
        Location l2 = api.deserialisePosition(object.get("max").getAsString());
        this.boundingBox = BoundingBox.of(l1, l2);
        List<String> list = new MagicList<>();
        for (JsonElement element : object.getAsJsonArray("flags")) {
            addFlag(element.getAsString());
        }
        for (JsonElement element : object.getAsJsonArray("players")) {
            addPlayer(UUID.fromString(element.getAsString()));
        }
        if (object.has("owner") && object.get("owner").isJsonPrimitive()) {
            String string = object.get("owner").getAsString();
            setOwner(UUID.fromString(string));
        } else setOwner(null);
        name = object.get("name") != null && !object.get("name").isJsonNull() ? object.get("name").getAsString() : null;
        if (!object.has("description") || object.get("description").isJsonNull()) description = null;
        else description = object.get("description").getAsString();
        loadChildren(object);
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
        return !children.isEmpty();
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
