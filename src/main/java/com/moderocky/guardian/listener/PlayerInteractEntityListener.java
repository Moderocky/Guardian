package com.moderocky.guardian.listener;

import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.api.GuardianAPI;
import com.moderocky.guardian.api.Zone;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author Moderocky
 * @version 1.0.0
 */
public class PlayerInteractEntityListener implements Listener {

    private final @NotNull GuardianAPI api = Guardian.getApi();

    @EventHandler(priority = EventPriority.LOW)
    public void onRegion(PlayerInteractEntityEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Location location = event.getRightClicked().getLocation();
        String hache = player.hashCode() + "0x14" + event.getRightClicked().hashCode() + location.getX() + "0" + location.getZ();
        Boolean boo = api.getCachedResult(hache);
        if (boo != null) {
            event.setCancelled(boo);
        } else {
            for (Zone zone : api.getZones(location)) {
                if (!zone.canInteract(location, "interact_with_entities", player)) {
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
