package com.moderocky.guardian.listener;

import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.api.GuardianAPI;
import com.moderocky.guardian.api.Zone;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author Moderocky
 * @version 1.0.0
 */
public class PlayerAttemptPickupItemListener implements Listener {

    private final @NotNull GuardianAPI api = Guardian.getApi();

    @EventHandler(priority = EventPriority.LOW)
    public void onRegion(EntityPickupItemEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player)) return;
        Entity entity = event.getEntity();
        Location location = event.getItem().getLocation();
        String hache = entity.hashCode() + "0x13" + location.getBlockX() + "0" + location.getBlockY() + "0" + location.getBlockZ();
        Boolean boo = api.getCachedResult(hache);
        if (boo != null) {
            event.setCancelled(boo);
        } else {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                for (Zone zone : api.getZones(location)) {
                    if (!zone.canInteract(location, "pick_up_items", player)) {
                        event.setCancelled(true);
                        api.addCachedResult(hache, true);
                        api.denyEvent(player);
                        return;
                    }
                }
            } else {
                for (Zone zone : api.getZones(location)) {
                    if (!zone.canInteract(location, "pick_up_items")) {
                        event.setCancelled(true);
                        api.addCachedResult(hache, true);
                        return;
                    }
                }
            }
            api.addCachedResult(hache, false);
        }
    }
}
