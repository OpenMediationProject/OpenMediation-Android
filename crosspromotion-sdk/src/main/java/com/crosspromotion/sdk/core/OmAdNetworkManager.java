// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.core;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.crosspromotion.sdk.core.imp.interstitial.InterstitialAdImp;
import com.crosspromotion.sdk.core.imp.promotion.PromotionAdImp;
import com.crosspromotion.sdk.core.imp.video.VideoAdImp;
import com.crosspromotion.sdk.interstitial.InterstitialAdListener;
import com.crosspromotion.sdk.promotion.PromotionAdListener;
import com.crosspromotion.sdk.promotion.PromotionAdRect;
import com.crosspromotion.sdk.utils.Cache;
import com.crosspromotion.sdk.utils.webview.ActWebView;
import com.crosspromotion.sdk.utils.webview.AdsWebView;
import com.crosspromotion.sdk.video.RewardedVideoListener;

import java.util.HashMap;
import java.util.Map;

public final class OmAdNetworkManager {
    private Map<String, InterstitialAdImp> mIsManagers;
    private Map<String, VideoAdImp> mRvManagers;
    private Map<String, PromotionAdImp> mPromotionManagers;

    private static final class OmHolder {
        private static final OmAdNetworkManager INSTANCE = new OmAdNetworkManager();
    }

    private OmAdNetworkManager() {
        mIsManagers = new HashMap<>();
        mRvManagers = new HashMap<>();
        mPromotionManagers = new HashMap<>();
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static OmAdNetworkManager getInstance() {
        return OmHolder.INSTANCE;
    }

    /**
     * The actual init method
     */
    public void init(Context context) {
        Cache.init();
        AdsWebView.getInstance().init();
        ActWebView.getInstance().init(context);
    }

    /**
     * only one listener will exist in the whole lifecycle
     *
     * @param placementId the placement id
     * @param listener    InterstitialAd listener
     */
    public void setInterstitialAdListener(String placementId, InterstitialAdListener listener) {
        if (TextUtils.isEmpty(placementId) || listener == null) {
            return;
        }
        InterstitialAdImp isManager = getIsManager(placementId);
        if (isManager != null) {
            isManager.setListener(listener);
        }
    }

    /**
     * Load interstitial ad.
     *
     * @param placementId the placement id
     */
    public void loadInterstitialAd(String placementId, String payload, Map extras) {
        InterstitialAdImp isManager = getIsManager(placementId);
        if (isManager != null) {
            isManager.loadAdsWithPayload(payload, extras);
        }
    }

    /**
     * Is interstitial ad ready boolean.
     *
     * @param placementId the placement id
     * @return the boolean
     */
    public boolean isInterstitialAdReady(String placementId) {
        InterstitialAdImp isManager = getIsManager(placementId);
        if (isManager != null) {
            return isManager.isReady();
        }
        return false;
    }

    /**
     * Shows the given scene's InterstitialAd, shows default if the scene does not exist
     *
     * @param placementId placementId
     */
    public void showInterstitialAd(String placementId) {
        InterstitialAdImp isManager = getIsManager(placementId);
        if (isManager != null) {
            isManager.showAds();
        }
    }

    /**
     * only one listener will exist in the whole lifecycle
     *
     * @param placementId the placement id
     * @param listener    RewardedVideo listener
     */
    public void setRewardedVideoListener(String placementId, RewardedVideoListener listener) {
        if (TextUtils.isEmpty(placementId) || listener == null) {
            return;
        }

        VideoAdImp rvManager = getRvManager(placementId);
        if (rvManager != null) {
            rvManager.setListener(listener);
        }
    }

    /**
     * Only developers call this method
     *
     * @param placementId the placement id
     */
    public void loadRewardedVideo(String placementId, String payload, Map extras) {
        VideoAdImp rvManager = getRvManager(placementId);
        if (rvManager != null) {
            rvManager.loadAdsWithPayload(payload, extras);
        }
    }

    /**
     * Is rewarded video ready boolean.
     *
     * @param placementId the placement id
     * @return the boolean
     */
    public boolean isRewardedVideoReady(String placementId) {
        VideoAdImp rvManager = getRvManager(placementId);
        if (rvManager != null) {
            return rvManager.isReady();
        }
        return false;
    }

    /**
     * Shows the given scene's RewardedVideo, shows default if the scene does not exist
     *
     * @param placementId the placement id
     */
    public void showRewardedVideo(String placementId) {
        VideoAdImp rvManager = getRvManager(placementId);
        if (rvManager != null) {
            rvManager.showAds();
        }
    }

    public void setPromotionAdListener(String placementId, PromotionAdListener listener) {
        if (TextUtils.isEmpty(placementId) || listener == null) {
            return;
        }
        PromotionAdImp manager = getPromotionManager(placementId);
        if (manager != null) {
            manager.setListener(listener);
        }
    }

    public void loadPromotionAd(String placementId, Map extras) {
        PromotionAdImp manager = getPromotionManager(placementId);
        if (manager != null) {
            manager.loadAds(extras);
        }
    }

    public boolean isPromotionAdReady(String placementId) {
        PromotionAdImp manager = getPromotionManager(placementId);
        if (manager != null) {
            return manager.isReady();
        }
        return false;
    }

    public void showPromotionAd(Activity activity, PromotionAdRect rect, String placementId) {
        PromotionAdImp manager = getPromotionManager(placementId);
        if (manager != null) {
            manager.showAds(activity, rect);
        }
    }

    public void hidePromotionAd(String placementId) {
        PromotionAdImp manager = getPromotionManager(placementId);
        if (manager != null) {
            manager.hideAds();
        }
    }

    private InterstitialAdImp getIsManager(String placementId) {
        if (!mIsManagers.containsKey(placementId)) {
            InterstitialAdImp interstitialAdImp = new InterstitialAdImp(placementId);
            mIsManagers.put(placementId, interstitialAdImp);
            return interstitialAdImp;
        }
        return mIsManagers.get(placementId);
    }

    private VideoAdImp getRvManager(String placementId) {
        if (!mRvManagers.containsKey(placementId)) {
            VideoAdImp videoAdImp = new VideoAdImp(placementId);
            mRvManagers.put(placementId, videoAdImp);
            return videoAdImp;
        }
        return mRvManagers.get(placementId);
    }

    public PromotionAdImp getPromotionManager(String placementId) {
        if (!mPromotionManagers.containsKey(placementId)) {
            PromotionAdImp adImp = new PromotionAdImp(placementId);
            mPromotionManagers.put(placementId, adImp);
            return adImp;
        }
        return mPromotionManagers.get(placementId);
    }

}
