package org.openhab.io.homekit.internal;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.storage.StorageService;
import org.openhab.io.homekit.Homekit;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitRoot;
import com.beowulfe.hap.HomekitServer;

/**
 * Provides access to OpenHAB items via the Homekit API
 *
 * @author Andy Lintner
 */
public class HomekitImpl implements Homekit {

    private final HomekitSettings settings = new HomekitSettings();
    private HomekitServer homekit;
    private HomekitRoot bridge;
    private StorageService storageService;
    private final HomekitChangeListener homekitRegistry = new HomekitChangeListener();
    private Logger logger = LoggerFactory.getLogger(HomekitImpl.class);

    public void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }

    public void setItemRegistry(ItemRegistry itemRegistry) {
        homekitRegistry.setSettings(settings);
        homekitRegistry.setItemRegistry(itemRegistry);
    }

    @Activate
    protected synchronized void activate(ComponentContext componentContext) {
        try {
            settings.fill(componentContext.getProperties());
            homekitRegistry.setSettings(settings);
        } catch (UnknownHostException e) {
            logger.error("Could not activate homekit io: " + e.getMessage(), e);
            return;
        }
        try {
            start();
        } catch (Exception e) {
            logger.error("Could not initialize homekit: " + e.getMessage(), e);
        }
    }

    @Deactivate
    protected void deactivate() {
        homekitRegistry.clearAccessories();
        bridge.stop();
        homekit.stop();
        bridge = null;
        homekit = null;
        homekitRegistry.setBridge(null);
        homekitRegistry.stop();
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
        homekit = new HomekitServer(settings.getNetworkInterface(), settings.getPort());
        bridge = homekit.createBridge(new HomekitAuthInfoImpl(storageService, settings.getPin()), settings.getName(),
                settings.getManufacturer(), settings.getModel(), settings.getSerialNumber());
        bridge.start();
        homekitRegistry.setBridge(bridge);
    }
}
