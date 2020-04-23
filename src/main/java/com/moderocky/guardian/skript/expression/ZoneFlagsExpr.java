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
import com.moderocky.guardian.api.GuardianAPI;
import com.moderocky.guardian.api.Zone;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;


@Name("Flags of Zone")
@Description({
        "Accesses the flags of a Guardian zone. ",
        "Flags used by Guardian are togglable protections for the zone. "
})
@Examples({
        "add \"prevent_teleport\" to the flags of zone \"my_zone\"",
        "add \"break_blocks\" and \"place_blocks\" to the flags of zone \"my_zone\"",
        "add {list_of_flags::*} to guardian zone \"my_zone\"'s flags",
        "reset zone \"my_zone\"'s flags",
        "set guardian zone \"my_zone\"'s flags to {list_of_flags::*}"
})
@Since("1.0.0")
@SuppressWarnings("unused")
public class ZoneFlagsExpr extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ZoneFlagsExpr.class, String.class, ExpressionType.PROPERTY,
                "[the] flags of [guardian] zone %string%", "[guardian] zone %string%'s flags");
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
    protected String[] get(@NotNull Event event) {
        String id = stringExpression.getSingle(event);
        if (id == null) return new String[0];
        GuardianAPI api = Guardian.getApi();
        if (!api.exists(id)) return new String[0];
        Zone zone = api.getZone(id);
        return zone.getFlags().toArray(new String[0]);
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(final Changer.ChangeMode mode) {
        switch (mode) {
            case REMOVE:
                return CollectionUtils.array(String[].class);
            case REMOVE_ALL:
                return CollectionUtils.array(String[].class);
            case DELETE:
            case SET:
                return CollectionUtils.array(String[].class);
            case ADD:
                return CollectionUtils.array(String[].class);
            case RESET:
            default:
                return null;
        }
    }

    @Override
    public void change(final @NotNull Event event, final @Nullable Object[] objects, final Changer.@NotNull ChangeMode mode) throws UnsupportedOperationException {
        String id = stringExpression.getSingle(event);
        if (id == null) return;
        GuardianAPI api = Guardian.getApi();
        if (!api.exists(id)) return;
        Zone zone = api.getZone(id);
        String[] strings = objects == null ? null : Arrays.copyOf(objects, objects.length, String[].class);

        switch (mode) {
            case SET:
                assert strings != null;
                zone.getFlags().forEach(zone::removeFlag);
                for (String line : strings) {
                    if (api.isFlag(line)) zone.addFlag(line);
                }
                break;
            case ADD:
                assert strings != null;
                for (String line : strings) {
                    if (api.isFlag(line)) zone.addFlag(line);
                }
                break;
            case DELETE:
                zone.getFlags().forEach(zone::removeFlag);
            case REMOVE:
                assert strings != null;
                for (String string : strings) {
                    zone.removeFlag(string);
                }
                break;
            case REMOVE_ALL:
                assert strings != null;
                for (String string : strings) {
                    zone.removeFlag(string);
                }
                break;
            case RESET:
                zone.getFlags().forEach(zone::removeFlag);
        }
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
        if (stringExpression != null)
            string = stringExpression.toString(event, debug);
        return "flags of zone " + string;
    }
}
