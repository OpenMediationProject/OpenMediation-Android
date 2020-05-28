// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.bid;

import android.text.TextUtils;
import android.util.SparseArray;

import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.Placement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuctionUtil {
    private static final String AUCTION_LOSE = "${AUCTION_LOSS}";

    public static void instanceNotifyBidWin(int abt, BaseInstance instance) {
        AdTimingAuctionManager.getInstance().notifyWin(abt, instance);
    }


    public static void s2sNotifyBidWin(String url, BaseInstance instance) {
        AdTimingAuctionManager.getInstance().notifyWin(url, instance);
    }

    public static void c2sNotifyBidLose(List<AdTimingBidResponse> bidResponses, Placement placement) {
        if (bidResponses == null || bidResponses.isEmpty() || placement == null) {
            return;
        }

        SparseArray<BaseInstance> insMap = placement.getInsMap();
        for (AdTimingBidResponse bidResponse : bidResponses) {
            BaseInstance instance = insMap.get(bidResponse.getIid());
            if (instance == null) {
                continue;
            }
            AdTimingAuctionManager.getInstance().notifyLose(placement.getHbAbt(), instance);
        }
    }

    public static void s2sNotifyBidLose(AdTimingBidResponse bidResponse, int code, BaseInstance instance) {
        if (bidResponse == null || instance == null) {
            return;
        }

        String lurl = bidResponse.getLurl();
        if (TextUtils.isEmpty(lurl)) {
            return;
        }
        if (lurl.contains(AUCTION_LOSE)) {
            lurl = lurl.replace(AUCTION_LOSE, String.valueOf(code));
        }
        AdTimingAuctionManager.getInstance().notifyLose(instance.getWfAbt(), lurl, instance);
    }

    public static void s2sNotifyBidLose(Map<BaseInstance, AdTimingBidResponse> bidResponses, int code) {
        if (bidResponses == null || bidResponses.isEmpty()) {
            return;
        }

        for (BaseInstance instance : bidResponses.keySet()) {
            if (instance == null) {
                continue;
            }
            AdTimingBidResponse bidResponse = bidResponses.get(instance);
            if (bidResponse == null) {
                continue;
            }
            String lurl = bidResponse.getLurl();
            if (TextUtils.isEmpty(lurl)) {
                continue;
            }
            if (lurl.contains(AUCTION_LOSE)) {
                lurl = lurl.replace(AUCTION_LOSE, String.valueOf(code));
            }
            AdTimingAuctionManager.getInstance().notifyLose(instance.getWfAbt(), lurl, instance);
        }
    }

    public static void removeBidResponse(List<AdTimingBidResponse> bidResponses, BaseInstance instance) {
        if (bidResponses == null || bidResponses.isEmpty()) {
            return;
        }
        List<AdTimingBidResponse> removeBidResponse = new ArrayList<>();
        for (AdTimingBidResponse bidResponse : bidResponses) {
            if (bidResponse.getIid() == instance.getId()) {
                removeBidResponse.add(bidResponse);
            }
        }

        if (!removeBidResponse.isEmpty()) {
            bidResponses.removeAll(removeBidResponse);
        }
    }

    public static Map<String, Object> generateMapRequestData(AdTimingBidResponse bidResponses) {
        if (bidResponses == null) {
            return null;
        }
        Map<String, Object> extras = new HashMap<>();
        extras.put("pay_load", bidResponses.getPayLoad());
        return extras;
    }

    public static String generateStringRequestData(AdTimingBidResponse bidResponses) {
        if (bidResponses == null) {
            return null;
        }
        return bidResponses.getPayLoad();
    }
}
