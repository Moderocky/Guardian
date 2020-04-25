package com.moderocky.guardian.logic;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LogicUtils {

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
        return new Vertex(((start.getX() + end.getX())/2), ((start.getY() + end.getY())/2), ((start.getZ() + end.getZ())/2));
    }

    public static List<Vertex> toVertices(List<Location> locations) {
        List<Vertex> vertices = new ArrayList<>();
        locations.forEach(location -> vertices.add(Vertex.from(location)));
        return vertices;
    }

    public static List<Location> toLocation(List<Vertex> vertices, World world) {
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

}
