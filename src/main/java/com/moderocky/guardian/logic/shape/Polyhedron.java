package com.moderocky.guardian.logic.shape;

import com.moderocky.guardian.logic.ascendancy.IOrder3;
import com.moderocky.guardian.logic.ascendancy.Polytope;
import com.moderocky.guardian.logic.handler.LogicUtils;
import com.moderocky.guardian.logic.handler.PolyProcessor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Polyhedron implements Polytope, IOrder3 {

    private final List<Vertex> vertices;
    private final List<Integer> indices;
    private final int vertexCount;

    public Polyhedron(List<Vertex> vertices) {
        correctVertices(vertices);
        this.vertices = new ArrayList<>();
        this.indices = new ArrayList<>();
        this.vertexCount = vertices.size();
        for (int i = 0; i < vertexCount; i++) {
            this.vertices.add(vertices.get(i));
            this.indices.add(i);
        }
    }

    private Polyhedron(List<Vertex> vertices, boolean skipCorrection) {
        if (!skipCorrection) correctVertices(vertices);
        this.vertices = new ArrayList<>();
        this.indices = new ArrayList<>();
        this.vertexCount = vertices.size();
        for (int i = 0; i < vertexCount; i++) {
            this.vertices.add(vertices.get(i));
            this.indices.add(i);
        }
    }

    public static Polyhedron createCuboid(Vertex min, Vertex max) {
        List<Vertex> vertices = new ArrayList<>();
        vertices.add(new Vertex(Math.min(min.getX(), max.getX()), Math.min(min.getY(), max.getY()), Math.min(min.getZ(), max.getZ())));
        vertices.add(new Vertex(Math.min(min.getX(), max.getX()), Math.max(min.getY(), max.getY()), Math.min(min.getZ(), max.getZ())));
        vertices.add(new Vertex(Math.min(min.getX(), max.getX()), Math.min(min.getY(), max.getY()), Math.max(min.getZ(), max.getZ())));
        vertices.add(new Vertex(Math.min(min.getX(), max.getX()), Math.max(min.getY(), max.getY()), Math.max(min.getZ(), max.getZ())));
        vertices.add(new Vertex(Math.max(min.getX(), max.getX()), Math.min(min.getY(), max.getY()), Math.min(min.getZ(), max.getZ())));
        vertices.add(new Vertex(Math.max(min.getX(), max.getX()), Math.max(min.getY(), max.getY()), Math.min(min.getZ(), max.getZ())));
        vertices.add(new Vertex(Math.max(min.getX(), max.getX()), Math.min(min.getY(), max.getY()), Math.max(min.getZ(), max.getZ())));
        vertices.add(new Vertex(Math.max(min.getX(), max.getX()), Math.max(min.getY(), max.getY()), Math.max(min.getZ(), max.getZ())));
        return new Polyhedron(vertices);
    }

    public static Polyhedron createCube(Vertex vertex, double diameter) {
        Vertex end = new Vertex(vertex.getX() + diameter, vertex.getY() + diameter, vertex.getZ() + diameter);
        return createCuboid(vertex, end);
    }

    private void correctVertices(final List<Vertex> vertices) {
        List<Vertex> workingCopy = new ArrayList<>(vertices);
        workingCopy.forEach(vertex -> {
            List<Vertex> temp = new ArrayList<>(workingCopy);
            temp.remove(vertex);
            PolyProcessor processor = new PolyProcessor(new Polyhedron(temp, true));
            if (processor.isInside(vertex)) vertices.remove(vertex);
        });
    }

    public List<Vertex> getVertices() {
        return this.vertices;
    }

    public List<Integer> getIndices() {
        return this.indices;
    }

    public BoundingBox getBoundingBox() {
        double minX, maxX, minY, maxY, minZ, maxZ;
        minX = maxX = vertices.get(0).getX();
        minY = maxY = vertices.get(0).getY();
        minZ = maxZ = vertices.get(0).getZ();
        for (int i = 1; i < vertexCount; i++) {
            if (vertices.get(i).getX() < minX) minX = vertices.get(i).getX();
            if (vertices.get(i).getY() < minY) minY = vertices.get(i).getY();
            if (vertices.get(i).getZ() < minZ) minZ = vertices.get(i).getZ();
            if (vertices.get(i).getX() > maxX) maxX = vertices.get(i).getX();
            if (vertices.get(i).getY() > maxY) maxY = vertices.get(i).getY();
            if (vertices.get(i).getZ() > maxZ) maxZ = vertices.get(i).getZ();
        }
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public int getVertexCount() {
        return this.vertexCount;
    }

    public List<Location> getLocations(World world) {
        List<Location> locations = new ArrayList<>();
        vertices.forEach(vertex -> locations.add(vertex.toLocation(world)));
        return locations;
    }

    @Override
    public int getOrder() {
        return 3;
    }

    @Override
    public Vertex[] vertices() {
        return vertices.toArray(new Vertex[0]);
    }

    @Override
    public boolean contains(Polytope polytope) {
        PolyProcessor processor = new PolyProcessor(this);
        for (Vertex vertex : polytope.vertices()) {
            if (!processor.isInside(vertex)) return false;
        }
        return true;
    }

    @Override
    public boolean contains(@NotNull Polygon polygon) {
        PolyProcessor processor = new PolyProcessor(this);
        for (Vertex vertex : polygon.vertices()) {
            if (!processor.isInside(vertex)) return false;
        }
        return true;
    }

    @Override
    public Polyhedron[] polyhedrons() {
        return new Polyhedron[]{this};
    }

    @Override
    public boolean contains(@NotNull Dion dion) {
        PolyProcessor processor = new PolyProcessor(this);
        for (Vertex vertex : dion.vertices()) {
            if (!processor.isInside(vertex)) return false;
        }
        return true;
    }

    @Override
    public Polygon[] polygons() {
        List<Polygon> polygons = new ArrayList<>();
        for (Vertex[] trio : LogicUtils.getTrios(vertices)) {
            polygons.add(new Polygon(Arrays.asList(trio), Arrays.asList(0, 1, 2)));
        }
        return polygons.toArray(new Polygon[0]);
    }

    @Override
    public boolean contains(@NotNull Vertex vertex) {
        PolyProcessor processor = new PolyProcessor(this);
        return processor.isInside(vertex);
    }

    @Override
    public Dion[] dions() {
        List<Dion> dions = new ArrayList<>();
        for (Vertex[] pair : LogicUtils.getPairs(vertices)) {
            dions.add(new Dion(pair[0], pair[1]));
        }
        return dions.toArray(new Dion[0]);
    }

}
