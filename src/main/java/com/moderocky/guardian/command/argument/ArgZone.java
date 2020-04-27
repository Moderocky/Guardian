package com.moderocky.guardian.command.argument;

import com.moderocky.guardian.Guardian;
import com.moderocky.mask.command.ArgString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ArgZone extends ArgString {

    @Override
    public boolean matches(String string) {
        return Guardian.getApi().exists(string);
    }

    @Override
    public @NotNull String getName() {
        return "zone_id";
    }

    @Override
    public @Nullable List<String> getCompletions() {
        List<String> strings = new ArrayList<>();
        Guardian.getApi().getZoneKeys().forEach(key -> strings.add(key.getKey()));
        return strings;
    }

}
