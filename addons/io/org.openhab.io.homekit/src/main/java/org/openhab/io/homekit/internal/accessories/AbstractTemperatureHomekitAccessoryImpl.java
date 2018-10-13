/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal.accessories;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.Metadata;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.OpenhabHomekitBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.accessories.TemperatureSensor;

/**
 *
 * @author Andy Lintner - Initial contribution
 */
abstract class AbstractTemperatureHomekitAccessoryImpl<T extends GenericItem> extends AbstractHomekitAccessoryImpl<T>
        implements TemperatureSensor {

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractTemperatureHomekitAccessoryImpl.class);

    private final static String METADATA_CONFIGURATION_KEY_MAXIMUM_TEMPERATURE = "maximumTemperature";
    private final static String METADATA_CONFIGURATION_KEY_MINIMUM_TEMPERATURE = "minimumTemperature";

    public AbstractTemperatureHomekitAccessoryImpl(Item item, Metadata metadata, HomekitAccessoryUpdater updater,
            OpenhabHomekitBridge bridge, Class<T> expectedItemClass) {
        super(item, metadata, updater, bridge, expectedItemClass);
    }

    @Override
    public double getMaximumTemperature() {
        Metadata metadata = getMetadata();
        Double fromMetadata = metadata != null
                ? toDouble(metadata.getConfiguration().get(METADATA_CONFIGURATION_KEY_MAXIMUM_TEMPERATURE))
                : null;
        return fromMetadata != null ? fromMetadata : bridge.getSettings().getMaximumTemperature();
    }

    @Override
    public double getMinimumTemperature() {
        Metadata metadata = getMetadata();
        Double fromMetadata = metadata != null
                ? toDouble(metadata.getConfiguration().get(METADATA_CONFIGURATION_KEY_MINIMUM_TEMPERATURE))
                : null;
        return fromMetadata != null ? fromMetadata : bridge.getSettings().getMinimumTemperature();
    }

    private Double toDouble(Object value) {
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).doubleValue();
        } else {
            LOGGER.warn("Unexpected type for metadata value: " + value.getClass());
            return null;
        }
    }
}
