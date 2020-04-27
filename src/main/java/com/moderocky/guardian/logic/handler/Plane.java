package com.moderocky.guardian.logic.handler;

import com.moderocky.guardian.logic.shape.Dion;
import com.moderocky.guardian.logic.shape.Vertex;

public class Plane {

    // Plane Equation: a * x + b * y + c * z + d = 0

    private final double a;
    private final double b;
    private final double c;
    private final double d;

    public Plane(double a, double b, double c, double d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public Plane(Vertex v1, Vertex v2, Vertex v3) {
        Dion v = new Dion(v1, v2);
        Dion u = new Dion(v1, v3);
        Dion n = Dion.multiple(u, v);

        double a = n.getX();
        double b = n.getY();
        double c = n.getZ();
        double d = -(a * v1.getX() + b * v1.getY() + c * v1.getZ());

        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;

    }

    public static Plane negative(Plane plane) {
        return new Plane(-plane.getA(), -plane.getB(), -plane.getC(), -plane.getD());
    }

    public static double multiple(Vertex vertex, Plane plane) {
        return (vertex.getX() * plane.getA() + vertex.getY() * plane.getB() + vertex.getZ() * plane.getC() + plane.getD());
    }

    public double getA() {
        return this.a;
    }

    public double getB() {
        return this.b;
    }

    public double getC() {
        return this.c;
    }

    public double getD() {
        return this.d;
    }
}
