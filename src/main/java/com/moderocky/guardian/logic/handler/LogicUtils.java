package com.moderocky.guardian.logic.handler;

import com.moderocky.guardian.logic.shape.Vertex;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unchecked")
public class LogicUtils {

    public static List<Block> getBlocks(BoundingBox boundingBox, World world) {
        Location[] bounds = new Location[]{
            boundingBox.getMin().toLocation(world),
            boundingBox.getMax().toLocation(world)
        };
        List<Block> blocks = new ArrayList<>();
        for (double y = bounds[0].getY(); y <= bounds[1].getY(); y++) {
            for (double x = bounds[0].getX(); x <= bounds[1].getX(); x++) {
                for (double z = bounds[0].getZ(); z <= bounds[1].getZ(); z++) {
                    blocks.add(new Location(bounds[0].getWorld(), (int) x, (int) y, (int) z).getBlock());
                }
            }
        }
        return blocks;
    }

    public static boolean containsList(List<List<Integer>> list, List<Integer> listItem) {
        Collections.sort(listItem);

        for (List<Integer> temp : list) {

            if (temp.size() == listItem.size()) {

                Collections.sort(temp);

                if (temp.equals(listItem)) {
                    return true;
                }

            }
        }
        return false;
    }

    public static Vertex getMidpoint(Vertex start, Vertex end) {
        return new Vertex(((start.getX() + end.getX()) / 2), ((start.getY() + end.getY()) / 2), ((start.getZ() + end.getZ()) / 2));
    }

    public static List<Vertex> toVertices(List<Location> locations) {
        List<Vertex> vertices = new ArrayList<>();
        locations.forEach(location -> vertices.add(Vertex.from(location)));
        return vertices;
    }

    public static List<Location> toLocations(List<Vertex> vertices, World world) {
        List<Location> locations = new ArrayList<>();
        vertices.forEach(vertex -> locations.add(vertex.toLocation(world)));
        return locations;
    }

    public static List<Vertex> getVertices(BoundingBox box) {
        List<Vertex> vertices = new ArrayList<>();
        vertices.add(new Vertex(box.getMinX(), box.getMinY(), box.getMinZ()));
        vertices.add(new Vertex(box.getMinX(), box.getMinY(), box.getMaxZ()));
        vertices.add(new Vertex(box.getMinX(), box.getMaxY(), box.getMinZ()));
        vertices.add(new Vertex(box.getMinX(), box.getMaxY(), box.getMaxZ()));
        vertices.add(new Vertex(box.getMaxX(), box.getMinY(), box.getMinZ()));
        vertices.add(new Vertex(box.getMaxX(), box.getMinY(), box.getMaxZ()));
        vertices.add(new Vertex(box.getMaxX(), box.getMaxY(), box.getMinZ()));
        vertices.add(new Vertex(box.getMaxX(), box.getMaxY(), box.getMaxZ()));
        return vertices;
    }

    public static BoundingBox getBoundingBox(List<Vertex> vertices) {
        if (vertices.size() == 8) {
            return BoundingBox.of(vertices.get(0).toVector(), vertices.get(7).toVector());
        } else if (vertices.size() > 1) {
            return BoundingBox.of(vertices.get(0).toVector(), vertices.get(1).toVector());
        }
        throw new IllegalArgumentException();
    }

    public static BoundingBox getBoundingBox(Vertex... vertices) {
        if (vertices.length == 8) {
            return BoundingBox.of(vertices[0].toVector(), vertices[7].toVector());
        } else if (vertices.length > 1) {
            return BoundingBox.of(vertices[0].toVector(), vertices[1].toVector());
        }
        throw new IllegalArgumentException();
    }

    public static <X> List<X[]> getPairs(Collection<X> collection) {
        List<X> list = new ArrayList<>(collection);
        List<X[]> xes = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                xes.add((X[]) new Object[]{list.get(i), list.get(j)});
            }
        }
        return xes;
    }

    public static <X> List<X[]> getTrios(Collection<X> collection) {
        List<X> list = new ArrayList<>(collection);
        List<X[]> xes = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                for (int k = i + 1; k < list.size(); k++) {
                    xes.add((X[]) new Object[]{list.get(i), list.get(j), list.get(k)});
                }
            }
        }
        return xes;
    }

    public static <X> List<X[]> getQuads(Collection<X> collection) {
        List<X> list = new ArrayList<>(collection);
        List<X[]> xes = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                for (int k = i + 1; k < list.size(); k++) {
                    for (int l = i + 1; l < list.size(); l++) {
                        xes.add((X[]) new Object[]{list.get(i), list.get(j), list.get(k), list.get(l)});
                    }
                }
            }
        }
        return xes;
    }

}
