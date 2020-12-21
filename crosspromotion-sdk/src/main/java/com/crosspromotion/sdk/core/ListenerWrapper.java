package com.crosspromotion.sdk.core;

import android.view.View;

import com.crosspromotion.sdk.banner.BannerAdListener;
import com.crosspromotion.sdk.interstitial.InterstitialAdListener;
import com.crosspromotion.sdk.nativead.Ad;
import com.crosspromotion.sdk.nativead.NativeAdListener;
import com.crosspromotion.sdk.promotion.PromotionAdListener;
import com.crosspromotion.sdk.utils.error.Error;
import com.crosspromotion.sdk.video.RewardedVideoListener;
import com.openmediation.sdk.utils.HandlerUtil;

public class ListenerWrapper {
    private RewardedVideoListener mVideoListener;
    private InterstitialAdListener mIsListener;
    private BannerAdListener mBannerListener;
    private NativeAdListener mNativeListener;
    private PromotionAdListener mPromotionAdListener;

    public void setVideoListener(RewardedVideoListener listener) {
        mVideoListener = listener;
    }

    public void setInterstitialListener(InterstitialAdListener listener) {
        mIsListener = listener;
    }

    public void setBannerListener(BannerAdListener listener) {
        mBannerListener = listener;
    }

    public void setNativeListener(NativeAdListener listener) {
        mNativeListener = listener;
    }

    public void setPromotionListener(PromotionAdListener listener) {
        mPromotionAdListener = listener;
    }

    public void onBannerAdsReady(final String placementId, final View view) {
        if (canSendCallback(mBannerListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mBannerListener.onBannerAdReady(placementId, view);
                }
            });
        }
    }

    public void onNativeAdsReady(final String placementId, final Ad ad) {
        if (canSendCallback(mNativeListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mNativeListener.onNativeAdReady(placementId, ad);
                }
            });
        }
    }

    public void onAdsLoadFailed(final String placementId, final Error error) {
        if (canSendCallback(mBannerListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mBannerListener.onBannerAdFailed(placementId, error);
                }
            });
            return;
        }
        if (canSendCallback(mVideoListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mVideoListener.onRewardedVideoAdLoadFailed(placementId, error);
                }
            });
            return;
        }

        if (canSendCallback(mIsListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mIsListener.onInterstitialAdLoadFailed(placementId, error);
                }
            });
            return;
        }
        if (canSendCallback(mNativeListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mNativeListener.onNativeAdFailed(placementId, error);
                }
            });
            return;
        }
        if (canSendCallback(mPromotionAdListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mPromotionAdListener.onPromotionAdLoadFailed(placementId, error);
                }
            });
        }
    }

    public void onAdsLoadSuccess(final String placementId) {
        if (canSendCallback(mVideoListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mVideoListener.onRewardedVideoAdLoadSuccess(placementId);
                }
            });
            return;
        }

        if (canSendCallback(mIsListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mIsListener.onInterstitialAdLoadSuccess(placementId);
                }
            });
            return;
        }
        if (canSendCallback(mPromotionAdListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mPromotionAdListener.onPromotionAdLoadSuccess(placementId);
                }
            });
        }
    }

    public void onAdOpened(final String placementId) {
        if (canSendCallback(mVideoListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mVideoListener.onRewardedVideoAdShowed(placementId);
                }
            });
            return;
        }

        if (canSendCallback(mIsListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mIsListener.onInterstitialAdShowed(placementId);
                }
            });
            return;
        }
        if (canSendCallback(mPromotionAdListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mPromotionAdListener.onPromotionAdShowed(placementId);
                }
            });
        }
    }

    public void onAdOpenFailed(final String placementId, final Error error) {
        if (canSendCallback(mBannerListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mBannerListener.onBannerAdShowFailed(placementId, error);
                }
            });
            return;
        }
        if (canSendCallback(mVideoListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mVideoListener.onRewardedVideoAdShowFailed(placementId, error);
                }
            });
            return;
        }
        if (canSendCallback(mIsListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mIsListener.onInterstitialAdShowFailed(placementId, error);
                }
            });
            return;
        }
        if (canSendCallback(mNativeListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mNativeListener.onNativeAdShowFailed(placementId, error);
                }
            });
            return;
        }
        if (canSendCallback(mPromotionAdListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mPromotionAdListener.onPromotionAdShowFailed(placementId, error);
                }
            });
        }
    }

    public void onAdClicked(final String placementId) {
        if (canSendCallback(mVideoListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mVideoListener.onRewardedVideoAdClicked(placementId);
                }
            });
            return;
        }

        if (canSendCallback(mIsListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mIsListener.onInterstitialAdClicked(placementId);
                }
            });
            return;
        }

        if (canSendCallback(mBannerListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mBannerListener.onBannerAdClicked(placementId);
                }
            });
            return;
        }
        if (canSendCallback(mNativeListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mNativeListener.onNativeAdClicked(placementId);
                }
            });
            return;
        }
        if (canSendCallback(mPromotionAdListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mPromotionAdListener.onPromotionAdClicked(placementId);
                }
            });
        }
    }

    public void onAdClosed(final String placementId) {
        if (canSendCallback(mVideoListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mVideoListener.onRewardedVideoAdClosed(placementId);
                }
            });
            return;
        }

        if (canSendCallback(mIsListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mIsListener.onInterstitialAdClosed(placementId);
                }
            });
            return;
        }
        if (canSendCallback(mPromotionAdListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mPromotionAdListener.onPromotionAdHidden(placementId);
                }
            });
        }
    }

    public void onRewardAdStarted(final String placementId) {
        if (canSendCallback(mVideoListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mVideoListener.onRewardedVideoAdStarted(placementId);
                }
            });
        }
    }

    public void onRewardAdEnded(final String placementId) {
        if (canSendCallback(mVideoListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mVideoListener.onRewardedVideoAdEnded(placementId);
                }
            });
        }
    }

    public void onRewardAdRewarded(final String placementId) {
        if (canSendCallback(mVideoListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mVideoListener.onRewardedVideoAdRewarded(placementId);
                }
            });
        }
    }

    public void onAdsEvent(final String placementId, final String event) {
        if (canSendCallback(mVideoListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mVideoListener.onVideoAdEvent(placementId, event);
                }
            });
            return;
        }

        if (canSendCallback(mIsListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mIsListener.onInterstitialAdEvent(placementId, event);
                }
            });
        }
    }

    private boolean canSendCallback(Object listener) {
        return listener != null;
    }

    private void sendCallback(Runnable callbackRunnable) {
        if (callbackRunnable != null) {
            HandlerUtil.runOnUiThread(callbackRunnable);
        }
    }
}
