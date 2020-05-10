package com.moderocky.guardian.logic.shape;

import com.moderocky.guardian.logic.ascendancy.IOrder4;
import com.moderocky.guardian.logic.ascendancy.Polytope;
import com.moderocky.guardian.logic.handler.LogicUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Polychoron implements Polytope, IOrder4 {

    private final List<Vertex> vertices;
    private final List<Integer> indices;
    private final int vertexCount;

    public Polychoron(List<Vertex> vertices) {
        this.vertices = new ArrayList<>();
        this.indices = new ArrayList<>();
        this.vertexCount = vertices.size();
        for (int i = 0; i < vertexCount; i++) {
            this.vertices.add(vertices.get(i));
            this.indices.add(i);
        }
    }


    @Override
    public int getOrder() {
        return 4;
    }

    @Override
    public Vertex[] vertices() {
        return new Vertex[0];
    }

    @Override
    public boolean contains(Polytope polytope) {
        return false;
    }

    @Override
    public Polychoron[] polychorons() {
        return new Polychoron[]{this};
    }

    @Override
    public boolean contains(@NotNull Polygon polygon) {
        return false; //TODO
    }

    @Override
    public Polyhedron[] polyhedrons() {
        List<Polyhedron> polyhedra = new ArrayList<>();
        for (Vertex[] quads : LogicUtils.getQuads(vertices)) {
            polyhedra.add(new Polyhedron(Arrays.asList(quads)));
        }
        return polyhedra.toArray(new Polyhedron[0]);
    }

    @Override
    public boolean contains(@NotNull Dion dion) {
        return false; //TODO
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
        return false; //TODO
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
