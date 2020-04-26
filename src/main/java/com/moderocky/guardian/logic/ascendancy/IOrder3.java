package com.moderocky.guardian.logic.ascendancy;

import com.moderocky.guardian.logic.shape.Polygon;
import com.moderocky.guardian.logic.shape.Polyhedron;
import org.jetbrains.annotations.NotNull;

public interface IOrder3 extends IOrder2 {

    boolean contains(@NotNull Polygon polygon);

    Polyhedron[] polyhedrons();

}
