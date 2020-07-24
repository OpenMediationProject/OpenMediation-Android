// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.device;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.text.TextUtils;
import android.webkit.WebSettings;

import com.openmediation.sdk.utils.AdtUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.OaidHelper;
import com.openmediation.sdk.utils.cache.DataCache;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.constant.KeyConstants;
import com.openmediation.sdk.utils.crash.CrashUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utils for Android native APIs
 */
public class DeviceUtil {

    private static String mSessionId;

    private DeviceUtil() {
    }

    /**
     * Is activity available boolean.
     *
     * @param activity activity
     * @return the boolean
     */
    public static boolean isActivityAvailable(Activity activity) {
        if (activity == null) {
            return false;
        }
        boolean flage = false;
        if (Build.VERSION.SDK_INT >= 17) {
            if (!activity.isDestroyed()) {
                flage = true;
            }
        } else {
            if (!activity.isFinishing()) {
                flage = true;
            }
        }
        return flage;
    }

    /**
     * device info, app info, and misc. info
     *
     * @param context context
     * @return the map
     */
    public static Map<String, Object> preFetchDeviceInfo(Context context) {
        Map<String, Object> map = new HashMap<>();
        map.put("UserAgent", getUserAgent(context));
        AdvertisingIdClient.AdInfo info = AdvertisingIdClient.getAdvertisingIdInfo(context);
        String gaid = info == null ? "" : info.getId();
        DeveloperLog.LogD("Gaid:" + gaid);
        if (!TextUtils.isEmpty(gaid)) {
            map.put("AdvertisingId", gaid);
        } else {
            OaidHelper.initOaidServer(context);
        }
        return map;
    }

    /**
     * Gets locale info.
     *
     * @return the locale info
     */
    public static Map<String, Object> getLocaleInfo() {
        Map<String, Object> map = new HashMap<>();
        Locale locale = Locale.getDefault();
        map.put(KeyConstants.RequestBody.KEY_LANG_NAME, locale.getDisplayLanguage());
        map.put(KeyConstants.RequestBody.KEY_LCOUNTRY, Locale.getDefault().getCountry());
        map.put(KeyConstants.RequestBody.KEY_LANG, Locale.getDefault().getLanguage());
        return map;
    }

    /**
     * Gets system properties.
     *
     * @param systemKey the system key
     * @return the system properties
     */
    public static String getSystemProperties(String systemKey) {
        Method getMethod = null;
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");

            getMethod = systemPropertiesClass.getMethod("get", String.class);
            Object result = getMethod.invoke(null, systemKey);
            if (result != null) {
                return result.toString();
            }
        } catch (Exception e) {
            DeveloperLog.LogD("DeviceUtil", e);
            CrashUtil.getSingleton().saveException(e);
        }
        return null;
    }

    private static String getUserAgent(Context context) {
        String userAgent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                userAgent = WebSettings.getDefaultUserAgent(context);
            } catch (Throwable e) {
                userAgent = System.getProperty("http.agent");
            }
        } else {
            userAgent = System.getProperty("http.agent");
        }
        StringBuilder sb = new StringBuilder();
        if (userAgent != null) {
            for (int i = 0, length = userAgent.length(); i < length; i++) {
                char c = userAgent.charAt(i);
                if (c <= '\u001f' || c >= '\u007f') {
                    sb.append(String.format("\\u%04x", (int) c));
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Gets facebook id.
     *
     * @param context the context
     * @return the facebook id
     */
    public static String getFacebookId(Context context) {
        String facebookId = "";
        String[] projection = {"aid"};
        Cursor cursor = null;
        try {
            if (!isFacebookInstall(context)) {
                return facebookId;
            }
            cursor = context.getContentResolver().query(Uri.parse("content://com.facebook.katana.provider.AttributionIdProvider"),
                    projection, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                return facebookId;
            } else {
                facebookId = cursor.getString(cursor.getColumnIndex("aid"));
            }
        } catch (Exception e) {
            DeveloperLog.LogE("DeviceUtil", e);
            DeveloperLog.LogE("Facebook ID get fail");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (facebookId == null) {
            facebookId = "";
        }
        return facebookId;
    }

    /**
     *
     */
    private static boolean isFacebookInstall(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(CommonConstants.PKG_FB, PackageManager.GET_GIDS);
            return packageInfo != null;
        } catch (Exception e) {
//            DeveloperLog.LogD("DeviceUtil", e);
        }
        return false;
    }

    /**
     * Gets time zone offset.
     *
     * @return the time zone offset
     * @see KeyConstants.RequestBody#KEY_ZO KeyConstants.RequestBody#KEY_ZO
     */
    public static int getTimeZoneOffset() {
        return TimeZone.getDefault().getOffset(System.currentTimeMillis()) / (60 * 1000);
    }

    /**
     * Gets time zone.
     *
     * @return the time zone
     * @see KeyConstants.RequestBody#KEY_TZ KeyConstants.RequestBody#KEY_TZ
     */
    public static String getTimeZone() {
        return TimeZone.getDefault().getID();
    }

    /**
     * Gets version name.
     *
     * @param context the context
     * @return the version name
     * @see KeyConstants.RequestBody#KEY_APPV KeyConstants.RequestBody#KEY_APPV
     */
    public static String getVersionName(Context context) {
        if (context == null) {
            return "";
        }
        String verName = "";
        try {
            verName = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            DeveloperLog.LogE("getVersionName", e);
        }
        return verName;
    }

    /**
     * Free storage in MB
     *
     * @return the fm
     * @see KeyConstants.RequestBody#KEY_FM KeyConstants.RequestBody#KEY_FM
     */
    public static long getFm() {
        File root = Environment.getDataDirectory();
        StatFs sf = new StatFs(root.getPath());
        long blockSize;
        long availCount;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = sf.getBlockSizeLong();
            availCount = sf.getAvailableBlocksLong();
        } else {
            blockSize = sf.getBlockSize();
            availCount = sf.getAvailableBlocks();
        }
        return (availCount * blockSize) / 1024 / 1024;
    }

    /**
     * Gets uid.
     *
     * @return unique virtual ID
     */
    public static String getUid() {
        // Only API >= 9 devices have android.os.Build.SERIAL
        // http://developer.android.com/reference/android/os/Build.html#SERIAL
        // If the device was upgraded or rooted, this API will return duplicated ID
        String uid;
        if (DataCache.getInstance().containsKey(KeyConstants.RequestBody.KEY_UID)) {
            uid = DataCache.getInstance().get(KeyConstants.RequestBody.KEY_UID, String.class);
        } else {
            uid = generateUid();
            DataCache.getInstance().set(KeyConstants.RequestBody.KEY_UID, uid);
            DataCache.getInstance().set(KeyConstants.RequestBody.KEY_FLT, System.currentTimeMillis());
        }
        return uid;
    }

    /**
     * Gets flt.
     *
     * @return the flt
     */
    public static long getFlt() {
        return DataCache.getInstance().containsKey(KeyConstants.RequestBody.KEY_FLT)
                ? DataCache.getInstance().get(KeyConstants.RequestBody.KEY_FLT, long.class) / 1000 : 0;
    }

    /**
     * Gets fit.
     *
     * @return the fit
     */
    public static long getFit() {
        try {
            Context context = AdtUtil.getApplication();
            if (context == null) {
                return 0;
            }
            String pkgName = context.getPackageName();
            PackageManager packageManager = context.getPackageManager();
            if (packageManager == null) {
                return 0;
            }

            PackageInfo packageInfo = packageManager.getPackageInfo(pkgName, PackageManager.GET_GIDS);
            if (packageInfo == null) {
                return 0;
            }
            return packageInfo.firstInstallTime / 1000;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Gets session id.
     *
     * @return the session id
     */
    public static String getSessionId() {
        if (mSessionId == null) {
            mSessionId = new UUID(getUniquePsuedoId().hashCode(), System.currentTimeMillis()).toString();
        }
        return mSessionId;
    }

    /**
     * Gets unique psuedo id.
     *
     * @return the unique psuedo id
     */
    public static String getUniquePsuedoId() {
        String serial = null;

        String mszDevIDShort = "35" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +

                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +

                Build.USER.length() % 10; //13-digits

        try {
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();
            //API>=9 uses serial
            return new UUID(mszDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            //serial needs to init
            serial = "serial";
        }
        //15-digit ID using hardware info
        return new UUID(mszDevIDShort.hashCode(), serial.hashCode()).toString();

    }

    /**
     * Is root boolean.
     *
     * @return the boolean
     */
//
    public static boolean isRoot() {
        String binPath = "/system/bin/su";
        String xBinPath = "/system/xbin/su";

        if (new File(binPath).exists() && isCanExecute(binPath)) {
            return true;
        }
        return new File(xBinPath).exists() && isCanExecute(xBinPath);
    }

    private static boolean isCanExecute(String filePath) {
        java.lang.Process process = null;
        try {
            process = Runtime.getRuntime().exec("ls -l " + filePath);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream(),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)));
            String str = in.readLine();
            if (str != null && str.length() >= 4) {
                char flag = str.charAt(3);
                return flag == 's' || flag == 'x';
            }
            in.close();
        } catch (Exception e) {
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }

    /**
     *
     */
    private static final String REG_EXC_IP = "^192\\.168\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)$";
    /**
     *
     */
    private static final String REG_EXA_IP = "^10\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)$";
    /**
     *
     */
    private static final String REG_EXB_IP = "^172\\.(1[6-9]|2\\d|3[0-1])\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)$";

    /**
     * Gets host ip.
     *
     * @return the host ip
     */
    public static String getHostIp() {
        String hostIp;
        Pattern ip = Pattern.compile("(" + REG_EXA_IP + ")|" + "(" + REG_EXB_IP + ")|" + "(" + REG_EXC_IP + ")");
        Enumeration<NetworkInterface> networkInterfaces = null;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            DeveloperLog.LogE("getHostIp", e);
        }
        InetAddress address;
        while (networkInterfaces != null && networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                address = inetAddresses.nextElement();
                String hostAddress = address.getHostAddress();
                Matcher matcher = ip.matcher(hostAddress);
                if (matcher.matches()) {
                    hostIp = hostAddress;
                    return hostIp;
                }

            }
        }
        return null;
    }

    /**
     * Gets battery info.
     *
     * @param context the context
     * @return the battery info
     */
    public static Map<String, Integer> getBatteryInfo(Context context) {
        if (context == null) {
            return null;
        }
        Map<String, Integer> map = new HashMap<>();

        try {
            Intent batteryIntent = context.registerReceiver((BroadcastReceiver) null,
                    new IntentFilter("android.intent.action.BATTERY_CHANGED"));
            int level = batteryIntent != null ? batteryIntent.getIntExtra("level", -1) : 0;
            int scale = batteryIntent != null ? batteryIntent.getIntExtra("scale", -1) : 0;
            int current = (int) ((float) level / (float) scale * 100.0F);
            if (level != -1 && scale != -1) {
                map.put(KeyConstants.RequestBody.KEY_BATTERY, current);
            }
            int status = batteryIntent != null ? batteryIntent.getIntExtra("status", -1) : 0;
            map.put(KeyConstants.RequestBody.KEY_BTCH, status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status == BatteryManager.BATTERY_STATUS_FULL ? 1 : 0);
            map.put(KeyConstants.RequestBody.KEY_LOWP, current > scale * 0.2 ? 0 : 1);
        } catch (Exception var5) {
            return map;
        }
        return map;
    }

    /**
     * Gets install vending.
     *
     * @param context the context
     * @return the install vending
     */
    public static JSONObject getInstallVending(Context context) {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        int intValue;
        try {
            PackageManager packageManager = context.getPackageManager();
            List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
            Map<String, Integer> hashMap = new HashMap();
            for (ApplicationInfo applicationInfo : installedApplications) {
                String installerPackageName = packageManager.getInstallerPackageName(applicationInfo.packageName);
                Integer num = hashMap.get(installerPackageName);
                if (num == null) {
                    intValue = 1;
                } else {
                    intValue = num.intValue() + 1;
                }
                hashMap.put(installerPackageName, intValue);
            }
            for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
                String str = "other";
                if (entry.getKey() == null) {
                    map.put("os", entry.getValue());
                } else if (entry.getKey().equals(CommonConstants.PKG_GP)) {
                    map.put("gp", entry.getValue());
                } else if (map.get(str) == null) {
                    map.put(str, entry.getValue());
                } else {
                    map.put(str, entry.getValue() + map.get(str));
                }
            }
        } catch (Exception e) {
        }
        return new JSONObject(map);
    }

    public static long getBtime() {
        return System.currentTimeMillis() - SystemClock.elapsedRealtime();
    }

    public static long disk() {
        long blockCount;
        StatFs statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
        if (Build.VERSION.SDK_INT >= 18) {
            long blockSizeLong = statFs.getBlockSizeLong();
            blockCount = statFs.getBlockCountLong() * blockSizeLong;
        } else {
            int blockSize = statFs.getBlockSize();
            blockCount = (long) (statFs.getBlockCount() * blockSize);
        }
        double pow = Math.pow(2.0d, 20.0d);
        return (long) (blockCount / pow);
    }

    public static long getTotalRAM(Context context) {
        if (context == null) {
            return 0;
        }
        long size;
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(outInfo);
        size = outInfo.totalMem;
        return size;
    }

    private static String generateUid() {
        String androidId = "";
        try {
            androidId = android.provider.Settings.Secure.getString(AdtUtil.getApplication().getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            androidId = "";
        }
        String serial = "serial";
        try {
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();
        } catch (Exception exception) {
        }
        if (TextUtils.isEmpty(serial)) {
            serial = "serial";
        }
        // cobines the above with UUID
        return new UUID(androidId.hashCode(), serial.hashCode()).toString();
    }
}
