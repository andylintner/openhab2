/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal.accessories;

import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.Metadata;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.OpenhabHomekitBridge;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.Lightbulb;

/**
 * Abstract class implementing a Homekit Lightbulb using a SwitchItem
 *
 * @author Andy Lintner
 */
abstract class AbstractHomekitLightbulbImpl<T extends SwitchItem> extends AbstractHomekitAccessoryImpl<T>
        implements Lightbulb {

    public AbstractHomekitLightbulbImpl(Item item, Metadata metadata, HomekitAccessoryUpdater updater,
            OpenhabHomekitBridge bridge, Class<T> expectedItemClass) {
        super(item, metadata, updater, bridge, expectedItemClass);
    }

    @Override
    public CompletableFuture<Boolean> getLightbulbPowerState() {
        OnOffType state = getItem().getStateAs(OnOffType.class);
        return CompletableFuture.completedFuture(state != null ? state == OnOffType.ON : null);
    }

    @Override
    public CompletableFuture<Void> setLightbulbPowerState(boolean value) throws Exception {
        GenericItem item = getItem();
        if (item instanceof SwitchItem) {
            ((SwitchItem) item).send(value ? OnOffType.ON : OnOffType.OFF);
        } else if (item instanceof GroupItem) {
            ((GroupItem) item).send(value ? OnOffType.ON : OnOffType.OFF);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeLightbulbPowerState(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), callback);
    }

    @Override
    public void unsubscribeLightbulbPowerState() {
        getUpdater().unsubscribe(getItem());
    }

}
