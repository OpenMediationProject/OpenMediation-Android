package com.nbmediation.sdk.api.unity;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.nbmediation.sdk.InitCallback;
import com.nbmediation.sdk.NmAds;
import com.nbmediation.sdk.R;
import com.nbmediation.sdk.banner.AdSize;
import com.nbmediation.sdk.banner.BannerAd;
import com.nbmediation.sdk.banner.BannerAdListener;
import com.nbmediation.sdk.core.NmManager;
import com.nbmediation.sdk.interstitial.InterstitialAd;
import com.nbmediation.sdk.interstitial.InterstitialAdListener;
import com.nbmediation.sdk.utils.AdLog;
import com.nbmediation.sdk.utils.DeveloperLog;
import com.nbmediation.sdk.utils.HandlerUtil;
import com.nbmediation.sdk.utils.error.Error;
import com.nbmediation.sdk.utils.model.Scene;
import com.nbmediation.sdk.video.RewardedVideoAd;
import com.nbmediation.sdk.video.RewardedVideoListener;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiantao.tu on 2020/5/20.
 */
public class NmSdk {
    private static boolean isDebug = false;
    private static String TAG = "NmSdkAPI";

    public static final int bannerViewId = R.id.unity_banner;

    public static void init(Activity paramActivity, String paramString) {
        LogD("init(appkey):" + paramString);
        NmAds.init(paramActivity, paramString, null);
    }

    public static void init(Activity paramActivity, String paramString, InitCallback paramInitCallback) {
        LogD("init(appkey,callback):" + paramString);
        NmAds.init(paramActivity, paramString, paramInitCallback);
    }

    public static boolean isInit() {
        boolean tmp3_0 = NmAds.isInit();
        LogD("isInit:" + tmp3_0);
        return tmp3_0;
    }

    public static void setIAP(float paramFloat, String paramString) {
        NmAds.setIAP(paramFloat, paramString);
    }


    public static void setRewardedVideoListener(final VideoListener paramVideoListener) {
        LogD("setRewardedVideoListener");
        RewardedVideoAd.setAdListener(new RewardedVideoListener() {
            public void onRewardedVideoAvailabilityChanged(boolean paramAnonymousBoolean) {
                paramVideoListener.onRewardedVideoAvailabilityChanged(paramAnonymousBoolean);
            }

            public void onRewardedVideoAdShowed(Scene paramAnonymousScene) {
                paramVideoListener.onRewardedVideoAdShowed(paramAnonymousScene != null ? paramAnonymousScene.getN() : "");
            }

            @Override
            public void onRewardedVideoAdShowFailed(Scene scene, Error error) {
                paramVideoListener.onRewardedVideoAdShowFailed(scene != null ? scene.getN() : "", error.toString());
            }


            public void onRewardedVideoAdClicked(Scene paramAnonymousScene) {
                paramVideoListener.onRewardedVideoAdClicked(paramAnonymousScene != null ? paramAnonymousScene.getN() : "");
            }

            public void onRewardedVideoAdClosed(Scene paramAnonymousScene) {
                paramVideoListener.onRewardedVideoAdClosed(paramAnonymousScene != null ? paramAnonymousScene.getN() : "");
            }

            public void onRewardedVideoAdStarted(Scene paramAnonymousScene) {
                paramVideoListener.onRewardedVideoAdStarted(paramAnonymousScene != null ? paramAnonymousScene.getN() : "");
            }

            public void onRewardedVideoAdEnded(Scene paramAnonymousScene) {
                paramVideoListener.onRewardedVideoAdEnded(paramAnonymousScene != null ? paramAnonymousScene.getN() : "");
            }

            public void onRewardedVideoAdRewarded(Scene paramAnonymousScene) {
                paramVideoListener.onRewardedVideoAdRewarded(paramAnonymousScene != null ? paramAnonymousScene.getN() : "");
            }
        });
    }

    public static void setExtId(String paramString) {
        setExtId("", paramString);
    }

    public static void setExtId(String paramString1, String paramString2) {
        if (!TextUtils.isEmpty(paramString2)) {
            RewardedVideoAd.setExtId(paramString1, paramString2);
        }
    }

    public static void showRewardedVideo() {
        RewardedVideoAd.showAd("");
    }

    public static void showRewardedVideo(String paramString) {
        RewardedVideoAd.showAd(paramString);
    }

    public static boolean isRewardedVideoReady() {
        return RewardedVideoAd.isReady();
    }

    public static void setInterstitialListener(final InterstitialListener paramInterstitialListener) {
        LogD("setInterstitialListener");
        InterstitialAd.setAdListener(new InterstitialAdListener() {
            public void onInterstitialAdAvailabilityChanged(boolean paramAnonymousBoolean) {
                paramInterstitialListener.onInterstitialAdAvailabilityChanged(paramAnonymousBoolean);
            }

            public void onInterstitialAdShowed(Scene paramAnonymousScene) {
                paramInterstitialListener.onInterstitialAdShowed(paramAnonymousScene != null ? paramAnonymousScene.getN() : "");
            }

            public void onInterstitialAdShowFailed(Scene paramAnonymousScene, Error paramAnonymousAdTimingError) {
                paramInterstitialListener.onInterstitialAdShowFailed(paramAnonymousScene != null ? paramAnonymousScene.getN() : "", paramAnonymousAdTimingError.toString());
            }

            public void onInterstitialAdClosed(Scene paramAnonymousScene) {
                paramInterstitialListener.onInterstitialAdClosed(paramAnonymousScene != null ? paramAnonymousScene.getN() : "");
            }

            public void onInterstitialAdClicked(Scene paramAnonymousScene) {
                paramInterstitialListener.onInterstitialAdClicked(paramAnonymousScene != null ? paramAnonymousScene.getN() : "");
            }
        });
    }

    public static void showInterstitial() {
    }

    public static void showInterstitial(String paramString) {
        InterstitialAd.showAd(paramString);
    }

    public static boolean isInterstitialReady() {
        return InterstitialAd.isReady();
    }

    private static Map<String, UnityBannerAd> bannerCache = new ConcurrentHashMap<>();

    public synchronized static void loadBanner(final Activity activity, final String placementId) {
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UnityBannerAd unityBannerAd = bannerCache.get(placementId);
                if (unityBannerAd != null) {

                    if (unityBannerAd.isDestroy()) {
                        bannerCache.remove(placementId);
                    }
                    if (unityBannerAd.isReady()) {
                        return;

                    }
                }
                BannerAd bannerAd = new BannerAd(activity, placementId, new BannerAdListener() {
                    @Override
                    public void onAdReady(View view) {
                        UnityBannerAd proxyAd = bannerCache.get(placementId);
                        if (proxyAd != null && !proxyAd.isDestroy()) {
                            proxyAd.setReady(true);
                            proxyAd.setReadyView(view);
                        }

                    }

                    @Override
                    public void onAdFailed(String error) {

                    }

                    @Override
                    public void onAdClicked() {

                    }
                });
                bannerCache.put(placementId, new UnityBannerAd(bannerAd));
                bannerAd.setAdSize(AdSize.AD_SIZE_320X50);
                bannerAd.loadAd();
            }
        });

    }

    public static boolean isBannerReady(String placementId) {
        UnityBannerAd unityBannerAd = bannerCache.get(placementId);
        if (unityBannerAd == null) {
            return false;
        }
        return unityBannerAd.isReady();
    }

    public static void showBanner(final Activity activity, final String placementId) {
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UnityBannerAd unityBannerAd = bannerCache.get(placementId);
                View view;
                if (unityBannerAd == null ||
                        !unityBannerAd.isReady() ||
                        (view = unityBannerAd.getReadyView()) == null) {
                    return;
                }
                try {
                    if (null != view.getParent()) {
                        ((ViewGroup) view.getParent()).removeView(view);
                    }
                    ViewGroup adContainer = activity.getWindow().getDecorView().findViewById(android.R.id.content);

                    if (adContainer == null) {
                        throw new RuntimeException("get view for android.R.id.content is null,error");
                    }

                    View oldBannerView = adContainer.findViewById(bannerViewId);
                    if (oldBannerView != null) {
                        adContainer.removeView(oldBannerView);
                    }

                    View newBannerView = generateBannerView(activity.getApplicationContext(), view);
                    adContainer.addView(newBannerView);

                } catch (Throwable e) {
                    String errorMessage = e.getLocalizedMessage();
                    if (errorMessage != null)
                        Log.e("AdtDebug", errorMessage);
                }
            }
        });

    }

    public static void hideBanner(final Activity activity, final String placementId, final boolean isDestroy) {
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UnityBannerAd unityBannerAd = bannerCache.get(placementId);

                try {
                    ViewGroup adContainer = activity.getWindow().getDecorView().findViewById(android.R.id.content);

                    if (adContainer == null) {
                        throw new RuntimeException("get view for android.R.id.content is null,error");
                    }

                    View oldBannerView = adContainer.findViewById(bannerViewId);
                    if (oldBannerView != null) {
                        adContainer.removeView(oldBannerView);
                    }
                    View view;
                    if (unityBannerAd == null ||
                            !unityBannerAd.isReady() ||
                            (view = unityBannerAd.getReadyView()) == null) {
                        return;
                    }
                    if (null != view.getParent()) {
                        ((ViewGroup) view.getParent()).removeView(view);
                    }

                } catch (Throwable e) {
                    String errorMessage = e.getLocalizedMessage();
                    Log.e("AdtDebug", errorMessage == null ? "" : errorMessage);
                } finally {
                    if (unityBannerAd != null && isDestroy) {
                        unityBannerAd.destroy();
                        bannerCache.remove(placementId);
                    }
                }
            }
        });

    }

    public static void destroyAllForBanner(Activity activity) {
        hideBanner(activity, "", false);
        Iterator<Map.Entry<String, UnityBannerAd>> it = bannerCache.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, UnityBannerAd> entry = it.next();
            if (!entry.getValue().isDestroy()) {
                entry.getValue().destroy();
            }
            it.remove();
        }
    }

    private static RelativeLayout generateBannerView(Context context, View bannerView) {
        RelativeLayout relativeLayout = new RelativeLayout(context);
        relativeLayout.setId(bannerViewId);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        relativeLayout.setLayoutParams(layoutParams);
        RelativeLayout.LayoutParams bannerLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        bannerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        bannerLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        relativeLayout.addView(bannerView, bannerLayoutParams);
        return relativeLayout;
    }


    public static void Debug(boolean paramBoolean) {
        Log.d(TAG, "Debug:" + paramBoolean);
        isDebug = paramBoolean;
        AdLog.getSingleton().isDebug(true);
        DeveloperLog.enableDebug(null, true);
    }

    private static void LogD(String paramString) {
        if (isDebug) {
            Log.d(TAG, paramString);
        }
    }

    private static void LogE(String paramString) {
        if (isDebug) {
            Log.e(TAG, paramString);
        }
    }

    //*********支持开发者管理placement加载***********//
    public static void loadRewardedVideo(String placementId){
        NmManager.getInstance().loadRewardedVideo(placementId);
    }

    public static void setRewardedVideoListener(String placementId, final VideoListener paramVideoListener){
        NmManager.getInstance().setRewardedVideoListener(placementId, new RewardedVideoListener() {
            public void onRewardedVideoAvailabilityChanged(boolean paramAnonymousBoolean) {
                paramVideoListener.onRewardedVideoAvailabilityChanged(paramAnonymousBoolean);
            }

            public void onRewardedVideoAdShowed(Scene paramAnonymousScene) {
                paramVideoListener.onRewardedVideoAdShowed(paramAnonymousScene.getN());
            }

            @Override
            public void onRewardedVideoAdShowFailed(Scene scene, Error error) {
                paramVideoListener.onRewardedVideoAdShowFailed(scene.getN(), error.toString());
            }


            public void onRewardedVideoAdClicked(Scene paramAnonymousScene) {
                paramVideoListener.onRewardedVideoAdClicked(paramAnonymousScene.getN());
            }

            public void onRewardedVideoAdClosed(Scene paramAnonymousScene) {
                paramVideoListener.onRewardedVideoAdClosed(paramAnonymousScene.getN());
            }

            public void onRewardedVideoAdStarted(Scene paramAnonymousScene) {
                paramVideoListener.onRewardedVideoAdStarted(paramAnonymousScene.getN());
            }

            public void onRewardedVideoAdEnded(Scene paramAnonymousScene) {
                paramVideoListener.onRewardedVideoAdEnded(paramAnonymousScene.getN());
            }

            public void onRewardedVideoAdRewarded(Scene paramAnonymousScene) {
                paramVideoListener.onRewardedVideoAdRewarded(paramAnonymousScene.getN());
            }
        });
    }

    public static boolean isRewardedVideoReady(String placementId){
        return NmManager.getInstance().isRewardedVideoReady(placementId);
    }

    public static void showRewardedVideo(final String placementId, final String scene){
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NmManager.getInstance().showRewardedVideo(placementId, scene);
            }
        });
    }


    public static void loadInterstitial(String placementId){
        NmManager.getInstance().loadInterstitialAd(placementId);
    }

    public static void setInterstitialListener(String placementId, final InterstitialListener paramInterstitialListener){
        NmManager.getInstance().setInterstitialAdListener(placementId, new InterstitialAdListener() {
            public void onInterstitialAdAvailabilityChanged(boolean paramAnonymousBoolean) {
                paramInterstitialListener.onInterstitialAdAvailabilityChanged(paramAnonymousBoolean);
            }

            public void onInterstitialAdShowed(Scene paramAnonymousScene) {
                paramInterstitialListener.onInterstitialAdShowed(paramAnonymousScene.getN());
            }

            public void onInterstitialAdShowFailed(Scene paramAnonymousScene, Error paramAnonymousAdTimingError) {
                paramInterstitialListener.onInterstitialAdShowFailed(paramAnonymousScene.getN(), paramAnonymousAdTimingError.toString());
            }

            public void onInterstitialAdClosed(Scene paramAnonymousScene) {
                paramInterstitialListener.onInterstitialAdClosed(paramAnonymousScene.getN());
            }

            public void onInterstitialAdClicked(Scene paramAnonymousScene) {
                paramInterstitialListener.onInterstitialAdClicked(paramAnonymousScene.getN());
            }
        });
    }

    public static boolean isInterstitialReady(String placementId){
        return NmManager.getInstance().isInterstitialAdReady(placementId);
    }

    public static void showInterstitial(final String placementId, final String scene){
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NmManager.getInstance().showInterstitialAd(placementId, scene);
            }
        });
    }
}