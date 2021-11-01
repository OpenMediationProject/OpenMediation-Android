// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp;

import android.text.TextUtils;

import com.openmediation.sdk.core.AbstractHybridAds;
import com.openmediation.sdk.core.OmCacheManager;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.error.ErrorBuilder;
import com.openmediation.sdk.utils.error.ErrorCode;

/**
 * HybridCacheManager for Banner and Splash
 */
public abstract class HybridCacheManager extends AbstractHybridAds {
    public static String TAG = "HybridCacheManager";

    public HybridCacheManager(String placementId) {
        super(placementId);
    }

    @Override
    public void s2sBid() {
        try {
            mReadWfFromLocal = false;
            String cache = OmCacheManager.getInstance().getWaterfallData(mPlacement.getId(), mPlacement.getT());
            if (!TextUtils.isEmpty(cache)) {
                mReadWfFromLocal = true;
                DeveloperLog.LogD(TAG, "Om wf read local cache, placementId: " + mPlacement.getId());
                onInternalRequestSuccess(cache);
            } else {
                // has no cache
                DeveloperLog.LogD(TAG, "Om wf need read from cache, but has no local cache, placementId: " + mPlacement.getId());
            }
        } catch (Throwable e) {
            Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_UNKNOWN_ERROR
                    , "HybridCacheManager Read waterfall cache error: " + e.getMessage(), ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
            DeveloperLog.LogE(error.toString() + ", failed when read cache" +
                    ", Placement:" + mPlacement, e);
            callbackLoadError(error);
        }
        super.s2sBid();
    }
}
