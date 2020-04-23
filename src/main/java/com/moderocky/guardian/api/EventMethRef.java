package com.moderocky.guardian.api;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public interface EventMethRef {

    boolean hashCache();

    @NotNull String hashKey();

    @NotNull Class<Event> getEventClass();

    @NotNull String getFlag();

}
