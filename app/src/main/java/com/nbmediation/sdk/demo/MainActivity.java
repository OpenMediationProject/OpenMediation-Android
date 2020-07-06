// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.nbmediation.sdk.InitCallback;
import com.nbmediation.sdk.NmAds;
import com.nbmediation.sdk.api.unity.NmSdk;
import com.nbmediation.sdk.banner.AdSize;
import com.nbmediation.sdk.banner.BannerAd;
import com.nbmediation.sdk.banner.BannerAdListener;
import com.nbmediation.sdk.core.NmManager;
import com.nbmediation.sdk.demo.utils.NewApiUtils;
import com.nbmediation.sdk.interstitial.InterstitialAd;
import com.nbmediation.sdk.interstitial.InterstitialAdListener;
import com.nbmediation.sdk.nativead.AdIconView;
import com.nbmediation.sdk.nativead.AdInfo;
import com.nbmediation.sdk.nativead.MediaView;
import com.nbmediation.sdk.nativead.NativeAd;
import com.nbmediation.sdk.nativead.NativeAdListener;
import com.nbmediation.sdk.nativead.NativeAdView;
import com.nbmediation.sdk.utils.error.Error;
import com.nbmediation.sdk.utils.model.Scene;
import com.nbmediation.sdk.video.RewardedVideoAd;
import com.nbmediation.sdk.video.RewardedVideoListener;

public class MainActivity extends AppCompatActivity {

    private Button rewardVideoButton;
    private Button interstitialButton;
    private Button bannerButton;
    private Button nativeButton;


    private LinearLayout adContainer;
    private View adView;
    private NativeAdView nativeAdView;


    private BannerAd bannerAd;
    private NativeAd nativeAd;

    private final static int WRITE_EXTERNAL_STORAGE_CODE = 102;

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
        bannerButton = findViewById(R.id.btn_banner);
        nativeButton = findViewById(R.id.btn_native);
        adContainer = findViewById(R.id.ad_container);
        initSDK();
        if (RewardedVideoAd.isReady()) {
            setRewardVideoButtonStat(true);
        }
        if (InterstitialAd.isReady()) {
            setInterstitialButtonStat(true);
        }
        if (PackageManager.PERMISSION_GRANTED
                != ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_CODE);
        }

    }

    private void initSDK() {
        NewApiUtils.printLog("start init sdk");
        NmAds.init(this, NewApiUtils.APPKEY, new InitCallback() {
            @Override
            public void onSuccess() {
                NewApiUtils.printLog("init success");
                setVideoListener();
                setInterstitialListener();
            }

            @Override
            public void onError(Error result) {
                NewApiUtils.printLog("init failed " + result.toString());

            }
        });
    }

    private void setVideoListener() {
        RewardedVideoAd.setAdListener(new RewardedVideoListener() {
            @Override
            public void onRewardedVideoAvailabilityChanged(boolean available) {
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
                NewApiUtils.printLog("onInterstitialAdShowFailed " + scene);
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

    public void showRewardVideo(View view) {
        RewardedVideoAd.showAd();
        setRewardVideoButtonStat(false);
    }

    public void showInterstitial(View view) {
        InterstitialAd.showAd();
        setInterstitialButtonStat(false);
    }

    public void loadAndShowBanner(View view) {
        adContainer.removeAllViews();
        bannerButton.setEnabled(false);
        bannerButton.setText("Banner Ad Loading...");
        if (bannerAd != null) {
            bannerAd.destroy();
        }
        bannerAd = new BannerAd(this, NewApiUtils.P_BANNER, new BannerAdListener() {
            @Override
            public void onAdReady(View view) {
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
            public void onAdFailed(String error) {
                bannerButton.setEnabled(true);
                bannerButton.setText("Banner Load Failed, Try Again");

            }

            @Override
            public void onAdClicked() {

            }
        });
        bannerAd.setAdSize(AdSize.AD_SIZE_320X50);
        bannerAd.loadAd();

    }

    public void loadAndShowNative(View view) {
        nativeButton.setEnabled(false);
        nativeButton.setText("Native Ad Loading...");
        if (nativeAd != null) {
            nativeAd.destroy();
        }
        adContainer.removeAllViews();
        nativeAd = new NativeAd(this, NewApiUtils.P_NATIVE, new NativeAdListener() {
            @Override
            public void onAdFailed(String msg) {
                nativeButton.setEnabled(true);
                nativeButton.setText("Native Load Failed, Try Again");
            }

            @Override
            public void onAdReady(AdInfo info) {
                adContainer.removeAllViews();
                adView = LayoutInflater.from(MainActivity.this).inflate(R.layout.native_ad_layout, null);

                TextView title = adView.findViewById(R.id.ad_title);
                if (!TextUtils.isEmpty(info.getTitle())) {
                    title.setText(info.getTitle());
                }


                TextView desc = adView.findViewById(R.id.ad_desc);
                if (!TextUtils.isEmpty(info.getDesc())) {
                    desc.setText(info.getDesc());
                }


                Button btn = adView.findViewById(R.id.ad_btn);

                if (!TextUtils.isEmpty(info.getCallToActionText())) {
                    btn.setText(info.getCallToActionText());
                } else {
                    btn.setVisibility(View.GONE);
                }


                MediaView mediaView = adView.findViewById(R.id.ad_media);

                nativeAdView = new NativeAdView(MainActivity.this);


                AdIconView adIconView = adView.findViewById(R.id.ad_icon_media);
                RelativeLayout adDescRl = adView.findViewById(R.id.ad_desc_rl);
                if (info.isTemplate()) {
                    adDescRl.setVisibility(View.GONE);
                }

                nativeAdView.addView(adView);

                nativeAdView.setTitleView(title);
                nativeAdView.setDescView(desc);
                nativeAdView.setAdIconView(adIconView);
                nativeAdView.setCallToActionView(btn);
                nativeAdView.setMediaView(mediaView);

                nativeAd.registerNativeAdView(nativeAdView);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                adContainer.addView(nativeAdView, layoutParams);
                nativeButton.setEnabled(true);
                nativeButton.setText("Load And Show Native Ad");

            }

            @Override
            public void onAdClicked() {

            }
        });
        nativeAd.loadAd();

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

    public void showVideo1(View view) {
        if (NmManager.getInstance().isRewardedVideoReady("236")) {
            NmSdk.showRewardedVideo("236", "");
        } else {
            Toast.makeText(this, "没准备好，稍后再试", Toast.LENGTH_SHORT).show();
        }

    }

    public void showVideo2(View view) {
        if (NmManager.getInstance().isRewardedVideoReady("246")) {
            NmSdk.showRewardedVideo("246", "");
        } else {
            Toast.makeText(this, "没准备好，稍后再试", Toast.LENGTH_SHORT).show();
        }

    }

}
