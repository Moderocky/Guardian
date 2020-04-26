package com.moderocky.guardian.util;

import com.moderocky.guardian.logic.shape.Dion;
import com.moderocky.guardian.logic.shape.Polyhedron;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ParticleUtils {

    public static void drawLine(Particle particle, Location start, Location end, double distance) {
        World world = start.getWorld();
        double dist = start.distance(end);
        Vector p1 = start.toVector();
        Vector p2 = end.toVector();
        Vector vec = p2.clone().subtract(p1).normalize().multiply(distance);
        world.spawnParticle(particle, p1.getX(), p1.getY(), p1.getZ(), 1, 0, 0, 0, 0);
        world.spawnParticle(particle, p2.getX(), p2.getY(), p2.getZ(), 1, 0, 0, 0, 0);
        double length = 0;
        for (; length < dist; p1.add(vec)) {
            world.spawnParticle(particle, p1.getX(), p1.getY(), p1.getZ(), 1, 0, 0, 0, 0);
            length += distance;
        }
    }

    public static void drawLine(Particle particle, @Nullable Object data, Location start, Location end, double distance) {
        World world = start.getWorld();
        double dist = start.distance(end);
        Vector p1 = start.toVector();
        Vector p2 = end.toVector();
        Vector vec = p2.clone().subtract(p1).normalize().multiply(distance);
        world.spawnParticle(particle, p1.getX(), p1.getY(), p1.getZ(), 1, 0, 0, 0, 0, data);
        world.spawnParticle(particle, p2.getX(), p2.getY(), p2.getZ(), 1, 0, 0, 0, 0, data);
        double length = 0;
        for (; length < dist; p1.add(vec)) {
            world.spawnParticle(particle, p1.getX(), p1.getY(), p1.getZ(), 1, 0, 0, 0, 0, data);
            length += distance;
        }
    }

    public static void drawBox(Particle particle, @Nullable Object data, BoundingBox box, World world, double distance) {
        Location c1 = box.getMin().toLocation(world);
        Location c2 = c1.clone();
        c2.setX(box.getMaxX() + 1);
        Location c3 = c1.clone();
        c3.setZ(box.getMaxZ() + 1);
        Location c4 = c1.clone();
        c4.setX(box.getMaxX() + 1);
        c4.setZ(box.getMaxZ() + 1);
        Location c8 = box.getMax().toLocation(world).add(1, 1, 1);
        Location c7 = c8.clone();
        c7.setX(box.getMinX());
        Location c6 = c8.clone();
        c6.setZ(box.getMinZ());
        Location c5 = c8.clone();
        c5.setX(box.getMinX());
        c5.setZ(box.getMinZ());

        drawLine(particle, data, c1, c2, distance);
        drawLine(particle, data, c1, c3, distance);
        drawLine(particle, data, c1, c5, distance);
        drawLine(particle, data, c2, c4, distance);
        drawLine(particle, data, c2, c6, distance);
        drawLine(particle, data, c3, c4, distance);
        drawLine(particle, data, c3, c7, distance);
        drawLine(particle, data, c5, c6, distance);
        drawLine(particle, data, c5, c7, distance);
        drawLine(particle, data, c8, c7, distance);
        drawLine(particle, data, c8, c6, distance);
        drawLine(particle, data, c8, c4, distance);
    }

    public static void drawHash(Particle particle, double distance, BoundingBox box, World world) {
        Location c1 = box.getMin().toLocation(world);
        Location c2 = c1.clone();
        c2.setX(box.getMaxX() + 1);
        Location c3 = c1.clone();
        c3.setZ(box.getMaxZ() + 1);
        Location c4 = c1.clone();
        c4.setX(box.getMaxX() + 1);
        c4.setZ(box.getMaxZ() + 1);
        Location c8 = box.getMax().toLocation(world).add(1, 1, 1);
        Location c7 = c8.clone();
        c7.setX(box.getMinX());
        Location c6 = c8.clone();
        c6.setZ(box.getMinZ());
        Location c5 = c8.clone();
        c5.setX(box.getMinX());
        c5.setZ(box.getMinZ());

        drawHash(particle, distance, c1, c2, c3, c4, c5, c6, c7, c8);
    }

    public static void drawHash(Particle particle, double distance, Location... locations) {
        if (locations.length < 1) return;
        World world = locations[0].getWorld();
        List<Location> list = new ArrayList<>(Arrays.asList(locations));
        HashMap<Location, AtomicInteger> checker = new HashMap<>();
        list.forEach(location -> checker.put(location, new AtomicInteger(0)));
        for (Location location : list) {
            if (location.getWorld() != world) continue;
            List<Location> locs = new ArrayList<>(list);
            locs.remove(location);
            locs.forEach(l -> {
                AtomicInteger i = checker.get(l);
                AtomicInteger integer = checker.get(l);
                if (i.get() < (list.size()) && integer.get() < (list.size())) {
                    drawLine(particle, location, l, distance);
                    integer.getAndIncrement();
                    i.getAndIncrement();
                }
            });
        }
    }

    public static void drawHash(Particle particle, double distance, World world, Polyhedron polyhedron) {
        Dion[] dions = polyhedron.dions();
        for (Dion dion : dions) {
            drawLine(particle, dion.getStart().toLocation(world), dion.getEnd().toLocation(world), distance);
        }
    }

}
