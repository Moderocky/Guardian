package com.moderocky.guardian.config;

import com.moderocky.guardian.Guardian;
import mx.kenzie.fern.Fern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class GuardianConfig {

    public boolean enableWand = true;

    public Material wandMaterial = Material.DEBUG_STICK;

    public int wandModelData = 102;

    public Material polywandMaterial = Material.DEBUG_STICK;

    public int polywandModelData = 103;

    public String setPosition = "Set Zone Position #%s";

    public String clearPosition = "Cleared Zone Positions";

    public long maxZoneDiameter = 128;

    public String actionDenyMessage = "§bYou cannot interact with this zone!";

    public boolean checkEntryExit = true;

    public PermissionDefault allowBasicFlags = PermissionDefault.TRUE;

    public PermissionDefault allowSpecialFlags = PermissionDefault.OP;

    public long saveDelay = 100;

    public long actionCacheResetDelay = 30;

    public boolean compressData = true;

    public GuardianConfig() {
        this.load();
    }

    private void load() {
        final File file = new File(this.getFolderPath() + this.getFileName());
        if (!file.exists()) return;
        try (Fern fern = new Fern(new FileInputStream(file))) {
            fern.toObject(this);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void save() {
        final File file = new File(this.getFolderPath() + this.getFileName());
        if (file.getParentFile() != null && !file.getParentFile().exists()) file.getParentFile().mkdirs();
        try {
            if (!file.exists()) file.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try (Fern fern = new Fern(new FileOutputStream(file))) {
            fern.write(this);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public @NotNull String getFolderPath() {
        return "plugins/Guardian/";
    }

    public @NotNull String getFileName() {
        return "config.fern";
    }

    public ItemStack getWand() {
        final ItemStack stack = new ItemStack(wandMaterial, 1);
        final ItemMeta meta = stack.getItemMeta();
        meta.setCustomModelData(wandModelData);
        meta.displayName(Component.text("Zone Wand", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
            Component.textOfChildren(
                Component.text("Pos #1: ", NamedTextColor.GRAY),
                Component.keybind("key.attack", NamedTextColor.AQUA)
            ).decoration(TextDecoration.ITALIC, false),
            Component.textOfChildren(
                Component.text("Pos #2: ", NamedTextColor.GRAY),
                Component.keybind("key.use", NamedTextColor.AQUA)
            ).decoration(TextDecoration.ITALIC, false)
        ));
        meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
        meta.setDestroyableKeys(Collections.emptyList());
        meta.getPersistentDataContainer().set(Guardian.getNamespacedKey("wand"), PersistentDataType.BYTE, (byte) 1);
        meta.addItemFlags(ItemFlag.values());
        stack.setItemMeta(meta);
        return stack;
    }

    public ItemStack getPolywand() {
        final ItemStack stack = new ItemStack(polywandMaterial, 1);
        final ItemMeta meta = stack.getItemMeta();
        meta.setCustomModelData(polywandModelData);
        meta.displayName(Component.text("Zone Polywand", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
            Component.textOfChildren(
                Component.text("Add vertex: ", NamedTextColor.GRAY),
                Component.keybind("key.attack", NamedTextColor.AQUA)
            ).decoration(TextDecoration.ITALIC, false),
            Component.textOfChildren(
                Component.text("Clear vertices: ", NamedTextColor.GRAY),
                Component.keybind("key.use", NamedTextColor.AQUA)
            ).decoration(TextDecoration.ITALIC, false)
        ));
        meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
        meta.setDestroyableKeys(Collections.emptyList());
        meta.getPersistentDataContainer().set(Guardian.getNamespacedKey("polywand"), PersistentDataType.BYTE, (byte) 1);
        meta.addItemFlags(ItemFlag.values());
        stack.setItemMeta(meta);
        return stack;
    }

    public void reload() {
        this.load();
    }

}
