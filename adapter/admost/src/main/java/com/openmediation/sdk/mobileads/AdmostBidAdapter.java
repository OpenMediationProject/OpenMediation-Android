/*
 * // Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * // Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;

import com.openmediation.sdk.bid.BidAdapter;
import com.openmediation.sdk.bid.BidCallback;
import com.openmediation.sdk.bid.BidConstance;
import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.mediation.MediationUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AdmostBidAdapter extends BidAdapter implements AdmostBidCallback {

    private final ConcurrentMap<String, BidCallback> mBidCallbacks;

    public AdmostBidAdapter() {
        mBidCallbacks = new ConcurrentHashMap<>();
    }

    @Override
    public void initBid(Context context, Map<String, Object> dataMap, BidCallback callback) {
        super.initBid(context, dataMap, callback);
        if (!AdmostSingleTon.getInstance().isInit()) {
            Activity activity;
            if (context instanceof Activity) {
                activity = (Activity) context;
            } else {
                activity = MediationUtil.getActivity();
            }
            AdmostSingleTon.getInstance().init(activity, String.valueOf(dataMap.get(BidConstance.BID_APP_KEY)), null);
        }
    }

    @Override
    public void executeBid(Context context, Map<String, Object> dataMap, BidCallback callback) {
        super.executeBid(context, dataMap, callback);
        String appKey = "";
        if (dataMap.get(BidConstance.BID_APP_KEY) != null) {
            appKey = String.valueOf(dataMap.get(BidConstance.BID_APP_KEY));
        }
        Activity activity;
        if (context instanceof Activity) {
            activity = (Activity) context;
        } else {
            activity = MediationUtil.getActivity();
        }
        AdmostSingleTon.getInstance().init(activity, appKey, new AdmostSingleTon.InitListener() {
            @Override
            public void initSuccess() {
                executeBid(dataMap, callback);
            }

            @Override
            public void initFailed(int code, String error) {
                if (callback != null) {
                    callback.bidFailed("AdMost SDK init error: " + error);
                }
            }
        });
    }

    private void executeBid(Map<String, Object> dataMap, BidCallback callback) {
        int adType = (int) dataMap.get(BidConstance.BID_AD_TYPE);
        String adUnitId = (String) dataMap.get(BidConstance.BID_PLACEMENT_ID);
        AdmostSingleTon.getInstance().addBidCallback(adUnitId, this);
        mBidCallbacks.put(adUnitId, callback);
        if (adType == BidConstance.INTERSTITIAL) {
            AdmostSingleTon.getInstance().loadInterstitial(adUnitId);
        } else if (adType == BidConstance.VIDEO) {
            AdmostSingleTon.getInstance().loadRewardedVideo(adUnitId);
        } else if (adType == BidConstance.BANNER) {
            AdmostSingleTon.getInstance().loadBanner(adUnitId);
        } else if (adType == BidConstance.NATIVE) {
            String pid = "";
            Object placementId = dataMap.get(BidConstance.BID_OM_PLACEMENT_ID);
            if (placementId != null) {
                pid = String.valueOf(placementId);
            }
            AdmostSingleTon.getInstance().loadNative(pid, adUnitId);
        } else {
            mBidCallbacks.remove(adUnitId);
            if (callback != null) {
                callback.bidFailed("unSupport bid type");
            }
        }
    }

    @Override
    public void onBidSuccess(String adUnitId, String network, int ecpm, Object object) {
        AdmostSingleTon.getInstance().removeBidCallback(adUnitId);
        BidCallback callback = mBidCallbacks.get(adUnitId);
        if (callback == null) {
            return;
        }
        BidResponse bidResponse = new BidResponse();
        double price = ecpm / 100.0;
        bidResponse.setPrice(price);
        bidResponse.setObject(object);
        callback.bidSuccess(bidResponse);
    }

    @Override
    public void onBidFailed(String adUnitId, String error) {
        AdmostSingleTon.getInstance().removeBidCallback(adUnitId);
        BidCallback callback = mBidCallbacks.get(adUnitId);
        if (callback == null) {
            return;
        }
        callback.bidFailed(error);
    }
}
