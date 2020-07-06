package com.moderocky.guardian.listener;

import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.api.GuardianAPI;
import com.moderocky.guardian.api.Zone;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * @author Moderocky
 * @version 1.0.0
 */
public class EntityExplodeListener implements Listener {

    private final @NotNull GuardianAPI api = Guardian.getApi();

    @EventHandler(priority = EventPriority.LOW)
    public void onRegion(EntityExplodeEvent event) {
        if (event.isCancelled()) return;
        if (event.getEntityType() == EntityType.PLAYER) return;
        new ArrayList<>(event.blockList()).forEach(block -> {
            Location location = block.getLocation();
            for (Zone zone : api.getZones(location)) {
                if (!zone.canInteract(location, "mob_griefing")) {
//                    event.setCancelled(true);
                    event.blockList().remove(block);
                    break;
                }
            }
        });
    }
}
