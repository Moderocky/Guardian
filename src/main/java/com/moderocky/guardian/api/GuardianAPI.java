package com.moderocky.guardian.api;

import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.config.GuardianConfig;
import com.moderocky.guardian.listener.BlanketUncaughtListener;
import com.moderocky.guardian.util.ParticleUtils;
import com.moderocky.mask.annotation.DoNotInstantiate;
import com.moderocky.mask.annotation.Internal;
import com.moderocky.mask.command.Commander;
import com.moderocky.mask.internal.utility.FileManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

@SuppressWarnings("unused")
public class GuardianAPI {

    private final @NotNull HashMap<NamespacedKey, Zone> zoneMap = new HashMap<>();
    private final @NotNull HashMap<World, List<NamespacedKey>> worldCache = new HashMap<>();
    private final @NotNull HashMap<WorldDistrict, List<NamespacedKey>> worldDistrictCache = new HashMap<>();
    private final @NotNull List<String> protectionFlags = new ArrayList<>();
    private final @NotNull HashMap<Chunk, Boolean> chunkMoveCheckCache = new HashMap<>();
    private final @NotNull HashMap<String, Boolean> eventResultCache = new HashMap<>();
    private final @NotNull HashMap<Block, List<Zone>> locationZoneCache = new HashMap<>();
    private final @NotNull HashMap<Class<Event>, EventMethRef> blindEventMap = new HashMap<>();

    private GuardianConfig config;

    @Internal
    @DoNotInstantiate
    public GuardianAPI() {
    }

    public void init() {
        config = Guardian.getInstance().getGuardianConfig();

        protectionFlags.clear();
        reload();

        addProtectionFlag("mob_griefing", config.allowBasicFlags);
        addProtectionFlag("mob_spawning", config.allowBasicFlags);
        addProtectionFlag("break_blocks", config.allowBasicFlags);
        addProtectionFlag("place_blocks", config.allowBasicFlags);
        addProtectionFlag("damage_players", config.allowBasicFlags);
        addProtectionFlag("damage_entities", config.allowBasicFlags);
        addProtectionFlag("damage_vehicles", config.allowBasicFlags);
        addProtectionFlag("open_containers", config.allowBasicFlags);
        addProtectionFlag("pick_up_items", config.allowBasicFlags);
        addProtectionFlag("interact_with_blocks", config.allowBasicFlags);
        addProtectionFlag("interact_with_entities", config.allowBasicFlags);
        addProtectionFlag("prevent_tree_growth", config.allowBasicFlags);
        if (config.checkEntryExit) {
            addProtectionFlag("prevent_entry", config.allowBasicFlags);
            addProtectionFlag("prevent_exit", config.allowBasicFlags);
        }
        addProtectionFlag("prevent_commands", config.allowSpecialFlags);
        addProtectionFlag("prevent_chat", config.allowSpecialFlags);
        addProtectionFlag("prevent_teleport", config.allowSpecialFlags);

        Bukkit.getScheduler().runTaskTimerAsynchronously(Guardian.getInstance(), this::save, 300, (config.saveDelay * 20));
        Bukkit.getScheduler().runTaskTimerAsynchronously(Guardian.getInstance(), this::updateCache, 300, (config.actionCacheResetDelay * 20));
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
        } catch (Throwable ignore) {
        }
    }

    public ItemStack getWand() {
        return config.getWand();
    }

    public ItemStack getPolywand() {
        return config.getPolywand();
    }

    public boolean isWand(ItemStack itemStack) {
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        NamespacedKey key = Guardian.getNamespacedKey("wand");
        Byte bi = container.get(key, PersistentDataType.BYTE);
        return bi != null && bi == 1;
    }

    public boolean isPolywand(ItemStack itemStack) {
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        NamespacedKey key = Guardian.getNamespacedKey("polywand");
        Byte bi = container.get(key, PersistentDataType.BYTE);
        return bi != null && bi == 1;
    }

    public EventMethRef getMethRef(Class<? extends Event> eventClass) {
        return blindEventMap.get(eventClass);
    }

    public void updateCache() {
        chunkMoveCheckCache.clear();
        eventResultCache.clear();
        locationZoneCache.clear();
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
    public void addProtectionFlag(String flag) {
        protectionFlags.add(flag);
        Bukkit.getPluginManager().addPermission(new Permission("guardian.flag." + flag, PermissionDefault.TRUE));
    }

    /**
     * Used to register a protection flag.
     *
     * @param flag The flag id
     * @param perm The permission default
     */
    public void addProtectionFlag(String flag, PermissionDefault perm) {
        protectionFlags.add(flag);
        Bukkit.getPluginManager().addPermission(new Permission("guardian.flag." + flag, perm));
    }

    public boolean isProtectionFlag(String flag) {
        return protectionFlags.contains(flag);
    }

    public @NotNull List<String> getProtectionFlags() {
        return new ArrayList<>(protectionFlags);
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

    public List<Location> getPolywandPositions(Player player) {
        String string = player.getPersistentDataContainer().get(Guardian.getNamespacedKey("polywand_pos"), PersistentDataType.STRING);
        if (string == null || string.length() < 1) return new ArrayList<>();
        List<Location> locations = new ArrayList<>();
        for (String s : string.split("/")) {
            locations.add(deserialisePosition(s));
        }
        return locations;
    }

    public boolean requiresMoveCheck(Location location) {
        Chunk chunk = location.getChunk();
        if (chunkMoveCheckCache.containsKey(chunk)) {
            return chunkMoveCheckCache.get(chunk);
        }
        List<NamespacedKey> keys = getZones(chunk.getWorld());
        for (NamespacedKey key : keys) {
            Zone zone = getZone(key);
            if (!zone.getFlags().contains("prevent_entry") && !zone.getFlags().contains("prevent_exit")) continue;
            if (Arrays.asList(zone.getChunks()).contains(chunk)) {
                chunkMoveCheckCache.put(chunk, true);
                return true;
            }
        }
        chunkMoveCheckCache.put(chunk, false);
        return false;
    }

    public boolean getInteractionResult(String flag, Location location) {
        List<Zone> zones = getZones(location);
        if (zones.isEmpty()) return true;
        for (Zone zone : zones) {
            if (!zone.canInteract(location, flag)) {
                return false;
            }
        }
        return true;
    }

    public boolean getInteractionResult(String flag, Location location, Player player) {
        List<Zone> zones = getZones(location);
        if (zones.isEmpty()) return true;
        for (Zone zone : zones) {
            if (!zone.canInteract(location, flag, player)) {
                return false;
            }
        }
        return true;
    }

    public String createHache(String flag, Location... locations) {
        StringBuilder hache = new StringBuilder(flag.hashCode() + "x");
        for (Location l : locations) {
            hache.append("x").append(Math.floor(l.getX())).append("0").append(Math.floor(l.getY())).append("0").append(Math.floor(l.getZ()));
        }
        return hache.toString().trim();
    }

    public String createHache(String flag, Player player, Location... locations) {
        StringBuilder hache = new StringBuilder(flag.hashCode() + "x");
        for (Location l : locations) {
            hache.append("x").append(Math.floor(l.getX())).append("0").append(Math.floor(l.getY())).append("0").append(Math.floor(l.getZ()));
        }
        return hache.toString().trim();
    }

    public void denyEvent(Player player) {
        if (config.actionDenyMessage != null)
            player.sendActionBar(config.actionDenyMessage);
    }

    public void displayBlocks(Zone zone, Player player) {
        List<Block> blocks = zone.getBlocks();
        for (Block block : blocks) {
            player.sendBlockChange(block.getLocation(), Material.WHITE_STAINED_GLASS.createBlockData());
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Block block : blocks) {
                    player.sendBlockChange(block.getLocation(), block.getBlockData());
                }
            }
        }.runTaskLater(Guardian.getInstance(), 30L);
    }

    public void displayBox(Player player) {
        Location l1 = getWandPosition(player, 1);
        Location l2 = getWandPosition(player, 2);
        if (l1 == null || l2 == null || l1.getWorld() != l2.getWorld() || l1.distanceSquared(l2) > (config.maxZoneDiameter * config.maxZoneDiameter))
            return;
        double d = Math.max(0.25, Math.min(1, (l1.distanceSquared(l2) / ((config.maxZoneDiameter * config.maxZoneDiameter) - l1.distanceSquared(l2)))));
        ParticleUtils.drawBox(Particle.END_ROD, null, BoundingBox.of(l1, l2), l1.getWorld(), d);
        highlightBlock(l1.getBlock(), Particle.FALLING_DUST, Material.REDSTONE_BLOCK.createBlockData());
        highlightBlock(l2.getBlock(), Particle.FALLING_DUST, Material.LAPIS_BLOCK.createBlockData());
    }

    public void displayPolyBox(Player player) {
        List<Location> locations = getPolywandPositions(player);
        ParticleUtils.drawHash(Particle.END_ROD, 0.25, locations.toArray(new Location[0]));
        locations.forEach(location -> highlightBlock(location.getBlock(), Particle.FALLING_DUST, Material.REDSTONE_BLOCK.createBlockData()));
    }

    public void displayBox(BoundingBox boundingBox, World world, Player player) {
//        ParticleUtils.drawHash(Particle.END_ROD, 0.25, boundingBox, world);
        ParticleUtils.drawBox(Particle.END_ROD, null, boundingBox, world, 0.25);
    }

    public void displayBox(BoundingBox boundingBox, World world, Particle particle, @Nullable Object data) {
        ParticleUtils.drawBox(particle, data, boundingBox, world, 0.25);
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

    @SuppressWarnings("all")
    public boolean canCreateZone(Zone zone, Player player) {
        for (NamespacedKey nearbyZone : getZones(zone.getWorld())) {
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

    public BaseComponent[] getCommandHelpMessage(Commander<?> commander) {
        ComponentBuilder builder = new ComponentBuilder("Command Help:");
        for (String pattern : commander.getPatterns()) {
            builder
                    .append(System.lineSeparator())
                    .reset()
                    .append(" - /" + commander.getCommand())
                    .color(net.md_5.bungee.api.ChatColor.DARK_GRAY)
                    .append(" ")
                    .append(pattern)
                    .event(new ClickEvent((pattern.contains("[") || pattern.contains("<")) ? ClickEvent.Action.SUGGEST_COMMAND : ClickEvent.Action.RUN_COMMAND, "/" + commander.getCommand() + " " + pattern.replaceFirst("(<.*|\\[.*)", "")))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText((pattern.contains("[") || pattern.contains("<")) ? "Click to suggest." : "Click to run.", net.md_5.bungee.api.ChatColor.AQUA)))
                    .color(ChatColor.GRAY);
        }
        return builder.create();
    }

    public List<String> getTabCompletions(Commander<?> commander, String[] args) {
        List<String> strings = commander.getTabCompletions(String.join(" ", args));
        if (strings == null || strings.isEmpty()) return null;
        final List<String> completions = new ArrayList<>();
        StringUtil.copyPartialMatches(args[args.length - 1], strings, completions);
        Collections.sort(completions);
        return completions;
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

    public @NotNull List<Zone> getZones(Location location) {
        Block block = location.getBlock();
        if (locationZoneCache.containsKey(block)) {
            return new ArrayList<>(locationZoneCache.get(block));
        }
        List<Zone> list = new ArrayList<>();
        for (Zone zone : getZones()) {
            if (location.getWorld() != zone.getWorld()) continue;
            if (!zone.getBoundingBox().contains(location.toVector())) continue;
            if (zone.isInside(location)) list.add(zone);
            if (zone instanceof Parent) {
                Parent<?> parent = (Parent<?>) zone;
                if (parent.hasChildren()) {
                    for (Zone child : parent.getChildren()) {
                        if (location.distanceSquared(child.getLocation()) > (child.getRadius() * child.getRadius()))
                            continue;
                        if (child.isInside(location)) list.add(child);
                    }
                }
            }
        }
        locationZoneCache.put(block, list);
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
        chunkMoveCheckCache.clear();
        locationZoneCache.clear();
        worldDistrictCache.clear();
        worldCache.clear();
    }

    protected void addCache(Zone zone) {
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
