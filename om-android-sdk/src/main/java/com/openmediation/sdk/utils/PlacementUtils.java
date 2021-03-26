// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils;

import android.net.Uri;
import android.text.TextUtils;

import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.utils.cache.DataCache;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.constant.KeyConstants;
import com.openmediation.sdk.utils.crash.CrashUtil;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.Configurations;
import com.openmediation.sdk.utils.model.ImpRecord;
import com.openmediation.sdk.utils.model.Placement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The type Placement utils.
 */
public class PlacementUtils {

    /**
     * Gets placement info.
     *
     * @param reqId the auction id
     * @param placementId the placement id
     * @param instances   the instances
     * @param payload     the payload
     * @return the placement info
     */
    public static Map<String, String> getPlacementInfo(String reqId, String placementId, BaseInstance instances, String payload) {
        Configurations config = DataCache.getInstance().getFromMem(KeyConstants.KEY_CONFIGURATION, Configurations.class);
        Map<String, String> maps = new HashMap<>();
        maps.put("AppKey", config.getMs().get(instances.getMediationId()).getK());
        maps.put("PlacementId", placementId);
        maps.put("InstanceKey", instances.getKey());
        maps.put("InstanceId", String.valueOf(instances.getId()));
        if (!TextUtils.isEmpty(payload)) {
            maps.put("pay_load", payload);
        }
        maps.put("AuctionId", reqId);
        return maps;
    }

    public static Map<String, Object> getLoadExtrasMap(String reqId, BaseInstance instance, BidResponse bidResponse) {
        Map<String, Object> extras = new HashMap<>();
        if (bidResponse != null && !TextUtils.isEmpty(bidResponse.getPayLoad())) {
            extras.put("pay_load", bidResponse.getPayLoad());
        }
        if (instance != null) {
            extras.put("InstanceId", String.valueOf(instance.getId()));
        }
        extras.put("AuctionId", reqId);
        return extras;
    }

    /**
     * Gets 1st Placement of the adType in config
     *
     * @param adType the ad type
     * @return placement
     */
    public static Placement getPlacement(int adType) {
        Configurations config = DataCache.getInstance().getFromMem(KeyConstants.KEY_CONFIGURATION, Configurations.class);
        if (config == null) {
            return null;
        }

        Set<String> keys = config.getPls().keySet();
        List<Placement> placements = new ArrayList<>();
        for (String key : keys) {
            Placement placement = config.getPls().get(key);
            if (placement == null || placement.getT() != adType) {
                continue;
            }
            placements.add(placement);
        }
        for (Placement placement : placements) {
            if (placement.getMain() == 1) {
                return placement;
            }
        }
        return placements.isEmpty() ? null : placements.get(0);
    }

    /**
     * Gets the 1st Placement if PlacementId is null
     *
     * @param placementId the placement id
     * @return placement
     */
    public static Placement getPlacement(String placementId) {
        Configurations config = DataCache.getInstance().getFromMem(KeyConstants.KEY_CONFIGURATION, Configurations.class);
        if (config == null) {
            return null;
        }
        return config.getPls().get(placementId);
    }


    /**
     * Placement event params json object.
     *
     * @param placementId the placement id
     * @return the json object
     */
    public static JSONObject placementEventParams(String placementId) {
        JSONObject jsonObject = new JSONObject();
        JsonUtil.put(jsonObject, "pid", placementId);
        return jsonObject;
    }

    protected static ImpRecord parseFromJson(String s) {
        if (TextUtils.isEmpty(s)) {
            return null;
        }
        DeveloperLog.LogD("imp string : " + s);
        ImpRecord impRecord = new ImpRecord();
        try {
            DeveloperLog.LogD("PlacementUtils imp string : " + Uri.decode(s));
            JSONObject object = new JSONObject(Uri.decode(s));
            Map<String, Map<String, ImpRecord.Imp>> impMap = new HashMap<>();
            Iterator<String> keys = object.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Map<String, ImpRecord.Imp> imps = jsonToImps(object.optJSONArray(key));
                if (imps != null && !imps.isEmpty()) {
                    impMap.put(key, imps);
                }
            }
            impRecord.setImpMap(impMap);
            return impRecord;
        } catch (JSONException e) {
            DeveloperLog.LogD("PlacementUtils", e);
            CrashUtil.getSingleton().saveException(e);
            return null;
        }
    }

    private static Map<String, ImpRecord.Imp> jsonToImps(JSONArray array) {
        if (array == null || array.length() == 0) {
            return null;
        }
        Map<String, ImpRecord.Imp> map = new HashMap<>();
        int len = array.length();
        for (int i = 0; i < len; i++) {
            JSONObject object = array.optJSONObject(i);
            ImpRecord.Imp imp = new ImpRecord.Imp();
            imp.setLashImpTime(object.optLong("last_imp_time"));
            imp.setImpCount(object.optInt("imp_count"));
            imp.setTime(object.optString("time"));
            String pkgName = object.optString("pkg_name");
            imp.setPkgName(pkgName);
            imp.setPlacmentId(object.optString("placement_id"));
            map.put(pkgName, imp);
        }
        return map;
    }

    private static List<ImpRecord.DayImp> jsonToDayImps(JSONArray array) {
        if (array == null || array.length() == 0) {
            return null;
        }
        List<ImpRecord.DayImp> dayImps = new ArrayList<>();
        int len = array.length();
        for (int i = 0; i < len; i++) {
            JSONObject object = array.optJSONObject(i);
            ImpRecord.DayImp dayImp = new ImpRecord.DayImp();
            dayImp.setImpCount(object.optInt("imp_count"));
            dayImp.setTime(object.optString("time"));
            dayImps.add(dayImp);
        }
        return dayImps;
    }

    protected static String transformToString(ImpRecord impRecord) {
        try {
            JSONObject object = new JSONObject();
            Map<String, Map<String, ImpRecord.Imp>> impMap = impRecord.getImpMap();
            Set<String> keys = impMap.keySet();
            for (String key : keys) {
                Map<String, ImpRecord.Imp> map = impMap.get(key);
                if (map == null || map.isEmpty()) {
                    continue;
                }
                JSONArray array = new JSONArray();
                for (Map.Entry<String, ImpRecord.Imp> imp : map.entrySet()) {
                    if (imp.getValue() == null) {
                        continue;
                    }
                    JSONObject o = new JSONObject();
                    o.put("imp_count", imp.getValue().getImpCount());
                    o.put("last_imp_time", imp.getValue().getLashImpTime());
                    o.put("pkg_name", imp.getValue().getPkgName());
                    o.put("placement_id", imp.getValue().getPlacmentId());
                    o.put("time", imp.getValue().getTime());

                    array.put(o);
                }
                object.put(key, array);
            }
            return object.toString();
        } catch (Exception e) {
            DeveloperLog.LogD("PlacementUtils", e);
            CrashUtil.getSingleton().saveException(e);
            return null;
        }
    }

    /**
     * Save placement impr count.
     *
     * @param placementId the placement id
     */
    public static void savePlacementImprCount(String placementId) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
            Date date = new Date();

            String today = dateFormat.format(date);

            ImpRecord impRecord = parseDayImpFromJson(DataCache.getInstance().get("DayImpRecord", String.class));
            if (impRecord == null) {
                impRecord = new ImpRecord();
            }

            Map<String, List<ImpRecord.DayImp>> impsMap = impRecord.getDayImp();
            if (impsMap == null) {
                impsMap = new HashMap<>();
            }

            String tpmKey = placementId.trim().concat("day_impr");

            List<ImpRecord.DayImp> imps = impsMap.get(tpmKey);

            if (imps != null && !imps.isEmpty()) {
                ImpRecord.DayImp imp = imps.get(0);
                if (imp == null) {
                    imp = new ImpRecord.Imp();
                    imp.setTime(today);
                    imp.setImpCount(1);
                } else {
                    if (today.equals(imp.getTime())) {
                        imp.setImpCount(imp.getImpCount() + 1);
                    } else {
                        imp.setTime(today);
                        imp.setImpCount(1);
                    }
                    imps.clear();
                }
                imps.add(imp);
            } else {
                imps = new ArrayList<>();
                ImpRecord.Imp imp = new ImpRecord.Imp();
                imp.setTime(today);
                imp.setImpCount(1);
                imps.add(imp);
            }

            impsMap.put(tpmKey, imps);
            impRecord.setDayImp(impsMap);

            DataCache.getInstance().set("DayImpRecord", Uri.encode(transDayImpToString(impRecord)));
        } catch (Throwable e) {
            DeveloperLog.LogD("PlacementUtils", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }

    /**
     * Gets placement impr count.
     *
     * @param placementId the placement id
     * @return the placement impr count
     */
    public static int getPlacementImprCount(String placementId) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
            Date date = new Date();

            String today = dateFormat.format(date);

            ImpRecord impRecord = parseDayImpFromJson(DataCache.getInstance().get("DayImpRecord", String.class));
            if (impRecord == null) {
                return 0;
            }

            Map<String, List<ImpRecord.DayImp>> impsMap = impRecord.getDayImp();
            if (impsMap == null) {
                return 0;
            }

            String tpmKey = placementId.trim().concat("day_impr");

            List<ImpRecord.DayImp> imps = impsMap.get(tpmKey);

            if (imps != null && !imps.isEmpty()) {
                ImpRecord.DayImp imp = imps.get(0);
                if (imp != null && imp.getTime().equals(today)) {
                    return imp.getImpCount();
                }
            }

            return 0;
        } catch (Throwable e) {
            DeveloperLog.LogD("PlacementUtils", e);
            CrashUtil.getSingleton().saveException(e);
        }
        return 0;
    }

    private static ImpRecord parseDayImpFromJson(String s) {
        if (TextUtils.isEmpty(s)) {
            return null;
        }
        ImpRecord impRecord = new ImpRecord();
        try {
            JSONObject object = new JSONObject(Uri.decode(s));
            Map<String, List<ImpRecord.DayImp>> dayImpMap = new HashMap<>();
            Iterator<String> keys = object.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                List<ImpRecord.DayImp> imps = jsonToDayImps(object.optJSONArray(key));
                if (imps != null && !imps.isEmpty()) {
                    dayImpMap.put(key, imps);
                }
            }
            impRecord.setDayImp(dayImpMap);
            return impRecord;
        } catch (JSONException e) {
            DeveloperLog.LogD("PlacementUtils", e);
            CrashUtil.getSingleton().saveException(e);
            return null;
        }
    }

    private static String transDayImpToString(ImpRecord impRecord) {
        try {
            JSONObject object = new JSONObject();
            Map<String, List<ImpRecord.DayImp>> impMap = impRecord.getDayImp();
            Set<String> keys = impMap.keySet();
            for (String key : keys) {
                List<ImpRecord.DayImp> dayImps = impMap.get(key);
                if (dayImps == null || dayImps.isEmpty()) {
                    continue;
                }
                JSONArray array = new JSONArray();
                for (ImpRecord.DayImp imp : dayImps) {
                    if (imp == null) {
                        continue;
                    }
                    JSONObject o = new JSONObject();
                    o.put("imp_count", imp.getImpCount());
                    o.put("time", imp.getTime());

                    array.put(o);
                }
                object.put(key, array);
            }
            return object.toString();
        } catch (Exception e) {
            DeveloperLog.LogD("PlacementUtils", e);
            CrashUtil.getSingleton().saveException(e);
            return null;
        }
    }

    public static boolean isCacheAdsType(int adType) {
        switch (adType) {
            case CommonConstants.BANNER:
            case CommonConstants.NATIVE:
                return false;
            default:
                return true;
        }
    }
}
