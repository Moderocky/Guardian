package com.moderocky.guardian.skript.expression;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.api.CuboidalZone;
import com.moderocky.guardian.api.GuardianAPI;
import com.moderocky.guardian.api.PolyhedralZone;
import com.moderocky.guardian.api.Zone;
import com.moderocky.guardian.logic.LogicUtils;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Name("Vertices (Corners) of Zone")
@Description({
        "Returns the vertices (corners) of a Guardian zone. ",
        "For cubic zones, this will be the corners. ",
        "For polyhedral zones, this will be the vertices. ",
        "Other zone types may not anything. "
})
@Examples({
        "set {_corners::*} to the corners of zone \"my_zone\""
})
@Since("1.0.5")
@SuppressWarnings("unused")
public class ZoneVerticesExpr extends SimpleExpression<Location> {

    static {
        Skript.registerExpression(ZoneVerticesExpr.class, Location.class, ExpressionType.PROPERTY,
                "[the] (vertices|corners) of [guardian] zone %string%", "[guardian] zone %string%'s (vertices|corners)");
    }

    @SuppressWarnings("null")
    private Expression<String> stringExpression;

    @SuppressWarnings({"unchecked", "null"})
    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        stringExpression = (Expression<String>) exprs[0];
        return true;
    }

    @Override
    protected Location[] get(@NotNull Event event) {
        String id = stringExpression.getSingle(event);
        if (id == null) return new Location[0];
        GuardianAPI api = Guardian.getApi();
        if (!api.exists(id)) return new Location[0];
        Zone zone = api.getZone(id);
        List<Location> locations = new ArrayList<>();
        if (zone instanceof CuboidalZone) {
            locations.addAll(LogicUtils.toLocations(LogicUtils.getVertices(((CuboidalZone) zone).getBoundingBox()), zone.getWorld()));
        } else if (zone instanceof PolyhedralZone) {
            locations.addAll(LogicUtils.toLocations(((PolyhedralZone) zone).getPolyhedron().getVertices(), zone.getWorld()));
        }
        return locations.toArray(new Location[0]);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public @NotNull Class<? extends Location> getReturnType() {
        return Location.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        String string = "<none>";
        if (stringExpression != null)
            string = stringExpression.toString(event, debug);
        return "vertices of zone " + string;
    }
}
