// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core;

public class MetaData {
    private Boolean mUserConsent = null;
    private Boolean mAgeRestricted = null;
    private Integer mUserAge = null;
    private String mUserGender = null;
    private Boolean mUSPrivacyLimit = null;

    public Boolean getGDPRConsent() {
        return mUserConsent;
    }

    public void setGDPRConsent(Boolean consent) {
        this.mUserConsent = consent;
    }

    public Boolean getAgeRestricted() {
        return mAgeRestricted;
    }

    public void setAgeRestricted(Boolean ageRestricted) {
        this.mAgeRestricted = ageRestricted;
    }

    public Integer getUserAge() {
        return mUserAge;
    }

    public void setUserAge(Integer userAge) {
        this.mUserAge = userAge;
    }

    public String getUserGender() {
        return mUserGender;
    }

    public void setUserGender(String userGender) {
        this.mUserGender = userGender;
    }

    public Boolean getUSPrivacyLimit() {
        return mUSPrivacyLimit;
    }

    public void setUSPrivacyLimit(Boolean value) {
        this.mUSPrivacyLimit = value;
    }

    @Override
    public String toString() {
        return "MetaData{" +
                "mUserConsent=" + mUserConsent +
                ", mAgeRestricted=" + mAgeRestricted +
                ", mUserAge=" + mUserAge +
                ", mUserGender=" + mUserGender  +
                ", mUSPrivacyLimit=" + mUSPrivacyLimit +
                '}';
    }
}
