package org.openhab.io.homekit.accessory.registry;

import java.util.function.Predicate;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.Metadata;
import org.eclipse.smarthome.core.items.MetadataKey;
import org.eclipse.smarthome.core.items.MetadataRegistry;

public class ItemDiscovery implements RegistryChangeListener<Item> {

    private final Predicate<String> tagFilter;
    private final AccessoryRegistry accessoryRegistry;
    private final MetadataRegistry metadataRegistry;
    private final String metadataNamespace;

    public ItemDiscovery(ItemRegistry itemRegistry, String metadataNamespace, MetadataRegistry metadataRegistry,
            Predicate<String> tagFilter, AccessoryRegistry accessoryRegistry) {
        this.tagFilter = tagFilter;
        this.accessoryRegistry = accessoryRegistry;
        this.metadataNamespace = metadataNamespace;
        this.metadataRegistry = metadataRegistry;
        itemRegistry.addRegistryChangeListener(this);
    }

    @Override
    public void added(Item element) {
        Metadata metadata = getMetadata(element);
        if (metadata != null && isCorrectNamespace(metadata)) {
            accessoryRegistry.add(element, metadata);
        } else if (tagMatches(element)) {
            accessoryRegistry.add(element, metadata);
        }
    }

    @Override
    public void removed(Item element) {
        Metadata metadata = getMetadata(element);
        if (metadata != null && isCorrectNamespace(metadata)) {
            accessoryRegistry.remove(element.getName());
        } else if (tagMatches(element)) {
            accessoryRegistry.remove(element.getName());
        }
    }

    @Override
    public void updated(Item oldElement, @Nullable Item element) {
        Metadata oldMetadata = getMetadata(oldElement);
        Metadata metadata = getMetadata(element);
        if (oldMetadata != null && isCorrectNamespace(oldMetadata)) {
            if (metadata != null && isCorrectNamespace(metadata)) {
                accessoryRegistry.itemUpdated(oldElement, element);
            } else {
                accessoryRegistry.remove(oldElement.getName());
            }
        } else if (isCorrectNamespace(metadata)) {
            accessoryRegistry.add(element, metadata);
        } else if (tagMatches(oldElement)) {
            if (tagMatches(element)) {
                accessoryRegistry.itemUpdated(oldElement, element);
            } else {
                accessoryRegistry.remove(oldElement.getName());
            }
        } else if (tagMatches(element)) {
            accessoryRegistry.add(element, metadata);
        }
    }

    private boolean tagMatches(Item item) {
        return item.getTags().stream().anyMatch(tagFilter)
                && metadataRegistry.get(new MetadataKey(metadataNamespace, item.getName())) == null;
    }

    private Metadata getMetadata(Item item) {
        return metadataRegistry.get(new MetadataKey(metadataNamespace, item.getName()));
    }

    private boolean isCorrectNamespace(Metadata element) {
        MetadataKey key = element.getUID();
        return key.getNamespace().equals(metadataNamespace);
    }

}
