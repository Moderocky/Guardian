package com.moderocky.guardian.api;

import com.google.gson.JsonObject;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface Child<Z extends Zone> {

    Z getParent();

    void setParent(@NotNull Z parent);

    void save(final @NotNull JsonObject section);

    void load(final @NotNull JsonObject section);

}
