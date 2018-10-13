/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal.accessories;

import java.util.Optional;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.Metadata;
import org.openhab.io.homekit.accessory.registry.Accessory;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.OpenhabHomekitBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitAccessory;

/**
 * Abstract class for HomekitAccessory implementations, this provides the
 * accessory metadata using information from the underlying Item.
 *
 * @author Andy Lintner
 */
abstract class AbstractHomekitAccessoryImpl<T extends GenericItem> implements HomekitAccessory, Accessory {

    private final int accessoryId;
    private final String itemName;
    private final String itemLabel;
    private final HomekitAccessoryUpdater updater;
    private T item;
    private final Class<T> expectedItemClass;
    protected final OpenhabHomekitBridge bridge;

    private Metadata metadata;

    private Logger logger = LoggerFactory.getLogger(AbstractHomekitAccessoryImpl.class);

    @SuppressWarnings("unchecked")
    public AbstractHomekitAccessoryImpl(Item itemParam, Metadata metadata, HomekitAccessoryUpdater updater,
            OpenhabHomekitBridge bridge, Class<T> expectedItemClass) {
        this.bridge = bridge;
        this.accessoryId = calculateId(itemParam);
        this.metadata = metadata;
        this.itemName = itemParam.getName();
        this.itemLabel = itemParam.getLabel();
        this.updater = updater;
        this.expectedItemClass = expectedItemClass;
        Item item = findBaseItem(itemParam);

        typeCheck(item);
        this.item = (T) item;
        bridge.addAccessory(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updatedItem(Item oldItem, Item newItem) {
        typeCheck(newItem);
        logger.debug("Updated item type from {} to {}", oldItem.getName(), newItem.getName());
        this.item = (T) newItem;
    }

    @Override
    public void addChildItem(Item childItem, Optional<Metadata> metadata) {
        logger.error("Unhandled child item on {} - {}", item.getName(), childItem.getName());
    }

    @Override
    public void updatedMetadata(Metadata oldMetadata, Metadata newMetadata) {
        this.metadata = newMetadata;
    }

    @Override
    public void removed() {
        updater.unsubscribe(item);
        logger.debug("Accessory removed {}", item.getName());
    }

    @Override
    public int getId() {
        return accessoryId;
    }

    @Override
    public String getLabel() {
        return itemLabel;
    }

    @Override
    public String getManufacturer() {
        return "none";
    }

    @Override
    public String getModel() {
        return "none";
    }

    @Override
    public String getSerialNumber() {
        return "none";
    }

    @Override
    public void identify() {
        // We're not going to support this for now
    }

    protected String getItemName() {
        return itemName;
    }

    @Nullable
    protected Metadata getMetadata() {
        return metadata;
    }

    protected HomekitAccessoryUpdater getUpdater() {
        return updater;
    }

    protected T getItem() {
        return item;
    }

    private void typeCheck(Item item) {
        if (expectedItemClass != item.getClass() && !expectedItemClass.isAssignableFrom(item.getClass())) {
            logger.error("Type {} is a {} instead of the expected {}", item.getName(), item.getClass().getName(),
                    expectedItemClass.getName());
        }
    }

    private static Item findBaseItem(Item item) {
        if (item instanceof GroupItem && ((GroupItem) item).getBaseItem() != null) {
            return ((GroupItem) item).getBaseItem();
        } else {
            return item;
        }
    }

    private static int calculateId(Item item) {
        int id = new HashCodeBuilder().append(item.getName()).hashCode();
        if (id < 0) {
            id += Integer.MAX_VALUE;
        }
        if (id < 2) {
            id = 2; // 0 and 1 are reserved
        }
        return id;
    }
}
