// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;

import com.facebook.ads.BidderTokenProvider;
import com.openmediation.sdk.bid.AdTimingBidResponse;
import com.openmediation.sdk.bid.BidAdapter;
import com.openmediation.sdk.bid.BidCallback;
import com.openmediation.sdk.bid.BidConstance;
import com.openmediation.sdk.utils.AdLog;
import com.facebook.bidding.FBAdBidFormat;
import com.facebook.bidding.FBAdBidRequest;
import com.facebook.bidding.FBAdBidResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FacebookBidAdapter extends BidAdapter {
    private static final String CLAZZ = "com.facebook.bidding.FBAdBidRequest";
    private ConcurrentHashMap<String, FBAdBidResponse> mFbBidResponses;

    public FacebookBidAdapter() {
        mFbBidResponses = new ConcurrentHashMap<>();
    }

    @Override
    public void initBid(Context context, Map<String, Object> dataMap, BidCallback callback) {
        super.initBid(context, dataMap, callback);
    }

    @Override
    public void executeBid(Context context, Map<String, Object> dataMap, BidCallback callback) {
        super.executeBid(context, dataMap, callback);
        try {
            Class clazz = Class.forName(CLAZZ);
            String placementId = (String) dataMap.get(BidConstance.BID_PLACEMENT_ID);
            int bidAdType = (int) dataMap.get(BidConstance.BID_AD_TYPE);
            String appKey = (String) dataMap.get(BidConstance.BID_APP_KEY);
            FBAdBidRequest bidRequest = new FBAdBidRequest(
                    context,
                    appKey,
                    placementId,
                    getBidFormat(bidAdType));
            bidRequest.getFBBid(new FbBidResCallback(callback));
        } catch (ClassNotFoundException e) {
            AdLog.getSingleton().LogE("Facebook bid sdk not been integrated");
            if (callback != null) {
                callback.bidFailed("Facebook bid sdk not integrated");
            }
        }
    }

    @Override
    public String getBiddingToken(Context context) {
        return BidderTokenProvider.getBidderToken(context);
    }

    @Override
    public void notifyWin(String placementId) {
        super.notifyWin(placementId);
        if (mFbBidResponses.containsKey(placementId)) {
            FBAdBidResponse bidResponse = mFbBidResponses.get(placementId);
            if (bidResponse != null) {
                bidResponse.notifyWin();
            }
        }
    }

    @Override
    public void notifyLose(String placementId) {
        super.notifyLose(placementId);
        if (mFbBidResponses.containsKey(placementId)) {
            FBAdBidResponse bidResponse = mFbBidResponses.get(placementId);
            if (bidResponse != null) {
                bidResponse.notifyLoss();
            }
        }
    }

    private FBAdBidFormat getBidFormat(int adType) {
        switch (adType) {
            case BidConstance.BANNER:
                return FBAdBidFormat.BANNER_320_50;
            case BidConstance.NATIVE:
                return FBAdBidFormat.NATIVE;
            case BidConstance.INTERSTITIAL:
                return FBAdBidFormat.INTERSTITIAL;
            case BidConstance.VIDEO:
                return FBAdBidFormat.REWARDED_VIDEO;
            default:
                return null;
        }
    }

    private class FbBidResCallback implements FBAdBidRequest.BidResponseCallback {

        private BidCallback mCallback;

        FbBidResCallback(BidCallback callback) {
            mCallback = callback;
        }

        @Override
        public void handleBidResponse(FBAdBidResponse fbAdBidResponse) {
            if (fbAdBidResponse == null) {
                if (mCallback != null) {
                    mCallback.bidFailed("Facebook bid response return null");
                }
                return;
            }
            if (fbAdBidResponse.isSuccess()) {
                mFbBidResponses.put(fbAdBidResponse.getPlacementId(), fbAdBidResponse);
                AdTimingBidResponse response = new AdTimingBidResponse();
                response.setOriginal(fbAdBidResponse.toString());
                response.setCur(fbAdBidResponse.getCurrency());
                response.setPrice(fbAdBidResponse.getPrice());
                response.setPayLoad(fbAdBidResponse.getPayload());
                if (mCallback != null) {
                    mCallback.bidSuccess(response);
                }
            } else {
                if (fbAdBidResponse.getPlacementId() != null) {
                    mFbBidResponses.remove(fbAdBidResponse.getPlacementId());
                }
                if (mCallback != null) {
                    mCallback.bidFailed(fbAdBidResponse.getErrorMessage());
                }
            }
        }
    }
}
