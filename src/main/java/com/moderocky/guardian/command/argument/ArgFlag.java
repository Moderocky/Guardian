package com.moderocky.guardian.command.argument;

import com.moderocky.guardian.Guardian;
import com.moderocky.mask.command.ArgString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ArgFlag extends ArgString {

    @Override
    public boolean matches(String string) {
        return Guardian.getApi().getProtectionFlags().contains(string);
    }

    @Override
    public @NotNull String getName() {
        return "flag";
    }

    @Override
    public @Nullable List<String> getCompletions() {
        return Guardian.getApi().getProtectionFlags();
    }

}
