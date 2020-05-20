// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.bid;

import android.content.Context;

import java.util.Map;

public abstract class BidAdapter implements BidApi {

    @Override
    public void executeBid(Context context, Map<String, Object> dataMap, BidCallback callback) {

    }

    @Override
    public void notifyWin(String placementId) {

    }

    @Override
    public void notifyLose(String placementId) {

    }
}
