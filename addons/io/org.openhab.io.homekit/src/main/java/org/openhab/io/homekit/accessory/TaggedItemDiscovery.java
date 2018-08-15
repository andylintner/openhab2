package org.openhab.io.homekit.accessory;

import java.util.function.Predicate;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;

public class TaggedItemDiscovery implements RegistryChangeListener<Item> {

    private final Predicate<String> tagFilter;
    private final AccessoryRegistry accessoryRegistry;

    public TaggedItemDiscovery(ItemRegistry itemRegistry, Predicate<String> tagFilter,
            AccessoryRegistry accessoryRegistry) {
        this.tagFilter = tagFilter;
        this.accessoryRegistry = accessoryRegistry;
        itemRegistry.addRegistryChangeListener(this);
    }

    @Override
    public void added(Item element) {
        if (tagMatches(element)) {
            accessoryRegistry.add(element, null);
        }
    }

    @Override
    public void removed(Item element) {
        if (tagMatches(element)) {
            accessoryRegistry.add(element, null);
        }
    }

    @Override
    public void updated(Item oldElement, @Nullable Item element) {
        if (tagMatches(oldElement)) {
            if (tagMatches(element)) {
                accessoryRegistry.itemUpdated(oldElement, element);
            } else {
                accessoryRegistry.remove(oldElement.getName());
            }
        } else if (tagMatches(element)) {
            accessoryRegistry.add(element, null);
        }
    }

    private boolean tagMatches(Item item) {
        return item.getTags().stream().anyMatch(tagFilter);
    }

}
