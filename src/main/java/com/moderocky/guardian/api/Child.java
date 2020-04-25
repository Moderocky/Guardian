package com.moderocky.guardian.api;

import com.moderocky.guardian.api.Zone;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public interface Child<Z extends Zone> {

    void setParent(@NotNull Z parent);

    Z getParent();

    void save(@NotNull ConfigurationSection section);

    void load(@NotNull ConfigurationSection section);

}
