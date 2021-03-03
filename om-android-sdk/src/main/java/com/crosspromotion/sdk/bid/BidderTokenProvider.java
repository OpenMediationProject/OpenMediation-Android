package com.crosspromotion.sdk.bid;

import android.os.Looper;

import com.crosspromotion.sdk.utils.BidderToken;

public class BidderTokenProvider {

    public static String getBidderToken() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalThreadStateException("Method getBidderToken must be called from worker thread," +
                    "currently inferred thread is main thread.");
        }
        return BidderToken.getBidToken();
    }
}
