package org.openhab.io.homekit.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;

public enum HomekitTag {

    DIMMABLE_LIGHTING("DimmableLighting", i -> HomekitDeviceType.DIMMABLE_LIGHTBULB),
    CURRENT_HUMIDITY("CurrentHumidity", i -> HomekitDeviceType.HUMIDITY_SENSOR),
    LIGHTING("Lighting", item -> {
        if (item instanceof ColorItem) {
            return HomekitDeviceType.COLORFUL_LIGHTBULB;
        } else if (item instanceof DimmerItem) {
            return HomekitDeviceType.DIMMABLE_LIGHTBULB;
        } else {
            return HomekitDeviceType.LIGHTBULB;
        }
    }),
    LIGHT("Light", LIGHTING.deviceTypeSupplier),
    SWITCHABLE("Switchable", i -> HomekitDeviceType.SWITCH),
    CURRENT_TEMPERATURE("CurrentTemperature", i -> HomekitDeviceType.TEMPERATURE_SENSOR),
    THERMOSTAT("Thermostat", i -> HomekitDeviceType.THERMOSTAT),
    COLORFUL_LIGHTING("ColorfulLighting", i -> HomekitDeviceType.COLORFUL_LIGHTBULB),
    BLINDS("Blinds", i -> HomekitDeviceType.WINDOW_COVERING);

    private static final Map<String, HomekitTag> tagMap = new HashMap<>();

    static {
        for (HomekitTag tagType : HomekitTag.values()) {
            tagMap.put(tagType.tag, tagType);
        }
    }

    private String tag;
    private Function<Item, HomekitDeviceType> deviceTypeSupplier;

    private HomekitTag(String tag, Function<Item, HomekitDeviceType> deviceTypeSupplier) {
        this.tag = tag;
        this.deviceTypeSupplier = deviceTypeSupplier;
    }

    public HomekitDeviceType getType(Item item) {
        return deviceTypeSupplier.apply(item);
    }

    public static HomekitTag valueOfTag(String tag) {
        return tagMap.get(tag);
    }
}
