// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.utils;

import android.text.TextUtils;

import com.crosspromotion.sdk.bean.AdAppBean;
import com.crosspromotion.sdk.bean.AdBean;
import com.crosspromotion.sdk.bean.AdMark;
import com.crosspromotion.sdk.bean.AdVideoBean;
import com.openmediation.sdk.utils.InsUtil;
import com.openmediation.sdk.utils.PlacementUtils;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.Placement;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 *
 */
public final class ResponseUtil {

    public static List<AdBean> transformResponse(JSONArray array) {
        if (array == null || array.length() == 0) {
            return null;
        }
        LinkedList<AdBean> adBeans = new LinkedList<>();
        int len = array.length();
        for (int i = 0; i < len; i++) {
            JSONObject adBean = array.optJSONObject(i);
            adBeans.add(jsonToAd(adBean));
        }
        return adBeans;
    }

    private static AdBean jsonToAd(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        AdBean adBean = new AdBean();
        adBean.setOriData(jsonObject.toString());
        adBean.setTitle(jsonObject.optString("title"));
        JSONArray imgArray = jsonObject.optJSONArray("imgs");
        if (imgArray != null && imgArray.length() > 0) {
            List<String> imgs = new ArrayList<>();
            int len = imgArray.length();
            for (int i = 0; i < len; i++) {
                String imgUrl = imgArray.optString(i);
                if (!TextUtils.isEmpty(imgUrl)) {
                    imgs.add(imgUrl);
                }
            }
            adBean.setMainimgUrl(imgs);
        }

        JSONObject videoObject = jsonObject.optJSONObject("video");
        if (videoObject != null) {
            AdVideoBean videoBean = new AdVideoBean(videoObject);
            adBean.setVideoBean(videoBean);
        }
        adBean.setLink(jsonObject.optString("link"));
        adBean.setWebView(jsonObject.optInt("iswv") == 1);
        JSONArray clktrackerArray = jsonObject.optJSONArray("clktks");
        if (clktrackerArray != null && clktrackerArray.length() > 0) {
            List<String> clktrackers = new ArrayList<>();
            int clkLen = clktrackerArray.length();
            for (int b = 0; b < clkLen; b++) {
                String clktracker = clktrackerArray.optString(b);
                if (!TextUtils.isEmpty(clktracker)) {
                    clktrackers.add(clktracker);
                }
            }
            adBean.setClktrackers(clktrackers);
        }
        JSONArray imptrackerArray = jsonObject.optJSONArray("imptks");
        if (imptrackerArray != null && imptrackerArray.length() > 0) {
            List<String> imptrackers = new ArrayList<>();
            int impLen = imptrackerArray.length();
            for (int b = 0; b < impLen; b++) {
                String imptracker = imptrackerArray.optString(b);
                if (!TextUtils.isEmpty(imptracker)) {
                    imptrackers.add(imptracker);
                }
            }
            adBean.setImptrackers(imptrackers);
        }
        JSONObject appObject = jsonObject.optJSONObject("app");
        if (appObject != null) {
            AdAppBean appBean = new AdAppBean(appObject);
            adBean.setAppBean(appBean);
        }
        adBean.setDescription(jsonObject.optString("descn"));
        JSONArray resArray = jsonObject.optJSONArray("resources");
        if (resArray != null && resArray.length() > 0) {
            List<String> resources = new ArrayList<>();
            int resLen = resArray.length();
            for (int b = 0; b < resLen; b++) {
                String res = resArray.optString(b);
                if (!TextUtils.isEmpty(res)) {
                    resources.add(res);
                }
            }
            adBean.setResources(resources);
        }
        JSONObject mkObject = jsonObject.optJSONObject("mk");
        if (mkObject != null) {
            AdMark mk = new AdMark(mkObject);
            adBean.setMk(mk);
        }
        adBean.setExpire(jsonObject.optLong("expire"));

        adBean.setRevenue(jsonObject.optDouble("r", 0d));
        adBean.setRevenuePrecision(jsonObject.optInt("rp", -1));
        return adBean;
    }

    /**
     * set InstanceRevenue
     * @param placementId placementId
     * @param extras extras
     * @param adBean adBean
     */
    public static void setInstanceRevenue(String placementId, Map extras, AdBean adBean) {
        if (extras == null || extras.get("InstanceId") == null) {
            return;
        }
        // for promotion ad
        Placement placement = PlacementUtils.getPlacement(placementId);
        if (placement == null || placement.getT() != CommonConstants.PROMOTION) {
            return;
        }
        String instanceId = String.valueOf(extras.get("InstanceId"));
        BaseInstance instance = InsUtil.getInsById(placement, instanceId);
        if (instance != null) {
            instance.setRevenue(adBean.getRevenue());
            instance.setRevenuePrecision(adBean.getRevenuePrecision());
        }
    }

}
