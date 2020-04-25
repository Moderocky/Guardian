package com.moderocky.guardian.logic;

public class Geodesic {

    private final Vertex start;
    private final Vertex end;
    private final double x;
    private final double y;
    private final double z;

    public Geodesic(Vertex start, Vertex end) {
        this.start = start;
        this.end = end;
        this.x = end.getX() - start.getX();
        this.y = end.getY() - start.getY();
        this.z = end.getZ() - start.getZ();
    }

    public static Geodesic multiple(Geodesic u, Geodesic v) {
        double x = u.getY() * v.getZ() - u.getZ() * v.getY();
        double y = u.getZ() * v.getX() - u.getX() * v.getZ();
        double z = u.getX() * v.getY() - u.getY() * v.getX();
        Vertex p0 = v.getStart();
        Vertex p1 = Vertex.add(p0, new Vertex(x, y, z));
        return new Geodesic(p0, p1);
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
}
