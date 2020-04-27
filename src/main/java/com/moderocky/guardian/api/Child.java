package com.moderocky.guardian.api;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public interface Child<Z extends Zone> {

    Z getParent();

    void setParent(@NotNull Z parent);

    void save(@NotNull ConfigurationSection section);

    void load(@NotNull ConfigurationSection section);

}
