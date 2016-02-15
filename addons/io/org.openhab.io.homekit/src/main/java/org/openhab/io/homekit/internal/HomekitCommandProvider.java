package org.openhab.io.homekit.internal;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.eclipse.smarthome.core.storage.StorageService;
import org.openhab.io.homekit.Homekit;

public class HomekitCommandProvider implements CommandProvider {

    private StorageService storageService;
    private Homekit homekit;

    @Override
    public String getHelp() {
        return "---Homekit commands---\n\t" + "clearHomekitPairings: remove all pairings with Homekit clients\n\t"
                + "allowUnauthenticatedHomekitRequests <boolean>: enable or disable unauthenticated access to facilitate debugging\n";
    }

    public void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }

    public void setHomekit(Homekit homekit) {
        this.homekit = homekit;
    }

    public void _clearHomekitPairings(CommandInterpreter interpreter)
            throws InvalidAlgorithmParameterException, IOException {
        new HomekitAuthInfoImpl(storageService, null).clear();
        homekit.refreshAuthInfo();
        interpreter.println("Cleared homekit pairings");
    }

    public void _allowUnauthenticatedHomekitRequests(CommandInterpreter interpreter) {
        String arg = interpreter.nextArgument();
        if (arg == null) {
            interpreter.println("true/false is required as an argument");
            return;
        }
        boolean allow = Boolean.parseBoolean(arg);
        homekit.allowUnauthenticatedRequests(allow);
        interpreter.println((allow ? "Enabled " : "Disabled ") + "unauthenticated homekit access");
    }

}
