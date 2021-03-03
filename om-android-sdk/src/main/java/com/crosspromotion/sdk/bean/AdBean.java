// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.openmediation.sdk.utils.JsonUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AdBean implements Parcelable {

    private String mOriData;
    private String mTitle;
    private List<String> mMainimgUrls;
    private List<String> mLocalImgUrls;

    private AdVideoBean videoBean;

    // click through url
    private String mLink;
    // Open link with webview, 0:No,1:Yes
    private boolean isWebview;
    private List<String> mImptrackers;
    private List<String> mClktrackers;

    private AdAppBean appBean;

    private String mDescription;
    private List<String> mResources;
    private AdMark mMk;
    private int mExpire;
    private int mRatingCount;

    // The fill time stamp
    private long mFillTime;

    private List<String> mLocalRes;

    private double mRevenue;

    private int mRevenuePrecision;

    private AtomicInteger mSuccess = new AtomicInteger(0);
    private AtomicInteger mFailed = new AtomicInteger(0);

    public void setOriData(String mOriData) {
        this.mOriData = mOriData;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public List<String> getMainimgUrl() {
        return mMainimgUrls;
    }

    public void setMainimgUrl(List<String> mMainimgUrls) {
        this.mMainimgUrls = mMainimgUrls;
    }

    public List<String> getLocalImgUrl() {
        return mLocalImgUrls;
    }

    public void setLocalImgUrl(List<String> localImgUrls) {
        mLocalImgUrls = localImgUrls;
    }

    public String getVideoUrl() {
        if (videoBean != null) {
            return videoBean.getUrl();
        }
        return "";
    }

    public String getAdUrl() {
        return mLink;
    }

    public void setLink(String mLink) {
        this.mLink = mLink;
    }

    public boolean isWebview() {
        return isWebview;
    }

    public void setWebview(boolean webview) {
        isWebview = webview;
    }

    public List<String> getImptrackers() {
        return mImptrackers;
    }

    public void setImptrackers(List<String> mImptrackers) {
        this.mImptrackers = mImptrackers;
    }

    public List<String> getClktrackers() {
        return mClktrackers;
    }

    public void setClktrackers(List<String> mClktrackers) {
        this.mClktrackers = mClktrackers;
    }

    public String getPkgName() {
        if (appBean != null) {
            return appBean.getId();
        }
        return "";
    }

    public String getIconUrl() {
        if (appBean != null) {
            return appBean.getIcon();
        }
        return "";
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public List<String> getResources() {
        return mResources;
    }

    public void setResources(List<String> mResources) {
        this.mResources = mResources;
    }

    public AdMark getAdMark() {
        return mMk;
    }

    public void setMk(AdMark mMk) {
        this.mMk = mMk;
    }

    public void setVideoBean(AdVideoBean videoBean) {
        this.videoBean = videoBean;
    }

    public void setAppBean(AdAppBean appBean) {
        this.appBean = appBean;
    }

    public void setExpire(int mExpire) {
        this.mExpire = mExpire;
    }

    public void setFillTime(long mFillTime) {
        this.mFillTime = mFillTime;
    }

    public List<String> getLocalResources() {
        return mLocalRes;
    }

    public void setLocalResources(List<String> mLocalRes) {
        this.mLocalRes = mLocalRes;
    }

    public boolean isExpired() {
        return mExpire > 0 && ((System.currentTimeMillis() - mFillTime) / 1000) > mExpire;
    }

    public void setIconUrl(String url) {
        if (appBean != null) {
            appBean.setReplaceIcon(url);
        }
    }

    public void setVideoUrl(String url) {
        if (videoBean != null) {
            videoBean.setReplaceUrl(url);
        }
    }

    public void setRevenue(double revenue) {
        this.mRevenue = revenue;
    }

    public double getRevenue() {
        return mRevenue;
    }

    public int getRevenuePrecision() {
        return mRevenuePrecision;
    }

    public void setRevenuePrecision(int revenuePrecision) {
        this.mRevenuePrecision = revenuePrecision;
    }

    public String getAdString() {
        try {
            JSONObject object = new JSONObject(mOriData);
            if (appBean != null) {
                JsonUtil.put(object, "app", appBean.getAdObject());
            }
            if (videoBean != null) {
                JsonUtil.put(object, "video", videoBean.getAdObject());
            }
            if (mLocalImgUrls != null) {
                JSONArray array = new JSONArray();
                for (String re : mLocalImgUrls) {
                    array.put(re);
                }
                JsonUtil.put(object, "imgs", array);
            }
            if (mLocalRes != null) {
                JSONArray array = new JSONArray();
                for (String re : mLocalRes) {
                    array.put(re);
                }
                JsonUtil.put(object, "resources", array);
            }
            return object.toString();
        } catch (Exception ignore) {
        }
        return mOriData;
    }

    public AtomicInteger getSuccess() {
        return mSuccess;
    }

    public AtomicInteger getFailed() {
        return mFailed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mOriData);
        dest.writeString(this.mTitle);
        dest.writeStringList(this.mMainimgUrls);
        dest.writeStringList(this.mLocalImgUrls);
        dest.writeParcelable(this.videoBean, flags);
        dest.writeString(this.mLink);
        dest.writeByte(this.isWebview ? (byte) 1 : (byte) 0);
        dest.writeStringList(this.mImptrackers);
        dest.writeStringList(this.mClktrackers);
        dest.writeParcelable(this.appBean, flags);
        dest.writeString(this.mDescription);
        dest.writeStringList(this.mResources);
        dest.writeParcelable(this.mMk, flags);
        dest.writeInt(this.mExpire);
        dest.writeInt(this.mRatingCount);
        dest.writeLong(this.mFillTime);
        dest.writeStringList(this.mLocalRes);
        dest.writeSerializable(this.mSuccess);
        dest.writeSerializable(this.mFailed);
        dest.writeDouble(this.mRevenue);
        dest.writeInt(this.mRevenuePrecision);
    }

    public AdBean() {
    }

    protected AdBean(Parcel in) {
        this.mOriData = in.readString();
        this.mTitle = in.readString();
        this.mMainimgUrls = in.createStringArrayList();
        this.mLocalImgUrls = in.createStringArrayList();
        this.videoBean = in.readParcelable(AdVideoBean.class.getClassLoader());
        this.mLink = in.readString();
        this.isWebview = in.readByte() != 0;
        this.mImptrackers = in.createStringArrayList();
        this.mClktrackers = in.createStringArrayList();
        this.appBean = in.readParcelable(AdAppBean.class.getClassLoader());
        this.mDescription = in.readString();
        this.mResources = in.createStringArrayList();
        this.mMk = in.readParcelable(AdMark.class.getClassLoader());
        this.mExpire = in.readInt();
        this.mRatingCount = in.readInt();
        this.mFillTime = in.readLong();
        this.mLocalRes = in.createStringArrayList();
        this.mSuccess = (AtomicInteger) in.readSerializable();
        this.mFailed = (AtomicInteger) in.readSerializable();
        this.mRevenue = in.readDouble();
        this.mRevenuePrecision = in.readInt();
    }

    public static final Creator<AdBean> CREATOR = new Creator<AdBean>() {
        @Override
        public AdBean createFromParcel(Parcel source) {
            return new AdBean(source);
        }

        @Override
        public AdBean[] newArray(int size) {
            return new AdBean[size];
        }
    };

    public void replaceOnlineResToLocal(String url, String path) {
        if (TextUtils.equals(url, getIconUrl())) {
            setIconUrl(path);
        } else if (getMainimgUrl() != null && getMainimgUrl().contains(url)) {
            List<String> imgUrls = getMainimgUrl();
            int size = imgUrls.size();
            List<String> localImgUrl = getLocalImgUrl();
            if (localImgUrl == null) {
                localImgUrl = new ArrayList<>(size);
            }
            for (int i = 0; i < size; i ++ ) {
                if (TextUtils.equals(url, imgUrls.get(i))) {
                    localImgUrl.add(i, path);
                    break;
                }
            }
            setLocalImgUrl(localImgUrl);
        } else if (TextUtils.equals(url, getVideoUrl())) {
            setVideoUrl(path);
        } else {
            List<String> resList = getResources();
            if (resList == null) {
                return;
            }
            if (resList.contains(url)) {
                List<String> localRes = getLocalResources();
                int size = resList.size();
                if (localRes == null) {
                    localRes = new ArrayList<>(size);
                }
                for (int i = 0; i < size; i ++ ) {
                    if (TextUtils.equals(url, resList.get(i))) {
                        localRes.add(i, path);
                        break;
                    }
                }
                setLocalResources(localRes);
            }
        }
    }
}
