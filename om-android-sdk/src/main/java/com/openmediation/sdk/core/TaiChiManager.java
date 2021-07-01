/*
 * Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.core;

import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.FirebaseUtil;
import com.openmediation.sdk.utils.JsonUtil;
import com.openmediation.sdk.utils.WorkExecutor;
import com.openmediation.sdk.utils.cache.DataCache;
import com.openmediation.sdk.utils.cache.LifetimeRevenueData;
import com.openmediation.sdk.utils.constant.KeyConstants;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.event.EventUploadManager;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.Configurations;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TaiChiManager {

    public static void reportUserRevenue(final BaseInstance instance) {
        if (instance == null) {
            return;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    resetUarData();
                    // Add User Ad Revenue
                    LifetimeRevenueData.addUserRevenue(instance.getRevenue());

                    Configurations config = DataCache.getInstance().getFromMem(KeyConstants.KEY_CONFIGURATION, Configurations.class);
                    if (config == null || !config.uarReportEnabled()) {
                        return;
                    }
                    // report user ad revenue to firebase
                    FirebaseUtil.reportUserAdRevenue(instance);
                    int level = getTopXRevenueLevel(config);
                    if (level < 0) {
                        return;
                    }
                    int last = getLastTopXLevel();
                    if (last >= level) {
                        return;
                    }
                    for (int i = last + 1; i <= level; i++) {
                        uploadTopXData(instance, i);
                    }
                    setCurrTopXLevel(level);
                } catch(Throwable ignored) {
                }
            }
        };
        WorkExecutor.execute(runnable);
    }

    private static void resetUarData() {
        String dateKey = DataCache.getInstance().getFromMem(KeyConstants.KEY_APP_KEY, String.class)
                + "_" + KeyConstants.KEY_USER_REVENUE_DATE;
        String dateValue = DataCache.getInstance().get(dateKey, String.class);
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        String format = df.format(new Date());
        // not same day
        if (!format.equals(dateValue)) {
            LifetimeRevenueData.clearUarData();
            setCurrTopXLevel(0);
            DataCache.getInstance().set(dateKey, format);
        }
    }

    private static void setCurrTopXLevel(int level) {
        String appKey = DataCache.getInstance().getFromMem(KeyConstants.KEY_APP_KEY, String.class);
        DataCache.getInstance().set(appKey + "_" + KeyConstants.KEY_TOPX_LEVEL, level);
    }

    /**
     * return Last Reported Level
     *
     * @return Integer
     */
    private static int getLastTopXLevel() {
        String appKey = DataCache.getInstance().getFromMem(KeyConstants.KEY_APP_KEY, String.class);
        Integer integer = DataCache.getInstance().get(appKey + "_" + KeyConstants.KEY_TOPX_LEVEL, int.class);
        return integer == null ? 0 : integer;
    }

    private static void uploadTopXData(BaseInstance instance, int level) {
        try {
            FirebaseUtil.reportTopXEvent(level);
            JSONObject eventJson = InsManager.buildReportData(instance);
            JsonUtil.put(eventJson, "price", LifetimeRevenueData.getUserRevenue());
            EventUploadManager.getInstance().uploadEvent(getUarEventId(level), eventJson);
        } catch(Throwable ignored) {
        }
    }

    private static int getTopXRevenueLevel(Configurations config) {
        double revenue = LifetimeRevenueData.getUserRevenue();
        DeveloperLog.LogD("TaiChiManager : User Ad Revenue : " + revenue);
        List<Double> uarX = config.getTopXRevenue();
        int size = uarX.size();
        for (int i = 0; i < size; i++) {
            if (revenue >= uarX.get(i)) {
                return size - i;
            }
        }
        return -1;
    }

    private static int getUarEventId(int level) {
        switch (level) {
            case 1:
                return EventId.REPORT_UAR_TOP50;
            case 2:
                return EventId.REPORT_UAR_TOP40;
            case 3:
                return EventId.REPORT_UAR_TOP30;
            case 4:
                return EventId.REPORT_UAR_TOP20;
            case 5:
                return EventId.REPORT_UAR_TOP10;
            default:
                return -1;
        }
    }
}
