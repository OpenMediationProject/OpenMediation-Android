package com.openmediation.sdk.mobileads;

import android.content.Context;

import com.openmediation.sdk.bid.BidAdapter;
import com.openmediation.sdk.bid.BidCallback;
import com.openmediation.sdk.bid.BidConstance;
import com.openmediation.sdk.mediation.MediationUtil;
import com.vungle.warren.Vungle;

import java.util.Map;

public class VungleBidAdapter extends BidAdapter {

    private static final int LIMITATION = 10;

    private String mAppKey;

    @Override
    public void initBid(Context context, Map<String, Object> dataMap, BidCallback callback) {
        super.initBid(context, dataMap, callback);
        if (MediationUtil.getContext() == null) {
            return;
        }
        if (dataMap == null || !dataMap.containsKey(BidConstance.BID_APP_KEY)) {
            return;
        }
        mAppKey = String.valueOf(dataMap.get(BidConstance.BID_APP_KEY));
        VungleSingleTon.getInstance().init(MediationUtil.getContext(), mAppKey, null);
    }

    @Override
    public String getBiddingToken(Context context) {
        if (!Vungle.isInitialized()) {
            VungleSingleTon.getInstance().init(MediationUtil.getContext(), mAppKey, null);
        }
        return Vungle.getAvailableBidTokens(MediationUtil.getContext(), LIMITATION);
    }
}
