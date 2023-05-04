package com.moderocky.guardian;

import com.moderocky.guardian.api.GuardianAPI;
import com.moderocky.guardian.command.GuardianCommand;
import com.moderocky.guardian.command.PolywandCommand;
import com.moderocky.guardian.command.WandCommand;
import com.moderocky.guardian.command.ZoneCommand;
import com.moderocky.guardian.config.GuardianConfig;
import com.moderocky.guardian.listener.*;
import com.moderocky.guardian.util.LegacyMessenger;
import com.moderocky.guardian.util.Messenger;
import com.moderocky.guardian.util.RGBMessenger;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class Guardian extends JavaPlugin {

    private static GuardianAPI api;
    private static Guardian instance;
    private static Messenger messenger;
    private final @NotNull GuardianConfig config = new GuardianConfig();

    public static Guardian getInstance() {
        return instance;
    }

    public static Messenger getMessenger() {
        return messenger;
    }

    public static GuardianAPI getApi() {
        return api;
    }


    @Override
    public void onEnable() {
        instance = this;
        try { // Check for 1.16+ RGB Support
            messenger = new RGBMessenger(this);
        } catch (Throwable throwable) { // Default to legacy system
            messenger = new LegacyMessenger(this);
        }
        api = new GuardianAPI();
        api.init();
        registerListeners();
        registerPermissions();
        registerCommands();
    }

    @Override
    public void onDisable() {
        api.save();
        instance = null;
        messenger = null;
        api = null;
    }

    private void registerPermissions() {
        final PluginManager manager = Bukkit.getPluginManager();
        manager.addPermission(new Permission("guardian.command.guardian", "Guardian command permission.", PermissionDefault.TRUE, null));
        manager.addPermission(new Permission("guardian.command.wand", "Wand command permission.", PermissionDefault.OP, null));
        manager.addPermission(new Permission("guardian.command.polywand", "Polywand command permission.", PermissionDefault.OP, null));
        manager.addPermission(new Permission("guardian.command.zone", "Zone command permission.", PermissionDefault.OP, null));
        manager.addPermission(new Permission("guardian.command.teleport", "Zone teleport permission.", PermissionDefault.OP, null));
        manager.addPermission(new Permission("guardian.zone.allow_oversized", "Oversized zone permission.", PermissionDefault.OP, null));
    }

    protected void registerListeners() {
        final PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(new AsyncPlayerChatListener(), this);
        manager.registerEvents(new BlockBreakListener(), this);
        manager.registerEvents(new BlockPlaceListener(), this);
        manager.registerEvents(new EntityDamageByEntityListener(), this);
        manager.registerEvents(new EntityExplodeListener(), this);
        manager.registerEvents(new EntitySpawnListener(), this);
        manager.registerEvents(new InventoryOpenListener(), this);
        manager.registerEvents(new PlayerAttemptPickupItemListener(), this);
        manager.registerEvents(new PlayerCommandPreprocessListener(), this);
        manager.registerEvents(new PlayerInteractEntityListener(), this);
        manager.registerEvents(new PlayerInteractListener(), this);
        manager.registerEvents(new PlayerTeleportListener(), this);
        if (config.checkEntryExit) manager.registerEvents(new PlayerMoveListener(), this);
    }

    protected void registerCommands() {
        new GuardianCommand().register(this);
        new WandCommand().register(this);
        new PolywandCommand().register(this);
        new ZoneCommand().register(this);
    }

    public static NamespacedKey getNamespacedKey(String key) {
        return new NamespacedKey(instance, key);
    }

    @NotNull
    public GuardianConfig getGuardianConfig() {
        return config;
    }

}
