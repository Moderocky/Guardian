package com.moderocky.guardian.logic.shape;

import com.moderocky.guardian.logic.ascendancy.IOrder0;
import com.moderocky.guardian.logic.ascendancy.Polytope;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class Vertex implements Polytope, IOrder0 {

    private double x;
    private double y;
    private double z;

    public Vertex(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Vertex from(Vector vector) {
        return new Vertex(vector.getX(), vector.getY(), vector.getZ());
    }

    public static Vertex from(Location location) {
        return new Vertex(location.getX(), location.getY(), location.getZ());
    }

    public static Vertex add(Vertex start, Vertex end) {
        return new Vertex(start.x + end.x, start.y + end.y, start.z + end.z);
    }

    public Vertex midpoint(Vertex vertex) {
        return new Vertex(((this.getX() + vertex.getX()) / 2), ((this.getY() + vertex.getY()) / 2), ((this.getZ() + vertex.getZ()) / 2));
    }

    public double distance(Vertex vertex) {
        return Math.sqrt(distanceSquared(vertex));
    }

    public double distanceSquared(Vertex vertex) {
        return ((vertex.x - this.x) * (vertex.x - this.x)) + ((vertex.y - this.y) * (vertex.y - this.y)) + ((vertex.z - this.z) * (vertex.z - this.z));
    }

    public double taxicabDistance(Vertex vertex) {
        return Math.abs(vertex.x - this.x) + Math.abs(vertex.y - this.y) + Math.abs(vertex.z - this.z);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z);
    }

    public Vector toVector() {
        return new Vector(x, y, z);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Vertex[] vertices() {
        return new Vertex[]{this};
    }

    @Override
    public boolean contains(Polytope polytope) {
        for (Vertex vertex : polytope.vertices()) {
            if (!vertex.equals(this)) return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vertex)) return false;
        Vertex vertex = (Vertex) o;
        return vertex.x == x && vertex.y == y && vertex.z == z;
    }

    @Override
    public int hashCode() {
        return (int) (x + y + z);
    }

    @SuppressWarnings("all")
    public Vertex clone() {
        return new Vertex(x, y, z);
    }

}
