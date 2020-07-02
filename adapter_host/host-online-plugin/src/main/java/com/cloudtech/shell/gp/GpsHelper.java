package com.cloudtech.shell.gp;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Keep;
import android.text.TextUtils;

import com.cloudtech.shell.utils.ContextHolder;
import com.cloudtech.shell.utils.MethodBuilderFactory;
import com.cloudtech.shell.utils.PreferencesUtils;
import com.cloudtech.shell.utils.Reflection;
import com.cloudtech.shell.utils.SwitchConfig;
import com.cloudtech.shell.utils.ThreadPoolProxy;
import com.cloudtech.shell.utils.YeLog;

public class GpsHelper {

    private static final String ADVERTISING_ID_KEY = "advertisingId";
    private static final String IS_LIMIT_AD_TRACKING_ENABLED_KEY = "isLimitAdTrackingEnabled";
    private static final String sAdvertisingIdClientClassName =
        "com.google.android.gms.ads.identifier.AdvertisingIdClient";
    private static String advertisingId = null;

    //初始化的时候调用
    public static void startLoadGaid() {
        asyncFetchAdvertisingInfoIfNotCached(ContextHolder.getGlobalAppContext(), null);
    }


    @Keep
    public static String getAdvertisingId() {
        if (SwitchConfig.DEBUG_USE_EMULATOR) {
            return "GAID_EMULATOR";
        }
        return TextUtils.isEmpty(advertisingId) ? PreferencesUtils.getDefString(ADVERTISING_ID_KEY,
                "") : advertisingId;
    }

    public static boolean isLimitAdTrackingEnabled() {
        long value = PreferencesUtils.getDefLong(IS_LIMIT_AD_TRACKING_ENABLED_KEY, 0);
        return 1 == value;
    }


    private static void asyncFetchAdvertisingInfoIfNotCached(Context context, GpsHelperListener gpsHelperListener) {

        if (TextUtils.isEmpty(advertisingId)) {
            asyncFetchAdvertisingInfo(context, gpsHelperListener);
        } else {
            if (gpsHelperListener != null) {
                gpsHelperListener.onFetchAdInfoCompleted();
            }
        }
    }

    private static void asyncFetchAdvertisingInfo(final Context context, final GpsHelperListener gpsHelperListener) {

            YeLog.d("GpsHelper >> fetch GoogleAdvertisingInfo(GAID)");

            ThreadPoolProxy.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Reflection.MethodBuilder methodBuilder =
                            MethodBuilderFactory.create(null, "getAdvertisingIdInfo")
                                .setStatic(Class.forName(sAdvertisingIdClientClassName))
                                .addParam(Context.class, context);

                        Object adInfo = methodBuilder.execute();

                        if (adInfo != null) {
                            GpsHelper.updateSharedPreferences(adInfo);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (gpsHelperListener != null) {
                                        gpsHelperListener.onFetchAdInfoCompleted();
                                    }
                                }
                            });
                            return;
                        }
                    } catch (Exception exception) {
                        //exception.printStackTrace();
                        YeLog.d("Unable to obtain AdvertisingIdClient.getAdvertisingIdInfo()");
                    }

                    try {
                        AdvertisingIdClient.AdInfo adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
                        if (adInfo != null) {
                            GpsHelper.updateSharedPreferences(adInfo);
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (gpsHelperListener != null) {
                                    gpsHelperListener.onFetchAdInfoCompleted();
                                }
                            }
                        });
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                }
            });
    }

    //获取到advertisingId
    public static void updateSharedPreferences(Object adInfo) {
        advertisingId = reflectedGetAdvertisingId(adInfo, "");
        boolean isLimitAdTrackingEnabled = reflectedIsLimitAdTrackingEnabled(adInfo, false);

        PreferencesUtils.putDefString(ADVERTISING_ID_KEY, advertisingId);
        PreferencesUtils.putDefLong(IS_LIMIT_AD_TRACKING_ENABLED_KEY, isLimitAdTrackingEnabled ? 1 : 0);

    }

    private static String reflectedGetAdvertisingId(Object adInfo, String defaultValue) {
        try {
            return (String) MethodBuilderFactory.create(adInfo, "getId").execute();
        } catch (Exception exception) {
            return defaultValue;
        }
    }

    private static boolean reflectedIsLimitAdTrackingEnabled(Object adInfo, boolean defaultValue) {
        try {
            Boolean result =
                    (Boolean) MethodBuilderFactory.create(adInfo, "isLimitAdTrackingEnabled").execute();
            return (result != null) ? result : defaultValue;
        } catch (Exception exception) {
            return defaultValue;
        }
    }

    private static final Handler handler = new Handler(Looper.getMainLooper());

    public interface GpsHelperListener {
        void onFetchAdInfoCompleted();
    }

}
