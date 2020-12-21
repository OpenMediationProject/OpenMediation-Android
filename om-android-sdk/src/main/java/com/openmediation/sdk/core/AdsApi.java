// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core;

import android.app.Activity;

import com.openmediation.sdk.utils.ActLifecycle;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.model.Instance;
import com.openmediation.sdk.utils.model.Placement;
import com.openmediation.sdk.utils.model.PlacementInfo;

import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * The type Ads api.
 */
public abstract class AdsApi {
    protected WeakReference<Activity> mActivityReference = new WeakReference<>(null);

    /**
     * Returns placement info for the ad type
     *
     * @return placement info
     */
    protected abstract PlacementInfo getPlacementInfo();

    /**
     * Instance initialization method
     *
     * @param instance the instance
     */
    protected void initInsAndSendEvent(Instance instance) {
        if (mActivityReference == null || mActivityReference.get() == null) {
            mActivityReference = new WeakReference<Activity>(ActLifecycle.getInstance().getActivity());
        }
    }

    /**
     * Checks if an instance is available
     *
     * @param instance the instance to be checked
     * @return whether the instance's adapter is available
     */
    protected abstract boolean isInsAvailable(Instance instance);

    /**
     * Instance shows ads
     *
     * @param instance the instance to show ads
     */
    protected abstract void insShow(Instance instance);

    /**
     * Instance loads ads
     *
     * @param instance the instance to load ads
     * @param extras   the extras
     */
    protected abstract void insLoad(Instance instance, Map<String, Object> extras);

    /**
     * Called when availability changes
     *
     * @param available availability
     * @param error     the error
     */
    protected abstract void onAvailabilityChanged(boolean available, Error error);

    /**
     * On resume.
     *
     * @param activity the activity
     */
    public void onResume(Activity activity) {
    }

    /**
     * On pause.
     *
     * @param activity the activity
     */
    public void onPause(Activity activity) {
    }

    /**
     * Callback load error.
     *
     * @param error the error
     */
    protected void callbackLoadError(Error error) {
    }

    /**
     * Callback available on manual.
     */
    protected void callbackAvailableOnManual() {
    }

    /**
     * Callback load success on manual.
     */
    protected void callbackLoadSuccessOnManual() {

    }

    /**
     * Callback load failed on manual.
     *
     * @param error the error
     */
    protected void callbackLoadFailedOnManual(Error error) {
    }

    /**
     * Callback show error.
     *
     * @param error the error
     */
    protected void callbackShowError(Error error) {
    }

    /**
     * Callback ad closed.
     */
    protected void callbackAdClosed() {
    }

    /**
     * Sets current placement.
     *
     * @param placement the placement
     */
    protected void setCurrentPlacement(Placement placement) {
    }

    /**
     * before load starts, checks: init---frequency control---show in progress---trigger type
     * When manually triggered, first checks available ads, and replenishes if necessary before checking if loading is in progress
     * Tiggers other than Manual are automatically called by the SDK,
     *
     * @param type load triggered by: Manual,Init,AdClose,Interval
     */
    protected void loadAdWithAction(OmManager.LOAD_TYPE type) {
    }

    /**
     * For an instance to load ads
     *
     * @param instance the instance
     */
    protected void loadInsAndSendEvent(Instance instance) {
    }

    /**
     * Show ad.
     *
     * @param scene the scene
     */
    protected void showAd(String scene) {
    }

    /**
     * Is placement available boolean.
     *
     * @return the boolean
     */
    protected boolean isPlacementAvailable() {
        return false;
    }

    /**
     * Has available cache boolean.
     *
     * @return the boolean
     */
    protected boolean hasAvailableCache() {
        return false;
    }

    /**
     * Should notify available changed boolean.
     *
     * @param available the available
     * @return the boolean
     */
    protected boolean shouldNotifyAvailableChanged(boolean available) {
        return false;
    }

    /**
     * On ins init failed.
     *
     * @param instance the instance
     * @param error    the error
     */
    protected void onInsInitFailed(Instance instance, Error error) {
    }

    /**
     * On ins ready.
     *
     * @param instance the instance
     */
    protected synchronized void onInsReady(final Instance instance) {
    }

    /**
     * On ins load failed.
     *
     * @param instance the instance
     * @param error    the error
     */
    protected synchronized void onInsLoadFailed(Instance instance, Error error) {
    }

    /**
     * On ins open.
     *
     * @param instance the instance
     */
    protected void onInsOpen(final Instance instance) {
    }

    /**
     * On ins click.
     *
     * @param instance the instance
     */
    protected void onInsClick(Instance instance) {
    }

    /**
     * On ins close.
     */
    protected void onInsClose() {
    }

    /**
     * On ins capped.
     *
     * @param instance the instance
     */
    protected void onInsCapped(Instance instance) {
    }

    /**
     * Check schedule task started.
     */
    protected void checkScheduleTaskStarted() {
    }
}
