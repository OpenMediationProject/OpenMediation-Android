// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.bid;

import android.text.TextUtils;

import com.openmediation.sdk.utils.model.BaseInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuctionUtil {

    private static final String AUCTION_LOSE = "${AUCTION_LOSS}";

    public static void notifyWin(BaseInstance instance, AdTimingBidResponse bidResponse) {
        if (bidResponse == null) {
            return;
        }
        String nurl = bidResponse.getNurl();
        if (TextUtils.isEmpty(nurl)) {
            AdTimingAuctionManager.getInstance().notifyWin(instance);
        } else {
            AdTimingAuctionManager.getInstance().notifyWin(nurl, instance);
        }
    }

    public static void notifyLose(BaseInstance instance, AdTimingBidResponse bidResponse, int code) {
        if (bidResponse == null) {
            return;
        }
        String lurl = bidResponse.getLurl();
        if (TextUtils.isEmpty(lurl)) {
            AdTimingAuctionManager.getInstance().notifyLose(instance, code);
        } else {
            if (lurl.contains(AUCTION_LOSE)) {
                lurl = lurl.replace(AUCTION_LOSE, String.valueOf(code));
            }
            AdTimingAuctionManager.getInstance().notifyLose(lurl, instance);
        }
    }

    public static void notifyLose(Map<BaseInstance, AdTimingBidResponse> bidResponses, int code) {
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
                AdTimingAuctionManager.getInstance().notifyLose(instance, code);
            } else {
                if (lurl.contains(AUCTION_LOSE)) {
                    lurl = lurl.replace(AUCTION_LOSE, String.valueOf(code));
                }
                AdTimingAuctionManager.getInstance().notifyLose(lurl, instance);
            }
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
