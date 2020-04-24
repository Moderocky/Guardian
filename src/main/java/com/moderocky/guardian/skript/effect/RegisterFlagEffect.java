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
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@Name("Register Guardian Flag")
@Description({
        "Register a new Guardian flag. ",
        "Flags used by Guardian are togglable protections for zones. ",
        "You will have to supplement the flag behaviour for yourself. "
})
@Examples({
        "register new flag \"my_cool_flag\"",
        "register new flag \"no_guns_allowed\""
})
@Since("1.0.4")
public class RegisterFlagEffect extends Effect {

    static {
        Skript.registerEffect(RegisterFlagEffect.class,
                "register [a] new [guardian] flag %string%");
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
    protected void execute(@NotNull Event event) {
        if (stringExpression == null) return;
        String id = stringExpression.getSingle(event);
        if (id == null) return;
        id = id
                .replace(" ", "_")
                .toLowerCase()
                .replaceAll("([^a-z0-9_-]+)", "_");
        while (id.contains("__")) id = id.replace("__", "_");
        final GuardianAPI api = Guardian.getApi();
        if (api.getFlags().contains(id)) return;
        api.addFlag(id, PermissionDefault.OP);
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        String string = "<none>";
        if (stringExpression != null)
            string = stringExpression.toString(event, debug);
        return "register new flag " + string;
    }

}
