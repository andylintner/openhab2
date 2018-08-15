package org.openhab.io.homekit.accessory;

import java.util.Optional;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.Metadata;

public interface Accessory {

    public void addChildItem(Item item, Optional<Metadata> metadata);

    public void removed();

    public void updatedMetadata(Metadata oldMetadata, Metadata newMetadata);

    public void updatedItem(Item oldItem, Item newItem);
}
