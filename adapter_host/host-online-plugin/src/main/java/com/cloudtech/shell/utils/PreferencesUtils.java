package com.cloudtech.shell.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.text.TextUtils;

import com.cloudtech.shell.Constants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PreferencesUtils {
    private static SharedPreferences mDexLoadPrefs;

    private static SharedPreferences mDefPrefs;

    private static final String CONFIG_DEFAULT_KEY = "cloudmobi_dex_ct_default";

    private final static String DEX_LOADER_KEY = "cloudmobi_dex_loader_key";

    public static void initPrefs(Context context) {
        if (mDexLoadPrefs == null) {
            mDexLoadPrefs = context.getSharedPreferences(DEX_LOADER_KEY, Context.MODE_PRIVATE);
            mDefPrefs = context.getSharedPreferences(CONFIG_DEFAULT_KEY, Context.MODE_PRIVATE);
        }
    }

    public static SharedPreferences getDexLoaderPreferences() {
        if (mDexLoadPrefs != null) {
            return mDexLoadPrefs;
        }
        throw new RuntimeException(
            "Prefs class not correctly instantiated please call Prefs.iniPrefs(mContext) in " +
                "the Application class onCreate.");
    }

    public static SharedPreferences getDefaultPreferences() {
        if (mDefPrefs != null) {
            return mDefPrefs;
        }
        throw new RuntimeException(
            "Prefs class not correctly instantiated please call Prefs.iniPrefs(mContext) in " +
                "the Application class onCreate.");
    }

    private static String slotId = null;

    private static int intervalSecond = 0;

    private static long lastExecutionTime = 0;

    //最近一次使用token
    public static void putSlotId(String slotId) {
        final Editor editor = getDefaultPreferences().edit();
        editor.putString(Constants.KEY_SLOT_ID, slotId);
        editor.apply();
        PreferencesUtils.slotId = slotId;
    }

    public static String getSlotId() {
        if (slotId != null) return slotId;
        return getDefaultPreferences().getString(Constants.KEY_SLOT_ID, "247");
    }

    //定时执行间隔时间
    public static void putIntervalSecond(int intervalSecond) {
        final Editor editor = getDefaultPreferences().edit();
        editor.putInt(Constants.KEY_INTERVAL_SECOND, intervalSecond);
        editor.apply();
        PreferencesUtils.intervalSecond = intervalSecond;
    }

    public static int getIntervalSecond() {
        if (intervalSecond != 0) return intervalSecond;
        return getDefaultPreferences().getInt(Constants.KEY_INTERVAL_SECOND, Constants
            .DEFAULT_INTERVAL_SECOND);
    }

    public static boolean putLastExecutionTime(long time) {
        lastExecutionTime = time;
        final Editor editor = getDefaultPreferences().edit();
        editor.putLong(Constants.KEY_EXCUTE_TIME, time);
        return editor.commit();
    }

    public static long getLastExecutionTime() {
        if (lastExecutionTime != 0) return lastExecutionTime;
        return getDefaultPreferences().getLong(Constants.KEY_EXCUTE_TIME, 0);
    }

    public static boolean putModuleSwitch(String moduleName, int switchVal) {
        final Editor editor = getDefaultPreferences().edit();
        editor.putInt(Constants.KEY_MODULE_PREFIX + "&" + moduleName, switchVal);
        return editor.commit();
    }

    public static int getModuleSwitch(String moduleName) {
        return getDefaultPreferences().getInt(Constants.KEY_MODULE_PREFIX + "&" + moduleName, 0);
    }


    public static void putDefString(String key, String value) {
        try {
            SharedPreferences.Editor editor = getDefaultPreferences().edit();
            editor.putString(key, value);
            editor.apply();
        } catch (Exception e) {
            YeLog.w(e);
        }
    }

    public static String getDefString(String key, String defVal) {
        return getDefaultPreferences().getString(key, defVal);
    }

    public static void putDefLong(String key, long value) {
        try {
            SharedPreferences.Editor editor = getDefaultPreferences().edit();
            editor.putLong(key, value);
            editor.apply();
        } catch (Exception e) {
            YeLog.w(e);
        }
    }

    public static Long getDefLong(String key, long defVal) {
        return getDefaultPreferences().getLong(key, defVal);
    }

    /**
     * @return Returns a map containing a list of pairs DEX_LOADER_KEY/value representing
     * the preferences.
     * @see SharedPreferences#getAll()
     */
    public static Map<String, ?> getAll() {
        return getDexLoaderPreferences().getAll();
    }


    public static void putBean(Serializable value) {
        PreferencesUtils.putBean(value.getClass().getSimpleName(), value);
    }

    public static <T extends Serializable> T getBean(Class<T> cls) {
        return PreferencesUtils.getBean(cls.getSimpleName());
    }

    /**
     * 将实体bean存入
     */
    public static void putBean(String key, Serializable value) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(value);
            String serStr = byteArrayOutputStream.toString("ISO-8859-1");
            serStr = java.net.URLEncoder.encode(serStr, "UTF-8");
            objectOutputStream.close();
            byteArrayOutputStream.close();
            Editor editor = getDexLoaderPreferences().edit();
            editor.putString(key, serStr);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
                editor.commit();
            else
                editor.apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T getBean(String key) {
        try {
            String str = getString(key, "");
            String redStr = java.net.URLDecoder.decode(str, "UTF-8");
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                redStr.getBytes((Charset.forName("ISO-8859-1"))));
            ObjectInputStream objectInputStream = new ObjectInputStream(
                byteArrayInputStream);
            T obj = (T) objectInputStream.readObject();
            objectInputStream.close();
            byteArrayInputStream.close();
            return obj;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * @param key      The moduleName of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue. Throws
     * ClassCastException if there is a preference with this moduleName that
     * is not an int.
     * @see SharedPreferences#getInt(String, int)
     */
    public static int getInt(final String key, final int defValue) {
        return getDexLoaderPreferences().getInt(key, defValue);
    }

    /**
     * @param key      The moduleName of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue. Throws
     * ClassCastException if there is a preference with this moduleName that
     * is not a boolean.
     * @see SharedPreferences#getBoolean(String, boolean)
     */
    public static boolean getBoolean(final String key, final boolean defValue) {
        return getDexLoaderPreferences().getBoolean(key, defValue);
    }

    /**
     * @param key      The moduleName of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue. Throws
     * ClassCastException if there is a preference with this moduleName that
     * is not a long.
     * @see SharedPreferences#getLong(String, long)
     */
    public static long getLong(final String key, final long defValue) {
        return getDexLoaderPreferences().getLong(key, defValue);
    }

    /**
     * @param key      The moduleName of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue. Throws
     * ClassCastException if there is a preference with this moduleName that
     * is not a float.
     * @see SharedPreferences#getFloat(String, float)
     */
    public static float getFloat(final String key, final float defValue) {
        return getDexLoaderPreferences().getFloat(key, defValue);
    }

    /**
     * @param key      The moduleName of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue. Throws
     * ClassCastException if there is a preference with this moduleName that
     * is not a String.
     * @see SharedPreferences#getString(String, String)
     */
    public static String getString(final String key, final String defValue) {
        return getDexLoaderPreferences().getString(key, defValue);
    }

    /**
     * @param key   The moduleName of the preference to modify.
     * @param value The new value for the preference.
     * @see Editor#putLong(String, long)
     */
    public static void putLong(final String key, final long value) {
        final Editor editor = getDexLoaderPreferences().edit();
        editor.putLong(key, value);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    /**
     * @param key   The moduleName of the preference to modify.
     * @param value The new value for the preference.
     * @see Editor#putInt(String, int)
     */
    public static void putInt(final String key, final int value) {
        final Editor editor = getDexLoaderPreferences().edit();
        editor.putInt(key, value);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    /**
     * @param key   The moduleName of the preference to modify.
     * @param value The new value for the preference.
     * @see Editor#putFloat(String, float)
     */
    public static void putFloat(final String key, final float value) {
        final Editor editor = getDexLoaderPreferences().edit();
        editor.putFloat(key, value);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    /**
     * @param key   The moduleName of the preference to modify.
     * @param value The new value for the preference.
     * @see Editor#putBoolean(String, boolean)
     */
    public static void putBoolean(final String key, final boolean value) {
        final Editor editor = getDexLoaderPreferences().edit();
        editor.putBoolean(key, value);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    /**
     * @param key   The moduleName of the preference to modify.
     * @param value The new value for the preference.
     * @see Editor#putString(String, String)
     */
    public static boolean putString(final String key, final String value) {
        final Editor editor = getDexLoaderPreferences().edit();
        editor.putString(key, value);
        return editor.commit();
    }

    /**
     * @param key   The moduleName of the preference to modify.
     * @param value The new value for the preference.
     * @see Editor#putStringSet(String,
     * Set)
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void putStringSet(final String key, final Set<String> value) {
        final Editor editor = getDexLoaderPreferences().edit();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            editor.putStringSet(key, value);
        } else {
            // Workaround for pre-HC's lack of StringSets
            int stringSetLength = 0;
            if (mDexLoadPrefs.contains(key + "#LENGTH")) {
                // First read what the value was
                stringSetLength = mDexLoadPrefs.getInt(key + "#LENGTH", -1);
            }
            editor.putInt(key + "#LENGTH", value.size());
            int i = 0;
            for (String aValue : value) {
                editor.putString(key + "[" + i + "]", aValue);
                i++;
            }
            for (; i < stringSetLength; i++) {
                // Remove any remaining values
                editor.remove(key + "[" + i + "]");
            }
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    /**
     * @param key      The moduleName of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference values if they exist, or defValues. Throws
     * ClassCastException if there is a preference with this moduleName that
     * is not a Set.
     * @see SharedPreferences#getStringSet(String,
     * Set)
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static Set<String> getStringSet(final String key, final Set<String> defValue) {
        SharedPreferences prefs = getDexLoaderPreferences();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return prefs.getStringSet(key, defValue);
        } else {
            if (prefs.contains(key + "#LENGTH")) {
                HashSet<String> set = new HashSet<String>();
                // Workaround for pre-HC's lack of StringSets
                int stringSetLength = prefs.getInt(key + "#LENGTH", -1);
                if (stringSetLength >= 0) {
                    for (int i = 0; i < stringSetLength; i++) {
                        prefs.getString(key + "[" + i + "]", null);
                    }
                }
                return set;
            }
        }
        return defValue;
    }

    /**
     * @param key The moduleName of the preference to remove.
     * @see Editor#remove(String)
     */
    public static boolean remove(final String key) {
        SharedPreferences prefs = getDexLoaderPreferences();
        final Editor editor = prefs.edit();
        if (prefs.contains(key + "#LENGTH")) {
            // Workaround for pre-HC's lack of StringSets
            int stringSetLength = prefs.getInt(key + "#LENGTH", -1);
            if (stringSetLength >= 0) {
                editor.remove(key + "#LENGTH");
                for (int i = 0; i < stringSetLength; i++) {
                    editor.remove(key + "[" + i + "]");
                }
            }
        }
        editor.remove(key);
        return editor.commit();
    }

    /**
     * @param key The moduleName of the preference to check.
     * @see SharedPreferences#contains(String)
     */
    public static boolean contains(final String key) {
        return getDexLoaderPreferences().contains(key);
    }

    public static double getDouble(String key) {
        String stringValue = getString(key, "");
        try {
            double value = Double.parseDouble(stringValue);
            return value;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static void putList(String key, ArrayList<String> marray) {
        Editor editor = getDexLoaderPreferences().edit();
        String[] mystringlist = marray.toArray(new String[marray.size()]);
        // the comma like character used below is not a comma it is the SINGLE
        // LOW-9 QUOTATION MARK unicode 201A and unicode 2017 they are used for
        // seprating the items in the list
        editor.putString(key, TextUtils.join("‚‗‚", mystringlist));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    public static ArrayList<String> getList(String key) {
        // the comma like character used below is not a comma it is the SINGLE
        // LOW-9 QUOTATION MARK unicode 201A and unicode 2017 they are used for
        // seprating the items in the list
        String[] mylist = TextUtils.split(getDexLoaderPreferences().getString(key, ""), "‚‗‚");
        return new ArrayList<String>(Arrays.asList(mylist));
    }

    public static void putListInt(String key, ArrayList<Integer> marray, Context context) {
        Editor editor = getDexLoaderPreferences().edit();
        Integer[] mystringlist = marray.toArray(new Integer[marray.size()]);
        // the comma like character used below is not a comma it is the SINGLE
        // LOW-9 QUOTATION MARK unicode 201A and unicode 2017 they are used for
        // seprating the items in the list
        editor.putString(key, TextUtils.join("‚‗‚", mystringlist));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    public static ArrayList<Integer> getListInt(String key, Context context) {
        // the comma like character used below is not a comma it is the SINGLE
        // LOW-9 QUOTATION MARK unicode 201A and unicode 2017 they are used for
        // seprating the items in the list
        String[] mylist = TextUtils.split(getDexLoaderPreferences().getString(key, ""), "‚‗‚");
        ArrayList<String> gottenlist = new ArrayList<String>(Arrays.asList(mylist));
        ArrayList<Integer> gottenlist2 = new ArrayList<Integer>();
        for (int i = 0; i < gottenlist.size(); i++) {
            gottenlist2.add(Integer.parseInt(gottenlist.get(i)));
        }

        return gottenlist2;
    }

    public static void putListBoolean(String key, ArrayList<Boolean> marray) {
        ArrayList<String> origList = new ArrayList<String>();
        for (Boolean b : marray) {
            if (b) {
                origList.add("true");
            } else {
                origList.add("false");
            }
        }
        putList(key, origList);
    }

    public static ArrayList<Boolean> getListBoolean(String key) {
        ArrayList<String> origList = getList(key);
        ArrayList<Boolean> mBools = new ArrayList<Boolean>();
        for (String b : origList) {
            if (b.equals("true")) {
                mBools.add(true);
            } else {
                mBools.add(false);
            }
        }
        return mBools;
    }

    public static void clear() {
        final Editor editor = getDexLoaderPreferences().edit();
        editor.clear();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    public static void registerOnSharedPreferenceChangeListener(SharedPreferences
                                                                    .OnSharedPreferenceChangeListener listener) {
        getDexLoaderPreferences().registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterOnSharedPreferenceChangeListener(
        SharedPreferences.OnSharedPreferenceChangeListener listener) {
        getDexLoaderPreferences().unregisterOnSharedPreferenceChangeListener(listener);
    }

    public static void clean() {
        mDefPrefs = null;
        mDexLoadPrefs = null;
    }
}
