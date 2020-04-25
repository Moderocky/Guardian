package com.moderocky.guardian.api;

import com.google.common.base.Ascii;
import com.moderocky.mask.api.commons.Describable;
import com.moderocky.mask.api.commons.Nameable;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Zone implements Nameable, Describable {

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

    public void clearPlayers() {
        players.clear();
    }

    public final @NotNull List<String> getFlags() {
        return flags;
    }

    public void clearFlags() {
        flags.clear();
    }

    public void addFlag(@NotNull String flag) {
        if (!flags.contains(flag)) flags.add(flag);
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

    @Override
    public String getName() {
        return convertCase(getKey().getKey());
    }

    /**
     * @return The individual zone key, in the form "plugin:zone_id" to prevent conflicts.
     */
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    /**
     * @return The individual zone weight, used to deal with conflicting instructions
     */
    public abstract int getWeight();

    /**
     * This method should return the LARGEST POSSIBLE radius from the point in {@link #getLocation()}.
     * It is used in narrowing checks, so any point within your zone MUST fall within this radius.
     * <p>
     * What this means is that if your zone is a strange shape, such as a giant 3D pentagram,
     * this radius must be at least the distance from the {@link #getLocation()} to the furthest covered point.
     * <p>
     * This is used as a fast check to see if a position might fall within your zone, before
     * the actual {@link #isInside(Location)} method is called.
     * If your zone is a super-complex shape and the inside check has massive overhead, we wouldn't want to
     * be calling that needlessly.
     * <p>
     * You should therefore over-estimate this, just in case.
     *
     * @return The over-estimated radius
     */
    public abstract double getRadius();

    /**
     * Whether a location falls inside your zone type.
     * For simple (cuboidal) zones, this might be as simple as checking whether it is in bounds, or whether it is
     * within a radius (spherical) - but your zone might be more complex.
     * Here is where you should perform your checks.
     * <p>
     * When this is called, it is already known that:
     * • Location exists
     * • Location is in the same world as {@link #getWorld()}
     * • Location falls within a distance of {@link #getRadius()} around {@link #getLocation()}
     *
     * @param location The location to test
     * @return boo
     */
    public abstract boolean isInside(@NotNull Location location);

    /**
     * Whether an interaction can occur.
     * This sort of interaction is one without a player cause, such as a creeper exploding or a block decaying.
     * <p>
     * This will be called after {@link #isInside(Location)} is checked.
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
     * This will be called after {@link #isInside(Location)} is checked.
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
     * This method should be used for proverbial 'owners' of the zone.
     * It will be used when a player tries to alter flags or delete the zone.
     * <p>
     * This is also used when checking whether a player can create a zone intersecting existing zones.
     *
     * @param player The UUID of the potential editor
     * @return Whether this player can make changes to the zone (i.e. flags, etc.)
     */
    public abstract boolean canEdit(@NotNull UUID player);

    public abstract @NotNull World getWorld();

    public abstract @NotNull Location getLocation();

    public abstract void save(@NotNull ConfigurationSection section);

    public abstract void load(@NotNull ConfigurationSection section);

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
