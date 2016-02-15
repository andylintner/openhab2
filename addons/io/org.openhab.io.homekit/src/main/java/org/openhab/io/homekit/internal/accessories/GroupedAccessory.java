package org.openhab.io.homekit.internal.accessories;

import org.openhab.io.homekit.internal.HomekitTaggedItem;

import com.beowulfe.hap.HomekitAccessory;

/**
 * An accessory that is too complex to be represented by a single item. A
 * grouped accessory is made up of multiple items, each implementing a single
 * characteristic of the accessory.
 *
 * @author Andy Lintner
 */
public interface GroupedAccessory extends HomekitAccessory {

    public String getGroupName();

    public void addCharacteristic(HomekitTaggedItem item);

    public boolean isComplete();
}
