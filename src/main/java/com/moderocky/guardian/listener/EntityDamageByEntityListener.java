package com.moderocky.guardian.listener;

import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.api.GuardianAPI;
import com.moderocky.guardian.api.Zone;
import com.moderocky.mask.template.CompleteListener;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author Moderocky
 * @version 1.0.0
 */
public class EntityDamageByEntityListener implements CompleteListener {

    private final @NotNull GuardianAPI api = Guardian.getApi();

    @EventHandler(priority = EventPriority.LOW)
    public void onRegion(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        Location location = event.getEntity().getLocation();
        Entity entity = event.getDamager();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (event.getEntity() instanceof Player) {
                for (Zone zone : api.getZones(location)) {
                    if (!zone.canInteract(location, "damage_players", player)) {
                        event.setCancelled(true);
                        api.denyEvent(player);
                        return;
                    }
                }
            } else {
                for (Zone zone : api.getZones(location)) {
                    if (!zone.canInteract(location, "damage_entities", player)) {
                        event.setCancelled(true);
                        api.denyEvent(player);
                        return;
                    }
                }
            }
        }
    }
}
