package com.moderocky.guardian.api;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Zone {

    private final @NotNull List<String> flags = new ArrayList<>();
    private final @NotNull List<UUID> players = new ArrayList<>();
    private final @NotNull NamespacedKey key;

    public Zone(@NotNull NamespacedKey id) {
        this.key = id;
    }

    public Zone(@NotNull NamespacedKey id, @NotNull ConfigurationSection section) {
        this.key = id;
        load(section);
    }

    public List<UUID> getAllowedPlayers() {
        return players;
    }

    public void addPlayer(UUID uuid) {
        players.add(uuid);
    }

    public void removePlayer(UUID uuid) {
        players.remove(uuid);
    }

    public boolean isAllowed(UUID player) {
        return players.contains(player);
    }

    public final @NotNull List<String> getFlags() {
        return flags;
    }

    public void addFlag(@NotNull String flag) {
        flags.add(flag);
    }

    public void removeFlag(@NotNull String flag) {
        flags.remove(flag);
    }

    public boolean hasFlag(@NotNull String flag) {
        return flags.contains(flag);
    }

    /**
     * This should DEFINITELY be overridden!
     * It is a basic attempt at a check. That is all.
     * <p>
     * Unless this is overridden it treats the zone as a circle around the location.
     *
     * @param zone The zone to check against
     * @param <Z> Something extending Zone
     * @return boo
     */
    public <Z extends Zone> boolean overlaps(Z zone) {
        double d = getLocation().distance(zone.getLocation());
        return (d < (getRadius() + zone.getRadius()) || zone.overlaps(this));
    }

    public WorldDistrict getDistrict() {
        return new WorldDistrict(getLocation());
    }

    /**
     * @return The individual region key, in the form "plugin:region_id" to prevent conflicts.
     */
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    /**
     * @return The individual region weight, used to deal with conflicting instructions
     */
    public abstract int getWeight();

    /**
     * This method should return the LARGEST POSSIBLE radius from the point in {@link #getLocation()}.
     * It is used in narrowing checks, so any point within your zone MUST fall within this radius.
     * <p>
     * What this means is that if your region is a strange shape, such as a giant 3D pentagram,
     * this radius must be at least the distance from the {@link #getLocation()} to the furthest covered point.
     * <p>
     * This is used as a fast check to see if a position might fall within your region, before
     * the actual {@link #isInside(Location)} method is called.
     * If your region is a super-complex shape and the inside check has massive overhead, we wouldn't want to
     * be calling that needlessly.
     * <p>
     * You should also over-estimate this, just in case.
     *
     * @return The over-estimated radius
     */
    public abstract double getRadius();

    public abstract boolean isInside(@NotNull Location location);

    /**
     * Whether an interaction can occur.
     * This sort of interaction is one without a player cause, such as a creeper exploding or a block decaying.
     * <p>
     * Feel free to override this if you have a more special check.
     *
     * @param location The location
     * @param interactionType The interaction type
     * @return boo
     */
    public boolean canInteract(@NotNull Location location, @NotNull String interactionType) {
        return !flags.contains(interactionType);
    }

    /**
     * Whether a player-caused interaction can occur.
     * Interactions not directly caused by players will be covered by {@link #canInteract(Location, String)}.
     * <p>
     * Feel free to override this if you have a more special check.
     *
     * @param location The location
     * @param interactionType The interaction type
     * @param player The interacting player
     * @return boo
     */
    public boolean canInteract(@NotNull Location location, @NotNull String interactionType, @NotNull Player player) {
        return !flags.contains(interactionType) || isAllowed(player.getUniqueId());
    }

    /**
     * @param player The UUID of the potential editor
     * @return Whether this player can make changes to the region (i.e. flags, etc.)
     */
    public abstract boolean canEdit(@NotNull UUID player);

    public abstract @NotNull World getWorld();

    public abstract @NotNull Location getLocation();

    public abstract void save(@NotNull ConfigurationSection section);

    public abstract void load(@NotNull ConfigurationSection section);

}
