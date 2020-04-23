package com.moderocky.guardian.api;

import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.config.GuardianConfig;
import com.moderocky.guardian.listener.BlanketUncaughtListener;
import com.moderocky.guardian.util.ParticleUtils;
import com.moderocky.mask.annotation.DoNotInstantiate;
import com.moderocky.mask.annotation.Internal;
import com.moderocky.mask.internal.utility.FileManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class GuardianAPI {

    private final @NotNull HashMap<NamespacedKey, Zone> zoneMap = new HashMap<>();
    private final @NotNull HashMap<World, List<NamespacedKey>> worldCache = new HashMap<>();
    private final @NotNull HashMap<WorldDistrict, List<NamespacedKey>> worldDistrictCache = new HashMap<>();
    private final @NotNull List<String> flags = new ArrayList<>();
    private final @NotNull HashMap<String, Boolean> eventResultCache = new HashMap<>();
    private final @NotNull HashMap<Class<Event>, EventMethRef> blindEventMap = new HashMap<>();

    @Internal
    @DoNotInstantiate
    public GuardianAPI() {
    }

    public void init() {
        GuardianConfig config = Guardian.getInstance().getGuardianConfig();

        flags.clear();
        reload();

        addFlag("mob_griefing", config.allowBasicFlags);
        addFlag("mob_spawning", config.allowBasicFlags);
        addFlag("break_blocks", config.allowBasicFlags);
        addFlag("place_blocks", config.allowBasicFlags);
        addFlag("damage_players", config.allowBasicFlags);
        addFlag("damage_entities", config.allowBasicFlags);
        addFlag("damage_vehicles", config.allowBasicFlags);
        addFlag("open_containers", config.allowBasicFlags);
        addFlag("pick_up_items", config.allowBasicFlags);
        addFlag("interact_with_blocks", config.allowBasicFlags);
        addFlag("interact_with_entities", config.allowBasicFlags);
        addFlag("prevent_tree_growth", config.allowBasicFlags);
        addFlag("prevent_commands", config.allowSpecialFlags);
        addFlag("prevent_chat", config.allowSpecialFlags);
        addFlag("prevent_teleport", config.allowSpecialFlags);

        Bukkit.getScheduler().runTaskTimerAsynchronously(Guardian.getInstance(), this::save, 300, 2000);
        Bukkit.getScheduler().runTaskTimerAsynchronously(Guardian.getInstance(), this::updateCache, 300, 200);

    }

    public void registerBlindEvent(EventMethRef methRef) {
        try {
            blindEventMap.put(methRef.getEventClass(), methRef);
            Class<Event> eventClass = methRef.getEventClass();
            Method method = eventClass.getDeclaredMethod("getHandlerList");
            method.setAccessible(true);
            HandlerList list = (HandlerList) method.invoke(null);
            for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry : Guardian.getInstance().getPluginLoader().createRegisteredListeners(new BlanketUncaughtListener(), Guardian.getInstance()).entrySet()) {
                list.registerAll(entry.getValue());
            }
        } catch (Throwable ignore) {}
    }

    public EventMethRef getMethRef(Class<? extends Event> eventClass) {
        return blindEventMap.get(eventClass);
    }

    public void updateCache() {
        eventResultCache.clear();
    }

    public Boolean getCachedResult(String hashedEvent) {
        return eventResultCache.get(hashedEvent);
    }

    public void addCachedResult(String hashedEvent, boolean isCancelled) {
        eventResultCache.put(hashedEvent, isCancelled);
    }

    /**
     * Used to register a protection flag.
     * It will be usable by all players by default.
     * <p>
     * You can annul these permissions.
     *
     * @param flag The flag id
     */
    public void addFlag(String flag) {
        flags.add(flag);
        Bukkit.getPluginManager().addPermission(new Permission("guardian.flag." + flag, PermissionDefault.TRUE));
    }

    /**
     * Used to register a protection flag.
     *
     * @param flag The flag id
     * @param perm The permission default
     */
    public void addFlag(String flag, PermissionDefault perm) {
        flags.add(flag);
        Bukkit.getPluginManager().addPermission(new Permission("guardian.flag." + flag, perm));
    }

    public boolean isFlag(String flag) {
        return flags.contains(flag);
    }

    public @NotNull List<String> getFlags() {
        return new ArrayList<>(flags);
    }

    public Location getWandPosition(Player player, int i) {
        int pos = Math.max(1, Math.min(2, i));
        String string = player.getPersistentDataContainer().get(Guardian.getNamespacedKey("wand_pos_" + pos), PersistentDataType.STRING);
        if (string == null) return null;
        try {
            return deserialisePosition(string);
        } catch (Throwable throwable) {
            return null;
        }
    }

    public void denyEvent(Player player) {
        player.sendActionBar("You cannot interact with this zone!");
    }

    public void displayBox(Player player) {
        Location l1 = getWandPosition(player, 1);
        Location l2 = getWandPosition(player, 2);
        if (l1 == null || l2 == null || l1.getWorld() != l2.getWorld() || l1.distance(l2) > 128) return;
        double d = Math.max(0.25, Math.min(1, (l1.distance(l2) / 128 - l1.distance(l2))));
        ParticleUtils.drawBox(Particle.END_ROD, null, BoundingBox.of(l1, l2), l1.getWorld(), d);
        highlightBlock(l1.getBlock(), Particle.FALLING_DUST, Material.REDSTONE_BLOCK.createBlockData());
        highlightBlock(l2.getBlock(), Particle.FALLING_DUST, Material.LAPIS_BLOCK.createBlockData());
    }

    public void displayBox(BoundingBox boundingBox, World world, Player player) {
//        ParticleUtils.drawHash(Particle.END_ROD, 0.25, boundingBox, world);
        ParticleUtils.drawBox(Particle.END_ROD, null, boundingBox, world, 0.25);
    }

    public void highlightBlock(Block block, Particle particle, @Nullable Object data) {
        BoundingBox box = BoundingBox.of(block.getLocation(), block.getLocation());
        ParticleUtils.drawBox(particle, data, box, block.getWorld(), 0.15);
    }

    public String serialisePosition(Location location) {
        return location.getWorld().getName() + ":" + location.getX() + ":" + location.getY() + ":" + location.getZ();
    }

    public Location deserialisePosition(String string) {
        if (string == null || string.isEmpty()) throw new IllegalArgumentException();
        String[] parts = string.split(":");
        if (parts.length == 4) {
            World w = Bukkit.getServer().getWorld(parts[0]);
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            return new Location(w, x, y, z);
        }
        throw new IllegalArgumentException();
    }

    public boolean canCreateZone(Zone zone, Player player) {
        for (NamespacedKey nearbyZone : getNearbyZones(zone.getDistrict())) {
            if (zoneMap.get(nearbyZone).overlaps(zone) && !zoneMap.get(nearbyZone).canEdit(player.getUniqueId()))
                return false;
        }
        return true;
    }

    public List<NamespacedKey> getZoneKeys() {
        return new ArrayList<>(zoneMap.keySet());
    }

    public List<Zone> getZones() {
        return new ArrayList<>(zoneMap.values());
    }

    public void registerZone(Zone zone) {
        zoneMap.put(zone.getKey(), zone);
        addCache(zone);
    }

    public void removeZone(Zone zone) {
        zoneMap.remove(zone.getKey());
        removeCache(zone);
    }

    public boolean exists(NamespacedKey key) {
        return zoneMap.containsKey(key);
    }

    public List<NamespacedKey> getZones(World world) {
        return new ArrayList<>(worldCache.getOrDefault(world, new ArrayList<>()));
    }

    public List<NamespacedKey> getNearbyZones(WorldDistrict district) {
        return new ArrayList<>(worldDistrictCache.getOrDefault(district, new ArrayList<>()));
    }

    public List<Zone> getZones(Location location) {
        List<Zone> list = new ArrayList<>();
        for (Zone zone : getZones()) {
            if (location.distance(zone.getLocation()) > zone.getRadius()) continue;
            if (zone.isInside(location)) list.add(zone);
        }
        return list;
    }

    @SuppressWarnings("deprecation")
    public boolean exists(String namespace, String key) {
        return zoneMap.containsKey(new NamespacedKey(namespace, key));
    }

    @SuppressWarnings("deprecation")
    public boolean exists(String key) {
        if (key.contains(":")) return zoneMap.containsKey(new NamespacedKey(key.split(":")[0], key.split(":")[1]));
        for (NamespacedKey namespacedKey : zoneMap.keySet()) {
            if (namespacedKey.getKey().equalsIgnoreCase(key)) return true;
        }
        return false;
    }

    public Zone getZone(NamespacedKey key) {
        return zoneMap.get(key);
    }

    @SuppressWarnings("deprecation")
    public Zone getZone(String namespace, String key) {
        return zoneMap.get(new NamespacedKey(namespace, key));
    }

    @SuppressWarnings("deprecation")
    public Zone getZone(String key) {
        if (key.contains(":")) return zoneMap.get(new NamespacedKey(key.split(":")[0], key.split(":")[1]));
        for (NamespacedKey namespacedKey : zoneMap.keySet()) {
            if (namespacedKey.getKey().equalsIgnoreCase(key)) return zoneMap.get(namespacedKey);
        }
        return null;
    }

    private void clearCaches() {
        worldDistrictCache.clear();
        worldCache.clear();
    }

    private void addCache(Zone zone) {
        {
            List<NamespacedKey> keys = worldCache.getOrDefault(zone.getWorld(), new ArrayList<>());
            if (!keys.contains(zone.getKey())) keys.add(zone.getKey());
            if (worldCache.containsKey(zone.getWorld())) worldCache.replace(zone.getWorld(), keys);
            else worldCache.put(zone.getWorld(), keys);
        }
        {
            List<NamespacedKey> keys = worldDistrictCache.getOrDefault(zone.getDistrict(), new ArrayList<>());
            if (!keys.contains(zone.getKey())) keys.add(zone.getKey());
            if (worldDistrictCache.containsKey(zone.getDistrict()))
                worldDistrictCache.replace(zone.getDistrict(), keys);
            else worldDistrictCache.put(zone.getDistrict(), keys);
        }
    }

    private void removeCache(Zone zone) {
        {
            List<NamespacedKey> keys = worldCache.getOrDefault(zone.getWorld(), new ArrayList<>());
            keys.remove(zone.getKey());
            if (worldCache.containsKey(zone.getWorld())) worldCache.replace(zone.getWorld(), keys);
        }
        {
            List<NamespacedKey> keys = worldDistrictCache.getOrDefault(zone.getDistrict(), new ArrayList<>());
            keys.remove(zone.getKey());
            if (worldDistrictCache.containsKey(zone.getDistrict()))
                worldDistrictCache.replace(zone.getDistrict(), keys);
        }
    }

    public void scheduleSave() {
        Bukkit.getScheduler().runTaskLaterAsynchronously(Guardian.getInstance(), this::save, 80L);
//        save();
    }

    public void save() {
        File file = getStorageFile();
        FileManager.clear(file);
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        for (Zone zone : zoneMap.values()) {
            String key = zone.getKey().getNamespace() + "." + zone.getKey().getKey();
            configuration.set(key + ".class_loader", zone.getClass().getName());
            if (configuration.isConfigurationSection(key)) {
                ConfigurationSection section = configuration.getConfigurationSection(key);
                if (section != null) {
                    zone.save(section);
                    section.set("class_loader", zone.getClass().getName());
                    continue;
                }
            }
            ConfigurationSection section = configuration.createSection(key);
            zone.save(section);
        }
        FileManager.save(configuration, file);
    }

    @SuppressWarnings("all")
    public void load() {
        File file = getStorageFile();
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        List<String> namespaces = new ArrayList<>(configuration.getKeys(false));
        for (String namespace : namespaces) {
            List<String> keys = new ArrayList<>(configuration.getConfigurationSection(namespace).getKeys(false));
            for (String key : keys) {
                ConfigurationSection section = configuration.getConfigurationSection(namespace + "." + key);
                if (section == null) continue;
                try {
                    NamespacedKey namespacedKey = new NamespacedKey(namespace, key);
                    Class<? extends Zone> clarse = (Class<? extends Zone>) Class.forName(section.getString("class_loader"));
                    Constructor<? extends Zone> constructor = clarse.getConstructor(NamespacedKey.class, ConfigurationSection.class);
                    Zone zone = constructor.newInstance(namespacedKey, section);
                    zoneMap.put(namespacedKey, zone);
                    addCache(zone);
                } catch (Throwable ignore) {
                    ignore.printStackTrace();
                }
            }
        }
    }

    public void reload() {
        clearCaches();
        load();
    }

    private File getStorageFile() {
        File file = new File("plugins/Guardian/", "zone_storage.yml");
        FileManager.putIfAbsent(file);
        return file;
    }

}
