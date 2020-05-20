// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.bid;

import android.util.SparseArray;

import com.nbmediation.sdk.utils.model.BaseInstance;
import com.nbmediation.sdk.utils.model.Placement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuctionUtil {

    public static void instanceNotifyBidWin(int abt, BaseInstance instance) {
        AdTimingAuctionManager.getInstance().notifyWin(abt, instance);
    }

    public static void instanceNotifyBidLose(List<AdTimingBidResponse> bidResponses, Placement placement) {
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

    public static Map<String, Object> generateMapRequestData(List<AdTimingBidResponse> bidResponses,
                                                             BaseInstance instance) {
        if (bidResponses == null || bidResponses.isEmpty()) {
            return null;
        }
        for (AdTimingBidResponse response : bidResponses) {
            if (response.getIid() == instance.getId()) {
                Map<String, Object> extras = new HashMap<>();
                extras.put("pay_load", response.getPayLoad());
                return extras;
            }
        }
        return null;
    }

    public static String generateStringRequestData(List<AdTimingBidResponse> bidResponses, BaseInstance instance) {
        if (bidResponses == null || bidResponses.isEmpty()) {
            return null;
        }
        for (AdTimingBidResponse response : bidResponses) {
            if (response.getIid() == instance.getId()) {
                return response.getPayLoad();
            }
        }
        return null;
    }
}
