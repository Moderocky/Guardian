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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Name("Allowed Players of Zone")
@Description({
        "Accesses the allowed players of a Guardian zone. "
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
public class ZoneAccessorsExpr extends SimpleExpression<OfflinePlayer> {

    static {
        Skript.registerExpression(ZoneAccessorsExpr.class, OfflinePlayer.class, ExpressionType.PROPERTY,
                "[the] allowed players of [guardian] zone %string%", "[guardian] zone %string%'s allowed players");
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
        return CollectionUtils.array(OfflinePlayer[].class);
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
                if (players == null) break;
                zone.getAllowedPlayers().forEach(zone::removePlayer);
                for (OfflinePlayer player : players) {
                    if (!zone.isAllowed(player.getUniqueId())) zone.addPlayer(player.getUniqueId());
                }
                break;
            case ADD:
                if (players == null) break;
                for (OfflinePlayer player : players) {
                    if (!zone.isAllowed(player.getUniqueId())) zone.addPlayer(player.getUniqueId());
                }
                break;
            case DELETE:
                zone.clearPlayers();
                break;
            case REMOVE:
            case REMOVE_ALL:
                if (players == null) break;
                for (OfflinePlayer player : players) {
                    zone.removePlayer(player.getUniqueId());
                }
                break;
            case RESET:
                zone.clearPlayers();
                break;
        }
    }

    @Override
    public boolean isSingle() {
        return false;
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
        return "allowed players of zone " + string;
    }
}
