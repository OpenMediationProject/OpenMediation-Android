// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.bean;

import android.text.TextUtils;

import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.JsonUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AdBean {

    private String mOriData;
    private String mTitle;
    private List<String> mMainimgUrls;
    private List<String> mLocalImgUrls;

    private AdVideoBean videoBean;

    // click through url
    private String mLink;
    // Open link with webview, 0:No,1:Yes
    private boolean isWebView;
    private List<String> mImptrackers;
    private List<String> mClktrackers;

    private AdAppBean appBean;

    private String mDescription;
    private List<String> mResources;
    private AdMark mMk;
    private long mExpire;
    private int mRatingCount;

    // The fill time stamp
    private long mFillTime;

    private List<String> mLocalRes;

    private double mRevenue;

    private int mRevenuePrecision;

    private AtomicInteger mSuccess = new AtomicInteger(0);
    private AtomicInteger mFailed = new AtomicInteger(0);

    public AdBean() {
    }

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

    public boolean isWebView() {
        return isWebView;
    }

    public void setWebView(boolean webView) {
        isWebView = webView;
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

    public void setExpire(long mExpire) {
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

    public synchronized void replaceOnlineResToLocal(String url, String path) {
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
            if (resList == null || resList.isEmpty()) {
                return;
            }
            if (resList.contains(url)) {
                List<String> localRes = getLocalResources();
                int size = resList.size();
                if (localRes == null || localRes.isEmpty()) {
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

    public static String toJsonString(AdBean adBean) {
        if (adBean == null) {
            return "";
        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("mOriData", adBean.mOriData);
            jsonObject.put("mTitle", adBean.mTitle);
            jsonObject.put("mMainimgUrls", getArray(adBean.mMainimgUrls));
            jsonObject.put("mLocalImgUrls", getArray(adBean.mLocalImgUrls));
            jsonObject.put("videoBean", AdVideoBean.toJSONObject(adBean.videoBean));
            jsonObject.put("mLink", adBean.mLink);
            jsonObject.put("isWebView", adBean.isWebView);
            jsonObject.put("mImptrackers", getArray(adBean.mImptrackers));
            jsonObject.put("mClktrackers", getArray(adBean.mClktrackers));
            jsonObject.put("appBean", AdAppBean.toJSONObject(adBean.appBean));
            jsonObject.put("mDescription", adBean.mDescription);
            jsonObject.put("mResources", getArray(adBean.mResources));
            jsonObject.put("mMk", AdMark.toJSONObject(adBean.mMk));
            jsonObject.put("mExpire", adBean.mExpire);
            jsonObject.put("mRatingCount", adBean.mRatingCount);
            jsonObject.put("mFillTime", adBean.mFillTime);
            jsonObject.put("mLocalRes", getArray(adBean.mLocalRes));
            jsonObject.put("mRevenue", adBean.mRevenue);
            jsonObject.put("mRevenuePrecision", adBean.mRevenuePrecision);
            return jsonObject.toString();
        } catch (Exception e) {
            DeveloperLog.LogE("AdBean convert JSONObject error: " + e.getMessage());
        }
        return "";
    }

    public static AdBean toAdBean(String jsonString) {
        if (TextUtils.isEmpty(jsonString)) {
            return null;
        }
        try {
            AdBean adBean = new AdBean();
            JSONObject jsonObject = new JSONObject(jsonString);
            adBean.mOriData = jsonObject.optString("mOriData");
            adBean.mTitle = jsonObject.optString("mTitle");
            adBean.mMainimgUrls = getList(jsonObject.optJSONArray("mMainimgUrls"));
            adBean.mLocalImgUrls = getList(jsonObject.optJSONArray("mLocalImgUrls"));
            adBean.mClktrackers = getList(jsonObject.optJSONArray("mClktrackers"));
            JSONObject videoObject = jsonObject.optJSONObject("videoBean");
            if (videoObject != null) {
                adBean.videoBean = new AdVideoBean(videoObject);
            }
            adBean.mLink = jsonObject.optString("mLink");
            adBean.isWebView = jsonObject.optBoolean("isWebView");
            adBean.mImptrackers = getList(jsonObject.optJSONArray("mImptrackers"));
            adBean.mClktrackers = getList(jsonObject.optJSONArray("mClktrackers"));
            JSONObject appObject = jsonObject.optJSONObject("appBean");
            if (appObject != null) {
                adBean.appBean = new AdAppBean(appObject);
            }
            adBean.mDescription = jsonObject.optString("mDescription");
            adBean.mResources = getList(jsonObject.optJSONArray("mResources"));
            JSONObject mkObject = jsonObject.optJSONObject("mMk");
            if (mkObject != null) {
                adBean.mMk = new AdMark(mkObject);
            }
            adBean.mExpire = jsonObject.optLong("mExpire");
            adBean.mRatingCount = jsonObject.optInt("mRatingCount");
            adBean.mFillTime = jsonObject.optLong("mFillTime");
            adBean.mLocalRes = getList(jsonObject.optJSONArray("mLocalRes"));
            adBean.mRevenue = jsonObject.optDouble("mRevenue");
            adBean.mRevenuePrecision = jsonObject.optInt("mRevenuePrecision");
            return adBean;
        } catch (Exception e) {
            DeveloperLog.LogE("JSONObject convert AdBean error: " + e.getMessage());
        }
        return null;
    }

    private static JSONArray getArray(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        JSONArray array = new JSONArray();
        for (String string : list) {
            array.put(string);
        }
        return array;
    }

    private static List<String> getList(JSONArray array) {
        if (array == null || array.length() == 0) {
            return null;
        }
        int length = array.length();
        List<String> list = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            list.add(array.optString(i));
        }
        return list;
    }
}
