package com.moderocky.guardian.command;

import com.moderocky.guardian.Guardian;
import mx.kenzie.centurion.TypedArgument;

public class FlagArgument extends TypedArgument<String> {
    public static final FlagArgument FLAG = new FlagArgument();

    public FlagArgument() {
        super(String.class);
        this.label = "flag";
        this.possibilities = Guardian.getApi().getProtectionFlags().toArray(new String[0]);
    }

    @Override
    public boolean matches(String s) {
        return Guardian.getApi().getProtectionFlags().contains(s);
    }

    @Override
    public String parse(String s) {
        return s;
    }

}
