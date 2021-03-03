package com.openmediation.sdk.mobileads;

import android.content.Context;

import com.openmediation.sdk.bid.BidAdapter;
import com.openmediation.sdk.bid.BidCallback;
import com.openmediation.sdk.bid.BidConstance;
import com.vungle.warren.Vungle;

import java.util.Map;

public class VungleBidAdapter extends BidAdapter {

    private static final int LIMITATION = 10;

    private Context mContext;
    private String mAppKey;

    @Override
    public void initBid(Context context, Map<String, Object> dataMap, BidCallback callback) {
        super.initBid(context, dataMap, callback);
        if (context == null) {
            return;
        }
        if (dataMap == null || !dataMap.containsKey(BidConstance.BID_APP_KEY)) {
            return;
        }
        mContext = context.getApplicationContext();
        mAppKey = String.valueOf(dataMap.get(BidConstance.BID_APP_KEY));
        VungleSingleTon.getInstance().init(mContext, mAppKey, null);
    }

    @Override
    public String getBiddingToken(Context context) {
        if (!Vungle.isInitialized()) {
            VungleSingleTon.getInstance().init(mContext, mAppKey, null);
        }
        return Vungle.getAvailableBidTokens(context, LIMITATION);
    }
}
