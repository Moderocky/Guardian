package com.moderocky.guardian.config;

import com.moderocky.guardian.Guardian;
import com.moderocky.mask.annotation.Configurable;
import com.moderocky.mask.gui.ItemFactory;
import com.moderocky.mask.template.Config;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class GuardianConfig implements Config {

    @Configurable(section = "wand")
    public boolean enableWand = true;

    @Configurable(section = "wand")
    public Material wandMaterial = Material.DEBUG_STICK;

    @Configurable(section = "wand")
    @Configurable.Bounded(minValue = 0, maxValue = 999999999)
    public int modelData = 102;

    @Configurable(section = "wand")
    public String setPosition = "Set Zone Position #%s";

    @Configurable(section = "zone")
    @Configurable.Comment(text = {
            "The maximum diameter for a player-created zone.",
            "This is from corner to corner."
    })
    @Configurable.Bounded(minValue = 1, maxValue = 100000)
    public long maxZoneDiameter = 128;

    @Configurable(section = "zone")
    public String actionDenyMessage = "§bYou cannot interact with this zone!";

    @Configurable(section = "settings")
    @Configurable.Comment(text = "Allowed values are TRUE, OP, NOT_OP and FALSE")
    public PermissionDefault allowBasicFlags = PermissionDefault.TRUE;

    @Configurable(section = "settings")
    @Configurable.Comment(text = "Allowed values are TRUE, OP, NOT_OP and FALSE")
    public PermissionDefault allowSpecialFlags = PermissionDefault.OP;

    @Configurable(section = "settings")
    @Configurable.Comment(text = "Delay between saving to file - in seconds.")
    @Configurable.Bounded(minValue = 10, maxValue = 100000)
    public long saveDelay = 100;

    @Configurable(section = "settings")
    @Configurable.Comment(text = {
            "Delay between clearing action caches (used to speed up the plugin) - in seconds.",
            "Longer delays will increase performance, but have the potential to cause false blocking."
    })
    public long actionCacheResetDelay = 16;

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
            itemMeta.setCustomModelData(modelData);
            itemMeta.setDisplayName("§6Zone Wand");
            itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
            itemMeta.setDestroyableKeys(Collections.emptyList());
            itemMeta.getPersistentDataContainer().set(Guardian.getNamespacedKey("wand"), PersistentDataType.BYTE, (byte) 1);
            itemMeta.addItemFlags(ItemFlag.values());
        }).create();
    }

}
