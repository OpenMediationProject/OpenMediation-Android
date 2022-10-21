// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.openmediation.sdk.ImpressionData;
import com.openmediation.sdk.ImpressionDataListener;
import com.openmediation.sdk.InitCallback;
import com.openmediation.sdk.InitConfiguration;
import com.openmediation.sdk.OmAds;
import com.openmediation.sdk.banner.AdSize;
import com.openmediation.sdk.banner.BannerAd;
import com.openmediation.sdk.banner.BannerAdListener;
import com.openmediation.sdk.demo.utils.Constants;
import com.openmediation.sdk.interstitial.InterstitialAd;
import com.openmediation.sdk.interstitial.InterstitialAdListener;
import com.openmediation.sdk.promotion.PromotionAd;
import com.openmediation.sdk.promotion.PromotionAdListener;
import com.openmediation.sdk.promotion.PromotionAdRect;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.model.Scene;
import com.openmediation.sdk.video.RewardedVideoAd;
import com.openmediation.sdk.video.RewardedVideoListener;

public class MainActivity extends Activity implements View.OnClickListener {

    private static String TAG = "MainActivity";

    private Button rewardVideoButton;
    private Button interstitialButton;
    private Button bannerButton;
    private Button splashButton;
    private Button promotionButton;
    private Button nativeRecyclerView;
    private boolean isShowPromotion = false;

    private LinearLayout adContainer;

    private BannerAd bannerAd;

    private ImpressionDataListener mDataListener;
    private PromotionAdListener mPromotionAdListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Constants.ENABLE_LOG = true;
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
        splashButton = findViewById(R.id.btn_splash);
        adContainer = findViewById(R.id.ad_container);
        nativeRecyclerView = findViewById(R.id.btn_native_list);

        rewardVideoButton.setOnClickListener(this);
        interstitialButton.setOnClickListener(this);
        promotionButton.setOnClickListener(this);
        bannerButton.setOnClickListener(this);
        splashButton.setOnClickListener(this);
        nativeRecyclerView.setOnClickListener(this);

        WebView.setWebContentsDebuggingEnabled(true);
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
        nativeRecyclerView.setEnabled(enable);
        splashButton.setEnabled(enable);
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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reward_video:
                showRewardVideo();
                break;
            case R.id.btn_interstitial:
                showInterstitial();
                break;
            case R.id.btn_banner:
                loadAndShowBanner();
                break;
            case R.id.btn_splash:
                showSplash();
                break;
            case R.id.btn_promotion:
                showPromotion();
                break;
            case R.id.btn_native_list:
                startActivity(new Intent(MainActivity.this, NativeRecyclerActivity.class));
                break;
        }
    }

    private void initSDK() {
        OmAds.setGDPRConsent(true);
        Constants.printLog("om start init sdk");
        InitConfiguration configuration = new InitConfiguration.Builder()
                .appKey(Constants.APPKEY)
                .logEnable(true)
                .build();
        OmAds.init(this, configuration, new InitCallback() {
            @Override
            public void onSuccess() {
                Constants.printLog("init success");
                setButtonEnable(true);
            }

            @Override
            public void onError(Error result) {
                Constants.printLog("init failed " + result.toString());
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
    }

    private void setVideoListener() {
        RewardedVideoAd.setAdListener(new RewardedVideoListener() {
            @Override
            public void onRewardedVideoAvailabilityChanged(boolean available) {
                Constants.printLog("MainActivity----onRewardedVideoAvailabilityChanged----" + available);

                if (available) {
                    setRewardVideoButtonStat(true);
                }
            }

            @Override
            public void onRewardedVideoAdShowed(Scene scene) {
                Constants.printLog("onRewardedVideoAdShowed " + scene);
            }

            @Override
            public void onRewardedVideoAdShowFailed(Scene scene, Error error) {
                Constants.printLog("onRewardedVideoAdShowFailed " + scene);
            }

            @Override
            public void onRewardedVideoAdClicked(Scene scene) {
                Constants.printLog("onRewardedVideoAdClicked " + scene);
            }

            @Override
            public void onRewardedVideoAdClosed(Scene scene) {
                Constants.printLog("onRewardedVideoAdClosed " + scene);
            }

            @Override
            public void onRewardedVideoAdStarted(Scene scene) {
                Constants.printLog("onRewardedVideoAdStarted " + scene);
            }

            @Override
            public void onRewardedVideoAdEnded(Scene scene) {
                Constants.printLog("onRewardedVideoAdEnded " + scene);
            }

            @Override
            public void onRewardedVideoAdRewarded(Scene scene) {
                Constants.printLog("onRewardedVideoAdRewarded " + scene);
            }
        });
    }


    private void setInterstitialListener() {
        InterstitialAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialAdAvailabilityChanged(boolean available) {
                Constants.printLog("MainActivity----onInterstitialAdAvailabilityChanged----" + available);
                if (available) {
                    setInterstitialButtonStat(true);
                }
            }

            @Override
            public void onInterstitialAdShowed(Scene scene) {
                Constants.printLog("onInterstitialAdShowed " + scene);
            }

            @Override
            public void onInterstitialAdShowFailed(Scene scene, Error error) {
                Constants.printLog("onInterstitialAdShowFailed " + error);
            }

            @Override
            public void onInterstitialAdClosed(Scene scene) {
                Constants.printLog("onInterstitialAdClosed " + scene);
            }

            @Override
            public void onInterstitialAdClicked(Scene scene) {
                Constants.printLog("onInterstitialAdClicked " + scene);
            }
        });
    }

    private void setPromotionListener() {
        mPromotionAdListener = new PromotionAdListener() {
            @Override
            public void onPromotionAdAvailabilityChanged(boolean available) {
                if (available) {
                    setPromotionButtonStat(true);
                }
                Constants.printLog("onPromotionAdAvailabilityChanged " + available);
            }

            @Override
            public void onPromotionAdShowed(Scene scene) {
                Constants.printLog("onPromotionAdShowed " + scene);
            }

            @Override
            public void onPromotionAdShowFailed(Scene scene, Error error) {
                Constants.printLog("onPromotionAdShowFailed " + scene);
            }

            @Override
            public void onPromotionAdHidden(Scene scene) {
                Constants.printLog("onPromotionAdHidden " + scene);
            }

            @Override
            public void onPromotionAdClicked(Scene scene) {
                Constants.printLog("onPromotionAdClicked " + scene);
            }
        };
        PromotionAd.addAdListener(mPromotionAdListener);
    }

    public void showRewardVideo() {
        RewardedVideoAd.showAd();
        setRewardVideoButtonStat(false);
    }

    public void showInterstitial() {
        InterstitialAd.showAd();
        setInterstitialButtonStat(false);
    }

    public void showPromotion() {
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

    public void showSplash() {
        startActivity(new Intent(MainActivity.this, SplashAdActivity.class));
    }

    public void loadAndShowBanner() {
        bannerButton.setEnabled(false);
        bannerButton.setText("Banner Ad Loading...");
        if (bannerAd != null) {
            bannerAd.destroy();
        }
        bannerAd = new BannerAd(Constants.P_BANNER, new BannerAdListener() {
            @Override
            public void onBannerAdLoaded(String placementId, View view) {
                Constants.printLog("onBannerAdLoaded: " + placementId);
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
                Constants.printLog("onBannerAdLoadFailed: " + placementId + ", error: " + error);
                bannerButton.setEnabled(true);
                bannerButton.setText("Banner Load Failed, Try Again");
            }

            @Override
            public void onBannerAdClicked(String placementId) {
                Constants.printLog("onBannerAdClicked: " + placementId);
            }

        });
        bannerAd.setAdSize(AdSize.BANNER);
        bannerAd.loadAd();
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
        if (mDataListener != null) {
            OmAds.removeImpressionDataListener(mDataListener);
        }
        PromotionAd.removeAdListener(mPromotionAdListener);
    }

}
