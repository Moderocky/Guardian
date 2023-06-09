package com.moderocky.guardian.listener;

import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.api.GuardianAPI;
import com.moderocky.guardian.api.Zone;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Moderocky
 * @version 1.0.0
 */
public class PlayerMoveListener implements Listener {

    private final @NotNull GuardianAPI api = Guardian.getApi();

    @EventHandler(priority = EventPriority.LOW)
    public void onRegion(PlayerMoveEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Location to = event.getTo();
        Location from = event.getFrom();
        if (!api.requiresMoveCheck(to) && !api.requiresMoveCheck(from)) return;
        List<Zone> zTo = api.getZones(to);
        List<Zone> zFrom = api.getZones(from);
        entry:
        {
            if (zTo.equals(zFrom)) break entry;
            for (Zone zone : zTo) {
                if (!zFrom.contains(zone) && !zone.canInteract(to, "prevent_entry", player)) {
                    event.setCancelled(true);
                    api.denyEvent(player);
                    break entry;
                }
            }
        }
        exit:
        {
            if (zTo.equals(zFrom)) break exit;
            for (Zone zone : zFrom) {
                if (!zTo.contains(zone) && !zone.canInteract(from, "prevent_exit", player)) {
                    event.setCancelled(true);
                    api.denyEvent(player);
                    break exit;
                }
            }
        }
    }

}
