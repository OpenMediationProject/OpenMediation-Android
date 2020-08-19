// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mediation;

import android.content.Context;

public class CustomAdParams {
    protected String mAdapterName = CustomAdParams.this.getClass().getSimpleName();
    protected Boolean mUserConsent = null;
    protected Boolean mAgeRestricted = null;
    protected Integer mUserAge = null;
    protected String mUserGender = null;
    protected Boolean mUSPrivacyLimit = null;

    public void setGDPRConsent(Context context, boolean consent) {
        mUserConsent = consent;
    }

    public void setAgeRestricted(Context context, boolean restricted) {
        mAgeRestricted = restricted;
    }

    public void setUserAge(Context context, int age) {
        mUserAge = age;
    }

    public void setUserGender(Context context, String gender) {
        mUserGender = gender;
    }

    public void setUSPrivacyLimit(Context context, boolean value) {
        mUSPrivacyLimit = value;
    }
}
