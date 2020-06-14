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
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;


@Name("All Guardian Zones")
@Description({
        "Returns all Guardian zones ",
        "This might be useful for listing. ",
        "These zones are returned as strings, bearing the full ID.",
        "In most cases, this will be 'guardian:zone_id_here' - to preserve compatibility."
})
@Examples({
        "set {_zones::*} to all guardian zones"
})
@Since("1.1.0")
@SuppressWarnings("unused")
public class ZonesExpr extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ZonesExpr.class, String.class, ExpressionType.PROPERTY,
                "all [guardian] zones");
    }

    @SuppressWarnings({"unchecked", "null"})
    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        return true;
    }

    @Override
    protected String[] get(@NotNull Event event) {
        GuardianAPI api = Guardian.getApi();
        List<Zone> zones = api.getZones();
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
        return "all guardian zones";
    }
}
