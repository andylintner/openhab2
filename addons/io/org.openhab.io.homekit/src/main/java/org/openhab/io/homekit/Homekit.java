package org.openhab.io.homekit;

import java.io.IOException;

public interface Homekit {

    public void refreshAuthInfo() throws IOException;

    void allowUnauthenticatedRequests(boolean allow);
}
