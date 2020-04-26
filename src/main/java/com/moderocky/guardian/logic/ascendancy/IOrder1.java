package com.moderocky.guardian.logic.ascendancy;

import com.moderocky.guardian.logic.shape.Dion;
import com.moderocky.guardian.logic.shape.Vertex;
import org.jetbrains.annotations.NotNull;

public interface IOrder1 extends IOrder0 {

    boolean contains(@NotNull Vertex vertex);

    Dion[] dions();

}
