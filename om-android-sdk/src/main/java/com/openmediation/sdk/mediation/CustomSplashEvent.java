package com.openmediation.sdk.mediation;

import android.app.Activity;
import android.view.ViewGroup;

public abstract class CustomSplashEvent extends CustomAdEvent {

    /**
     * @param container the Splash AD Container
     */
    public void show(Activity activity, ViewGroup container) {}

    public void show(Activity activity) {}

    public abstract boolean isReady();

    protected synchronized void onInsShowSuccess() {
        CallbackManager.getInstance().onInsShowSuccess(mPlacementId, mInstancesKey, mInsId);
    }

    protected synchronized void onInsShowFailed(AdapterError error) {
        CallbackManager.getInstance().onInsShowFailed(mPlacementId, mInstancesKey, mInsId, error);
    }

    protected synchronized void onInsDismissed() {
        CallbackManager.getInstance().onInsClosed(mPlacementId, mInstancesKey, mInsId);
    }

    protected synchronized void onInsTick(long millisUntilFinished) {
        CallbackManager.getInstance().onInsTick(mPlacementId, mInstancesKey, mInsId, millisUntilFinished);
    }
}
