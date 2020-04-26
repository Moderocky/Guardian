package com.moderocky.guardian.api;

import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.logic.handler.LogicUtils;
import com.moderocky.guardian.logic.handler.PolyProcessor;
import com.moderocky.guardian.logic.shape.Polyhedron;
import com.moderocky.guardian.logic.shape.Vertex;
import com.moderocky.guardian.util.ParticleUtils;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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

    public PolyhedralZone(@NotNull NamespacedKey id, @NotNull ConfigurationSection section) {
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

    public BoundingBox getBoundingBox() {
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
    public void save(@NotNull ConfigurationSection section) {
        api = Guardian.getApi();
        World world = getWorld();
        section.set("location", api.serialisePosition(getLocation()));
        List<String> strings = new ArrayList<>();
        polyhedron.getLocations(world).forEach(location -> strings.add(api.serialisePosition(location)));
        section.set("vertices", strings);
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
        {
            List<String> list = section.getStringList("vertices");
            List<Location> locations = new ArrayList<>();
            for (String string : list) {
                locations.add(api.deserialisePosition(string));
            }
            polyhedron = new Polyhedron(LogicUtils.toVertices(locations));
        }
        this.boundingBox = polyhedron.getBoundingBox();
        for (String string : section.getStringList("flags")) {
            addFlag(string);
        }
        for (String string : section.getStringList("players")) {
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
