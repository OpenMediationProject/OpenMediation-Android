// Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.cache;

import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.constant.KeyConstants;

import java.math.BigDecimal;

public class LifetimeRevenueData {

    public static void addRevenue(double revenue) {
        try {
            double lifetimeRevenue = getLifetimeRevenue();
            BigDecimal bigDecimal = new BigDecimal(revenue);
            double result = new BigDecimal(lifetimeRevenue).add(bigDecimal).setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue();
            DataCache.getInstance().set(getRevenueKey(), result);
        } catch (Exception e) {
            DeveloperLog.LogE("Error in LifetimeRevenueData.addRevenue() : revenue is " + revenue + ", Error: " + e.getMessage());
        }
    }

    /**
     * The accumulated value of the user ad revenue
     *
     * @return double
     */
    public static double getLifetimeRevenue() {
        String key = getRevenueKey();
        Double revenue = DataCache.getInstance().get(key, double.class);
        return (revenue == null || Double.isNaN(revenue)) ? 0d : revenue;
    }

    private static String getRevenueKey() {
        String appKey = DataCache.getInstance().getFromMem(KeyConstants.KEY_APP_KEY, String.class);
        return appKey + "_" + KeyConstants.KEY_REVENUE;
    }
}
