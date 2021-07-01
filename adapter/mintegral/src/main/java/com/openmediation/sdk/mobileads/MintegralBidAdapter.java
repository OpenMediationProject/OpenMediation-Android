// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.text.TextUtils;

import com.mbridge.msdk.mbbid.out.BidListennning;
import com.mbridge.msdk.mbbid.out.BidLossCode;
import com.mbridge.msdk.mbbid.out.BidManager;
import com.mbridge.msdk.mbbid.out.BidResponsed;
import com.openmediation.sdk.bid.BidAdapter;
import com.openmediation.sdk.bid.BidCallback;
import com.openmediation.sdk.bid.BidConstance;
import com.openmediation.sdk.bid.BidLoseReason;
import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MintegralBidAdapter extends BidAdapter {
    private static final String CLAZZ = "com.mbridge.msdk.mbbid.out.BidManager";

    private ConcurrentHashMap<String, BidResponsed> mBidResponses;

    public MintegralBidAdapter() {
        mBidResponses = new ConcurrentHashMap<>();
    }

    @Override
    public String getBiddingToken(Context context) {
        try {
            Class clazz = Class.forName(CLAZZ);
            return BidManager.getBuyerUid(MediationUtil.getContext());
        } catch(Throwable e) {
            AdLog.getSingleton().LogE("Mintegral getBuyerUid Error: " + e.getMessage());
        }
        return "";
    }

    @Override
    public void executeBid(final Context context, final Map<String, Object> dataMap, final BidCallback callback) {
        super.executeBid(context, dataMap, callback);
        MintegralSingleTon.getInstance().initSDK(MediationUtil.getContext(), String.valueOf(dataMap.get(BidConstance.BID_APP_KEY)), new MintegralSingleTon.InitCallback() {
            @Override
            public void onSuccess() {
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
                } catch(ClassNotFoundException e) {
                    AdLog.getSingleton().LogE("Mintegral bid sdk not integrated");
                    if (callback != null) {
                        callback.bidFailed("Mintegral bid sdk not integrated");
                    }
                } catch(Throwable e) {
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
            if (bidResponsed != null && MediationUtil.getContext() != null) {
                bidResponsed.sendWinNotice(MediationUtil.getContext());
            }
        }
    }

    @Override
    public void notifyLose(String placementId, Map<String, Object> dataMap) {
        super.notifyLose(placementId, dataMap);
        if (mBidResponses.containsKey(placementId)) {
            BidResponsed bidResponse = mBidResponses.get(placementId);
            if (bidResponse != null && MediationUtil.getContext() != null) {
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
                bidResponse.sendLossNotice(MediationUtil.getContext(), lossCode);
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
            } catch(Exception ignored) {
            }
            response.setPayLoad(bidResponsed.getBidToken());
            if (mCallback != null) {
                mCallback.bidSuccess(response);
            }
        }
    }
}
