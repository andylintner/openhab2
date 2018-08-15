package org.openhab.io.homekit.accessory;

import java.util.function.Predicate;

import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.Metadata;
import org.eclipse.smarthome.core.items.MetadataRegistry;

public class MetadataRegistryItemDiscovery implements RegistryChangeListener<Metadata> {

    private final ItemRegistry itemRegistry;
    private final Predicate<Metadata> metadataFilter;
    private final AccessoryRegistry accessoryRegistry;

    public MetadataRegistryItemDiscovery(MetadataRegistry metadataRegistry, ItemRegistry itemRegistry,
            Predicate<Metadata> metadataFilter, AccessoryRegistry accessoryRegistry) {
        this.itemRegistry = itemRegistry;
        this.metadataFilter = metadataFilter;
        this.accessoryRegistry = accessoryRegistry;
        metadataRegistry.addRegistryChangeListener(this);
    }

    @Override
    public void added(Metadata element) {
        if (metadataFilter.test(element)) {
            Item item = itemRegistry.get(element.getUID().getItemName());
            accessoryRegistry.add(item, element);
        }
    }

    @Override
    public void removed(Metadata element) {
        if (metadataFilter.test(element)) {
            accessoryRegistry.remove(element.getUID().getItemName());
        }
    }

    @Override
    public void updated(Metadata oldElement, Metadata element) {
        Item item = itemRegistry.get(element.getUID().getItemName());
        if (metadataFilter.test(oldElement)) {
            if (metadataFilter.test(element)) {
                accessoryRegistry.metadataUpdated(item, oldElement, element);
            } else {
                accessoryRegistry.remove(oldElement.getUID().getItemName());
            }
        } else if (metadataFilter.test(element)) {
            accessoryRegistry.add(item, element);
        }
    }

}
