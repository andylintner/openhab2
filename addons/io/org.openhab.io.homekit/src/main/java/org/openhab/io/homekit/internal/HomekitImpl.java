/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.MetadataRegistry;
import org.eclipse.smarthome.core.storage.StorageService;
import org.openhab.io.homekit.Homekit;
import org.openhab.io.homekit.accessory.registry.AccessoryRegistry;
import org.openhab.io.homekit.accessory.registry.MetadataRegistryItemDiscovery;
import org.openhab.io.homekit.accessory.registry.ItemDiscovery;
import org.openhab.io.homekit.internal.accessories.HomekitAccessoryFactory;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to openHAB items via the Homekit API
 *
 * @author Andy Lintner
 */
public class HomekitImpl implements Homekit {

    private final HomekitSettings settings = new HomekitSettings();
    private AccessoryRegistry accessoryRegistry;
    @SuppressWarnings("unused") // Keep a reference
    private ItemDiscovery taggedItemDiscovery;
    @SuppressWarnings("unused") // Keep a reference
    private MetadataRegistryItemDiscovery metadataRegistryItemDiscovery;
    private OpenhabHomekitBridge bridge = null;
    private StorageService storageService;
    private Logger logger = LoggerFactory.getLogger(HomekitImpl.class);
    private ItemRegistry itemRegistry;
    private MetadataRegistry metadataRegistry;

    private static final String METADATA_NAMESPACE = "homekit";

    public void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }

    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    public void setMetadataRegistry(MetadataRegistry metadataRegistry) {
        this.metadataRegistry = metadataRegistry;
    }

    protected synchronized void activate(ComponentContext componentContext) {
        modified(componentContext);
        accessoryRegistry = new AccessoryRegistry(new HomekitAccessoryFactory(new HomekitAccessoryUpdater(), bridge),
                METADATA_NAMESPACE, metadataRegistry);
        taggedItemDiscovery = new ItemDiscovery(itemRegistry, METADATA_NAMESPACE, metadataRegistry,
                HomekitTag::hasTag, accessoryRegistry);
        metadataRegistryItemDiscovery = new MetadataRegistryItemDiscovery(metadataRegistry, itemRegistry, "homekit",
                accessoryRegistry);
    }

    protected synchronized void modified(ComponentContext componentContext) {
        try {
            settings.fill(componentContext.getProperties());
        } catch (UnknownHostException e) {
            logger.debug("Could not initialize homekit: {}", e.getMessage(), e);
            return;
        }
        try {
            start();
        } catch (Exception e) {
            logger.error("Could not initialize homekit: {}", e.getMessage(), e);
        }
    }

    protected void deactivate() {
        if (bridge != null) {
            bridge.stop();
            bridge = null;
        }
    }

    @Override
    public void refreshAuthInfo() throws IOException {
        if (bridge != null) {
            bridge.refreshAuthInfo();
        }
    }

    @Override
    public void allowUnauthenticatedRequests(boolean allow) {
        if (bridge != null) {
            bridge.allowUnauthenticatedRequests(allow);
        }
    }

    private void start() throws IOException, InvalidAlgorithmParameterException {
        if (bridge == null) {
            bridge = new OpenhabHomekitBridge(settings, storageService);
            // TODO: Add all accessories back
        }
    }
}
