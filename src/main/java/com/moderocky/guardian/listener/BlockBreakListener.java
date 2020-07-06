package com.moderocky.guardian.listener;

import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.api.GuardianAPI;
import com.moderocky.guardian.api.Zone;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author Moderocky
 * @version 1.0.0
 */
public class BlockBreakListener implements Listener {

    private final @NotNull GuardianAPI api = Guardian.getApi();

    @EventHandler(priority = EventPriority.LOW)
    public void onRegion(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        Location location = block.getLocation();
        Player player = event.getPlayer();
        String hache = player.hashCode() + "0x11" + block.hashCode();
        Boolean boo = api.getCachedResult(hache);
        if (boo != null) {
            event.setCancelled(boo);
        } else {
            for (Zone zone : api.getZones(location)) {
                if (!zone.canInteract(location, "break_blocks", player)) {
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
