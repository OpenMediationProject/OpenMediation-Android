// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mediation;

import android.app.Activity;
import android.text.TextUtils;

import java.util.Map;

public abstract class CustomAdEvent {

    protected String mPlacementId;
    protected String mInstancesKey;
    protected boolean isDestroyed;

    private String mInsId;

    public void loadAd(Activity activity, Map<String, String> config) throws Throwable {
        isDestroyed = false;
    }

    public abstract int getMediation();

    public abstract void destroy(Activity activity);

    protected boolean check(Activity activity, Map<String, String> config) {
        mPlacementId = config.get("PlacementId");
        if (TextUtils.isEmpty(mPlacementId)) {
            onInsError("PlacementId is empty");
            return false;
        }

        mInstancesKey = config.get("InstanceKey");
        if (TextUtils.isEmpty(mInstancesKey)) {
            onInsError("Mediation PlacementId is empty");
            return false;
        }

        mInsId = config.get("InstanceId");
        if (TextUtils.isEmpty(mInsId)) {
            onInsError("Mediation InstanceId is empty");
            return false;
        }
        return true;
    }

    protected void onInsClicked() {
        CallbackManager.getInstance().onInsClick(mPlacementId, mInstancesKey, mInsId);
    }

    protected synchronized void onInsError(String error) {
        CallbackManager.getInstance().onInsError(mPlacementId, mInstancesKey, mInsId, error);
    }

    protected synchronized void onInsReady(Object o) {
        CallbackManager.getInstance().onInsReady(mPlacementId, mInstancesKey, mInsId, o);
    }
}
