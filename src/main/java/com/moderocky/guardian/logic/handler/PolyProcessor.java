package com.moderocky.guardian.logic.handler;

import com.moderocky.guardian.logic.shape.Plane;
import com.moderocky.guardian.logic.shape.Polygon;
import com.moderocky.guardian.logic.shape.Polyhedron;
import com.moderocky.guardian.logic.shape.Vertex;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PolyProcessor {

    private final double measurementError = 0.001;
    private final Polyhedron polyhedron;
    private double x0, x1, y0, y1, z0, z1;
    private List<Polygon> polygons;
    private List<Plane> planes;
    private int faceCount;
    private double maxDist;

    public PolyProcessor(@NotNull Polyhedron polyhedron) {
        this.polyhedron = polyhedron;
        this.setHedralBoundary(polyhedron);
        this.setHedralUnitError(polyhedron);
        this.setConvexFaces(polyhedron);
    }

    public double getMinX() {
        return this.x0;
    }

    public double getMaxX() {
        return this.x1;
    }

    public double getMinY() {
        return this.y0;
    }

    public double getMaxY() {
        return this.y1;
    }

    public double getMinZ() {
        return this.z0;
    }

    public double getMaxZ() {
        return this.z1;
    }

    public List<Polygon> getPolygons() {
        return this.polygons;
    }

    public List<Plane> getPlanes() {
        return this.planes;
    }

    public int getFaceCount() {
        return this.faceCount;
    }

    public boolean isInside(Vertex vertex) {
        for (int i = 0; i < this.faceCount; i++) {

            double dis = Plane.multiple(vertex, this.planes.get(i));

            if (dis > 0) {
                return false;
            }

        }

        return true;
    }

    public boolean isInside(double x, double y, double z) {
        return isInside(new Vertex(x, y, z));
    }

    private void setHedralUnitError(Polyhedron polyhedron) {
        this.maxDist = ((Math.abs(this.x0) + Math.abs(this.x1) +
                Math.abs(this.y0) + Math.abs(this.y1) +
                Math.abs(this.z0) + Math.abs(this.z1)) / 6 * measurementError);
    }

    private void setHedralBoundary(Polyhedron polyhedron) {
        List<Vertex> vertices = polyhedron.getVertices();

        int n = polyhedron.getVertexCount();

        double xmin, xmax, ymin, ymax, zmin, zmax;

        xmin = xmax = vertices.get(0).getX();
        ymin = ymax = vertices.get(0).getY();
        zmin = zmax = vertices.get(0).getZ();

        for (int i = 1; i < n; i++) {
            if (vertices.get(i).getX() < xmin) xmin = vertices.get(i).getX();
            if (vertices.get(i).getY() < ymin) ymin = vertices.get(i).getY();
            if (vertices.get(i).getZ() < zmin) zmin = vertices.get(i).getZ();
            if (vertices.get(i).getX() > xmax) xmax = vertices.get(i).getX();
            if (vertices.get(i).getY() > ymax) ymax = vertices.get(i).getY();
            if (vertices.get(i).getZ() > zmax) zmax = vertices.get(i).getZ();
        }

        this.x0 = xmin;
        this.x1 = xmax;
        this.y0 = ymin;
        this.y1 = ymax;
        this.z0 = zmin;
        this.z1 = zmax;
    }

    private void setConvexFaces(Polyhedron polyhedron) {
        List<Polygon> polygons = new ArrayList<>();
        List<Plane> facePlanes = new ArrayList<>();
        List<List<Integer>> faceVertexIndex = new ArrayList<>();
        List<Plane> planes = new ArrayList<>();
        List<Vertex> vertices = polyhedron.getVertices();
        int numberOfFaces;
        double maxError = this.maxDist;
        int n = polyhedron.getVertexCount();

        for (int i = 0; i < n; i++) {
            Vertex v1 = vertices.get(i);
            for (int j = i + 1; j < n; j++) {
                Vertex v2 = vertices.get(j);
                for (int k = j + 1; k < n; k++) {
                    Vertex v3 = vertices.get(k);

                    Plane trianglePlane = new Plane(v1, v2, v3);
                    int onLeftCount = 0;
                    int onRightCount = 0;
                    List<Integer> planeIndices = new ArrayList<>();

                    for (int l = 0; l < n; l++) {
                        if (l != i && l != j && l != k) {
                            Vertex vertex = vertices.get(l);
                            double distance = Plane.multiple(vertex, trianglePlane);

                            if (Math.abs(distance) < maxError) {
                                planeIndices.add(l);
                            } else {
                                if (distance < 0) {
                                    onLeftCount++;
                                } else {
                                    onRightCount++;
                                }
                            }
                        }
                    }

                    if (onLeftCount == 0 || onRightCount == 0) {
                        List<Integer> integers = new ArrayList<>();

                        integers.add(i);
                        integers.add(j);
                        integers.add(k);

                        int m = planeIndices.size();

                        if (m > 0) {
                            integers.addAll(planeIndices);
                        }

                        if (!LogicUtils.containsList(faceVertexIndex, integers)) {
                            faceVertexIndex.add(integers);

                            if (onRightCount == 0) {
                                planes.add(trianglePlane);
                            } else {
                                planes.add(Plane.negative(trianglePlane));
                            }
                        }
                    } else {
                        // TODO
                    }
                }
            }
        }

        numberOfFaces = faceVertexIndex.size();

        for (int i = 0; i < numberOfFaces; i++) {
            facePlanes.add(new Plane(planes.get(i).getA(), planes.get(i).getB(), planes.get(i).getC(), planes.get(i).getD()));
            List<Vertex> gp = new ArrayList<>();
            List<Integer> vi = new ArrayList<>();
            int count = faceVertexIndex.get(i).size();
            for (int j = 0; j < count; j++) {
                vi.add(faceVertexIndex.get(i).get(j));
                gp.add(new Vertex(vertices.get(vi.get(j)).getX(),
                        vertices.get(vi.get(j)).getY(),
                        vertices.get(vi.get(j)).getZ()));
            }

            polygons.add(new Polygon(gp, vi));
        }

        this.polygons = polygons;
        this.planes = facePlanes;
        this.faceCount = numberOfFaces;
    }

    public double getMaxDist() {
        return maxDist;
    }

    public double getMeasurementError() {
        return measurementError;
    }

    public Polyhedron getPolyhedron() {
        return polyhedron;
    }

    public BoundingBox getBoundingBox() {
        return new BoundingBox(x0, y0, z0, x1, y1, z1);
    }

    public List<Location> getLocations(World world) {
        return polyhedron.getLocations(world);
    }

}


