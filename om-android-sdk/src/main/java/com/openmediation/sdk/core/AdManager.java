// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core;

import com.openmediation.sdk.mediation.CustomAdEvent;
import com.openmediation.sdk.mediation.CustomEventFactory;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.model.BaseInstance;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to manage banner,native ads's relationShip between instance and adapter
 */
public class AdManager {
    private Map<BaseInstance, CustomAdEvent> mAdEvents;//adEvents making requests

    private static final class AdManagerHolder {
        private static final AdManager INSTANCE = new AdManager();
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static AdManager getInstance() {
        return AdManagerHolder.INSTANCE;
    }

    private AdManager() {
        mAdEvents = new HashMap<>();
    }

    private void addAdEvent(BaseInstance instances, CustomAdEvent adEvent) {
        if (instances == null || adEvent == null) {
            return;
        }
        mAdEvents.put(instances, adEvent);
    }


    /**
     * Gets ins ad event.
     *
     * @param adType    the ad type
     * @param instances the instances
     * @return the ins ad event
     */
    public CustomAdEvent getInsAdEvent(int adType, BaseInstance instances) {
        try {
            if (instances == null) {
                return null;
            }
            CustomAdEvent adEvent = mAdEvents.get(instances);
            if (adEvent == null) {
                DeveloperLog.LogD("get Ins Event by create new : " + instances.toString());
                switch (adType) {
                    case CommonConstants.BANNER:
                        adEvent = CustomEventFactory.createBanner(instances.getPath());
                        break;
                    case CommonConstants.NATIVE:
                        adEvent = CustomEventFactory.createNative(instances.getPath());
                        break;
                    case CommonConstants.SPLASH:
                        adEvent = CustomEventFactory.createSplash(instances.getPath());
                    default:
                        break;
                }

                addAdEvent(instances, adEvent);
                // set GDPR Age Gender
                AdapterRepository.getInstance().setCustomParams(adEvent);
            } else {
                DeveloperLog.LogD("get Ins Event from map: " + instances.toString());
            }
            return adEvent;
        } catch (Exception e) {
            DeveloperLog.LogD("AdManager", e);
        }
        return null;
    }

    /**
     * Remove ins ad event.
     *
     * @param instances the instances
     */
    public void removeInsAdEvent(BaseInstance instances) {
        if (mAdEvents.isEmpty()) {
            return;
        }
        mAdEvents.remove(instances);
    }
}
