package com.moderocky.guardian.listener;

import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.api.GuardianAPI;
import com.moderocky.guardian.api.Zone;
import com.moderocky.guardian.config.GuardianConfig;
import com.moderocky.mask.template.CompleteListener;
import org.bukkit.Location;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * @author Moderocky
 * @version 1.0.0
 */
public class InventoryOpenListener implements CompleteListener {

    private final @NotNull GuardianAPI api = Guardian.getApi();
    private final @NotNull GuardianConfig config = Guardian.getInstance().getGuardianConfig();

    @EventHandler(priority = EventPriority.LOW)
    public void onRegion(InventoryOpenEvent event) {
        if (event.isCancelled()) return;
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof Container)) return;
        Player player = (Player) event.getPlayer();
        Location location = ((Container) holder).getLocation();
        String hache = player.hashCode() + "0x12" + holder.hashCode();
        Boolean boo = api.getCachedResult(hache);
        if (boo != null) {
            event.setCancelled(boo);
        } else {
            for (Zone zone : api.getZones(location)) {
                if (!zone.canInteract(location, "open_containers", player)) {
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
