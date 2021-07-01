package com.openmediation.sdk.mobileads;

import android.content.Context;

import com.crosspromotion.sdk.CrossPromotionAds;

public class CrossPromotionSingleTon {

    private boolean isDidInit;

    private static class CrossPromotion {
        private static final CrossPromotionSingleTon INSTANCE = new CrossPromotionSingleTon();
    }

    private CrossPromotionSingleTon() {
    }

    public static CrossPromotionSingleTon getInstance() {
        return CrossPromotion.INSTANCE;
    }

    public synchronized boolean init(Context context) {
        if (!isDidInit) {
            if (context == null) {
                return false;
            }
            CrossPromotionAds.init(context);
            isDidInit = true;
            return true;
        }
        return true;
    }

    public boolean isInit() {
        return isDidInit;
    }
}
