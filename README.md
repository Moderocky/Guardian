# Guardian
A lightweight and accessible region plugin.

User? Download the latest release [here](https://gitlab.com/Moderocky/Guardian/-/tags).
Developer? Learn about writing an extension or contributing below.

### Features
- Intuitive creation system
- Easy-to-extend framework
- Simple flag system to avoid priority disputes
- Support for non-cuboidal zones
- Lightweight event-caching system
- No version-dependent code

### Getting Started
1. Download the latest Guardian release [here](https://gitlab.com/Moderocky/Guardian/-/tags).
2. Put `Guardian.jar` into your plugins folder.
3. Restart your server.
4. Give players the `guardian.command.wand` permission to spawn a zone /wand. (Optional!)
5. Give players the `guardian.command.zone` permission to create and edit zones. (Optional!)

### Creating an Addon

Maven repository information:
```xml
<repository>
    <id>pan-repo</id>
    <name>Pandaemonium Repository</name>
    <url>https://gitlab.com/api/v4/projects/18568066/packages/maven</url>
</repository>
```

Adding Guardian as a dependency:
```xml
<dependency>
    <groupId>com.moderocky</groupId>
    <artifactId>Guardian</artifactId>
    <version>VERSION</version>
    <scope>provided</scope>
</dependency>
```

Note to Gradle users: I have no idea how Gradle works, but I'm sure you can figure this out for yourself. :)

Guardian's API is designed to be very easy to extend.
To obtain a GuardianAPI instance (needed to register things) use `com.moderocky.guardian.Guardian.getApi()`

#### Custom Zone Types

If you would like to create a custom zone type (e.g. a spherical or polyhedral area!?) you can extend the `com.moderocky.guardian.api.Zone` class and fill in the affected behaviour. Please see the documentation in this class [(here)](https://gitlab.com/Moderocky/Guardian/-/blob/master/src/main/java/com/moderocky/guardian/api/Zone.java) for more information about using this.
You may also extend and override an existing zone type (e.g. CuboidalZone) to create your own.

Remember to register your zone via `GuardianAPI#registerZone(Zone)` so that it will be saved.

#### Custom Flags

Protection flags in Guardian are BLOCKING!
This means that when an interaction check is carried out, Guardian will check through any overlapping regions at the area.
When it encounters a zone with this flag active, the check will stop there.
This means that flags should almost always be used to override the default behaviour rather than to assure it.

You may add a zone flag using `GuardianAPI#addFlag(String)`. This will make it automatically available in commands and tab completers.

To add specific behaviour for your flag, such as preventing certain events from taking place, you should create your own listener.

There are multiple ways to perform zone checks, and a few standard examples are detailed below.

##### Interactions with a Player

Most zone interactions will involve a player. This could be something like a player breaking a block, or trying to open a door.
For the following code to work, we need the location of the event `location`, a player `player`, and the specific flag for our interaction `"interaction_flag_id_here"`.
Remember - the location is where the interaction actually happens (such as the location of a block being broken). It is NOT the location of the player. The player might be standing outside the zone performing interactions inside.

We also need the GuardianAPI instance `api` to perform some region checks.

```java_holder_method_tree
    for (Zone zone : api.getZones(location)) {
        if (!zone.canInteract(location, "interaction_flag_id_here", player)) { // We perform this check with a player. Some regions might allow specific players to edit them.
            event.setCancelled(true); // We have now dealt with this interaction.
            api.denyEvent(player); // Warns the player that they can't interact here.
            return; // We've blocked the interaction - no need to check any more regions.
        }
    }
```

Remember that flags are designed to be BLOCKING - once the event has been cancelled, it stays cancelled.

##### Interactions without a Player

Most zone interactions will not have an attached player. This could be something like a creeper exploding, ice melting, etc.
For the following code to work, we need the location of the event `location` and the specific flag for our interaction `"interaction_flag_id_here"`.

We also need the GuardianAPI instance `api` to perform some region checks.

```java_holder_method_tree
    for (Zone zone : api.getZones(location)) {
        if (!zone.canInteract(location, "interaction_flag_id_here")) { // There is no player involved in this event - i.e. a creeper exploding
            event.setCancelled(true); // We have now dealt with this interaction.
            return; // We've blocked the interaction - no need to check any more regions.
        }
    }
```

##### Using a Hache (Cache-Hash)

Some events, such as a player breaking a block, might be attempted repeatedly. The player could keep trying to break the same block.

Obviously, we don't want to perform the same check repeatedly (many overlapping polygonal regions -> lots of overhead).
This is what the built-in 'Hache' system is for.

We can generate our hache (a sort of hashcode specific to this interaction) based on the event details.
Remember: it should be relatively unique (i.e. not just `event.hashCode()`), but we also need the SAME hache to be generated by this interaction again.

The cache is reset regularly, and also when updating a region (flags, creating/deleting, adding a player, etc.) to try and avoid any erroneous checks.
The aim of this is simply to cut down on overhead in frequently-repeated checks.

Good examples:
- `player.hashCode() + "0x99" + block.hashCode()` -> this string will be generated if the same player breaks the same block again
- `player.hashCode() + "0x24" + location.getX() + "0" + location.getY() + "0" + location.getZ()` -> string can be reproduced

Bad examples:
- `player.hashCode()` -> this would block any future event involving this player
- `location.hashCode()` -> anything involving a `getLocation()` method is likely to create a new location object, meaning a new hashcode
- `"my_event_here"` -> we need this to be interaction-specific, not event specific

```java_holder_method_tree
    String hache = // Result cache code.
    Boolean boo = api.getCachedResult(hache); // Has an identical event taken place recently?
    if (boo != null) { // Yes it has?
        event.setCancelled(boo); // Good - we can re-use the result. No need to re-check the regions. :)
    } else {
        for (Zone zone : api.getZones(location)) {
            if (!zone.canInteract(location, "interaction_flag_id_here", player)) {
                event.setCancelled(true); // We have now dealt with this interaction.
                api.addCachedResult(hache, true); // Cache this unique result to cut down on overhead.
                api.denyEvent(player); // Warns the player that they can't interact here.
                return; // We've blocked the interaction - no need to check any more regions.
            }
        }
        api.addCachedResult(hache, false); // Cache this unique result to cut down on overhead.
    }
```



