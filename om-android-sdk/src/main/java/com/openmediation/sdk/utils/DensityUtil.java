// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils;


import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * The type Density util.
 */
public class DensityUtil {
    private DensityUtil() {
    }

    public static int dip2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    /**
     *
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().densityDpi;
        return (int) ((pxValue * 160) / scale + 0.5f);
    }

    /**
     * Gets direction.
     *
     * @param context the context
     * @return the direction
     */
    public static int getDirection(Context context) {
        if (context == null) {
            return Configuration.ORIENTATION_UNDEFINED;
        }
        //
        Configuration mConfiguration = context.getResources().getConfiguration();
        //
        return mConfiguration.orientation;
    }

    /***
     * display's dimensions
     *
     * @param context the context
     * @return the display
     */
    public static DisplayMetrics getDisplay(Context context) {
        return context.getResources().getDisplayMetrics();
    }

    /**
     * Gets phone width.
     *
     * @param context the context
     * @return the phone width
     */
    public static int getPhoneWidth(Context context) {
        return getDisplay(context).widthPixels;
    }

    /**
     * Gets phone height.
     *
     * @param context the context
     * @return the phone height
     */
    public static int getPhoneHeight(Context context) {
        return getDisplay(context).heightPixels;
    }

    /**
     * Gets density dpi.
     *
     * @param context the context
     * @return the density dpi
     */
    public static int getDensityDpi(Context context) {
        return getDisplay(context).densityDpi;
    }

    /**
     * Gets dim size.
     *
     * @return the dim size
     */
    public static int getDimSize() {
        return Resources.getSystem().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
    }

    /**
     * Gets xdpi.
     *
     * @param context the context
     * @return the xdpi
     */
    public static int getXdpi(Context context) {
        return (int) getDisplay(context).xdpi;
    }

    /**
     * Gets ydpi.
     *
     * @param context the context
     * @return the ydpi
     */
    public static int getYdpi(Context context) {
        return (int) getDisplay(context).ydpi;
    }

    /**
     * Gets screen density.
     *
     * @return the screen density
     */
    public static int getScreenDensity() {
        int density = Resources.getSystem().getDisplayMetrics().densityDpi;
        int low = (DisplayMetrics.DENSITY_MEDIUM + DisplayMetrics.DENSITY_LOW) / 2;
        int high = (DisplayMetrics.DENSITY_MEDIUM + DisplayMetrics.DENSITY_HIGH) / 2;

        if (density == 0) {
            return 1;
        } else if (density < low) {
            return 0;
        } else if (density > high) {
            return 2;
        } else {
            return 1;
        }
    }

    /**
     * Gets screen size.
     *
     * @return the screen size
     */
    public static int getScreenSize() {
        int screenSize = Resources.getSystem().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return 1;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return 2;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return 3;
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                return 4;
            default:
                return 0;
        }
    }
}
