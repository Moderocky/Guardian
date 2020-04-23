package com.moderocky.guardian.listener;

import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.api.GuardianAPI;
import com.moderocky.guardian.api.Zone;
import com.moderocky.guardian.config.GuardianConfig;
import com.moderocky.mask.template.CompleteListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

/**
 * @author Moderocky
 * @version 1.0.0
 */
public class PlayerInteractListener implements CompleteListener {

    private final @NotNull GuardianAPI api = Guardian.getApi();
    private final @NotNull GuardianConfig config = Guardian.getInstance().getGuardianConfig();

    @EventHandler
    public void onPlayerWandInteract(PlayerInteractEvent event) {
        if (!config.enableWand) return;
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        if (event.isCancelled()) return;
        if (block == null) return;
        if (event.getItem() == null) return;
        ItemStack itemStack = event.getItem();
        if (!api.isWand(itemStack)) return;
        event.setCancelled(true);

        Location location = block.getLocation().add(0.5, 0.5, 0.5).add(event.getBlockFace().getDirection().normalize().multiply(0.5));
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            player.getPersistentDataContainer().set(Guardian.getNamespacedKey("wand_pos_1"), PersistentDataType.STRING, api.serialisePosition(block.getLocation()));
            location.getWorld().spawnParticle(Particle.BLOCK_DUST, location, 12, Material.REDSTONE_BLOCK.createBlockData());
            player.sendActionBar(config.setPosition.replace("%s", "1"));
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            player.getPersistentDataContainer().set(Guardian.getNamespacedKey("wand_pos_2"), PersistentDataType.STRING, api.serialisePosition(block.getLocation()));
            location.getWorld().spawnParticle(Particle.BLOCK_DUST, location, 12, Material.LAPIS_BLOCK.createBlockData());
            player.sendActionBar(config.setPosition.replace("%s", "2"));
        }
        api.displayBox(player);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onRegion(PlayerInteractEvent event) {
        if (event.isCancelled()) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        Location location = block.getLocation();
        Player player = event.getPlayer();
//        String hache = player.hashCode() + "0x15" + block.hashCode();
//        Boolean boo = api.getCachedResult(hache);
        // This event can't be effectively cached.
        for (Zone zone : api.getZones(location)) {
            if (!zone.canInteract(location, "interact_with_blocks", player)) {
                event.setCancelled(true);
                api.denyEvent(player);
                return;
            }
        }
    }

}
