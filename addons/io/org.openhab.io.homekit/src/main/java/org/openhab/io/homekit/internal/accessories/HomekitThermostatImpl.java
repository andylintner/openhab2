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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.Metadata;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitMetadataClass;
import org.openhab.io.homekit.internal.HomekitTag;
import org.openhab.io.homekit.internal.OpenhabHomekitBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.properties.ThermostatMode;
import com.beowulfe.hap.accessories.thermostat.BasicThermostat;

/**
 * Implements Thermostat as a GroupedAccessory made up of multiple items:
 * <ul>
 * <li>Cooling Threshold: Decimal type</li>
 * <li>Heating Threshold: Decimal type</li>
 * <li>Auto Threshold: Decimal type</li>
 * <li>Current Temperature: Decimal type</li>
 * <li>Heating/Cooling Mode: String type (see HomekitSettings.thermostat*Mode)</li>
 * </ul>
 *
 * @author Andy Lintner
 */
class HomekitThermostatImpl extends AbstractTemperatureHomekitAccessoryImpl<GroupItem> implements BasicThermostat {

    private Item currentTemperatureItem;
    private Item heatingCoolingModeItem;
    private Item targetTemperatureItem;

    private static final String DEFAULT_COOL_MODE = "Cooling";
    private static final String DEFAULT_HEAT_MODE = "Heating";
    private static final String DEFAULT_AUTO_MODE = "Auto";
    private static final String DEFAULT_OFF_MODE = "Off";

    private Logger logger = LoggerFactory.getLogger(HomekitThermostatImpl.class);

    public HomekitThermostatImpl(Item item, Metadata metadata, HomekitAccessoryUpdater updater,
            OpenhabHomekitBridge bridge) {
        super(item, metadata, updater, bridge, GroupItem.class);
    }

    @Override
    public void addChildItem(Item childItem, Optional<Metadata> metadata) {
        if (metadata.isPresent()) {
            HomekitMetadataClass mc = HomekitMetadataClass.fromMetadataClass(metadata.get().getValue());
            switch (mc) {
                case THERMOSTAT_CURRENT_TEMPERATURE:
                    currentTemperatureItem = childItem;
                    break;

                case THERMOSTAT_TARGET_TEMPERATURE:
                    targetTemperatureItem = childItem;
                    break;

                case THERMOSTAT_HEATING_COOLING_MODE:
                    heatingCoolingModeItem = childItem;
                    break;

                default:
                    logger.error("Unrecognized accessory child: {}", mc);
                    break;
            }
            return;
        }
        HomekitTag.fromItem(childItem).ifPresent(tag -> {
            switch (tag) {
                case CURRENT_TEMPERATURE:
                    currentTemperatureItem = childItem;
                    break;

                case TARGET_TEMPERATURE:
                    targetTemperatureItem = childItem;
                    break;

                case HEATING_COOLING_MODE:
                    heatingCoolingModeItem = childItem;
                    break;

                default:
                    logger.error("Unrecognized accessory child: {}", tag);
                    break;
            }
        });
    }

    @Override
    public CompletableFuture<ThermostatMode> getCurrentMode() {
        if (heatingCoolingModeItem == null) {
            return CompletableFuture.completedFuture(null);
        }
        State state = heatingCoolingModeItem.getState();
        ThermostatMode mode;
        String stringValue = state.toString();

        if (stringValue.equals(getCoolModeValue())) {
            mode = ThermostatMode.COOL;
        } else if (stringValue.equals(getHeatModeValue())) {
            mode = ThermostatMode.HEAT;
        } else if (stringValue.equals(getAutoModeValue())) {
            mode = ThermostatMode.AUTO;
        } else if (stringValue.equals(getOffModeValue())) {
            mode = ThermostatMode.OFF;
        } else if (stringValue.equals("UNDEF") || stringValue.equals("NULL")) {
            logger.debug("Heating cooling target mode not available. Relaying value of OFF to Homekit");
            mode = ThermostatMode.OFF;
        } else {
            logger.error(
                    "Unrecognized heating cooling target mode: {}. Expected cool, heat, auto, or off strings in value.",
                    stringValue);
            mode = ThermostatMode.OFF;
        }
        return CompletableFuture.completedFuture(mode);
    }

    @Override
    public CompletableFuture<Double> getCurrentTemperature() {
        if (currentTemperatureItem != null) {
            DecimalType state = currentTemperatureItem.getStateAs(DecimalType.class);
            if (state == null) {
                return CompletableFuture.completedFuture(null);
            }
            return CompletableFuture.completedFuture(state.doubleValue());
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<ThermostatMode> getTargetMode() {
        return getCurrentMode();
    }

    @Override
    public CompletableFuture<Double> getTargetTemperature() {
        if (targetTemperatureItem != null) {
            DecimalType state = targetTemperatureItem.getStateAs(DecimalType.class);
            if (state == null) {
                return CompletableFuture.completedFuture(null);
            }
            return CompletableFuture.completedFuture(state.doubleValue());
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public void setTargetMode(ThermostatMode mode) throws Exception {
        String modeString = null;
        switch (mode) {
            case AUTO:
                modeString = getAutoModeValue();
                break;

            case COOL:
                modeString = getCoolModeValue();
                break;

            case HEAT:
                modeString = getHeatModeValue();
                break;

            case OFF:
                modeString = getOffModeValue();
                break;
        }
        ((StringItem) heatingCoolingModeItem).send(new StringType(modeString));
    }

    @Override
    public void setTargetTemperature(Double value) throws Exception {
        ((NumberItem) targetTemperatureItem).send(new DecimalType(BigDecimal.valueOf(value)));
    }

    @Override
    public void subscribeCurrentMode(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe((GenericItem) heatingCoolingModeItem, callback);
    }

    @Override
    public void subscribeCurrentTemperature(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe((GenericItem) currentTemperatureItem, callback);
    }

    @Override
    public void subscribeTargetMode(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe((GenericItem) heatingCoolingModeItem, callback);
    }

    @Override
    public void subscribeTargetTemperature(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe((GenericItem) targetTemperatureItem, callback);
    }

    @Override
    public void unsubscribeCurrentMode() {
        getUpdater().unsubscribe((GenericItem) heatingCoolingModeItem);
    }

    @Override
    public void unsubscribeCurrentTemperature() {
        getUpdater().unsubscribe((GenericItem) currentTemperatureItem);
    }

    @Override
    public void unsubscribeTargetMode() {
        getUpdater().unsubscribe((GenericItem) heatingCoolingModeItem);
    }

    @Override
    public void unsubscribeTargetTemperature() {
        getUpdater().unsubscribe((GenericItem) targetTemperatureItem);
    }

    private String getCoolModeValue() {
        // TODO: Get from metadata
        return DEFAULT_COOL_MODE;
    }

    private String getHeatModeValue() {
        // TODO: Get from metadata
        return DEFAULT_HEAT_MODE;
    }

    private String getAutoModeValue() {
        // TODO: Get from metadata
        return DEFAULT_AUTO_MODE;
    }

    private String getOffModeValue() {
        // TODO: Get from metadata
        return DEFAULT_OFF_MODE;
    }

}
