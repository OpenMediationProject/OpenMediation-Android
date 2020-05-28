package com.nbmediation.sdk.api.unity;

/**
 * Created by jiantao.tu on 2020/5/20.
 */
public interface InterstitialListener {

    void onInterstitialAdAvailabilityChanged(boolean paramBoolean);

    void onInterstitialAdShowed(String paramString);

    void onInterstitialAdShowFailed(String paramString1, String paramString2);

    void onInterstitialAdClosed(String paramString);

    void onInterstitialAdClicked(String paramString);
}
