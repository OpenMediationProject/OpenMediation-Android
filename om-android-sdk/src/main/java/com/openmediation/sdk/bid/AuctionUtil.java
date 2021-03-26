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

    public static void notifyWin(BaseInstance instance, BidResponse bidResponse) {
        if (bidResponse == null) {
            return;
        }
        String nurl = bidResponse.getNurl();
        bidResponse.setNotified(true);
        if (TextUtils.isEmpty(nurl)) {
            BidAuctionManager.getInstance().notifyWin(instance);
        } else {
            BidAuctionManager.getInstance().notifyWin(nurl, instance);
        }
    }

    public static void notifyLose(BaseInstance instance, BidResponse bidResponse, int code) {
        if (bidResponse == null) {
            return;
        }
        String lurl = bidResponse.getLurl();
        bidResponse.setNotified(true);
        if (TextUtils.isEmpty(lurl)) {
            BidAuctionManager.getInstance().notifyLose(instance, code);
        } else {
            if (lurl.contains(AUCTION_LOSE)) {
                lurl = lurl.replace(AUCTION_LOSE, String.valueOf(code));
            }
            BidAuctionManager.getInstance().notifyLose(lurl, instance);
        }
    }

    public static void notifyLose(Map<BaseInstance, BidResponse> bidResponses, int code) {
        if (bidResponses == null || bidResponses.isEmpty()) {
            return;
        }

        for (BaseInstance instance : bidResponses.keySet()) {
            if (instance == null) {
                continue;
            }
            BidResponse bidResponse = bidResponses.get(instance);
            if (bidResponse == null) {
                continue;
            }
            String lurl = bidResponse.getLurl();
            bidResponse.setNotified(true);
            if (TextUtils.isEmpty(lurl)) {
                BidAuctionManager.getInstance().notifyLose(instance, code);
            } else {
                if (lurl.contains(AUCTION_LOSE)) {
                    lurl = lurl.replace(AUCTION_LOSE, String.valueOf(code));
                }
                BidAuctionManager.getInstance().notifyLose(lurl, instance);
            }
        }
    }

    public static void removeBidResponse(List<BidResponse> bidResponses, BaseInstance instance) {
        if (bidResponses == null || bidResponses.isEmpty()) {
            return;
        }
        List<BidResponse> removeBidResponse = new ArrayList<>();
        for (BidResponse bidResponse : bidResponses) {
            if (bidResponse.getIid() == instance.getId()) {
                removeBidResponse.add(bidResponse);
            }
        }

        if (!removeBidResponse.isEmpty()) {
            bidResponses.removeAll(removeBidResponse);
        }
    }

    public static Map<String, Object> generateMapRequestData(BidResponse bidResponses) {
        if (bidResponses == null) {
            return null;
        }
        Map<String, Object> extras = new HashMap<>();
        extras.put("pay_load", bidResponses.getPayLoad());
        return extras;
    }

    public static String generateStringRequestData(BidResponse bidResponses) {
        if (bidResponses == null) {
            return null;
        }
        return bidResponses.getPayLoad();
    }
}
