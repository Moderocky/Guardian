package com.moderocky.guardian.skript.effect;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.api.CuboidalZone;
import com.moderocky.guardian.api.GuardianAPI;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@Name("Create Zone")
@Description({
        "Creates a Guardian zone between the specified locations.",
        "Note: this will overlap any existing zones."
})
@Examples({
        "create zone \"my_cool_zone\" between {_corner1} and {_corner2}"
})
@Since("1.0.0")
public class CreateZoneEffect extends Effect {

    static {
        Skript.registerEffect(CreateZoneEffect.class, "create [guardian] zone %string% between %location% and %location%");
    }

    @SuppressWarnings("null")
    private Expression<String> stringExpression;
    private Expression<Location> locationExpression1;
    private Expression<Location> locationExpression2;

    @SuppressWarnings({"unchecked", "null"})
    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        stringExpression = (Expression<String>) exprs[0];
        locationExpression1 = (Expression<Location>) exprs[1];
        locationExpression2 = (Expression<Location>) exprs[2];
        return true;
    }

    @Override
    protected void execute(@NotNull Event event) {
        if (stringExpression == null || locationExpression1 == null || locationExpression2 == null) return;
        String id = stringExpression.getSingle(event);
        Location l1 = locationExpression1.getSingle(event);
        Location l2 = locationExpression2.getSingle(event);
        if (id == null || l1 == null || l2 == null) return;
        if (l1.getWorld() != l2.getWorld()) return;
        final GuardianAPI api = Guardian.getApi();
        if (api.exists(id)) return;
        CuboidalZone zone = CuboidalZone.createZone(id, l1, l2);
        api.registerZone(zone);
        api.scheduleSave();
        api.updateCache();
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        String string = "<none>";
        String l1 = "<none>";
        String l2 = "<none>";
        if (stringExpression != null)
            string = stringExpression.toString(event, debug);
        if (locationExpression1 != null)
            l1 = locationExpression1.toString(event, debug);
        if (locationExpression2 != null)
            l2 = locationExpression2.toString(event, debug);
        return "create zone " + string + " between " + l1 + " and " + l2;
    }

}
