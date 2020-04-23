package com.moderocky.guardian.listener;

import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.api.EventMethRef;
import com.moderocky.guardian.api.GuardianAPI;
import com.moderocky.guardian.api.Zone;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BlanketUncaughtListener implements Listener {

    private final @NotNull GuardianAPI api = Guardian.getApi();

    @EventHandler
    public <Z extends Event> void onEvent(Z event) {
        if (!(event instanceof Cancellable)) return;
        Cancellable cancellable = (Cancellable) event;
        if (cancellable.isCancelled()) return;
        EventMethRef ref = api.getMethRef(event.getClass());
        if (ref == null) return;
        String flag = ref.getFlag();
        boolean cache = ref.hashCache();
        try {
            Method method = event.getClass().getMethod("getLocation");
            Location location = (Location) method.invoke(event);
            if (event instanceof PlayerEvent) {
                Player player = ((PlayerEvent) event).getPlayer();
                String hache = player.hashCode() + ref.hashKey() + location.getBlockX() + "0" + location.getBlockY() + "0" + location.getBlockZ();
                Boolean boo = api.getCachedResult(hache);
                if (boo != null && cache) {
                    cancellable.setCancelled(boo);
                } else {
                    for (Zone zone : api.getZones(location)) {
                        if (!zone.canInteract(location, flag, player)) {
                            cancellable.setCancelled(true);
                            if (cache)
                                api.addCachedResult(hache, true);
                            api.denyEvent(player);
                            return;
                        }
                    }
                    if (cache)
                        api.addCachedResult(hache, false);
                }
            } else {
                String hache = ref.hashKey() + location.getBlockX() + "0" + location.getBlockY() + "0" + location.getBlockZ();
                Boolean boo = api.getCachedResult(hache);
                if (boo != null && cache) {
                    cancellable.setCancelled(boo);
                } else {
                    for (Zone zone : api.getZones(location)) {
                        if (!zone.canInteract(location, flag)) {
                            cancellable.setCancelled(true);
                            if (cache)
                                api.addCachedResult(hache, true);
                            return;
                        }
                    }
                    if (cache)
                        api.addCachedResult(hache, false);
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return;
        }
    }

}
