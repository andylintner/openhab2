package org.openhab.io.homekit.internal;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitAccessory;
import com.beowulfe.hap.HomekitRoot;
import com.beowulfe.hap.HomekitServer;

public class OpenhabHomekitBridge {

    private String targetState = null;
    private int targetVersion = 0;

    private final HomekitRoot root;
    private final HomekitServer homekit;
    private final Storage<String> storage;
    private final int startupTimeout;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final Logger logger = LoggerFactory.getLogger(OpenhabHomekitBridge.class);
    private final Map<Integer, Class<? extends HomekitAccessory>> currentState = new HashMap<>();
    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(THREADPOOL_NAME);

    private final static String THREADPOOL_NAME = "homekit";
    @NonNull
    private final static String STORAGE_TARGET_STATE_KEY = "targetState";
    @NonNull
    private final static String STORAGE_TARGET_VERSION_KEY = "targetVersion";

    public OpenhabHomekitBridge(HomekitSettings settings, StorageService storageService)
            throws InvalidAlgorithmParameterException, IOException {
        storage = storageService.getStorage("homekit");
        homekit = new HomekitServer(settings.getNetworkInterface(), settings.getPort());
        root = homekit.createBridge(new HomekitAuthInfoImpl(storageService, settings.getPin()), settings.getName(),
                settings.getManufacturer(), settings.getModel(), settings.getSerialNumber());
        targetState = storage.get(STORAGE_TARGET_STATE_KEY);
        targetVersion = Optional.ofNullable(storage.get(STORAGE_TARGET_VERSION_KEY)).map(s -> Integer.parseInt(s))
                .orElse(0);
        startupTimeout = settings.getStartupTimeout();
        scheduler.schedule(() -> this.timeoutExpired(), startupTimeout, TimeUnit.SECONDS);
    }

    public void stop() {
        root.stop();
        homekit.stop();
        currentState.clear();
    }

    public void refreshAuthInfo() throws IOException {
        root.refreshAuthInfo();
    }

    public void allowUnauthenticatedRequests(boolean allow) {
        root.allowUnauthenticatedRequests(allow);
    }

    public void removeAccessory(HomekitAccessory accessory) {
        root.removeAccessory(accessory);
    }

    public void addAccessory(HomekitAccessory accessory) {
        root.addAccessory(accessory);
        currentState.put(accessory.getId(), accessory.getClass());

        if (!started.get()) {
            if (serializeState().equals(targetState)) {
                logger.info("Reached homekit target state for version {}, starting", targetVersion);
                if (started.compareAndSet(false, true)) {
                    doStart();
                }
            } else {
                logger.debug("Delaying homekit startup until target state is reached or timeout of {} seconds expires",
                        startupTimeout);
            }
        } else {
            updateTarget();
            try {
                root.setConfigurationIndex(targetVersion);
            } catch (IOException e) {
                logger.error("Could not set homekit configuration index", e);
            }
            logger.debug("New homekit target state set");
        }
    }

    private String serializeState() {
        return currentState.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).sorted()
                .collect(Collectors.joining(" "));
    }

    private void doStart() {
        try {
            root.setConfigurationIndex(targetVersion);
        } catch (IOException e) {
            logger.error("Could not set homekit configuration index", e);
        }
        root.start();
    }

    private synchronized void updateTarget() {
        targetVersion++;
        targetState = serializeState();
        storage.put(STORAGE_TARGET_STATE_KEY, targetState);
        storage.put(STORAGE_TARGET_VERSION_KEY, Integer.toString(targetVersion));
    }

    private void timeoutExpired() {
        if (started.compareAndSet(false, true)) {
            updateTarget();
            doStart();
            logger.info("Timeout of {} seconds expired. Homekit started with new version {}.", startupTimeout,
                    targetVersion);
        }
    }
}
