package com.moderocky.guardian.logic.ascendancy;

import com.moderocky.guardian.logic.shape.Vertex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface Polytope {

    int getOrder();

    default int getMinimumVertices() {
        return getOrder() + 1;
    }

    Vertex[] vertices();

    boolean contains(Polytope polytope);

    default boolean hasLockingPoint(@NotNull Polytope polytope) {
        for (Vertex vertex : polytope.vertices()) {
            for (Vertex vx : vertices()) {
                if (vertex.equals(vx)) return true;
            }
        }
        return false;
    }

    default Vertex[][] getLockingPoints(@NotNull Polytope polytope) {
        List<Vertex[]> vertices = new ArrayList<>();
        for (Vertex vertex : polytope.vertices()) {
            for (Vertex vx : vertices()) {
                if (vertex.equals(vx)) vertices.add(new Vertex[]{vx, vertex});
            }
        }
        return vertices.toArray(new Vertex[0][]);
    }

}
