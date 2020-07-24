// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.bid;

import com.openmediation.sdk.banner.AdSize;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.Configurations;

import java.util.HashMap;
import java.util.Map;

final class BidUtil {

    static Map<String, Object> makeBidInitInfo(Configurations config, int mediationId) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(BidConstance.BID_APP_KEY, config.getMs().get(mediationId).getK());
        return configMap;
    }

    static Map<String, Object> makeBidRequestInfo(BaseInstance instance, int adType, AdSize adSize) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(BidConstance.BID_APP_KEY, instance.getAppKey());
        configMap.put(BidConstance.BID_PLACEMENT_ID, instance.getKey());
        configMap.put(BidConstance.BID_AD_TYPE, adType);
        if (adSize != null) {
            configMap.put(BidConstance.BID_BANNER_SIZE, adSize);
        }
        return configMap;
    }
}
