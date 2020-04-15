package com.moderocky.guardian;

import com.moderocky.guardian.api.GuardianAPI;
import com.moderocky.guardian.command.WandCommand;
import com.moderocky.guardian.command.ZoneCommand;
import com.moderocky.guardian.config.GuardianConfig;
import com.moderocky.guardian.listener.*;
import com.moderocky.guardian.util.Messenger;
import com.moderocky.mask.template.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Guardian extends Plugin {

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
    public void startup() {
        instance = this;
        messenger = new Messenger(this);
        api = new GuardianAPI();
        api.init();

        registerPermissions();
    }

    @Override
    public void disable() {
        api.save();

        instance = null;
        messenger = null;
        api = null;
    }

    private void registerPermissions() {
        List<Permission> permissions = new ArrayList<>();

        permissions.add(new Permission("guardian.command.zone", "Zone command permission.", PermissionDefault.OP, null));
        permissions.add(new Permission("guardian.command.wand", "Wand command permission.", PermissionDefault.OP, null));

        permissions.forEach(permission -> Bukkit.getPluginManager().addPermission(permission));
    }

    @Override
    protected void registerListeners() {
        register(
                new BlockBreakListener(),
                new BlockPlaceListener(),
                new EntityDamageByEntityListener(),
                new EntityExplodeListener(),
                new EntitySpawnListener(),
                new InventoryOpenListener(),
                new PlayerAttemptPickupItemListener(),
                new PlayerInteractEntityListener(),
                new PlayerInteractListener()
        );
    }

    @Override
    protected void registerCommands() {
        register(
                new WandCommand(),
                new ZoneCommand()
        );
    }

    @NotNull
    public GuardianConfig getGuardianConfig() {
        return config;
    }

}
