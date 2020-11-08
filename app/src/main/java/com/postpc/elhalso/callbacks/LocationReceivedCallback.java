package com.postpc.elhalso.callbacks;

import com.postpc.elhalso.data.LocationInfo;

/**
 * A callback for when a location is received
 */
public interface LocationReceivedCallback {
    void onLocationReceived(LocationInfo location);
}
