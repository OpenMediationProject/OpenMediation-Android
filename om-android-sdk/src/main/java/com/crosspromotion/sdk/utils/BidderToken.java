package com.crosspromotion.sdk.utils;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;

import com.openmediation.sdk.utils.AFManager;
import com.openmediation.sdk.utils.AdtUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.JsonUtil;
import com.openmediation.sdk.utils.OaidHelper;
import com.openmediation.sdk.utils.cache.DataCache;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.constant.KeyConstants;
import com.openmediation.sdk.utils.device.AdvertisingIdClient;
import com.openmediation.sdk.utils.device.DeviceUtil;
import com.openmediation.sdk.utils.helper.IapHelper;

import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;

public class BidderToken {

    public static String getBidToken() {
        return generateBidToken();
    }

    /**
     * @return JSON+ZLib+Base64
     */
    private static String generateBidToken() {
        JSONObject object = new JSONObject();
        Context context = AdtUtil.getApplication();
        JsonUtil.put(object, KeyConstants.Request.KEY_SDK_VERSION, CommonConstants.SDK_VERSION_NAME);
        JsonUtil.put(object, KeyConstants.RequestBody.KEY_FIT, DeviceUtil.getFit());
        JsonUtil.put(object, KeyConstants.RequestBody.KEY_FLT, DeviceUtil.getFlt());
        JsonUtil.put(object, KeyConstants.RequestBody.KEY_IAP, IapHelper.getIap());
        JsonUtil.put(object, KeyConstants.RequestBody.KEY_SESSION, DeviceUtil.getSessionId());
        JsonUtil.put(object, KeyConstants.RequestBody.KEY_UID, DeviceUtil.getUid());
        int dType;
        String did;
        String gaid = DataCache.getInstance().get(KeyConstants.RequestBody.KEY_GAID, String.class);
        if (TextUtils.isEmpty(gaid)) {
            try {
                AdvertisingIdClient.AdInfo adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
                gaid = adInfo != null ? adInfo.getId() : "";
            } catch (Exception e) {
                gaid = null;
            }
        }
        if (!TextUtils.isEmpty(gaid)) {
            did = gaid;
            dType = 2;
        } else if (!TextUtils.isEmpty(OaidHelper.getOaid())) {
            did = OaidHelper.getOaid();
            dType = 4;
        } else {
            did = "";
            dType = 0;
        }
        JsonUtil.put(object, KeyConstants.RequestBody.KEY_DID, did);
        JsonUtil.put(object, KeyConstants.RequestBody.KEY_DTYPE, dType);
        JsonUtil.put(object, KeyConstants.RequestBody.KEY_AF_ID, AFManager.getAfId(context));
        JsonUtil.put(object, KeyConstants.RequestBody.KEY_NG, DeviceUtil.isGpInstall(context));
        JsonUtil.put(object, KeyConstants.RequestBody.KEY_ZO, DeviceUtil.getTimeZoneOffset());
        JsonUtil.put(object, KeyConstants.RequestBody.KEY_JB, DeviceUtil.isRoot() ? 1 : 0);
        JsonUtil.put(object, KeyConstants.RequestBody.KEY_BRAND, Build.BRAND);
        JsonUtil.put(object, KeyConstants.RequestBody.KEY_FM, DeviceUtil.getFm());
        Map<String, Integer> battery = DeviceUtil.getBatteryInfo(context);
        if (battery == null || battery.isEmpty()) {
            JsonUtil.put(object, KeyConstants.RequestBody.KEY_BATTERY, 0);
        } else {
            for (Map.Entry<String, Integer> integerEntry : battery.entrySet()) {
                if (integerEntry == null) {
                    continue;
                }

                JsonUtil.put(object, integerEntry.getKey(), integerEntry.getValue());
            }

            if (!object.has(KeyConstants.RequestBody.KEY_BATTERY)) {
                JsonUtil.put(object, KeyConstants.RequestBody.KEY_BATTERY, 0);
            }
        }
        JsonUtil.put(object, KeyConstants.RequestBody.KEY_LCY, Locale.getDefault().getCountry());
        DeveloperLog.LogD("BidToken : " + object.toString());
        return Base64.encodeToString(ZLib.compress(object.toString().getBytes(Charset.forName(CommonConstants.CHARTSET_UTF8))),
                Base64.NO_WRAP);
    }
}
