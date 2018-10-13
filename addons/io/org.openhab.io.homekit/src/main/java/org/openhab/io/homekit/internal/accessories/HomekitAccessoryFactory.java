package org.openhab.io.homekit.internal.accessories;

import java.util.function.BiFunction;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.Metadata;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.openhab.io.homekit.accessory.registry.Accessory;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitMetadataClass;
import org.openhab.io.homekit.internal.HomekitTag;
import org.openhab.io.homekit.internal.OpenhabHomekitBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomekitAccessoryFactory implements BiFunction<Item, Metadata, Accessory> {

    private final HomekitAccessoryUpdater updater;
    private final OpenhabHomekitBridge bridge;
    private Logger logger = LoggerFactory.getLogger(HomekitAccessoryFactory.class);

    public HomekitAccessoryFactory(HomekitAccessoryUpdater updater, OpenhabHomekitBridge bridge) {
        this.updater = updater;
        this.bridge = bridge;
    }

    @Override
    public Accessory apply(Item item, Metadata metadata) {
        if (metadata != null) {
            switch (HomekitMetadataClass.fromMetadataClass(metadata.getValue())) {
                case LIGHTING:
                    return new HomekitLightbulbImpl(item, metadata, updater, bridge);
                case DIMMABLE_LIGHTING:
                    return new HomekitDimmableLightbulbImpl(item, metadata, updater, bridge);
                case COLORFUL_LIGHTING:
                    return new HomekitColorfulLightbulbImpl(item, metadata, updater, bridge);
                case CURRENT_HUMIDITY:
                    return new HomekitHumiditySensorImpl(item, metadata, updater, bridge);
                case SWITCHABLE:
                    return new HomekitSwitchImpl(item, metadata, updater, bridge);
                case CURRENT_TEMPERATURE:
                    return new HomekitTemperatureSensorImpl(item, metadata, updater, bridge);
                case THERMOSTAT:
                    return new HomekitThermostatImpl(item, metadata, updater, bridge);
                case BLINDS:
                    return new HomekitWindowCovering(item, metadata, updater, bridge);
                default:
                    break;
            }
        }
        return HomekitTag.fromItem(item).map(tag -> {
            switch (tag) {
                case LIGHT:
                case LIGHTING:
                    if (item instanceof ColorItem) {
                        return new HomekitColorfulLightbulbImpl(item, metadata, updater, bridge);
                    } else if (item instanceof DimmerItem) {
                        return new HomekitDimmableLightbulbImpl(item, metadata, updater, bridge);
                    } else {
                        return new HomekitLightbulbImpl(item, metadata, updater, bridge);
                    }

                case DIMMABLE_LIGHTING:
                    return new HomekitDimmableLightbulbImpl(item, metadata, updater, bridge);

                case COLORFUL_LIGHTING:
                    return new HomekitColorfulLightbulbImpl(item, metadata, updater, bridge);

                case CURRENT_HUMIDITY:
                    return new HomekitHumiditySensorImpl(item, metadata, updater, bridge);

                case SWITCHABLE:
                    return new HomekitSwitchImpl(item, metadata, updater, bridge);

                case CURRENT_TEMPERATURE:
                    return new HomekitTemperatureSensorImpl(item, metadata, updater, bridge);

                case THERMOSTAT:
                    return new HomekitThermostatImpl(item, metadata, updater, bridge);

                case BLINDS:
                    return new HomekitWindowCovering(item, metadata, updater, bridge);

                default:
                    logger.warn("Unrecognized homekit tag for root item {}", tag);
            }
            return null;
        }).orElse(null);
    }
}
