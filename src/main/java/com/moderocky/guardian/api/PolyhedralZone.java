package com.moderocky.guardian.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.logic.handler.LogicUtils;
import com.moderocky.guardian.logic.handler.PolyProcessor;
import com.moderocky.guardian.logic.shape.Polyhedron;
import com.moderocky.guardian.logic.shape.Vertex;
import com.moderocky.guardian.util.ParticleUtils;
import com.moderocky.mask.api.MagicList;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@SuppressWarnings("unused")
public class PolyhedralZone extends Zone {

    private GuardianAPI api;

    private Polyhedron polyhedron;
    private BoundingBox boundingBox;
    private Location location;
    private UUID owner;

    private String name = null;
    private String description = null;

    public PolyhedralZone(@NotNull NamespacedKey id) {
        super(id);
        throw new IllegalArgumentException("This constructor may not be used!");
    }

    public PolyhedralZone(@NotNull String id, @NotNull Location... locations) {
        super(Guardian.getNamespacedKey(id));
        this.api = Guardian.getApi();
        this.polyhedron = new Polyhedron(LogicUtils.toVertices(Arrays.asList(locations)));
        this.boundingBox = polyhedron.getBoundingBox();
        this.location = boundingBox.getCenter().toLocation(locations[0].getWorld());
    }

    public PolyhedralZone(@NotNull NamespacedKey id, @NotNull JsonObject section) {
        super(id, section);
    }

    public static PolyhedralZone createZone(Player player, String id, Location... locations) {
        PolyhedralZone zone = new PolyhedralZone(id, locations);
        zone.setOwner(player.getUniqueId());
        return zone;
    }

    public static PolyhedralZone createZone(String id, Location... locations) {
        PolyhedralZone zone = new PolyhedralZone(id, locations);
        zone.setOwner(null);
        return zone;
    }

    @Override
    public @NotNull BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public Polyhedron getPolyhedron() {
        return polyhedron;
    }

    private PolyProcessor getPolyProcessor() {
        return new PolyProcessor(polyhedron);
    }

    @Override
    public <Z extends Zone> boolean overlaps(Z zone) {
        if (zone instanceof PolyhedralZone) return overlaps((PolyhedralZone) zone);
        if (zone instanceof CuboidalZone) return overlaps((CuboidalZone) zone);
        else return super.overlaps(zone);
    }

    public boolean overlaps(PolyhedralZone zone) {
        PolyProcessor processor = getPolyProcessor();
        for (Vertex vertex : zone.getPolyhedron().getVertices()) {
            if (processor.isInside(vertex)) return true;
        }
        return false;
    }

    public boolean overlaps(CuboidalZone zone) {
        PolyProcessor processor = getPolyProcessor();
        for (Vertex vertex : LogicUtils.getVertices(zone.getBoundingBox())) {
            if (processor.isInside(vertex)) return true;
        }
        return false;
    }

    public boolean eclipses(PolyhedralZone zone) {
        PolyProcessor processor = getPolyProcessor();
        for (Vertex vertex : zone.getPolyhedron().getVertices()) {
            if (!processor.isInside(vertex)) return false;
        }
        return true;
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
        PolyProcessor processor = getPolyProcessor();
        return location.getWorld() == getWorld() && processor.isInside(Vertex.from(location));
    }

    @Override
    public boolean canEdit(@NotNull UUID player) {
        return player.equals(getOwner()) || Bukkit.getOfflinePlayer(player).isOp();
    }

    @Override
    public @NotNull List<Block> getBlocks() {
        List<Block> blocks = new ArrayList<>(LogicUtils.getBlocks(getBoundingBox(), getWorld()));
        blocks.removeIf(block -> !isInside(block.getLocation().add(0.5, 0.5, 0.5)));
        return blocks;
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
        ParticleUtils.drawHash(Particle.END_ROD, 0.25, polyhedron.getLocations(getWorld()).toArray(new Location[0]));
    }

    @Override
    public void save(final @NotNull JsonObject object) {
        api = Guardian.getApi();
        World world = getWorld();
        object.addProperty("location", api.serialisePosition(getLocation()));
        MagicList<String> strings = new MagicList<>();
        polyhedron.getLocations(world).forEach(location -> strings.add(api.serialisePosition(location)));
        object.add("vertices", strings.toJsonStringArray());
        object.add("flags", new MagicList<>(getFlags()).toJsonStringArray());
        MagicList<String> players = new MagicList<>(getAllowedPlayers()).collect((Function<UUID, String>) UUID::toString);
        object.add("players", players.toJsonStringArray());
        object.addProperty("owner", getOwner() != null ? getOwner().toString() : null);
        object.addProperty("name", name);
        object.add("desc", description == null ? null : new MagicList<>(description.split("\n")).toJsonStringArray());
    }

    @Override
    public void load(final @NotNull JsonObject object) {
        api = Guardian.getApi();
        this.location = api.deserialisePosition(object.get("location").getAsString());
        MagicList<Vertex> vertices = MagicList
                .from(object.getAsJsonArray("vertices"), JsonElement::getAsString)
                .collect((Function<String, Location>) string -> api.deserialisePosition(string))
                .collect((Function<Location, Vertex>) Vertex::from);
        polyhedron = new Polyhedron(vertices);
        this.boundingBox = polyhedron.getBoundingBox();
        for (String string : MagicList.from(object.getAsJsonArray("flags"), JsonElement::getAsString)) {
            addFlag(string);
        }
        for (String string : MagicList.from(object.getAsJsonArray("players"), JsonElement::getAsString)) {
            addPlayer(UUID.fromString(string));
        }
        if (object.has("owner") && object.get("owner").isJsonPrimitive()) {
            String string = object.get("owner").getAsString();
            setOwner(UUID.fromString(string));
        } else setOwner(null);
        name = object.get("name") != null && !object.get("name").isJsonNull() ? object.get("name").getAsString() : null;
        if (!object.has("description") || object.get("description").isJsonNull()) description = null;
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
