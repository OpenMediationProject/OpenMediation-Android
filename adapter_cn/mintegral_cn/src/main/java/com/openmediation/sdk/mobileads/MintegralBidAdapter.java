// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.text.TextUtils;

import com.mintegral.msdk.mtgbid.out.BidListennning;
import com.mintegral.msdk.mtgbid.out.BidLossCode;
import com.mintegral.msdk.mtgbid.out.BidManager;
import com.mintegral.msdk.mtgbid.out.BidResponsed;
import com.openmediation.sdk.bid.BidResponse;
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
    private Context mContext;
    private String appKey;

    public MintegralBidAdapter() {
        mBidResponses = new ConcurrentHashMap<>();
    }

    @Override
    public void initBid(Context context, Map<String, Object> dataMap, BidCallback callback) {
        super.initBid(context, dataMap, callback);
        if (context != null) {
            mContext = context.getApplicationContext();
            try {
                appKey = String.valueOf(dataMap.get(BidConstance.BID_APP_KEY));
                MintegralSingleTon.getInstance().initSDK(mContext, appKey, null);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public String getBiddingToken(Context context) {
        if (MintegralSingleTon.getInstance().getInitState() == MintegralSingleTon.InitState.INIT_SUCCESS) {
            try {
                Class clazz = Class.forName(CLAZZ);
                return BidManager.getBuyerUid(context);
            } catch (Exception e) {
                AdLog.getSingleton().LogE("Mintegral getBuyerUid Error: " + e.getMessage());
                return "";
            }
        }
        try {
            MintegralSingleTon.getInstance().initSDK(context, appKey, null);
        } catch (Exception ignored) {
        }
        return "";
    }

    @Override
    public void executeBid(final Context context, final Map<String, Object> dataMap, final BidCallback callback) {
        super.executeBid(context, dataMap, callback);

        MintegralSingleTon.getInstance().initSDK(context, String.valueOf(dataMap.get(BidConstance.BID_APP_KEY)), new MintegralSingleTon.InitCallback() {
            @Override
            public void onSuccess() {
                try {
                    Class clazz = Class.forName(CLAZZ);
                    mContext = context.getApplicationContext();
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
            public void onFailed(String msg) {
                if (callback != null) {
                    callback.bidFailed("Mintegral bid failed: " + msg);
                }
            }
        });
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
            BidResponse response = new BidResponse();
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
