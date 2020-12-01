package com.openmediation.sdk.mobileads;

import android.app.Activity;

import com.openmediation.sdk.adn.OmAdNetwork;

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

    public synchronized boolean init(Activity activity) {
        if (!isDidInit) {
            if (activity == null) {
                return false;
            }
            OmAdNetwork.init(activity);
            isDidInit = true;
            return true;
        }
        return true;
    }
}
