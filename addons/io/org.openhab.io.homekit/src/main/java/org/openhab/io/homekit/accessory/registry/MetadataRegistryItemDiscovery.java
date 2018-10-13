package org.openhab.io.homekit.accessory.registry;

import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.Metadata;
import org.eclipse.smarthome.core.items.MetadataKey;
import org.eclipse.smarthome.core.items.MetadataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataRegistryItemDiscovery implements RegistryChangeListener<Metadata> {

    private final ItemRegistry itemRegistry;
    private final AccessoryRegistry accessoryRegistry;
    private final String metadataNamespace;
    private final Logger logger = LoggerFactory.getLogger(MetadataRegistryItemDiscovery.class);

    public MetadataRegistryItemDiscovery(MetadataRegistry metadataRegistry, ItemRegistry itemRegistry,
            String metadataNamespace, AccessoryRegistry accessoryRegistry) {
        this.itemRegistry = itemRegistry;
        this.metadataNamespace = metadataNamespace;
        this.accessoryRegistry = accessoryRegistry;
        metadataRegistry.addRegistryChangeListener(this);
    }

    @Override
    public void added(Metadata element) {
        if (isCorrectNamepsace(element)) {
            Item item = itemRegistry.get(element.getUID().getItemName());
            if (item == null) {
                logger.warn("Could not find tagged item: " + element.getUID());
                return;
            }
            accessoryRegistry.add(item, element);
        }
    }

    @Override
    public void removed(Metadata element) {
        if (isCorrectNamepsace(element)) {
            accessoryRegistry.remove(element.getUID().getItemName());
        }
    }

    @Override
    public void updated(Metadata oldElement, Metadata element) {
        Item item = itemRegistry.get(element.getUID().getItemName());
        if (isCorrectNamepsace(oldElement)) {
            if (isCorrectNamepsace(element)) {
                accessoryRegistry.metadataUpdated(item, oldElement, element);
            } else {
                accessoryRegistry.remove(oldElement.getUID().getItemName());
            }
        } else if (isCorrectNamepsace(element)) {
            accessoryRegistry.add(item, element);
        }
    }

    private boolean isCorrectNamepsace(Metadata element) {
        MetadataKey key = element.getUID();
        return key.getNamespace().equals(metadataNamespace);
    }

}
