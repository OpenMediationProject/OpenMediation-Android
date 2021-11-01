// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.text.TextUtils;

import com.openmediation.sdk.banner.AdSize;
import com.openmediation.sdk.bid.BidAdapter;
import com.openmediation.sdk.bid.BidCallback;
import com.openmediation.sdk.bid.BidConstance;
import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.mediation.MediationUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PubNativeBidAdapter extends BidAdapter implements PubNativeCallback {

    public static final String PRICE = "price";

    private final ConcurrentMap<String, BidCallback> mBidCallbacks;

    public PubNativeBidAdapter() {
        mBidCallbacks = new ConcurrentHashMap<>();
    }

    @Override
    public void initBid(Context context, Map<String, Object> dataMap, BidCallback callback) {
        super.initBid(context, dataMap, callback);
        if (MediationUtil.getApplication() == null) {
            if (callback != null) {
                callback.bidFailed("Init Context is null");
            }
            return;
        }
        PubNativeSingleTon.getInstance().init(MediationUtil.getApplication(),
                String.valueOf(dataMap.get(BidConstance.BID_APP_KEY)), null);
    }

    @Override
    public void executeBid(Context context, Map<String, Object> dataMap, BidCallback callback) {
        super.executeBid(context, dataMap, callback);
        if (dataMap == null) {
            if (callback != null) {
                callback.bidFailed("Bid Failed : Context is null");
            }
            return;
        }
        String adUnitId = (String) dataMap.get(BidConstance.BID_PLACEMENT_ID);
        if (TextUtils.isEmpty(adUnitId)) {
            if (callback != null) {
                callback.bidFailed("Bid Failed : AdUnitId is null");
            }
            return;
        }
        int adType = (int) dataMap.get(BidConstance.BID_AD_TYPE);
        PubNativeSingleTon.getInstance().addBidCallback(adUnitId, this);
        mBidCallbacks.put(adUnitId, callback);
        String appKey = String.valueOf(dataMap.get(BidConstance.BID_APP_KEY));
        if (adType == BidConstance.BANNER) {
            AdSize adSize = (AdSize) dataMap.get(BidConstance.BID_BANNER_SIZE);
            PubNativeSingleTon.getInstance().loadBanner(MediationUtil.getApplication(), appKey, adUnitId, adSize);
        } else if (adType == BidConstance.NATIVE) {
            PubNativeSingleTon.getInstance().loadNative(MediationUtil.getApplication(), appKey, adUnitId);
        } else if (adType == BidConstance.INTERSTITIAL) {
            PubNativeSingleTon.getInstance().loadInterstitial(MediationUtil.getApplication(), appKey, adUnitId);
        } else if (adType == BidConstance.VIDEO) {
            PubNativeSingleTon.getInstance().loadRewardedVideo(MediationUtil.getApplication(), appKey, adUnitId);
        } else {
            mBidCallbacks.remove(adUnitId);
            if (callback != null) {
                callback.bidFailed("unSupport bid type");
            }
        }
    }

    @Override
    public void onBidSuccess(String placementId, Map<String, String> map, Object object) {
        PubNativeSingleTon.getInstance().removeBidCallback(placementId);
        BidCallback callback = mBidCallbacks.get(placementId);
        if (callback == null) {
            return;
        }
        if (map == null || map.isEmpty() || !map.containsKey(PRICE)) {
            callback.bidFailed("HyBid bid failed cause no bid response");
            return;
        }
        String price = map.get(PRICE);
        BidResponse bidResponse = new BidResponse();
        bidResponse.setOriginal(map.toString());
        bidResponse.setPrice(Double.parseDouble(price));
        bidResponse.setObject(object);
        callback.bidSuccess(bidResponse);
    }

    @Override
    public void onBidFailed(String placementId, String error) {
        PubNativeSingleTon.getInstance().removeBidCallback(placementId);
        BidCallback callback = mBidCallbacks.get(placementId);
        if (callback == null) {
            return;
        }
        callback.bidFailed(error);
    }

}
