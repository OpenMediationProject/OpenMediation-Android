// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.bid;

import android.content.Context;

import java.util.Map;

public interface BidApi {

    void initBid(Context context, Map<String, Object> dataMap, BidCallback callback);

    String getBiddingToken(Context context);

    void executeBid(Context context, Map<String, Object> dataMap, BidCallback callback);

    void notifyWin(String placementId, Map<String, Object> dataMap);

    void notifyLose(String placementId, Map<String, Object> dataMap);
}
