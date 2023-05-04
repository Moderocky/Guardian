package com.moderocky.guardian.command;

import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.api.Zone;
import mx.kenzie.centurion.TypedArgument;

import java.util.ArrayList;
import java.util.List;

public class ZoneArgument extends TypedArgument<Zone> {
    public static final ZoneArgument ZONE = new ZoneArgument();

    private int lastHash;
    private Zone lastValue;

    public ZoneArgument() {
        super(Zone.class);
        this.label = "zone";
    }

    @Override
    public boolean matches(String s) {
        for (Zone zone : Guardian.getApi().getZones()) {
            if (!zone.getKey().getKey().equalsIgnoreCase(s)) continue;
            this.lastValue = zone;
            this.lastHash = s.hashCode();
        }
        return false;
    }

    @Override
    public Zone parse(String s) {
        if (s.hashCode() == lastHash && lastValue != null) return lastValue;
        if (this.matches(s)) return lastValue;
        return null;
    }

    @Override
    public String[] possibilities() {
        final List<String> list = new ArrayList<>();
        for (Zone zone : Guardian.getApi().getZones()) list.add(zone.getKey().getKey());
        return list.toArray(new String[0]);
    }

}
