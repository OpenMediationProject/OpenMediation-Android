// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.core;

import java.util.HashMap;
import java.util.Map;

public final class CallbackBridge {
    private static Map<String, AbstractAdsManager> mListeners = new HashMap<>();

    public static void addListenerToMap(String placementId, AbstractAdsManager adsManager) {
        mListeners.put(placementId, adsManager);
    }

    public static AbstractAdsManager getListener(String placementId) {
        return mListeners.get(placementId);
    }

    public static void removeListenerFromMap(String placementId) {
        mListeners.remove(placementId);
    }
}
