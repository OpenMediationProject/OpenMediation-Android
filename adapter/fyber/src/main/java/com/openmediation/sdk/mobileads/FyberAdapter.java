/*
 * Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.fyber.inneractive.sdk.external.InneractiveAdManager;
import com.fyber.inneractive.sdk.external.InneractiveAdRequest;
import com.fyber.inneractive.sdk.external.InneractiveUserConfig;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.MediationUtil;

import java.util.Map;

public class FyberAdapter extends CustomAdsAdapter {

    @Override
    public String getMediationVersion() {
        return InneractiveAdManager.getVersion();
    }

    @Override
    public String getAdapterVersion() {
        return com.openmediation.sdk.mobileads.fyber.BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_30;
    }

    @Override
    public void setGDPRConsent(Context context, boolean consent) {
        super.setGDPRConsent(context, consent);
        InneractiveAdManager.setGdprConsent(consent);
    }

    @Override
    public void setUSPrivacyLimit(Context context, boolean value) {
        super.setUSPrivacyLimit(context, value);
        String ccpaStringVal = value ? "1YY-" : "1YN-";
        InneractiveAdManager.setUSPrivacyString(ccpaStringVal);
    }

    @Override
    public boolean isAdNetworkInit() {
        return InneractiveAdManager.wasInitialized();
    }

    @Override
    public void initBannerAd(Activity activity, Map<String, Object> extras, BannerAdCallback callback) {
        super.initBannerAd(activity, extras, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
            }
            return;
        }
        initSDK();
        if (callback != null) {
            callback.onBannerAdInitSuccess();
        }
    }

    @Override
    public void loadBannerAd(Activity activity, String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        super.loadBannerAd(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
            }
            return;
        }
        FyberBannerManager.getInstance().loadAd(MediationUtil.getContext(), adUnitId, extras, creteRequest(adUnitId), callback);
    }

    @Override
    public boolean isBannerAdAvailable(String adUnitId) {
        return FyberBannerManager.getInstance().isAdAvailable(adUnitId);
    }

    @Override
    public void destroyBannerAd(String adUnitId) {
        super.destroyBannerAd(adUnitId);
        FyberBannerManager.getInstance().destroyAd(adUnitId);
    }

    private void initSDK() {
        if (!InneractiveAdManager.wasInitialized()) {
            String appKey = mAppKey;
            InneractiveAdManager.initialize(MediationUtil.getContext(), appKey);
            if (mUserConsent != null) {
                setGDPRConsent(MediationUtil.getContext(), mUserConsent);
            }
            if (mUSPrivacyLimit != null) {
                setUSPrivacyLimit(MediationUtil.getContext(), mUSPrivacyLimit);
            }
        }
    }

    private InneractiveAdRequest creteRequest(String adUnitId) {
        InneractiveAdRequest request = new InneractiveAdRequest(adUnitId);
        if (mUserGender != null || mUserAge != null) {
            InneractiveUserConfig userConfig = new InneractiveUserConfig();
            if ("male".equals(mUserGender)) {
                userConfig.setGender(InneractiveUserConfig.Gender.MALE);
            } else if ("female".equals(mUserGender)) {
                userConfig.setGender(InneractiveUserConfig.Gender.FEMALE);
            }
            if (mUserAge != null) {
                userConfig.setAge(mUserAge);
            }
            request.setUserParams(userConfig);
        }
        return request;
    }
}
