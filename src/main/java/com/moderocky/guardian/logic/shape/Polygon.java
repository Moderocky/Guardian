package com.moderocky.guardian.logic.shape;

import com.moderocky.guardian.logic.ascendancy.IOrder2;
import com.moderocky.guardian.logic.ascendancy.Polytope;
import com.moderocky.guardian.logic.handler.LogicUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Polygon implements Polytope, IOrder2 {

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

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public Vertex[] vertices() {
        return vertices.toArray(new Vertex[0]);
    }

    @Override
    public boolean contains(@NotNull Dion dion) {
        return contains(dion.getStart()) && contains(dion.getEnd());
    }

    @Override
    public Polygon[] polygons() {
        return new Polygon[]{this};
    }

    @Override
    public boolean contains(@NotNull Vertex vertex) {
        for (Vertex[] trio : LogicUtils.getTrios(vertices)) {
            Plane plane = new Plane(trio[0], trio[1], trio[2]);
            double dis = Plane.multiple(vertex, plane);
            if (Math.abs(dis) < 0.001) return true;
        }
        return false;
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
