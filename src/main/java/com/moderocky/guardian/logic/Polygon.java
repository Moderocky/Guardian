package com.moderocky.guardian.logic;

import java.util.ArrayList;
import java.util.List;

public class Polygon {

    private final List<Vertex> vertices;
    private final List<Integer> indices;
    private final int vertexCount;

    public Polygon(List<Vertex> vertices, List<Integer> integers) {
        this.vertices = new ArrayList<>();
        this.indices = new ArrayList<>();
        this.vertexCount = vertices.size();

        for (int i = 0; i < vertexCount; i++) {
            this.vertices.add(vertices.get(i));
            this.indices.add(integers.get(i));
        }
    }

    public List<Vertex> getVertices() {
        return this.vertices;
    }

    public List<Integer> getIndices() {
        return this.indices;
    }

    public int getVertexCount() {
        return this.vertexCount;
    }
}
