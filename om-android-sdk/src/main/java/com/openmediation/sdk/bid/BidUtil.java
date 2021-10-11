// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.bid;

import android.text.TextUtils;

import com.openmediation.sdk.banner.AdSize;
import com.openmediation.sdk.utils.AdsUtil;
import com.openmediation.sdk.utils.event.AdvanceEventId;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.Configurations;

import java.util.HashMap;
import java.util.Map;

public final class BidUtil {
    private static final String AUCTION_LOSE = "${AUCTION_LOSS}";

    static Map<String, Object> makeBidInitInfo(Configurations config, int mediationId) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(BidConstance.BID_APP_KEY, config.getMs().get(mediationId).getK());
        return configMap;
    }

    static Map<String, Object> makeBidRequestInfo(String placementId, BaseInstance instance, int adType, AdSize adSize) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(BidConstance.BID_OM_PLACEMENT_ID, placementId);
        configMap.put(BidConstance.BID_APP_KEY, instance.getAppKey());
        configMap.put(BidConstance.BID_PLACEMENT_ID, instance.getKey());
        configMap.put(BidConstance.BID_AD_TYPE, adType);
        if (adSize != null) {
            configMap.put(BidConstance.BID_BANNER_SIZE, adSize);
        }
        return configMap;
    }

    public static void notifyWin(BaseInstance instance) {
        if (instance == null || !instance.isBid()) {
            return;
        }
        BidResponse bidResponse = instance.getBidResponse();
        if (bidResponse == null) {
            AdsUtil.advanceEventReport(instance, AdvanceEventId.CODE_NOTIFY_WIN_RES_NULL,
                    AdvanceEventId.MSG_NOTIFY_WIN_RES_NULL);
            return;
        }
        String nurl = bidResponse.getNurl();
        bidResponse.setNotified(true);
        if (TextUtils.isEmpty(nurl)) {
            BidManager.getInstance().notifyWin(instance);
        } else {
            BidManager.getInstance().notifyWin(nurl, instance);
        }
    }

    public static void notifyLose(BaseInstance instance, int code) {
        if (instance == null || !instance.isBid()) {
            return;
        }
        BidResponse bidResponse = instance.getBidResponse();
        if (bidResponse == null) {
            AdsUtil.advanceEventReport(instance, AdvanceEventId.CODE_NOTIFY_LOSE_RES_NULL,
                    AdvanceEventId.MSG_NOTIFY_LOSE_RES_NULL);
            return;
        }
        String lurl = bidResponse.getLurl();
        bidResponse.setNotified(true);
        if (TextUtils.isEmpty(lurl)) {
            BidManager.getInstance().notifyLose(instance, code);
        } else {
            if (lurl.contains(AUCTION_LOSE)) {
                lurl = lurl.replace(AUCTION_LOSE, String.valueOf(code));
            }
            BidManager.getInstance().notifyLose(lurl, instance);
        }
        instance.setBidResponse(null);
    }
}
