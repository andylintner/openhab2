package org.openhab.io.homekit.internal;

import java.util.HashMap;
import java.util.Map;

public enum HomekitMetadataClass {

    LIGHTING("Lighting"),
    CURRENT_TEMPERATURE("CurrentTemperature"),
    THERMOSTAT_CURRENT_TEMPERATURE("Thermostat.CurrentTemperature"),
    THERMOSTAT_TARGET_TEMPERATURE("Thermostat.TargetTemperature"),
    THERMOSTAT_HEATING_COOLING_MODE("Thermostat.HeatingCoolingMode"),
    DIMMABLE_LIGHTING("DimmableLighting"),
    COLORFUL_LIGHTING("ColorfulLighting"),
    CURRENT_HUMIDITY("CurrentHumidity"),
    SWITCHABLE("Switchable"),
    THERMOSTAT("Thermostat"),
    BLINDS("Blinds");

    private final String metadataClass;

    HomekitMetadataClass(String metadataClass) {
        this.metadataClass = metadataClass;
    }

    private static final Map<String, HomekitMetadataClass> homekitMetadataClassMap = new HashMap<>();

    static {
        for (HomekitMetadataClass homekitMetadataClass : HomekitMetadataClass.values()) {
            homekitMetadataClassMap.put(homekitMetadataClass.metadataClass, homekitMetadataClass);
        }
    }

    public static boolean hasMetadataClass(String metadataClass) {
        return homekitMetadataClassMap.containsKey(metadataClass);
    }

    public static HomekitMetadataClass fromMetadataClass(String metadataClass) {
        return homekitMetadataClassMap.get(metadataClass);
    }

}
