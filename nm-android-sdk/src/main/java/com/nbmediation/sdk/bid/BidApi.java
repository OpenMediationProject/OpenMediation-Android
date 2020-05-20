// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.bid;

import android.content.Context;

import java.util.Map;

public interface BidApi {

    void executeBid(Context context, Map<String, Object> dataMap, BidCallback callback);

    void notifyWin(String placementId);

    void notifyLose(String placementId);
}
