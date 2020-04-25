package com.moderocky.guardian.logic;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.List;

public class Polyhedron {

    private final List<Vertex> vertices;
    private final List<Integer> indices;
    private final int vertexCount;

    public Polyhedron(List<Vertex> vertices) {
        this.vertices = new ArrayList<>();
        this.indices = new ArrayList<>();
        this.vertexCount = vertices.size();
        for (int i = 0; i < vertexCount; i++) {
            this.vertices.add(vertices.get(i));
            this.indices.add(i);
        }
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

}
