package com.moderocky.guardian.skript.condition;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.api.GuardianAPI;
import com.moderocky.guardian.api.Zone;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

@Name("Can Perform Action")
@Description({
        "Check if a given action can be performed at a location. "
})
@Examples({
        "add player to the allowed players of zone \"my_zone\"",
        "add player and target player to the allowed players of zone \"my_zone\"",
        "add {some_players::*} to guardian zone \"my_zone\"'s allowed players",
        "reset zone \"my_zone\"'s allowed players",
        "set guardian zone \"my_zone\"'s allowed players to {list_of_players::*}"
})
@Since("1.0.5")
@SuppressWarnings("unused")
public class CanPerformCond extends Condition {

    static {
        PropertyCondition.register(CanPerformCond.class, "can perform %string% at %location%", "players");
    }

    private Expression<String> stringExpression;
    private Expression<Location> locationExpression;
    private Expression<Player> playerExpression;

    @SuppressWarnings({"unchecked", "null"})
    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        playerExpression = (Expression<Player>) exprs[0];
        stringExpression = (Expression<String>) exprs[1];
        locationExpression = (Expression<Location>) exprs[2];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(@NotNull Event event) {
        GuardianAPI api = Guardian.getApi();
        return playerExpression.check(event, player -> {
            Location location = locationExpression.getSingle(event);
            String string = stringExpression.getSingle(event);
            for (Zone zone : api.getZones(location)) {
                assert location != null;
                assert string != null;
                if (!zone.canInteract(location, string, player)) return false;
            }
            return true;
        }, isNegated());
    }

    @Override
    public @NotNull String toString(Event event, boolean debug) {
        return PropertyCondition.toString(this, PropertyCondition.PropertyType.BE, event, debug, playerExpression,
                "can perform " + stringExpression.toString(event, debug) + " at " + locationExpression.toString(event, debug));
    }

}
