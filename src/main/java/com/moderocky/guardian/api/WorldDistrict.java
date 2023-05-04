package com.moderocky.guardian.api;

import org.bukkit.Location;
import org.bukkit.World;

@SuppressWarnings("unused")
public class WorldDistrict {

    private final long x;
    private final long z;
    private final World world;

    public WorldDistrict(Location location) {
        world = location.getWorld();
        x = Math.round(Math.floor(location.getX() / 2048) * 2048);
        z = Math.round(Math.floor(location.getZ() / 2048) * 2048);
    }

    public final long getX() {
        return x;
    }

    public final long getZ() {
        return z;
    }

    public final World getWorld() {
        return world;
    }

    @Override
    public String toString() {
        return "WorldDistrict{" +
            "x=" + x +
            ", z=" + z +
            ", world=" + world +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorldDistrict)) return false;
        WorldDistrict that = (WorldDistrict) o;
        return x == that.x && z == that.z && world == that.world;
    }

    @Override
    public int hashCode() {
        return (int) (world.hashCode() + x + z);
    }

}
