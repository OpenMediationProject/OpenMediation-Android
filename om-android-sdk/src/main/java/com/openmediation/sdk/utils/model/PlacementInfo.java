// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.model;

import com.openmediation.sdk.utils.constant.CommonConstants;

/**
 * This class includes placement info with adType
 *
 * 
 */
public class PlacementInfo {
    private String mId;
    private int mWidth;
    private int mHeight;
    private int mAdType;

    public PlacementInfo(String id) {
        mId = id;
    }

    public String getId() {
        return mId;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }


    public int getAdType() {
        return mAdType;
    }

    public PlacementInfo getPlacementInfo(int adType) {
        mAdType = adType;
        switch (adType) {
            case CommonConstants.BANNER:
                mWidth = 640;
                mHeight = 100;
                break;
            case CommonConstants.NATIVE:
                mWidth = 1200;
                mHeight = 627;
                break;
            case CommonConstants.INTERSTITIAL:
                mWidth = 768;
                mHeight = 1024;
                break;
            case CommonConstants.VIDEO:
                break;
            default:
                break;
        }
        return this;
    }

    public PlacementInfo getBannerPlacementInfo(int width, int height) {
        mAdType = CommonConstants.BANNER;
        mWidth = width;
        mHeight = height;
        return this;
    }

    @Override
    public String toString() {
        return "PlacementInfo{" +
                "mId='" + mId + '\'' +
                ", mWidth=" + mWidth +
                ", mHeight=" + mHeight +
                ", mAdType=" + mAdType +
                '}';
    }
}
