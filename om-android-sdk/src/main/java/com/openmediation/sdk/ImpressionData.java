// Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk;

import android.text.TextUtils;

import com.openmediation.sdk.utils.AdLog;
import com.openmediation.sdk.utils.InsUtil;
import com.openmediation.sdk.utils.PlacementUtils;
import com.openmediation.sdk.utils.cache.LifetimeRevenueData;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.MediationRule;
import com.openmediation.sdk.utils.model.Placement;
import com.openmediation.sdk.utils.model.Scene;

import org.json.JSONObject;

/**
 * Impression level revenue data
 */
public class ImpressionData {

    private final static String KEY_IMPRESSION_ID = "impression_id";
    private final static String KEY_INSTANCE_ID = "instance_id";
    private final static String KEY_INSTANCE_NAME = "instance_name";
    private final static String KEY_INSTANCE_PRIORITY = "instance_priority";
    private final static String KEY_AD_NETWORK_ID = "ad_network_id";
    private final static String KEY_AD_NETWORK_NAME = "ad_network_name";
    private final static String KEY_AD_NETWORK_UNIT_ID = "ad_network_unit_id";
    private final static String KEY_MEDIATION_RULE_ID = "mediation_rule_id";
    private final static String KEY_MEDIATION_RULE_NAME = "mediation_rule_name";
    private final static String KEY_MEDIATION_RULE_TYPE = "mediation_rule_type";
    private final static String KEY_MEDIATION_RULE_PRIORITY = "mediation_rule_priority";
    private final static String KEY_PLACEMENT_ID = "placement_id";
    private final static String KEY_PLACEMENT_NAME = "placement_name";
    private final static String KEY_PLACEMENT_AD_TYPE = "placement_ad_type";
    private final static String KEY_SCENE_NAME = "scene_name";
    private final static String KEY_CURRENCY = "currency";
    private final static String KEY_REVENUE = "revenue";
    private final static String KEY_PRECISION = "precision";
    private final static String KEY_AB_GROUP = "ab_group";
    private final static String KEY_LIFETIME_VALUE = "lifetime_value";

    private final JSONObject mJsonObject;

    private ImpressionData(BaseInstance instance, Scene scene) {
        mJsonObject = new JSONObject();
        try {
            String impressionId = instance.getReqId() + "_" + instance.getId();
            mJsonObject.putOpt(KEY_IMPRESSION_ID, impressionId);
            mJsonObject.putOpt(KEY_INSTANCE_ID, String.valueOf(instance.getId()));
            mJsonObject.putOpt(KEY_INSTANCE_NAME, instance.getName());
            mJsonObject.putOpt(KEY_INSTANCE_PRIORITY, instance.getPriority());
            mJsonObject.putOpt(KEY_AD_NETWORK_ID, String.valueOf(instance.getMediationId()));
            mJsonObject.putOpt(KEY_AD_NETWORK_NAME, InsUtil.getNetworkName(instance));
            mJsonObject.putOpt(KEY_AD_NETWORK_UNIT_ID, instance.getKey());
            MediationRule rule = instance.getMediationRule();
            if (rule != null) {
                mJsonObject.putOpt(KEY_MEDIATION_RULE_ID, String.valueOf(rule.getId()));
                mJsonObject.putOpt(KEY_MEDIATION_RULE_NAME, rule.getName());
                String ruleType = getRuleType(rule.getType());
                mJsonObject.putOpt(KEY_MEDIATION_RULE_TYPE, TextUtils.isEmpty(ruleType) ? JSONObject.NULL : ruleType);
                mJsonObject.putOpt(KEY_MEDIATION_RULE_PRIORITY, rule.getPriority());
            }
            Placement placement = PlacementUtils.getPlacement(instance.getPlacementId());
            if (placement != null) {
                mJsonObject.putOpt(KEY_PLACEMENT_ID, placement.getId());
                mJsonObject.putOpt(KEY_PLACEMENT_NAME, placement.getName());
                String adType = getAdType(placement.getT());
                mJsonObject.putOpt(KEY_PLACEMENT_AD_TYPE, TextUtils.isEmpty(adType) ? JSONObject.NULL : adType);
            }
            if (scene != null && !TextUtils.isEmpty(scene.getN())) {
                mJsonObject.putOpt(KEY_SCENE_NAME, scene.getN());
            } else {
                mJsonObject.putOpt(KEY_SCENE_NAME, JSONObject.NULL);
            }
            mJsonObject.putOpt(KEY_CURRENCY, "USD");
            mJsonObject.putOpt(KEY_REVENUE, instance.getShowRevenue());
            String precision = getPrecision(instance.getRevenuePrecision());
            mJsonObject.putOpt(KEY_PRECISION, TextUtils.isEmpty(precision) ? JSONObject.NULL : precision);
            String abTest = getAbTest(instance.getWfAbt());
            mJsonObject.putOpt(KEY_AB_GROUP, TextUtils.isEmpty(abTest) ? JSONObject.NULL : abTest);
            mJsonObject.putOpt(KEY_LIFETIME_VALUE, LifetimeRevenueData.getLifetimeRevenue());
        } catch (Exception e) {
            AdLog.getSingleton().LogE("ImpressionData", "Data conversion failed");
        }
    }

    public static ImpressionData create(BaseInstance instance, Scene scene) {
        if (instance == null) {
            return null;
        }
        return new ImpressionData(instance, scene);
    }

    public String getImpressionId() {
        return mJsonObject.optString(KEY_IMPRESSION_ID, null);
    }

    public String getInstanceId() {
        return mJsonObject.optString(KEY_INSTANCE_ID, null);
    }

    public String getInstanceName() {
        return mJsonObject.optString(KEY_INSTANCE_NAME, null);
    }

    public int getInstancePriority() {
        return mJsonObject.optInt(KEY_INSTANCE_PRIORITY);
    }

    public String getAdNetworkId() {
        return mJsonObject.optString(KEY_AD_NETWORK_ID, null);
    }

    public String getAdNetworkName() {
        return mJsonObject.optString(KEY_AD_NETWORK_NAME, null);
    }

    public String getAdNetworkUnitId() {
        return mJsonObject.optString(KEY_AD_NETWORK_UNIT_ID, null);
    }

    public String getMediationRuleId() {
        return mJsonObject.optString(KEY_MEDIATION_RULE_ID, null);
    }

    public String getMediationRuleName() {
        return mJsonObject.optString(KEY_MEDIATION_RULE_NAME, null);
    }

    public String getMediationRuleType() {
        return mJsonObject.optString(KEY_MEDIATION_RULE_TYPE, null);
    }

    public int getMediationRulePriority() {
        return mJsonObject.optInt(KEY_MEDIATION_RULE_PRIORITY);
    }

    public String getPlacementId() {
        return mJsonObject.optString(KEY_PLACEMENT_ID, null);
    }

    public String getPlacementName() {
        return mJsonObject.optString(KEY_PLACEMENT_NAME, null);
    }

    public String getPlacementAdType() {
        return mJsonObject.optString(KEY_PLACEMENT_AD_TYPE, null);
    }

    public String getSceneName() {
        return mJsonObject.optString(KEY_SCENE_NAME, null);
    }

    public String getCurrency() {
        return mJsonObject.optString(KEY_CURRENCY, null);
    }

    public double getRevenue() {
        return mJsonObject.optDouble(KEY_REVENUE);
    }

    public String getPrecision() {
        return mJsonObject.optString(KEY_PRECISION, null);
    }

    public String getAbGroup() {
        return mJsonObject.optString(KEY_AB_GROUP, null);
    }

    public double getLifetimeValue() {
        return mJsonObject.optDouble(KEY_LIFETIME_VALUE);
    }

    public JSONObject getAllData() {
        return mJsonObject;
    }

    @Override
    public String toString() {
        if (mJsonObject != null) {
            return mJsonObject.toString();
        }
        return super.toString();
    }

    private static String getAdType(int type) {
        switch (type) {
            case CommonConstants.BANNER:
                return "Banner";
            case CommonConstants.NATIVE:
                return "Native";
            case CommonConstants.VIDEO:
                return "Rewarded Video";
            case CommonConstants.INTERSTITIAL:
                return "Interstitial";
            case CommonConstants.SPLASH:
                return "Splash";
            case CommonConstants.PROMOTION:
                return "Cross Promote";
            default:
                return null;
        }
    }

    private static String getPrecision(int type) {
        switch (type) {
            case 0:
                return "undisclosed";
            case 1:
                return "exact";
            case 2:
                return "estimated";
            case 3:
                return "defined";
            default:
                return null;
        }
    }

    private static String getRuleType(int type) {
        switch (type) {
            case 0:
                return "Auto";
            case 1:
                return "Manual";
            default:
                return null;
        }
    }

    private static String getAbTest(int ab) {
        switch (ab) {
            case 1:
                return "A";
            case 2:
                return "B";
            default:
                return null;
        }
    }
}