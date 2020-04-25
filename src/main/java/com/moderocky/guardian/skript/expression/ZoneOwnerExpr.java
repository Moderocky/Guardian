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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Name("Owner of Zone")
@Description({
        "Accesses the owner of a Guardian zone. "
})
@Examples({
        "set the owner of zone \"my_zone\" to player",
        "set {_p} to zone \"my_zone\"'s owner",
        "delete zone \"my_zone\"'s owner"
})
@Since("1.0.5")
@SuppressWarnings("unused")
public class ZoneOwnerExpr extends SimpleExpression<OfflinePlayer> {

    static {
        Skript.registerExpression(ZoneOwnerExpr.class, OfflinePlayer.class, ExpressionType.PROPERTY,
                "[the] owner of [guardian] zone %string%", "[guardian] zone %string%'s owner");
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
    protected OfflinePlayer[] get(@NotNull Event event) {
        String id = stringExpression.getSingle(event);
        if (id == null) return new OfflinePlayer[0];
        GuardianAPI api = Guardian.getApi();
        if (!api.exists(id)) return new OfflinePlayer[0];
        Zone zone = api.getZone(id);
        List<OfflinePlayer> players = new ArrayList<>();
        zone.getAllowedPlayers().forEach(uuid -> players.add(Bukkit.getOfflinePlayer(uuid)));
        return players.toArray(new OfflinePlayer[0]);
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(final Changer.@NotNull ChangeMode mode) {
        switch (mode) {
            case SET:
            case DELETE:
                return CollectionUtils.array(OfflinePlayer.class, OfflinePlayer.class);
        }
        return null;
    }

    @Override
    public void change(final @NotNull Event event, final @Nullable Object[] objects, final Changer.@NotNull ChangeMode mode) throws UnsupportedOperationException {
        String id = stringExpression.getSingle(event);
        if (id == null) return;
        GuardianAPI api = Guardian.getApi();
        if (!api.exists(id)) return;
        Zone zone = api.getZone(id);
        OfflinePlayer[] players = objects == null ? null : Arrays.copyOf(objects, objects.length, OfflinePlayer[].class);

        switch (mode) {
            case SET:
                if (players == null || players.length == 0) break;
                if (zone instanceof CuboidalZone) {
                    ((CuboidalZone) zone).setOwner(players[0].getUniqueId());
                } else if (zone instanceof PolyhedralZone) {
                    ((PolyhedralZone) zone).setOwner(players[0].getUniqueId());
                }
                break;
            case ADD:
            case REMOVE:
            case REMOVE_ALL:
            case RESET:
                break;
            case DELETE:
                if (zone instanceof CuboidalZone) {
                    ((CuboidalZone) zone).setOwner(null);
                } else if (zone instanceof PolyhedralZone) {
                    ((PolyhedralZone) zone).setOwner(null);
                }
                break;
        }
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends OfflinePlayer> getReturnType() {
        return OfflinePlayer.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        String string = "<none>";
        if (stringExpression != null)
            string = stringExpression.toString(event, debug);
        return "owner of zone " + string;
    }
}
