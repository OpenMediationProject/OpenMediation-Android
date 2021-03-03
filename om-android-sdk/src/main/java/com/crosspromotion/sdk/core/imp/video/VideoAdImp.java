// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.core.imp.video;

import com.crosspromotion.sdk.AdsActivity;
import com.crosspromotion.sdk.core.AbstractAdsManager;
import com.crosspromotion.sdk.core.CallbackBridge;
import com.crosspromotion.sdk.utils.Cache;
import com.crosspromotion.sdk.video.RewardedVideoListener;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.constant.CommonConstants;

import java.io.File;

public final class VideoAdImp extends AbstractAdsManager {

    public VideoAdImp(String placementId) {
        super(placementId);
    }

    @Override
    protected int getAdType() {
        return CommonConstants.VIDEO;
    }

    public void setListener(RewardedVideoListener adListener) {
        mListenerWrapper.setVideoListener(adListener);
    }

    @Override
    public boolean isReady() {
        try {
            boolean result = super.isReady();
            if (!result) {
                return false;
            }
            File video = Cache.getCacheFile(mContext, mAdBean.getVideoUrl(), null);
            return video != null && video.exists() && video.length() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void showAds() {
        super.showAds();
        CallbackBridge.addListenerToMap(mPlacementId, this);
        show(AdsActivity.class);
    }

    @Override
    public void destroy() {
        super.destroy();
        CallbackBridge.removeListenerFromMap(mPlacementId);
    }

    public void onRewardedVideoStarted() {
        DeveloperLog.LogD("onRewardedVideoAdStarted : " + mPlacementId);
        mListenerWrapper.onRewardAdStarted(mPlacementId);
    }

    public void onRewardedVideoEnded() {
        DeveloperLog.LogD("onRewardedVideoEnded : " + mPlacementId);
        mListenerWrapper.onRewardAdEnded(mPlacementId);
    }

    public void onRewardedRewarded() {
        DeveloperLog.LogD("onRewardedRewarded : " + mPlacementId);
        mListenerWrapper.onRewardAdRewarded(mPlacementId);
    }
}
