package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.androidquery.AQuery;
import com.nbmediation.sdk.mediation.CustomNativeEvent;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.nativead.NativeAdView;
import com.nbmediation.sdk.utils.AdLog;
import com.qq.e.ads.nativ.NativeADEventListener;
import com.qq.e.ads.nativ.NativeADUnifiedListener;
import com.qq.e.ads.nativ.NativeUnifiedAD;
import com.qq.e.ads.nativ.NativeUnifiedADData;
import com.qq.e.ads.nativ.widget.NativeAdContainer;
import com.qq.e.comm.constants.AdPatternType;
import com.qq.e.comm.managers.GDTADManager;
import com.qq.e.comm.util.AdError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class TencentAdNative extends CustomNativeEvent implements NativeADUnifiedListener {

    private static String TAG = "OM-TencentAd: ";

    private NativeUnifiedADData mAdData;
    private NativeUnifiedAD mAdManager;
    private AQuery mAQuery;
    private NativeAdView adView;


    private Activity activity;

    @Override
    public void loadAd(final Activity activity, Map<String, String> config) {
        super.loadAd(activity, config);
        this.activity = activity;
        if (!check(activity, config)) {
            return;
        }
        String appKey = config.get("AppKey");
        if (!GDTADManager.getInstance().isInitialized()) {
            init(activity, appKey);
        }
        resetAdViews();
        mAdManager = new NativeUnifiedAD(activity, appKey, mInstancesKey, this);
//        mAdManager.setVideoPlayPolicy(VideoOption.VideoPlayPolicy.AUTO); // 本次拉回的视频广告，从用户的角度看是自动播放的
//        mAdManager.setVideoADContainerRender(VideoOption.VideoADContainerRender.SDK); // 视频播放前，用户看到的广告容器是由SDK渲染的
        mAdManager.loadData(1);
    }

    private void init(Activity activity, String appKey) {
        GDTADManager.getInstance().initWith(activity.getApplicationContext(), appKey);
    }

    @Override
    public void registerNativeView(NativeAdView adView) {
        if (isDestroyed || mAdData == null || activity == null) {
            return;
        }
        this.adView = adView;
        renderAdUi(adView);

        // 所有广告类型，注册mDownloadButton的点击事件
        List<View> clickableViews = new ArrayList<>();
        if (adView.getMediaView() != null) {
            clickableViews.add(adView.getMediaView());
        }

        if (adView.getAdIconView() != null) {
            clickableViews.add(adView.getAdIconView());
        }

        if (adView.getTitleView() != null) {
            clickableViews.add(adView.getTitleView());
        }

        if (adView.getDescView() != null) {
            clickableViews.add(adView.getDescView());
        }

        if (adView.getCallToActionView() != null) {
            clickableViews.add(adView.getCallToActionView());
        }
        final View hideView = new View(adView.getContext());
        for (View view : clickableViews) {
            if (view != null) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideView.callOnClick();
                    }
                });
            }
        }

        NativeAdContainer mContainer = new NativeAdContainer(activity);
        mContainer.addView(hideView);
        adView.addView(mContainer);

        clickableViews.clear();
        clickableViews.add(hideView);

        mAdData.bindAdToView(activity, mContainer, null, clickableViews);

//        if (mAdData.getAdPatternType() == AdPatternType.NATIVE_VIDEO) {
//
//        } else if (mAdData.getAdPatternType() == AdPatternType.NATIVE_2IMAGE_2TEXT ||
//                mAdData.getAdPatternType() == AdPatternType.NATIVE_1IMAGE_2TEXT) {
//            // 双图双文、单图双文：注册mImagePoster的点击事件
//        } else {
//        }
        mAdData.setNativeAdEventListener(new NativeADEventListener() {
            @Override
            public void onADExposed() {
                Log.d(TAG, "onADExposed: ");
            }

            @Override
            public void onADClicked() {
                if (isDestroyed) {
                    return;
                }
                onInsClicked();
                Log.d(TAG, "onADClicked: " + " clickUrl: " + mAdData.ext.get("clickUrl"));
            }

            @Override
            public void onADError(AdError error) {
                String msg = "onADError error code :" + error.getErrorCode()
                        + "  error msg: " + error.getErrorMsg();
                onInsError(msg);
                AdLog.getSingleton().LogD(TAG + msg);
            }

            @Override
            public void onADStatusChanged() {
                Log.d(TAG, "onADStatusChanged: ");
                updateAdAction(mAdData);
            }
        });
        updateAdAction(mAdData);
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_6;
    }

    @Override
    public void destroy(Activity activity) {
        if (mAdData != null) {
            mAdData.destroy();
        }
    }

    private static void updateAdAction(NativeUnifiedADData ad) {
        if (!ad.isAppAd()) {
            AdLog.getSingleton().LogD(TAG + "浏览");
            return;
        }
        switch (ad.getAppStatus()) {
            case 0:
                AdLog.getSingleton().LogD(TAG + "下载");
                break;
            case 1:
                AdLog.getSingleton().LogD(TAG + "启动");
                break;
            case 2:
                AdLog.getSingleton().LogD(TAG + "更新");
                break;
            case 4:
                AdLog.getSingleton().LogD(TAG + ad.getProgress() + "%");
                break;
            case 8:
                AdLog.getSingleton().LogD(TAG + "安装");
                break;
            case 16:
                AdLog.getSingleton().LogD(TAG + "下载失败，重新下载");
                break;
            default:
                AdLog.getSingleton().LogD(TAG + "浏览");
                break;
        }
    }


//    @Override
//    public void onAdFailed(String s) {
//        onInsError(s);
//    }
//
//    @Override
//    public void onAdClicked() {
//        onInsClicked();
//    }

    @Override
    public void onADLoaded(List<NativeUnifiedADData> list) {
        if (list != null && list.size() > 0) {
            mAdData = list.get(0);
            if (isDestroyed) {
                return;
            }
            initAd(mAdData);
            AdLog.getSingleton().LogD(TAG, "Native ad load success ");
        } else {
            String error = TAG + "Banner ad load failed: no ads";
            AdLog.getSingleton().LogD(error);
            onInsError(error);
        }

    }

    private void initAd(final NativeUnifiedADData ad) {
//        if (ad.getAdPatternType() == AdPatternType.NATIVE_VIDEO) {
//            if (!mPreloadVideo) {
//                return;
//            }
//            // 如果是视频广告，可以调用preloadVideo预加载视频素材
//            ad.preloadVideo(new VideoPreloadListener() {
//                @Override
//                public void onVideoCached() {
//                    Log.d(TAG, "onVideoCached");
//                    // 视频素材加载完成，此时展示广告不会有进度条。
//                    showAd(ad);
//                }
//
//                @Override
//                public void onVideoCacheFailed(int errorNo, String msg) {
//                    Log.d(TAG, "onVideoCacheFailed : " + msg);
//                }
//            });
//            return;
//        }
        showAd(ad);

    }

    private void showAd(final NativeUnifiedADData ad) {
        mAdInfo.setDesc(ad.getDesc());
        mAdInfo.setType(2);
        mAdInfo.setCallToActionText(ad.getCTAText());
        mAdInfo.setTitle(ad.getTitle());
        onInsReady(mAdInfo);
    }


    @Override
    public void onNoAD(AdError adError) {
        AdLog.getSingleton().LogD(TAG + "Banner ad load failed: code " + adError.getErrorCode() + " " + adError.getErrorMsg());
        onInsError(adError.getErrorMsg());
    }

    private void renderAdUi(NativeAdView adView) {
        mAQuery = new AQuery(adView);
        ImageView imageView = null;
        if (adView.getMediaView() != null) {
            adView.getMediaView().removeAllViews();
            imageView = new ImageView(adView.getContext());
            adView.getMediaView().addView(imageView);
            imageView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
            imageView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;

        }
        ImageView iconImageView = null;
        if (adView.getAdIconView() != null) {
            adView.getAdIconView().removeAllViews();
            iconImageView = new ImageView(adView.getContext());
            adView.getAdIconView().addView(iconImageView);
            iconImageView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
            iconImageView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;

        }

        int patternType = mAdData.getAdPatternType();
        if (patternType == AdPatternType.NATIVE_2IMAGE_2TEXT || patternType == AdPatternType.NATIVE_VIDEO) {
            mAQuery.id(iconImageView).image(mAdData.getIconUrl(), false, true);
            mAQuery.id(imageView).image(mAdData.getImgUrl(), false, true);
            mAQuery.id(adView.getTitleView()).text(mAdData.getTitle());
            mAQuery.id(adView.getDescView()).text(mAdData.getDesc());
        }
    }


    private void resetAdViews() {
        if (mAdData == null || adView != null) {
            return;
        }
        int patternType = mAdData.getAdPatternType();
        if (patternType == AdPatternType.NATIVE_2IMAGE_2TEXT || patternType == AdPatternType.NATIVE_VIDEO) {
            if (adView.getAdIconView() != null) {
                mAQuery.id(adView.getAdIconView()).clear();
            }
            if (adView.getMediaView() != null) {
                mAQuery.id(adView.getMediaView()).clear();
            }
            if (adView.getCallToActionView() != null) {
                mAQuery.id(adView.getCallToActionView()).clear();
            }

            if (adView.getTitleView() != null) {
                mAQuery.id(adView.getTitleView()).clear();
            }

            if (adView.getDescView() != null) {
                mAQuery.id(adView.getDescView()).clear();
            }

        }

    }

}
