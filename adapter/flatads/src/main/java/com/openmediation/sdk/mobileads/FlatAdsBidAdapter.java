// Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;

import com.openmediation.sdk.banner.AdSize;
import com.openmediation.sdk.bid.BidAdapter;
import com.openmediation.sdk.bid.BidCallback;
import com.openmediation.sdk.bid.BidConstance;
import com.openmediation.sdk.bid.BidResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FlatAdsBidAdapter extends BidAdapter implements FlatAdsBidCallback {
    private final ConcurrentMap<String, BidCallback> mBidCallbacks;

    public FlatAdsBidAdapter() {
        mBidCallbacks = new ConcurrentHashMap<>();
    }

    @Override
    public void initBid(Context context, Map<String, Object> dataMap, BidCallback callback) {
        super.initBid(context, dataMap, callback);
        FlatAdsSingleTon.getInstance().init(
                String.valueOf(dataMap.get(BidConstance.BID_APP_KEY)), null);
    }

    @Override
    public void executeBid(Context context, Map<String, Object> dataMap, BidCallback callback) {
        super.executeBid(context, dataMap, callback);
        String appKey = "";
        if (dataMap.get(BidConstance.BID_APP_KEY) != null) {
            appKey = String.valueOf(dataMap.get(BidConstance.BID_APP_KEY));
        }
        FlatAdsSingleTon.getInstance().init(appKey, new FlatAdsSingleTon.InitListener() {
            @Override
            public void initSuccess() {
                executeBid(dataMap, callback);
            }

            @Override
            public void initFailed(String error) {
                if (callback != null) {
                    callback.bidFailed("AdMost SDK init error: " + error);
                }
            }
        });
    }

    private void executeBid(Map<String, Object> dataMap, BidCallback callback) {
        int adType = (int) dataMap.get(BidConstance.BID_AD_TYPE);
        String adUnitId = (String) dataMap.get(BidConstance.BID_PLACEMENT_ID);
        FlatAdsSingleTon.getInstance().addBidCallback(adUnitId, this);
        mBidCallbacks.put(adUnitId, callback);
        if (adType == BidConstance.INTERSTITIAL) {
            FlatAdsSingleTon.getInstance().bidInterstitial(adUnitId);
        } else if (adType == BidConstance.VIDEO) {
            FlatAdsSingleTon.getInstance().bidVideo(adUnitId);
        } else if (adType == BidConstance.NATIVE) {
            FlatAdsSingleTon.getInstance().bidNative(adUnitId);
        } else if (adType == BidConstance.BANNER) {
            AdSize adSize = AdSize.BANNER;
            if (dataMap.containsKey(BidConstance.BID_BANNER_SIZE)) {
                adSize = (AdSize) dataMap.get(BidConstance.BID_BANNER_SIZE);
            }
            FlatAdsSingleTon.getInstance().bidBanner(adUnitId, adSize);
        } else {
            mBidCallbacks.remove(adUnitId);
            if (callback != null) {
                callback.bidFailed("unSupport bid type");
            }
        }
    }

    @Override
    public void onBidSuccess(String adUnitId, float ecpm, Object object) {
        FlatAdsSingleTon.getInstance().removeBidCallback(adUnitId);
        BidCallback callback = mBidCallbacks.get(adUnitId);
        if (callback == null) {
            return;
        }
        BidResponse bidResponse = new BidResponse();
        bidResponse.setPrice(ecpm);
        bidResponse.setObject(object);
        callback.bidSuccess(bidResponse);
    }

    @Override
    public void onBidFailed(String adUnitId, String error) {
        FlatAdsSingleTon.getInstance().removeBidCallback(adUnitId);
        BidCallback callback = mBidCallbacks.get(adUnitId);
        if (callback == null) {
            return;
        }
        callback.bidFailed(error);
    }
}
