package org.openhab.io.homekit.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.items.Item;

public enum HomekitTag {

    LIGHT("Light"),
    LIGHTING("Lighting"),
    CURRENT_TEMPERATURE("CurrentTemperature"),
    TARGET_TEMPERATURE("TargetTemperature"),
    HEATING_COOLING_MODE("homekit:HeatingCoolingMode"),
    DIMMABLE_LIGHTING("DimmableLighting"),
    COLORFUL_LIGHTING("ColorfulLighting"),
    CURRENT_HUMIDITY("CurrentHumidity"),
    SWITCHABLE("Switchable"),
    THERMOSTAT("Thermostat"),
    BLINDS("Blinds");

    private final String tag;

    HomekitTag(String tag) {
        this.tag = tag;
    }

    private static final Map<String, HomekitTag> tagMap = new HashMap<>();

    static {
        for (HomekitTag tagType : HomekitTag.values()) {
            tagMap.put(tagType.tag, tagType);
        }
    }

    public static boolean hasTag(String tag) {
        return tagMap.containsKey(tag);
    }

    public static HomekitTag fromTag(String tag) {
        return tagMap.get(tag);
    }

    public static Optional<HomekitTag> fromItem(Item item) {
        Stream<String> tags = item.getTags().stream().filter(HomekitTag::hasTag);
        return tags.findFirst().map(HomekitTag::fromTag);
    }
}
