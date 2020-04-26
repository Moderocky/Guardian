package com.moderocky.guardian.logic.ascendancy;

import com.moderocky.guardian.logic.shape.Dion;
import com.moderocky.guardian.logic.shape.Polygon;
import org.jetbrains.annotations.NotNull;

public interface IOrder2 extends IOrder1 {

    boolean contains(@NotNull Dion dion);

    Polygon[] polygons();

}
