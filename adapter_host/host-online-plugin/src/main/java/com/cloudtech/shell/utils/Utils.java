package com.cloudtech.shell.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Process;
import android.provider.Settings;
import android.support.annotation.NonNull;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Created by jiantao.tu on 2018/4/19.
 */
public class Utils {
    /**
     * 获取Android Id
     */
    public static String getAndroidId(Context context) {
        String androidId = "";
        try {
            androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure
                .ANDROID_ID);
            //YeLog.d(String.format("[msg=get AndroidId][result=success][androidId=%s]",
            // androidId));
        } catch (Exception e) {
            YeLog.e(String.format("[msg=get AndroidId][result=fail]"));
        }
        return androidId;
    }

    /**
     * 检查是否申明权限或者是否通过动态权限
     *
     * @param context
     * @param permission
     * @return
     */
    public static boolean checkPermission(@NonNull Context context, @NonNull String
        permission) {
        PackageManager pm = context.getPackageManager();
        int flag = pm.checkPermission(permission, context.getPackageName());
        if (PackageManager.PERMISSION_GRANTED == flag) {
            flag = context.checkPermission(permission, android.os.Process.myPid(), Process.myUid());
            return PackageManager.PERMISSION_GRANTED != flag;
        }
        return false;

    }

    public static int getNetworkType(Context context) {
        ConnectivityManager connectivityManager =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        int networkType = ConnectivityManager.TYPE_DUMMY;
        if (Utils.checkPermission(context, ACCESS_NETWORK_STATE)) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            networkType = activeNetworkInfo != null ? activeNetworkInfo.getType() :
                ConnectivityManager.TYPE_DUMMY;
        }
        return networkType;
    }

}
