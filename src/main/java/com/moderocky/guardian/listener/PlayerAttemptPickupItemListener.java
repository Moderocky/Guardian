package com.moderocky.guardian.listener;

import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.api.GuardianAPI;
import com.moderocky.guardian.api.Zone;
import com.moderocky.mask.template.CompleteListener;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author Moderocky
 * @version 1.0.0
 */
public class PlayerAttemptPickupItemListener implements CompleteListener {

    private final @NotNull GuardianAPI api = Guardian.getApi();

    @EventHandler(priority = EventPriority.LOW)
    public void onRegion(PlayerAttemptPickupItemEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Location location = event.getItem().getLocation();
        String hache = player.hashCode() + "0x13" + location.getBlockX() + "0" + location.getBlockY() + "0" + location.getBlockZ();
        Boolean boo = api.getCachedResult(hache);
        if (boo != null) {
            event.setCancelled(boo);
        } else {
            for (Zone zone : api.getZones(location)) {
                if (!zone.canInteract(location, "pick_up_items", player)) {
                    event.setCancelled(true);
                    api.addCachedResult(hache, true);
                    api.denyEvent(player);
                    return;
                }
            }
            api.addCachedResult(hache, false);
        }
    }
}
