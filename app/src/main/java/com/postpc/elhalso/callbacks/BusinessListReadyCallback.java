package com.postpc.elhalso.callbacks;

import com.postpc.elhalso.data.Business;

import java.util.List;

/**
 * Callback for when a businesses list is retrieved from Firebase
 */
public interface BusinessListReadyCallback {
    void onBusinessListReady(List<Business> businessList);
}
