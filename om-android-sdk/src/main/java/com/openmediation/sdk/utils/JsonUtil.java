// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

public class JsonUtil {
    public static JSONObject put(JSONObject jsonObject, String key, Object value) {
        try {
            if (jsonObject == null || value == null) {
                return jsonObject;
            } else if (value instanceof String) {
                if (TextUtils.isEmpty((String) value)) {
                    return jsonObject;
                }
                jsonObject.put(key, value);
            } else if (value instanceof Integer) {
                if (((int) value < 0)) {
                    return jsonObject;
                }
                jsonObject.put(key, value);
            } else if (value instanceof Double) {
                if (((double) value < 0)) {
                    return jsonObject;
                }
                jsonObject.put(key, value);
            } else {
                jsonObject.put(key, value);
            }
            return jsonObject;
        } catch (JSONException e) {
            DeveloperLog.LogD("JsonUtil error : ", e);
        }
        return jsonObject;
    }

    public static JSONObject convert(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            JSONObject object = new JSONObject();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (TextUtils.isEmpty(key) || value == null) {
                    continue;
                }
                if (value instanceof List) {
                    object.put(key, convertList((List) value));
                } else if (isArray(value)) {
                    object.put(key, convertArray(value));
                } else {
                    object.put(key, value);
                }
            }
            return object;
        } catch (Exception e) {
            String error = "Error: setCustomTags(), " + e.getMessage();
            DeveloperLog.LogE(error);
            AdLog.getSingleton().LogE(error);
            return null;
        }
    }

    public static boolean isArray(Object obj) {
        if (obj == null) {
            return false;
        }
        return obj.getClass().isArray();
    }

    public static JSONArray convertArray(Object value) {
        JSONArray jsonArray = new JSONArray();
        final int length = Array.getLength(value);
        for (int i = 0; i < length; ++i) {
            jsonArray.put(Array.get(value, i));
        }
        return jsonArray;
    }

    public static JSONArray convertList(List value) {
        JSONArray jsonArray = new JSONArray();
        for (Object o : value) {
            jsonArray.put(o);
        }
        return jsonArray;
    }
}
