// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.openmediation.sdk.ImpressionData;
import com.openmediation.sdk.ImpressionDataListener;
import com.openmediation.sdk.InitCallback;
import com.openmediation.sdk.InitConfiguration;
import com.openmediation.sdk.OmAds;
import com.openmediation.sdk.banner.AdSize;
import com.openmediation.sdk.banner.BannerAd;
import com.openmediation.sdk.banner.BannerAdListener;
import com.openmediation.sdk.demo.utils.NewApiUtils;
import com.openmediation.sdk.interstitial.InterstitialAd;
import com.openmediation.sdk.interstitial.InterstitialAdListener;
import com.openmediation.sdk.nativead.AdIconView;
import com.openmediation.sdk.nativead.AdInfo;
import com.openmediation.sdk.nativead.MediaView;
import com.openmediation.sdk.nativead.NativeAd;
import com.openmediation.sdk.nativead.NativeAdListener;
import com.openmediation.sdk.nativead.NativeAdView;
import com.openmediation.sdk.promotion.PromotionAd;
import com.openmediation.sdk.promotion.PromotionAdListener;
import com.openmediation.sdk.promotion.PromotionAdRect;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.model.Scene;
import com.openmediation.sdk.video.RewardedVideoAd;
import com.openmediation.sdk.video.RewardedVideoListener;

public class MainActivity extends Activity {

    private static String TAG = "MainActivity";

    private Button rewardVideoButton;
    private Button interstitialButton;
    private Button bannerButton;
    private Button nativeButton;
    private Button promotionButton;
    private boolean isShowPromotion = false;

    private LinearLayout adContainer;

    private BannerAd bannerAd;
    private AdInfo mAdInfo;

    private ImpressionDataListener mDataListener;

    private final NativeAdListener mNativeAdListener = new NativeAdListener() {
        @Override
        public void onNativeAdLoaded(String placementId, AdInfo info) {
            Log.d(TAG, "onNativeAdLoaded, placementId: " + placementId + ", AdInfo : " + info);
            mAdInfo = info;
            onNativeAdLoadSuccess(placementId, info);
        }

        @Override
        public void onNativeAdLoadFailed(String placementId, Error error) {
            Log.d(TAG, "onNativeAdLoadFailed, placementId: " + placementId + ", error : " + error);
            nativeButton.setEnabled(true);
            nativeButton.setText("Native Load Failed, Try Again");
        }

        @Override
        public void onNativeAdImpression(String placementId, AdInfo info) {
            Log.d(TAG, "onNativeAdImpression, placementId: " + placementId + ", info : " + info);
        }

        @Override
        public void onNativeAdClicked(String placementId, AdInfo info) {
            Log.d(TAG, "onNativeAdClicked, placementId: " + placementId + ", info : " + info);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NewApiUtils.ENABLE_LOG = true;
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorStatuBar));
        }
        rewardVideoButton = findViewById(R.id.btn_reward_video);
        interstitialButton = findViewById(R.id.btn_interstitial);
        promotionButton = findViewById(R.id.btn_promotion);
        bannerButton = findViewById(R.id.btn_banner);
        nativeButton = findViewById(R.id.btn_native);
        adContainer = findViewById(R.id.ad_container);
        setButtonEnable(false);
        initSDK();
        setListener();
        if (RewardedVideoAd.isReady()) {
            setRewardVideoButtonStat(true);
        }
        if (InterstitialAd.isReady()) {
            setInterstitialButtonStat(true);
        }
        if (PromotionAd.isReady()) {
            setPromotionButtonStat(true);
        }
    }

    private void setButtonEnable(boolean enable) {
        bannerButton.setEnabled(enable);
        nativeButton.setEnabled(enable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        OmAds.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        OmAds.onPause(this);
    }

    private void initSDK() {
        NewApiUtils.printLog("start init sdk");
        InitConfiguration configuration = new InitConfiguration.Builder()
                .appKey(NewApiUtils.APPKEY)
                .preloadAdTypes(OmAds.AD_TYPE.NONE)
                .logEnable(true)
                .build();
        OmAds.init(this, configuration, new InitCallback() {
            @Override
            public void onSuccess() {
                NewApiUtils.printLog("init success");
                setButtonEnable(true);
            }

            @Override
            public void onError(Error result) {
                NewApiUtils.printLog("init failed " + result.toString());
            }
        });
    }

    private void setListener() {
        setPromotionListener();
        setVideoListener();
        setInterstitialListener();
        mDataListener = new ImpressionDataListener() {
            @Override
            public void onImpression(Error error, ImpressionData impressionData) {
                Log.d(TAG, "-----onImpression-----error: " + error + ", impressionData: " + impressionData);
            }
        };
        OmAds.addImpressionDataListener(mDataListener);
        NativeAd.addAdListener(NewApiUtils.P_NATIVE, mNativeAdListener);
    }

    private void setVideoListener() {
        RewardedVideoAd.setAdListener(new RewardedVideoListener() {
            @Override
            public void onRewardedVideoAvailabilityChanged(boolean available) {
                NewApiUtils.printLog("MainActivity----onRewardedVideoAvailabilityChanged----" + available);

                if (available) {
                    setRewardVideoButtonStat(true);
                }
            }

            @Override
            public void onRewardedVideoAdShowed(Scene scene) {
                NewApiUtils.printLog("onRewardedVideoAdShowed " + scene);
            }

            @Override
            public void onRewardedVideoAdShowFailed(Scene scene, Error error) {
                NewApiUtils.printLog("onRewardedVideoAdShowFailed " + scene);
            }

            @Override
            public void onRewardedVideoAdClicked(Scene scene) {
                NewApiUtils.printLog("onRewardedVideoAdClicked " + scene);
            }

            @Override
            public void onRewardedVideoAdClosed(Scene scene) {
                NewApiUtils.printLog("onRewardedVideoAdClosed " + scene);
            }

            @Override
            public void onRewardedVideoAdStarted(Scene scene) {
                NewApiUtils.printLog("onRewardedVideoAdStarted " + scene);
            }

            @Override
            public void onRewardedVideoAdEnded(Scene scene) {
                NewApiUtils.printLog("onRewardedVideoAdEnded " + scene);
            }

            @Override
            public void onRewardedVideoAdRewarded(Scene scene) {
                NewApiUtils.printLog("onRewardedVideoAdRewarded " + scene);
            }
        });
    }


    private void setInterstitialListener() {
        InterstitialAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialAdAvailabilityChanged(boolean available) {
                NewApiUtils.printLog("MainActivity----onInterstitialAdAvailabilityChanged----" + available);
                if (available) {
                    setInterstitialButtonStat(true);
                }
            }

            @Override
            public void onInterstitialAdShowed(Scene scene) {
                NewApiUtils.printLog("onInterstitialAdShowed " + scene);
            }

            @Override
            public void onInterstitialAdShowFailed(Scene scene, Error error) {
                NewApiUtils.printLog("onInterstitialAdShowFailed " + error);
            }

            @Override
            public void onInterstitialAdClosed(Scene scene) {
                NewApiUtils.printLog("onInterstitialAdClosed " + scene);
            }

            @Override
            public void onInterstitialAdClicked(Scene scene) {
                NewApiUtils.printLog("onInterstitialAdClicked " + scene);
            }
        });
    }

    private void setPromotionListener() {
        PromotionAd.setAdListener(new PromotionAdListener() {
            @Override
            public void onPromotionAdAvailabilityChanged(boolean available) {
                if (available) {
                    setPromotionButtonStat(true);
                }
                NewApiUtils.printLog("onPromotionAdAvailabilityChanged " + available);
            }

            @Override
            public void onPromotionAdShowed(Scene scene) {
                NewApiUtils.printLog("onPromotionAdShowed " + scene);
            }

            @Override
            public void onPromotionAdShowFailed(Scene scene, Error error) {
                NewApiUtils.printLog("onPromotionAdShowFailed " + scene);
            }

            @Override
            public void onPromotionAdHidden(Scene scene) {
                NewApiUtils.printLog("onPromotionAdHidden " + scene);
            }

            @Override
            public void onPromotionAdClicked(Scene scene) {
                NewApiUtils.printLog("onPromotionAdClicked " + scene);
            }
        });
    }

    public void showRewardVideo(View view) {
        RewardedVideoAd.showAd();
        setRewardVideoButtonStat(false);
    }

    public void showInterstitial(View view) {
        InterstitialAd.showAd();
        setInterstitialButtonStat(false);
    }

    public void showPromotion(View view) {
        if (PromotionAd.isReady()) {
            PromotionAdRect adRect = new PromotionAdRect();
            adRect.setWidth(132);
            adRect.setScaleY(0.07f);
            adRect.setAngle(10);
            PromotionAd.showAd(this, adRect);
            isShowPromotion = true;
            setPromotionButtonStat(false);
        }
    }

    public void showSplash(View view) {
        startActivity(new Intent(MainActivity.this, SplashAdActivity.class));
    }

    public void loadAndShowBanner(View view) {
//        adContainer.removeAllViews();
        bannerButton.setEnabled(false);
        bannerButton.setText("Banner Ad Loading...");
//        if (bannerAd != null) {
//            bannerAd.destroy();
//        }
        if (bannerAd == null) {
            bannerAd = new BannerAd(NewApiUtils.P_BANNER, new BannerAdListener() {
                @Override
                public void onBannerAdLoaded(String placementId, View view) {
                    try {
                        if (null != view.getParent()) {
                            ((ViewGroup) view.getParent()).removeView(view);
                        }
                        adContainer.removeAllViews();
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                        adContainer.addView(view, layoutParams);
                    } catch (Exception e) {
                        Log.e("AdtDebug", e.getLocalizedMessage());
                    }
                    bannerButton.setEnabled(true);
                    bannerButton.setText("Load And Show Banner Ad");
                }

                @Override
                public void onBannerAdLoadFailed(String placementId, Error error) {
                    bannerButton.setEnabled(true);
                    bannerButton.setText("Banner Load Failed, Try Again");
                }

                @Override
                public void onBannerAdClicked(String placementId) {

                }

            });
        }
        bannerAd.setAdSize(AdSize.BANNER);
        bannerAd.loadAd();
    }

    private void onNativeAdLoadSuccess(String placementId, AdInfo info) {
        adContainer.removeAllViews();
        if (info.isTemplateRender()) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(Gravity.CENTER);
            adContainer.addView(info.getView(), layoutParams);
        } else {
            View adView = LayoutInflater.from(MainActivity.this).inflate(R.layout.native_ad_layout, null);
            TextView title = adView.findViewById(R.id.ad_title);
            title.setText(info.getTitle());
            TextView desc = adView.findViewById(R.id.ad_desc);
            desc.setText(info.getDesc());
            Button btn = adView.findViewById(R.id.ad_btn);
            btn.setText(info.getCallToActionText());
            MediaView mediaView = adView.findViewById(R.id.ad_media);
            NativeAdView nativeAdView = new NativeAdView(MainActivity.this);
            AdIconView adIconView = adView.findViewById(R.id.ad_icon_media);
            DisplayMetrics displayMetrics = MainActivity.this.getResources().getDisplayMetrics();
            mediaView.getLayoutParams().height = (int) (displayMetrics.widthPixels / (1200.0 / 627.0));
            nativeAdView.addView(adView);
            nativeAdView.setTitleView(title);
            nativeAdView.setDescView(desc);
            nativeAdView.setAdIconView(adIconView);
            nativeAdView.setCallToActionView(btn);
            nativeAdView.setMediaView(mediaView);

            NativeAd.registerNativeAdView(placementId, nativeAdView, info);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            adContainer.addView(nativeAdView, layoutParams);
        }
        nativeButton.setEnabled(true);
        nativeButton.setText("Load And Show Native Ad");
    }

    public void loadAndShowNative(View view) {
        nativeButton.setEnabled(false);
        nativeButton.setText("Native Ad Loading...");
        if (mAdInfo != null) {
            NativeAd.destroy(NewApiUtils.P_NATIVE, mAdInfo);
        }
        adContainer.removeAllViews();
        // for TikTok and TencentAd in China traffic
//        NativeAd.setDisplayParams(NewApiUtils.P_NATIVE, 300, 0);
        NativeAd.loadAd(NewApiUtils.P_NATIVE);
    }

    private void setRewardVideoButtonStat(boolean isEnable) {
        rewardVideoButton.setEnabled(isEnable);
        if (isEnable) {
            rewardVideoButton.setText("Show Reward Video Ad");
        } else {
            rewardVideoButton.setText("Reward Video Ad Loading...");
        }
    }

    private void setInterstitialButtonStat(boolean isEnable) {
        interstitialButton.setEnabled(isEnable);
        if (isEnable) {
            interstitialButton.setText("Show Interstitial Ad");
        } else {
            interstitialButton.setText("Interstitial Ad Loading...");
        }
    }

    private void setPromotionButtonStat(boolean isEnable) {
        promotionButton.setEnabled(isEnable);
        if (isEnable) {
            promotionButton.setText("Show Promotion Ad");
        } else {
            promotionButton.setText("Promotion Ad Loading...");
        }
    }

    @Override
    public void onBackPressed() {
        if (isShowPromotion) {
            PromotionAd.hideAd();
            isShowPromotion = false;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bannerAd != null) {
            bannerAd.destroy();
        }
        NativeAd.removeAdListener(NewApiUtils.P_NATIVE, mNativeAdListener);
        if (mAdInfo != null) {
            NativeAd.destroy(NewApiUtils.P_NATIVE, mAdInfo);
        }
        if (mDataListener != null) {
            OmAds.removeImpressionDataListener(mDataListener);
        }
    }
}
