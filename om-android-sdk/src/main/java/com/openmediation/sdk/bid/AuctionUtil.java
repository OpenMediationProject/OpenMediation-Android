// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.bid;

import android.text.TextUtils;

import com.openmediation.sdk.utils.model.BaseInstance;

public class AuctionUtil {

    private static final String AUCTION_LOSE = "${AUCTION_LOSS}";

    public static void notifyWin(BaseInstance instance, BidResponse bidResponse) {
        if (bidResponse == null) {
            return;
        }
        String nurl = bidResponse.getNurl();
        bidResponse.setNotified(true);
        if (TextUtils.isEmpty(nurl)) {
            BidAuctionManager.getInstance().notifyWin(instance);
        } else {
            BidAuctionManager.getInstance().notifyWin(nurl, instance);
        }
    }

    public static void notifyLose(BaseInstance instance, BidResponse bidResponse, int code) {
        if (bidResponse == null) {
            return;
        }
        String lurl = bidResponse.getLurl();
        bidResponse.setNotified(true);
        if (TextUtils.isEmpty(lurl)) {
            BidAuctionManager.getInstance().notifyLose(instance, code);
        } else {
            if (lurl.contains(AUCTION_LOSE)) {
                lurl = lurl.replace(AUCTION_LOSE, String.valueOf(code));
            }
            BidAuctionManager.getInstance().notifyLose(lurl, instance);
        }
    }
}
