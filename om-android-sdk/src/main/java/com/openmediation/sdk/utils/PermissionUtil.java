// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

public final class PermissionUtil {
    /**
     * Returns whether the user has granted the permissions.
     *
     * @param context     the context
     * @param permissions The permissions.
     * @return the boolean
     */
    public static boolean isGranted(Context context, final String... permissions) {
        for (String permission : permissions) {
            if (!isGranted(context, permission)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isGranted(Context context, final String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || PackageManager.PERMISSION_GRANTED
                == context.checkSelfPermission(permission);
    }
}
