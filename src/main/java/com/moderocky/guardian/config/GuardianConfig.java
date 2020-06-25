package com.moderocky.guardian.config;

import com.moderocky.guardian.Guardian;
import com.moderocky.mask.annotation.Configurable;
import com.moderocky.mask.gui.ItemFactory;
import com.moderocky.mask.mirror.Mirror;
import com.moderocky.mask.template.Config;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class GuardianConfig implements Config {

    @Configurable("wand")
    public boolean enableWand = true;

    @Configurable.Comment("Any Bukkit Material enum is permitted.")
    @Configurable("wand")
    public Material wandMaterial = Material.DEBUG_STICK;

    @Configurable.Comment("Allows a custom model data value to be used for the wand.")
    @Configurable("wand")
    @Configurable.Bounded(minValue = 0, maxValue = 999999999)
    public int wandModelData = 102;

    @Configurable.Comment("Any Bukkit Material enum is permitted.")
    @Configurable("wand")
    public Material polywandMaterial = Material.DEBUG_STICK;

    @Configurable.Comment("Allows a custom model data value to be used for the wand.")
    @Configurable("wand")
    @Configurable.Bounded(minValue = 0, maxValue = 999999999)
    public int polywandModelData = 103;

    @Configurable.Comment("Use '%s' to indicate the position number.")
    @Configurable("wand")
    public String setPosition = "Set Zone Position #%s";

    @Configurable("wand")
    public String clearPosition = "Cleared Zone Positions";

    @Configurable.Comment({
            "The maximum diameter for a player-created zone.",
            "This goes from corner to corner."
    })
    @Configurable("zone")
    @Configurable.Bounded(minValue = 1, maxValue = 100000)
    public long maxZoneDiameter = 128;

    @Configurable("zone")
    public String actionDenyMessage = "§bYou cannot interact with this zone!";

    @Configurable.Comment({"Allows use of the zone enter/exit flags.", "Might affect server performance if misused."})
    @Configurable("settings")
    public boolean checkEntryExit = true;

    @Configurable.Comment("Allowed values are TRUE, OP, NOT_OP and FALSE.")
    @Configurable("settings")
    public PermissionDefault allowBasicFlags = PermissionDefault.TRUE;

    @Configurable.Comment("Allowed values are TRUE, OP, NOT_OP and FALSE.")
    @Configurable("settings")
    public PermissionDefault allowSpecialFlags = PermissionDefault.OP;

    @Configurable.Comment("Delay between saving to file - in seconds.")
    @Configurable("settings")
    @Configurable.Bounded(minValue = 10, maxValue = 100000)
    public long saveDelay = 100;

    @Configurable.Comment({
            "Delay between clearing action caches (used to speed up the plugin) - in seconds.",
            "Longer delays will increase performance, but have the potential to cause false blocking."
    })
    @Configurable("settings")
    public long actionCacheResetDelay = 30;

    @Configurable("settings")
    @Configurable.Comment("Compress the stored zone data?")
    public boolean compressData = true;

    public GuardianConfig() {
        load();
    }

    @Override
    public @NotNull String getFolderPath() {
        return "plugins/Guardian/";
    }

    @Override
    public @NotNull String getFileName() {
        return "config.yml";
    }

    public ItemStack getWand() {
        return new ItemFactory(wandMaterial, 1, itemMeta -> {
            itemMeta.setCustomModelData(wandModelData);
            itemMeta.setDisplayName("§6Zone Wand");
            itemMeta.setLore(Arrays.asList("§7LClick to set Pos #1", "§7RClick to set Pos #2"));
            itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
            if (Mirror.classExists("co.aikar.timings.Timing"))
                new Mirror<>(itemMeta).method("setDestroyableKeys", Collection.class)
                        .invoke(Collections.emptyList());
            itemMeta.getPersistentDataContainer().set(Guardian.getNamespacedKey("wand"), PersistentDataType.BYTE, (byte) 1);
            itemMeta.addItemFlags(ItemFlag.values());
        }).create();
    }

    public ItemStack getPolywand() {
        return new ItemFactory(polywandMaterial, 1, itemMeta -> {
            itemMeta.setCustomModelData(polywandModelData);
            itemMeta.setDisplayName("§6Zone Polywand");
            itemMeta.setLore(Arrays.asList("§7LClick to add a vertex", "§7RClick to reset vertices"));
            itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
            if (Mirror.classExists("co.aikar.timings.Timing"))
                new Mirror<>(itemMeta).method("setDestroyableKeys", Collection.class)
                        .invoke(Collections.emptyList());
            itemMeta.getPersistentDataContainer().set(Guardian.getNamespacedKey("polywand"), PersistentDataType.BYTE, (byte) 1);
            itemMeta.addItemFlags(ItemFlag.values());
        }).create();
    }

}
