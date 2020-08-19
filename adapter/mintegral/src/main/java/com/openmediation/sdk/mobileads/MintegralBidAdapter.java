// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.text.TextUtils;

import com.mintegral.msdk.MIntegralSDK;
import com.mintegral.msdk.mtgbid.out.BidListennning;
import com.mintegral.msdk.mtgbid.out.BidLossCode;
import com.mintegral.msdk.mtgbid.out.BidManager;
import com.mintegral.msdk.mtgbid.out.BidResponsed;
import com.mintegral.msdk.out.MIntegralSDKFactory;
import com.openmediation.sdk.bid.AdTimingBidResponse;
import com.openmediation.sdk.bid.BidAdapter;
import com.openmediation.sdk.bid.BidCallback;
import com.openmediation.sdk.bid.BidConstance;
import com.openmediation.sdk.bid.BidLoseReason;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MintegralBidAdapter extends BidAdapter {
    private static final String CLAZZ = "com.mintegral.msdk.mtgbid.out.BidManager";
    private ConcurrentHashMap<String, BidResponsed> mBidResponses;
    private boolean mDidInitSdk;
    private Context mContext;

    public MintegralBidAdapter() {
        mBidResponses = new ConcurrentHashMap<>();
    }

    @Override
    public void initBid(Context context, Map<String, Object> dataMap, BidCallback callback) {
        super.initBid(context, dataMap, callback);
        mContext = context;
        initSDK(context, dataMap);
    }

    private void initSDK(Context context, Map<String, Object> dataMap) {
        try {
            if (mDidInitSdk || dataMap == null || context == null) {
                return;
            }
            String appKey = (String) dataMap.get(BidConstance.BID_APP_KEY);
            String[] tmp = appKey.split("#");
            String appId = tmp[0];
            String key = tmp[1];
            MIntegralSDK sdk = MIntegralSDKFactory.getMIntegralSDK();
            Map<String, String> map = sdk.getMTGConfigurationMap(appId, key);
            sdk.init(map, context.getApplicationContext());
            mDidInitSdk = true;
        } catch (Exception ignored) {
        }
    }

    @Override
    public String getBiddingToken(Context context) {
        try {
            Class clazz = Class.forName(CLAZZ);
            if (mDidInitSdk) {
                return BidManager.getBuyerUid(context);
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    @Override
    public void executeBid(Context context, Map<String, Object> dataMap, BidCallback callback) {
        super.executeBid(context, dataMap, callback);
        try {
            Class clazz = Class.forName(CLAZZ);
            String unitId = (String) dataMap.get(BidConstance.BID_PLACEMENT_ID);
            if (TextUtils.isEmpty(unitId)) {
                if (callback != null) {
                    callback.bidFailed("Mintegral bid failed: unitId is null");
                }
                return;
            }
            BidManager manager = new BidManager("", unitId);
            manager.setBidListener(new BidResCallback(unitId, callback));
            manager.bid();
        } catch (ClassNotFoundException e) {
            AdLog.getSingleton().LogE("Mintegral bid sdk not integrated");
            if (callback != null) {
                callback.bidFailed("Mintegral bid sdk not integrated");
            }
        } catch (Exception e) {
            AdLog.getSingleton().LogE("Mintegral bid failed: " + e.getMessage());
            if (callback != null) {
                callback.bidFailed("Mintegral bid failed");
            }
        }
    }

    @Override
    public void notifyWin(String placementId, Map<String, Object> dataMap) {
        super.notifyWin(placementId, dataMap);
        if (mBidResponses.containsKey(placementId)) {
            BidResponsed bidResponsed = mBidResponses.get(placementId);
            if (bidResponsed != null && mContext != null) {
                bidResponsed.sendWinNotice(mContext);
            }
        }
    }

    @Override
    public void notifyLose(String placementId, Map<String, Object> dataMap) {
        super.notifyLose(placementId, dataMap);
        if (mBidResponses.containsKey(placementId)) {
            BidResponsed bidResponse = mBidResponses.get(placementId);
            if (bidResponse != null && mContext != null) {
                int reason = -1;
                if (dataMap != null && dataMap.containsKey(BidConstance.BID_NOTIFY_REASON)) {
                    reason = (int) dataMap.get(BidConstance.BID_NOTIFY_REASON);
                }
                BidLossCode lossCode;
                if (reason == BidLoseReason.LOST_TO_HIGHER_BIDDER.getValue()) {
                    lossCode = BidLossCode.bidPriceNotHighest();
                } else if (reason == BidLoseReason.TIMEOUT.getValue()) {
                    lossCode = BidLossCode.bidTimeOut();
                } else {
                    lossCode = BidLossCode.bidWinButNotShow();
                }
                bidResponse.sendLossNotice(mContext, lossCode);
            }
        }
    }

    private class BidResCallback implements BidListennning {

        private String mUnitId;
        private BidCallback mCallback;

        BidResCallback(String unitId, BidCallback callback) {
            mUnitId = unitId;
            mCallback = callback;
        }

        @Override
        public void onFailed(String msg) {
            if (mCallback != null) {
                mCallback.bidFailed(msg);
            }
        }

        @Override
        public void onSuccessed(BidResponsed bidResponsed) {
            if (bidResponsed == null) {
                return;
            }
            mBidResponses.put(mUnitId, bidResponsed);
            AdTimingBidResponse response = new AdTimingBidResponse();
            response.setOriginal(bidResponsed.toString());
            response.setCur(bidResponsed.getCur());
            try {
                response.setPrice(Double.parseDouble(bidResponsed.getPrice()));
            } catch (Exception ignored) {
            }
            response.setPayLoad(bidResponsed.getBidToken());
            if (mCallback != null) {
                mCallback.bidSuccess(response);
            }
        }
    }
}
