package com.moderocky.guardian.api;

import com.moderocky.guardian.Guardian;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public interface Parent<Z extends Zone> {

    @NotNull List<Z> getChildren();

    boolean hasChildren();

    void addChild(@NotNull Z child);

    void removeChild(@NotNull Z child);

    void clearChildren();

    default void saveChildren(ConfigurationSection section) {
        for (Z child : getChildren()) {
            String id = child.getKey().getKey();
            section.set("children." + id + ".namespace", child.getKey().getNamespace());
            section.set("children." + id + ".is_child", true);
            ConfigurationSection childSection = section.getConfigurationSection(id);
            if (childSection == null) continue;
            child.save(childSection);
        }
    }

    @SuppressWarnings("all")
    default void loadChildren(ConfigurationSection section) {
        GuardianAPI api = Guardian.getApi();
        ConfigurationSection childSection = section.getConfigurationSection("children");
        if (childSection == null) return;
        List<String> keys = new ArrayList<>(childSection.getKeys(false));
        for (String key : keys) {
            ConfigurationSection sect = childSection.getConfigurationSection(key);
            if (sect == null) continue;
            String namespace = sect.getString("namespace");
            if (namespace == null || namespace.length() < 1) continue;
            try {
                NamespacedKey namespacedKey = new NamespacedKey(namespace, key);
                Class<Z> clarse = (Class<Z>) Class.forName(sect.getString("class_loader"));
                Constructor<Z> constructor = clarse.getConstructor(NamespacedKey.class, ConfigurationSection.class);
                Z zone = constructor.newInstance(namespacedKey, sect);
                if (zone instanceof Child) {
                    ((Child) zone).setParent((Zone) this);
                }
                addChild(zone);
            } catch (Throwable ignore) {
                ignore.printStackTrace();
            }
        }
    }

}
