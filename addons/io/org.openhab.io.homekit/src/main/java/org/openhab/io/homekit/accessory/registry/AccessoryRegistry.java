package org.openhab.io.homekit.accessory.registry;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.Metadata;
import org.eclipse.smarthome.core.items.MetadataKey;
import org.eclipse.smarthome.core.items.MetadataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccessoryRegistry {

    private final Map<String, Accessory> accessoriesByName = new ConcurrentHashMap<>();
    private final BiFunction<Item, Metadata, Accessory> accessoryFactory;
    private final Logger logger = LoggerFactory.getLogger(AccessoryRegistry.class);
    private final String metadataNamespace;
    private final MetadataRegistry metadataRegistry;

    public AccessoryRegistry(BiFunction<Item, Metadata, Accessory> accessoryFactory, String metadataNamespace,
            MetadataRegistry metadataRegistry) {
        this.accessoryFactory = accessoryFactory;
        this.metadataNamespace = metadataNamespace;
        this.metadataRegistry = metadataRegistry;
    }

    synchronized void add(Item item, @Nullable Metadata metadata) {
        if (!accessoriesByName.containsKey(item.getName())) {
            for (String groupName : item.getGroupNames()) {
                if (accessoriesByName.containsKey(groupName)) {
                    accessoriesByName.get(groupName).addChildItem(item,
                            Optional.ofNullable(metadata != null ? metadata : getMetadata(item)));
                    return;
                }
            }
            Accessory accessory = accessoryFactory.apply(item, metadata != null ? metadata : getMetadata(item));
            if (accessory != null) {
                accessoriesByName.put(item.getName(), accessory);
            }
        } else {
            logger.warn("Discovered duplicate item: {}", item.getName());
        }
    }

    synchronized void remove(String itemName) {
        if (accessoriesByName.containsKey(itemName)) {
            accessoriesByName.remove(itemName).removed();
        }
    }

    synchronized void metadataUpdated(Item item, Metadata oldMetadata, Metadata newMetadata) {
        Accessory accessory = accessoriesByName.get(item.getName());
        if (accessory != null) {
            accessory.updatedMetadata(oldMetadata, newMetadata);
        }
    }

    synchronized void itemUpdated(Item oldElement, Item element) {
        if (!oldElement.getName().equals(element.getName())) {
            Accessory accessory = accessoriesByName.remove(oldElement.getName());
            if (accessory != null) {
                accessoriesByName.put(element.getName(), accessory);
            }
        }
        Accessory accessory = accessoriesByName.get(element.getName());
        if (accessory != null) {
            accessory.updatedItem(oldElement, element);
        }
    }

    Metadata getMetadata(Item item) {
        return metadataRegistry.get(new MetadataKey(metadataNamespace, item.getName()));
    }
}
