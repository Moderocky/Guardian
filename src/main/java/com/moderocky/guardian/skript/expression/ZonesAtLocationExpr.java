package com.moderocky.guardian.skript.expression;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.api.GuardianAPI;
import com.moderocky.guardian.api.Zone;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;


@Name("Zones at Location")
@Description({
        "Returns all Guardian zones at the location/entity/block. ",
        "This might be useful for testing what protections should be applied (if you are using custom flags.) ",
        "These zones are returned as strings, bearing the full ID.",
        "In most cases, this will be 'guardian:zone_id_here' - to preserve compatibility."
})
@Examples({
        "set {_zones::*} to the guardian zones at player",
        "set {_zones::*} to zones at target block",
        "set {_zones::*} to all zones at {_loc}"
})
@Since("1.0.4")
@SuppressWarnings("unused")
public class ZonesAtLocationExpr extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ZonesAtLocationExpr.class, String.class, ExpressionType.PROPERTY,
                "([the|all]) [guardian] zones at %location%");
    }

    @SuppressWarnings("null")
    private Expression<Location> locationExpression;

    @SuppressWarnings({"unchecked", "null"})
    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        locationExpression = (Expression<Location>) exprs[0];
        return true;
    }

    @Override
    protected String[] get(@NotNull Event event) {
        Location location = locationExpression.getSingle(event);
        if (location == null) return new String[0];
        GuardianAPI api = Guardian.getApi();
        List<Zone> zones = api.getZones(location);
        if (zones.isEmpty()) return new String[0];
        List<String> strings = new ArrayList<>();
        zones.forEach(zone -> strings.add(zone.getKey().toString()));
        return strings.toArray(new String[0]);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public @NotNull Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        String string = "<none>";
        if (locationExpression != null)
            string = locationExpression.toString(event, debug);
        return "zones at " + string;
    }
}
