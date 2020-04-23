package com.moderocky.guardian.config;

import com.moderocky.mask.annotation.Configurable;
import com.moderocky.mask.gui.ItemFactory;
import com.moderocky.mask.template.Config;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

public class GuardianConfig implements Config {

    @Configurable(section = "wand")
    public boolean enableWand = true;

    @Configurable(section = "wand")
    public Material wandMaterial = Material.DEBUG_STICK;

    @Configurable(section = "wand")
    public int modelData = 102;

    @Configurable(section = "zone")
    public long maxZoneDiameter = 128;

    @Configurable(section = "settings")
    @Configurable.Comment(text = "Allowed values are TRUE, OP, NOT_OP and FALSE")
    public PermissionDefault allowBasicFlags = PermissionDefault.TRUE;

    @Configurable(section = "settings")
    @Configurable.Comment(text = "Allowed values are TRUE, OP, NOT_OP and FALSE")
    public PermissionDefault allowSpecialFlags = PermissionDefault.OP;

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
            itemMeta.addItemFlags(ItemFlag.values());
        }).create();
    }

}
