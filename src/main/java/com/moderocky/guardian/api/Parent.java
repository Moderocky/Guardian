package com.moderocky.guardian.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.moderocky.guardian.Guardian;
import dev.moderocky.mirror.Mirror;
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

    default void saveChildren(final JsonObject section) {
        JsonArray array = new JsonArray();
        for (Z child : getChildren()) {
            JsonObject object = new JsonObject();
            String id = child.getKey().getKey();
            child.save(object);
            object.addProperty("namespace", child.getKey().getNamespace());
            object.addProperty("key", child.getKey().getKey());
            object.addProperty("class_loader", child.getClass().getName());
            array.add(object);
        }
        section.add("children", array);
    }

    @SuppressWarnings("all")
    default void loadChildren(final JsonObject section) {
        GuardianAPI api = Guardian.getApi();
        if (section.has("children")) return;
        JsonArray array = section.getAsJsonArray("children");
        for (JsonElement element : array) {
            JsonObject object = element.getAsJsonObject();
            if (!object.has("class_loader")) continue;
            String namespace = object.get("namespace").getAsString();
            String key = object.get("key").getAsString();
            try {
                NamespacedKey namespacedKey = new NamespacedKey(namespace, key);
                Mirror<Class<Z>> mirror = Mirror.<Z>mirror(object.get("class_loader").getAsString());
                Z zone = mirror.<Z>instantiate(namespacedKey, object);
                if (zone instanceof Child) {
                    ((Child) zone).setParent((Zone) this);
                }
                addChild(zone);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

        }
    }

}
