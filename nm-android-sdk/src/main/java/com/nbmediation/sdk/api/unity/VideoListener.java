package com.nbmediation.sdk.api.unity;

/**
 * Created by jiantao.tu on 2020/5/20.
 */
public interface VideoListener {

    void onRewardedVideoAvailabilityChanged(boolean paramBoolean);

    void onRewardedVideoAdShowed(String paramString);

    void onRewardedVideoAdShowFailed(String paramString1, String paramString2);

    void onRewardedVideoAdClicked(String paramString);

    void onRewardedVideoAdClosed(String paramString);

    void onRewardedVideoAdStarted(String paramString);

    void onRewardedVideoAdEnded(String paramString);

    void onRewardedVideoAdRewarded(String paramString);
}
