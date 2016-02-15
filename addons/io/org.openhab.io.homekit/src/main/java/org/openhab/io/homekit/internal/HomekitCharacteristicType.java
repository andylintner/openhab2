package org.openhab.io.homekit.internal;

import java.util.HashMap;
import java.util.Map;

/**
 * Characteristics are used by complex accessories that can't be represented by
 * a single item (i.e. a thermostat)
 *
 * @author Andy Lintner
 */
public enum HomekitCharacteristicType {

    CURRENT_TEMPERATURE("currentTemperature"),
    COOLING_THRESHOLD("coolingThreshold"),
    HEATING_THRESHOLD("heatingThreshold"),
    HEATING_COOLING_MODE("heatingCoolingMode"),
    AUTO_THRESHOLD("autoThreshold");

    private static final Map<String, HomekitCharacteristicType> tagMap = new HashMap<>();

    static {
        for (HomekitCharacteristicType type : HomekitCharacteristicType.values()) {
            tagMap.put(type.tag, type);
        }
    }

    private final String tag;

    private HomekitCharacteristicType(String tag) {
        this.tag = tag;
    }

    public static HomekitCharacteristicType valueOfTag(String tag) {
        return tagMap.get(tag);
    }
}
