// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mediation;

import android.content.Context;

import com.openmediation.sdk.core.OmManager;

public class CustomAdParams {
    protected String mAdapterName = CustomAdParams.this.getClass().getSimpleName();
    protected Boolean mUserConsent = null;
    protected Boolean mAgeRestricted = null;
    protected Integer mUserAge = null;
    protected String mUserGender = null;
    protected Boolean mUSPrivacyLimit = null;

    public void setCustomParams(Context context) {
        mUserConsent = OmManager.getInstance().getGDPRConsent();
        mAgeRestricted = OmManager.getInstance().getAgeRestricted();
        mUserAge = OmManager.getInstance().getUserAge();
        mUserGender = OmManager.getInstance().getUserGender();
        mUSPrivacyLimit = OmManager.getInstance().getUSPrivacyLimit();

        if (mUserConsent != null) {
            setGDPRConsent(context, mUserConsent);
        }
        if (mAgeRestricted != null) {
            setAgeRestricted(context, mAgeRestricted);
        }
        if (mUserAge != null) {
            setUserAge(context, mUserAge);
        }
        if (mUserGender != null) {
            setUserGender(context, mUserGender);
        }
        if (mUSPrivacyLimit != null) {
            setUSPrivacyLimit(context, mUSPrivacyLimit);
        }
    }

    public void setGDPRConsent(Context context, boolean consent) {
    }

    public void setAgeRestricted(Context context, boolean restricted) {
    }

    public void setUserAge(Context context, int age) {
    }

    public void setUserGender(Context context, String gender) {
    }

    public void setUSPrivacyLimit(Context context, boolean value) {
    }
}
