package com.moderocky.guardian.logic.handler;

import com.moderocky.guardian.logic.ascendancy.Polytope;
import com.moderocky.guardian.logic.shape.Dion;
import com.moderocky.guardian.logic.shape.Vertex;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Manifold {

    private final @NotNull Set<Polytope> polytopes = new HashSet<>();
    private final double[] origin = new double[3];

    public Manifold() {
    }

    public void plot(@NotNull Polytope polytope) {
        if (polytopes.contains(polytope)) return;
        polytopes.add(polytope);
    }

    public void remove(@NotNull Polytope polytope) {
        polytopes.remove(polytope);
    }

    public boolean contains(@NotNull Polytope polytope) {
        return polytopes.contains(polytope);
    }

    public void clear() {
        polytopes.clear();
    }

    public @NotNull Set<Polytope> getPolytopes() {
        return polytopes;
    }

    public void setOrigin(double x, double y, double z) {
        origin[0] = x;
        origin[0] = y;
        origin[0] = z;
    }

    public List<Polytope> getEncompassing(Polytope polytope) {
        List<Polytope> polytopes = new ArrayList<>();
        for (Polytope poly : getPolytopes()) {
            if (poly.contains(polytope)) polytopes.add(poly);
        }
        return polytopes;
    }

    public double[] getOrigin() {
        return origin;
    }

    public Dion getLine(Vertex start, Vertex end) {
        return new Dion(start, end);
    }

    public Vector getVector(Vertex start, Vertex end) {
        return getLine(start, end).toVector();
    }

    public void transform(@NotNull Polytope polytope, double[] origin, @NotNull TransType trans, Number value) {
        trans.accept(polytope, origin, value);
    }

}
