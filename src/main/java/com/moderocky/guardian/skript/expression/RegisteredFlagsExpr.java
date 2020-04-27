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
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;


@Name("Registered Guardian Flags")
@Description({
        "Returns all registered Guardian flags. ",
        "Flags used by Guardian are togglable protections for zones. "
})
@Examples({
        "set {_flags::*} to all registered flags",
        "add all registered flags to the flags of zone \"my_zone\""
})
@Since("1.0.4")
@SuppressWarnings("unused")
public class RegisteredFlagsExpr extends SimpleExpression<String> {

    static {
        Skript.registerExpression(RegisteredFlagsExpr.class, String.class, ExpressionType.PROPERTY,
                "([the|all]) registered [guardian] flags");
    }

    @SuppressWarnings({"null"})
    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        return true;
    }

    @Override
    protected String[] get(@NotNull Event event) {
        GuardianAPI api = Guardian.getApi();
        return api.getProtectionFlags().toArray(new String[0]);
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
        return "all registered flags";
    }
}
