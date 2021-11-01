/*
 * // Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * // Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.mobileads;

import android.content.Context;

import com.inmobi.sdk.InMobiSdk;
import com.openmediation.sdk.banner.AdSize;
import com.openmediation.sdk.bid.BidAdapter;
import com.openmediation.sdk.bid.BidCallback;
import com.openmediation.sdk.bid.BidConstance;
import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.mediation.MediationUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMobiBidAdapter extends BidAdapter implements InMobiBidCallback {

    private final ConcurrentMap<String, BidCallback> mBidCallbacks;

    public InMobiBidAdapter() {
        mBidCallbacks = new ConcurrentHashMap<>();
    }

    @Override
    public void initBid(Context context, Map<String, Object> dataMap, BidCallback callback) {
        super.initBid(context, dataMap, callback);
        if (!InMobiSingleTon.getInstance().isInit()) {
            InMobiSingleTon.getInstance().init(MediationUtil.getContext(), String.valueOf(dataMap.get(BidConstance.BID_APP_KEY)), null);
        }
    }

    @Override
    public void executeBid(Context context, Map<String, Object> dataMap, BidCallback callback) {
        super.executeBid(context, dataMap, callback);
        String appKey = "";
        if (dataMap.get(BidConstance.BID_APP_KEY) != null) {
            appKey = String.valueOf(dataMap.get(BidConstance.BID_APP_KEY));
        }
        InMobiSingleTon.getInstance().init(MediationUtil.getContext(), appKey, new InMobiInitCallback() {
            @Override
            public void initSuccess() {
                executeBid(dataMap, callback);
            }

            @Override
            public void initFailed(String error) {
                if (callback != null) {
                    callback.bidFailed("InMobi SDK init error: " + error);
                }
            }
        });
    }

    private void executeBid(Map<String, Object> dataMap, BidCallback callback) {
        int adType = (int) dataMap.get(BidConstance.BID_AD_TYPE);
        String adUnitId = (String) dataMap.get(BidConstance.BID_PLACEMENT_ID);
        InMobiSingleTon.getInstance().addBidCallback(adUnitId, this);
        mBidCallbacks.put(adUnitId, callback);
        if (adType == BidConstance.INTERSTITIAL) {
            InMobiSingleTon.getInstance().loadInterstitial(adUnitId);
        } else if (adType == BidConstance.VIDEO) {
            InMobiSingleTon.getInstance().loadRewardedVideo(adUnitId);
        } else if (adType == BidConstance.BANNER) {
            AdSize adSize = (AdSize) dataMap.get(BidConstance.BID_BANNER_SIZE);
            InMobiSingleTon.getInstance().loadBanner(adUnitId, adSize);
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
    public void onBidSuccess(String adUnitId, double ecpm) {
        InMobiSingleTon.getInstance().removeBidCallback(adUnitId);
        BidCallback callback = mBidCallbacks.get(adUnitId);
        if (callback == null) {
            return;
        }
        BidResponse bidResponse = new BidResponse();
        bidResponse.setPrice(ecpm);
        callback.bidSuccess(bidResponse);
    }

    @Override
    public void onBidFailed(String adUnitId, String error) {
        InMobiSingleTon.getInstance().removeBidCallback(adUnitId);
        BidCallback callback = mBidCallbacks.get(adUnitId);
        if (callback == null) {
            return;
        }
        callback.bidFailed(error);
    }
}
