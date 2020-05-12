package com.moderocky.guardian;

import com.moderocky.guardian.api.GuardianAPI;
import com.moderocky.guardian.command.GuardianCommand;
import com.moderocky.guardian.command.PolywandCommand;
import com.moderocky.guardian.command.WandCommand;
import com.moderocky.guardian.command.ZoneCommand;
import com.moderocky.guardian.config.GuardianConfig;
import com.moderocky.guardian.listener.*;
import com.moderocky.guardian.util.Messenger;
import com.moderocky.mask.template.BukkitPlugin;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Guardian extends BukkitPlugin {

    private static GuardianAPI api;
    private static Guardian instance;
    private static Messenger messenger;
    private final @NotNull GuardianConfig config = new GuardianConfig();
    private final @NotNull Metrics metrics = new Metrics(this, 7168);

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
        permissions.add(new Permission("guardian.command.guardian", "Guardian command permission.", PermissionDefault.TRUE, null));
        permissions.add(new Permission("guardian.command.wand", "Wand command permission.", PermissionDefault.OP, null));
        permissions.add(new Permission("guardian.command.polywand", "Polywand command permission.", PermissionDefault.OP, null));
        permissions.add(new Permission("guardian.command.zone", "Zone command permission.", PermissionDefault.OP, null));
        permissions.add(new Permission("guardian.command.teleport", "Zone teleport permission.", PermissionDefault.OP, null));
        permissions.add(new Permission("guardian.zone.allow_oversized", "Oversized zone permission.", PermissionDefault.OP, null));

        permissions.forEach(permission -> Bukkit.getPluginManager().addPermission(permission));
    }

    @Override
    protected void registerListeners() {
        register(
                new AsyncPlayerChatListener(),
                new BlockBreakListener(),
                new BlockPlaceListener(),
                new EntityDamageByEntityListener(),
                new EntityExplodeListener(),
                new EntitySpawnListener(),
                new InventoryOpenListener(),
                new PlayerAttemptPickupItemListener(),
                new PlayerCommandPreprocessListener(),
                new PlayerInteractEntityListener(),
                new PlayerInteractListener(),
                new PlayerTeleportListener()
        );
        if (config.checkEntryExit) register(new PlayerMoveListener());
    }

    @Override
    protected void registerCommands() {
        register(
                new GuardianCommand(),
                new WandCommand(),
                new PolywandCommand(),
                new ZoneCommand()
        );
    }

    @Override
    protected void registerSyntax() {
        try {
            getAddon().loadClasses("com.moderocky.guardian.skript", "condition");
            getAddon().loadClasses("com.moderocky.guardian.skript", "effect");
            getAddon().loadClasses("com.moderocky.guardian.skript", "expression");
        } catch (Throwable ignore) {
        }
    }

    public @NotNull Metrics getMetrics() {
        return metrics;
    }

    @NotNull
    public GuardianConfig getGuardianConfig() {
        return config;
    }

}
