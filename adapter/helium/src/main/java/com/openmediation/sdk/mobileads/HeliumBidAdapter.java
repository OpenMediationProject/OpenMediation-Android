// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;

import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.bid.BidAdapter;
import com.openmediation.sdk.bid.BidCallback;
import com.openmediation.sdk.bid.BidConstance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class HeliumBidAdapter extends BidAdapter implements HeliumBidCallback {

    private final ConcurrentMap<String, BidCallback> mBidCallbacks;

    public HeliumBidAdapter() {
        mBidCallbacks = new ConcurrentHashMap<>();
    }

    @Override
    public void initBid(Context context, Map<String, Object> dataMap, BidCallback callback) {
        super.initBid(context, dataMap, callback);
        if (context == null) {
            if (callback != null) {
                callback.bidFailed("Init Context is null");
            }
            return;
        }
        HeliumSingleTon.InitState initState = HeliumSingleTon.getInstance().getInitState();
        if (initState == HeliumSingleTon.InitState.NOT_INIT) {
            HeliumSingleTon.getInstance().init(context, String.valueOf(dataMap.get(BidConstance.BID_APP_KEY)), null);
        }
    }

    @Override
    public void executeBid(Context context, Map<String, Object> dataMap, BidCallback callback) {
        super.executeBid(context, dataMap, callback);
        HeliumSingleTon.InitState initState = HeliumSingleTon.getInstance().getInitState();
        if (initState == HeliumSingleTon.InitState.NOT_INIT || initState == HeliumSingleTon.InitState.INIT_PENDING) {
            if (callback != null) {
                callback.bidFailed("Helium SDK not initialized");
            }
            return;
        }

        int adType = (int) dataMap.get(BidConstance.BID_AD_TYPE);
        String adUnitId = (String) dataMap.get(BidConstance.BID_PLACEMENT_ID);

        HeliumSingleTon.getInstance().addBidCallback(adUnitId, this);
        mBidCallbacks.put(adUnitId, callback);
        if (adType == BidConstance.INTERSTITIAL) {
            HeliumSingleTon.getInstance().loadInterstitial(adUnitId);
        } else if (adType == BidConstance.VIDEO) {
            HeliumSingleTon.getInstance().loadRewardedVideo(adUnitId);
        } else {
            mBidCallbacks.remove(adUnitId);
            if (callback != null) {
                callback.bidFailed("unSupport bid type");
            }
        }
    }

    @Override
    public void notifyWin(String placementId, Map<String, Object> dataMap) {
        super.notifyWin(placementId, dataMap);
    }

    @Override
    public void notifyLose(String placementId, Map<String, Object> dataMap) {
        super.notifyLose(placementId, dataMap);
    }

    @Override
    public void onBidSuccess(String placementId, Map<String, String> map) {
        HeliumSingleTon.getInstance().removeBidCallback(placementId);
        BidCallback callback = mBidCallbacks.get(placementId);
        if (callback == null) {
            return;
        }
        if (map == null || map.isEmpty() || !map.containsKey("price")) {
            callback.bidFailed("Helium bid failed cause no bid response");
            return;
        }
        String price = map.get("price");
        BidResponse bidResponse = new BidResponse();
        bidResponse.setOriginal(map.toString());
        bidResponse.setPrice(Double.parseDouble(price));
        callback.bidSuccess(bidResponse);
    }

    @Override
    public void onBidFailed(String placementId, String error) {
        HeliumSingleTon.getInstance().removeBidCallback(placementId);
        BidCallback callback = mBidCallbacks.get(placementId);
        if (callback == null) {
            return;
        }
        callback.bidFailed(error);
    }
}
