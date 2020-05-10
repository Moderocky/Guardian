package com.moderocky.guardian.listener;

import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.api.GuardianAPI;
import com.moderocky.guardian.api.Zone;
import com.moderocky.mask.template.CompleteListener;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * @author Moderocky
 * @version 1.0.0
 */
public class PlayerMoveListener implements CompleteListener {

    private final @NotNull GuardianAPI api = Guardian.getApi();

    @EventHandler(priority = EventPriority.LOW)
    public void onRegion(PlayerMoveEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        entry:
        {
            Location location = event.getTo();
            String hache = player.hashCode() + "0x21" + location.getBlockX() + "0" + location.getBlockY() + "0" + location.getBlockZ() + "0";
            Boolean boo = api.getCachedResult(hache);
            if (boo != null) {
                event.setCancelled(boo);
            } else {
                for (Zone zone : api.getZones(location)) {
                    if (!zone.canInteract(location, "prevent_entry", player)) {
                        event.setCancelled(true);
                        api.addCachedResult(hache, true);
                        api.denyEvent(player);
                        break entry;
                    }
                }
                api.addCachedResult(hache, false);
            }
        }
        exit:
        {
            Location to = event.getTo();
            Location location = event.getFrom();
            String hache = player.hashCode() + "0x22" + location.getBlockX() + "0" + location.getBlockY() + "0" + location.getBlockZ() + "0" + to.getBlockX() + "0" + to.getBlockY() + "0" + to.getBlockZ();
            Boolean boo = api.getCachedResult(hache);
            if (boo != null) {
                event.setCancelled(boo);
            } else {
                for (Zone zone : api.getZones(location)) {
                    if (api.getZones(to).contains(zone) && !zone.canInteract(location, "prevent_exit", player)) {
                        event.setCancelled(true);
                        api.addCachedResult(hache, true);
                        api.denyEvent(player);
                        break exit;
                    }
                }
                api.addCachedResult(hache, false);
            }
        }
    }

}
