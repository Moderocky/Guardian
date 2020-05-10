package com.moderocky.guardian.logic.shape;

import com.moderocky.guardian.logic.ascendancy.IOrder1;
import com.moderocky.guardian.logic.ascendancy.Polytope;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class Dion implements Polytope, IOrder1 {

    private final Vertex start;
    private final Vertex end;
    private final double x;
    private final double y;
    private final double z;

    public Dion(Vertex start, Vertex end) {
        this.start = start;
        this.end = end;
        this.x = end.getX() - start.getX();
        this.y = end.getY() - start.getY();
        this.z = end.getZ() - start.getZ();
    }

    public static Dion multiple(Dion u, Dion v) {
        double x = u.getY() * v.getZ() - u.getZ() * v.getY();
        double y = u.getZ() * v.getX() - u.getX() * v.getZ();
        double z = u.getX() * v.getY() - u.getY() * v.getX();
        Vertex p0 = v.getStart();
        Vertex p1 = Vertex.add(p0, new Vertex(x, y, z));
        return new Dion(p0, p1);
    }

    public final double getLength() {
        return start.distance(end);
    }

    public final Vertex getStart() {
        return this.start;
    }

    public final Vertex getEnd() {
        return this.end;
    }

    public final double getX() {
        return this.x;
    }

    public final double getY() {
        return this.y;
    }

    public final double getZ() {
        return this.z;
    }

    public Vector toVector() {
        return new Vector(x, y, z);
    }

    public double getAngleX() {
        double dy, dz;
        dy = end.getY() - start.getY();
        dz = end.getZ() - start.getZ();
        return Math.toDegrees(Math.atan2(dy, dz));
    }

    public double getAngleY() {
        double dx, dz;
        dx = end.getX() - start.getX();
        dz = end.getZ() - start.getZ();
        return Math.toDegrees(Math.atan2(dx, dz));
    }

    public double getAngleZ() {
        double dx, dy;
        dx = end.getX() - start.getX();
        dy = end.getY() - start.getY();
        return Math.toDegrees(Math.atan2(dx, dy));
    }

    @Override
    public boolean contains(@NotNull Vertex vertex) {
        return start.distance(vertex) + vertex.distance(end) == getLength();
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public @NotNull Vertex[] vertices() {
        return new Vertex[]{start, end};
    }

    @Override
    public boolean contains(Polytope polytope) {
        for (Vertex vertex : polytope.vertices()) {
            if (!contains(vertex)) return false;
        }
        return true;
    }

    @Override
    public Dion[] dions() {
        return new Dion[]{this};
    }

}
