// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk;

import android.app.Activity;

import com.openmediation.sdk.core.OmManager;
import com.openmediation.sdk.utils.AFManager;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;

/**
 * The type Om ads.
 */
public abstract class OmAds {

    /**
     * mediation SDK init method
     *
     * @param activity      required param
     * @param configuration required param: include appKey,channel,initHost logEnable and so on
     * @param callback      the callback
     */
    public static void init(Activity activity, InitConfiguration configuration, InitCallback callback) {
        if (configuration != null) {
            AdLog.getSingleton().isDebug(configuration.isLogEnable());
        }
        OmManager.getInstance().init(activity, configuration, callback);
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
     * setUserId
     * @param userId userId
     */
    public static void setUserId(String userId) {
        OmManager.getInstance().setUserId(userId);
    }

    public static String getUserId() {
        return OmManager.getInstance().getUserId();
    }

    /**
     * App defined user tags
     *
     * @param map Support basic data types and array types
     */
    public static void setCustomTags(Map<String, Object> map) {
        OmManager.getInstance().setCustomTags(map);
    }

    /**
     * Returns the tags set by the user
     * Returns null if not set
     */
    public static Map<String, Object> getCustomTags() {
        return OmManager.getInstance().getCustomTags();
    }

    /**
     * App defined user tag
     */
    public static void setCustomTag(String key, String value) {
        OmManager.getInstance().setCustomTagObject(key, value);
    }

    /**
     * App defined user tag
     */
    public static void setCustomTag(String key, String... values) {
        OmManager.getInstance().setCustomTagObjects(key, values);
    }

    /**
     * App defined user tag
     */
    public static void setCustomTag(String key, Number value) {
        OmManager.getInstance().setCustomTagObject(key, value);
    }

    /**
     * App defined user tag
     */
    public static void setCustomTag(String key, Number... values) {
        OmManager.getInstance().setCustomTagObjects(key, values);
    }

    /**
     * Remove app defined user tag
     */
    public static void removeCustomTag(String key) {
        OmManager.getInstance().removeCustomTag(key);
    }

    /**
     * Remove app defined user tag
     */
    public static void clearCustomTags() {
        OmManager.getInstance().clearCustomTags();
    }

    /**
     * Report AppsFlyer conversion data
     *
     * @param conversionData
     */
    public static void sendAFConversionData(Object conversionData) {
        AFManager.sendAFConversionData(conversionData);
    }

    /**
     * Report AppsFlyer deeplink data
     *
     * @param conversionData
     */
    public static void sendAFDeepLinkData(Object conversionData) {
        AFManager.sendAFDeepLinkData(conversionData);
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
     * Call this method to start listening for impression level revenue data events.
     *
     * @param listener - {@link ImpressionDataListener} interface implementation
     */
    public static void addImpressionDataListener(ImpressionDataListener listener) {
        ImpressionManager.addListener(listener);
    }

    /**
     * Call this method to unsubscribe from impression level revenue data events.
     *
     * @param listener - previously submitted to addListener() {@link ImpressionDataListener}
     */
    public static void removeImpressionDataListener(final ImpressionDataListener listener) {
        ImpressionManager.removeListener(listener);
    }


    /**
     * SDK supported pre loadable Ad types
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
        /*Ad type Promotion*/
        PROMOTION("promotion"),
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
