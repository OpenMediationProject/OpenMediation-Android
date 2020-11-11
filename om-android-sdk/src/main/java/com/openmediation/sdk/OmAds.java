// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk;

import android.app.Activity;

import com.openmediation.sdk.core.OmManager;
import com.openmediation.sdk.utils.AdLog;

/**
 * The type Om ads.
 */
public abstract class OmAds {

    /**
     * mediation SDK init method
     *
     * @param activity required param
     * @param appKey   required param: current app's identifier
     * @param callback the callback
     * @param types    optional param: ad types to be preloaded; null means preload all
     */
    public static void init(Activity activity, String appKey, InitCallback callback, AD_TYPE... types) {
        init(activity, appKey, "", callback, types);
    }

    /**
     * mediation SDK init method
     *
     * @param activity required param
     * @param appKey   required param: current app's identifier
     * @param callback the callback
     * @param channel  the Channel of App Store
     * @param types    optional param: ad types to be preloaded; null means preload all
     */
    public static void init(Activity activity, String appKey, String channel, InitCallback callback, AD_TYPE... types) {
        OmManager.getInstance().init(activity, appKey, channel, callback, types);
    }

    /**
     * Sets an activity that is resumed
     *
     * @param activity current resume activity
     */
    public static void onResume(Activity activity) {
        OmManager.getInstance().onResume(activity);
    }

    /**
     * Sets an activity that is on pause
     *
     * @param activity currently paused activity
     */
    public static void onPause(Activity activity) {
        OmManager.getInstance().onPause(activity);
    }

    /**
     * Returns SDK init status
     *
     * @return true : init OK; or false: init wrong
     */
    public static boolean isInit() {
        return OmManager.getInstance().isInit();
    }

    /**
     * Sets In-App-Purchase
     *
     * @param iapCount the IAP count
     * @param currency the IAP currency unit
     */
    public static void setIAP(float iapCount, String currency) {
        OmManager.getInstance().setIAP(iapCount, currency);
    }

    /**
     * Returns the SDk version
     *
     * @return the sdk version
     */
    public static String getSDKVersion() {
        return OmManager.getInstance().getSDKVersion();
    }

    /**
     * setLogEnable
     *
     * @param debug enable log output
     */
    public static void setLogEnable(boolean debug) {
        AdLog.getSingleton().isDebug(debug);
    }

    /**
     * setGDPRConsent "true" is Accepted, "false" is Refuse.
     * According to the GDPR, set method of this property must be called before "init", or by default will collect user's information.
     *
     * @param consent whether the user provided consent
     */
    public static void setGDPRConsent(boolean consent) {
        OmManager.getInstance().setGDPRConsent(consent);
    }

    /**
     * Set user age restricted
     *
     * @param restricted whether you want your content treated as child-directed for purposes of COPPA
     */
    public static void setAgeRestricted(boolean restricted) {
        OmManager.getInstance().setAgeRestricted(restricted);
    }

    /**
     * Set this property to configure the user's age.
     *
     * @param age user age
     */
    public static void setUserAge(int age) {
        OmManager.getInstance().setUserAge(age);
    }

    /**
     * Set the gender of the current user. "male" or "female"
     *
     * @param gender user gender
     */
    public static void setUserGender(String gender) {
        OmManager.getInstance().setUserGender(gender);
    }

    /**
     * According to the CCPA
     * true : If the user has opted out of "sale" of personal information
     * false : If "sale" of personal information is permitted
     * set method of this property must be called before "init", or by default will collect user's information.
     *
     * @param value privacy limit
     */
    public static void setUSPrivacyLimit(boolean value) {
        OmManager.getInstance().setUSPrivacyLimit(value);
    }

    /**
     * The user's current consent status
     *
     * @return consent status
     */
    public static Boolean getGDPRConsent() {
        return OmManager.getInstance().getGDPRConsent();
    }

    /**
     * The user's COPPA status
     *
     * @return COPPA status
     */
    public static Boolean getAgeRestricted() {
        return OmManager.getInstance().getAgeRestricted();
    }

    /**
     * The user's current age
     *
     * @return the user's current age
     */
    public static Integer getUserAge() {
        return OmManager.getInstance().getUserAge();
    }

    /**
     * The user's current gender
     *
     * @return the user's current gender
     */
    public static String getUserGender() {
        return OmManager.getInstance().getUserGender();
    }

    /**
     * The user's CCPA status
     *
     * @return CCPA status
     */
    public static Boolean getUSPrivacyLimit() {
        return OmManager.getInstance().getUSPrivacyLimit();
    }

    /**
     * SDK supported preloadable Ad types
     */
    public enum AD_TYPE {
        /**
         * The Rewarded video.
         */
        /*Ad type Rewarded_Video*/
        REWARDED_VIDEO("rewardedVideo"),
        /**
         * The Interstitial.
         */
        /*Ad type Interstitial*/
        INTERSTITIAL("interstitial"),
        /* No Preload*/
        NONE("none");

        private String mValue;

        AD_TYPE(String value) {
            this.mValue = value;
        }

        @Override
        public String toString() {
            return this.mValue;
        }

    }
}
