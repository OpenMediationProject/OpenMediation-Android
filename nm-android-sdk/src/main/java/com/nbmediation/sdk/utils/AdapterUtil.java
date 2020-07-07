// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.utils;

import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;

import com.nbmediation.sdk.NmAds;
import com.nbmediation.sdk.mediation.CustomAdsAdapter;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.utils.constant.CommonConstants;
import com.nbmediation.sdk.utils.crash.CrashUtil;
import com.nbmediation.sdk.utils.model.AdNetwork;

import org.json.JSONArray;

import java.lang.reflect.Constructor;
import java.nio.charset.Charset;

/**
 * The type Adapter util.
 */
public class AdapterUtil {

    /**
     * The constant MEDIATION_ADAPTER_BASE_PATH.
     */
    protected static final String MEDIATION_ADAPTER_BASE_PATH = "com.nbmediation.sdk.mobileads.";
    private static final String ADAPTER = "Adapter";
    private static SparseArray<CustomAdsAdapter> mAdapters = new SparseArray<>();
    private static SparseArray<String> mAdapterPaths;

    static {
        mAdapterPaths = new SparseArray<>();
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_1, getAdapterPath(MediationInfo.MEDIATION_ID_1));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_2, getAdapterPath(MediationInfo.MEDIATION_ID_2));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_3, getAdapterPath(MediationInfo.MEDIATION_ID_3));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_4, getAdapterPath(MediationInfo.MEDIATION_ID_4));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_5, getAdapterPath(MediationInfo.MEDIATION_ID_5));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_6, getAdapterPath(MediationInfo.MEDIATION_ID_6));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_7, getAdapterPath(MediationInfo.MEDIATION_ID_7));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_8, getAdapterPath(MediationInfo.MEDIATION_ID_8));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_9, getAdapterPath(MediationInfo.MEDIATION_ID_9));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_11, getAdapterPath(MediationInfo.MEDIATION_ID_11));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_12, getAdapterPath(MediationInfo.MEDIATION_ID_12));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_13, getAdapterPath(MediationInfo.MEDIATION_ID_13));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_14, getAdapterPath(MediationInfo.MEDIATION_ID_14));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_15, getAdapterPath(MediationInfo.MEDIATION_ID_15));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_17, getAdapterPath(MediationInfo.MEDIATION_ID_17));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_18, getAdapterPath(MediationInfo.MEDIATION_ID_18));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_19, getAdapterPath(MediationInfo.MEDIATION_ID_19));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_20, getAdapterPath(MediationInfo.MEDIATION_ID_20));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_21, getAdapterPath(MediationInfo.MEDIATION_ID_21));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_22, getAdapterPath(MediationInfo.MEDIATION_ID_22));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_23, getAdapterPath(MediationInfo.MEDIATION_ID_23));

        //plugin
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_32, getAdapterPath(MediationInfo.MEDIATION_ID_32));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_33, getAdapterPath(MediationInfo.MEDIATION_ID_33));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_34, getAdapterPath(MediationInfo.MEDIATION_ID_34));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_35, getAdapterPath(MediationInfo.MEDIATION_ID_35));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_36, getAdapterPath(MediationInfo.MEDIATION_ID_36));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_37, getAdapterPath(MediationInfo.MEDIATION_ID_37));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_38, getAdapterPath(MediationInfo.MEDIATION_ID_38));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_39, getAdapterPath(MediationInfo.MEDIATION_ID_39));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_40, getAdapterPath(MediationInfo.MEDIATION_ID_40));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_41, getAdapterPath(MediationInfo.MEDIATION_ID_41));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_42, getAdapterPath(MediationInfo.MEDIATION_ID_42));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_43, getAdapterPath(MediationInfo.MEDIATION_ID_43));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_44, getAdapterPath(MediationInfo.MEDIATION_ID_44));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_45, getAdapterPath(MediationInfo.MEDIATION_ID_45));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_46, getAdapterPath(MediationInfo.MEDIATION_ID_46));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_47, getAdapterPath(MediationInfo.MEDIATION_ID_47));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_48, getAdapterPath(MediationInfo.MEDIATION_ID_48));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_49, getAdapterPath(MediationInfo.MEDIATION_ID_49));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_50, getAdapterPath(MediationInfo.MEDIATION_ID_50));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_51, getAdapterPath(MediationInfo.MEDIATION_ID_51));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_52, getAdapterPath(MediationInfo.MEDIATION_ID_52));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_53, getAdapterPath(MediationInfo.MEDIATION_ID_53));
    }

    /**
     * @return the adns
     */
    public static JSONArray getAdns() {
        JSONArray jsonArray = new JSONArray();
        if (mAdapters == null) {
            mAdapters = new SparseArray<>();
        } else {
            mAdapters.clear();
        }
        //traverses to get adapters
        for (int i = 0; i < mAdapterPaths.size(); i++) {
            CustomAdsAdapter adapter = null;
            String className = mAdapterPaths.get(mAdapterPaths.keyAt(i));
            Throwable exception = null;
//            if (mAdapterPaths.keyAt(i) == 32) {
//                Log.i("tjt", "进来了");
//            }
            try {
                adapter = createAdapter(CustomAdsAdapter.class, className);
            } catch (Exception e) {
                if (e instanceof ClassNotFoundException) {
                    try {
                        String[] strSplit = className.split("\\.");
                        String str = strSplit[strSplit.length - 1];
                        String pluginName = str.substring(0, str.indexOf(ADAPTER));

                        ClassLoader pluginClassLoader = NmAds.PLUGIN_LOADERS.get(pluginName);//获取插件的ClassLoader
                        if (pluginClassLoader == null) {
                            exception = e;
                        } else {
                            adapter = pluginClassLoader.loadClass(className).asSubclass(CustomAdsAdapter.class).newInstance();
                        }
                    } catch (Throwable ex) {
                        exception = ex;
                    }
                } else {
                    exception = e;
                }
            }
            if (exception != null) {
                CrashUtil.getSingleton().saveException(exception);
                DeveloperLog.LogD("AdapterUtil getAdns : ", exception);
            } else {
                mAdapters.put(adapter.getAdNetworkId(), adapter);
                AdNetwork unityAdNetwork = getAdNetWork(adapter);
                jsonArray.put(unityAdNetwork.toJson());
            }

        }
        return jsonArray;
    }

    public synchronized static void createAdapterAll() {
        //traverses to get adapters
        for (int i = 0; i < mAdapterPaths.size(); i++) {
            int adNetworkId = mAdapterPaths.keyAt(i);
            if (mAdapters.get(adNetworkId) != null) {
                continue;
            }
            CustomAdsAdapter adapter = null;
            String className = mAdapterPaths.get(adNetworkId);
            Throwable exception = null;
            if (mAdapterPaths.keyAt(i) == 13 || mAdapterPaths.keyAt(i) == 33) {
                Log.i("tjt", "进来了");
            }
            try {
                adapter = createAdapter(CustomAdsAdapter.class, className);
            } catch (Exception e) {
                if (e instanceof ClassNotFoundException) {
                    try {
                        String[] strSplit = className.split("\\.");
                        String str = strSplit[strSplit.length - 1];
                        String pluginName = str.substring(0, str.indexOf(ADAPTER));

                        ClassLoader pluginClassLoader = NmAds.PLUGIN_LOADERS.get(pluginName);//获取插件的ClassLoader
                        if (pluginClassLoader == null) {
                            exception = e;
                        } else {
                            adapter = pluginClassLoader.loadClass(className).asSubclass(CustomAdsAdapter.class).newInstance();
                        }
                    } catch (Throwable ex) {
                        exception = ex;
                    }
                } else {
                    exception = e;
                }
            }
            if (exception != null) {
                CrashUtil.getSingleton().saveException(exception);
                DeveloperLog.LogD("AdapterUtil createPluginAll : ", exception);
            } else {
                mAdapters.put(adapter.getAdNetworkId(), adapter);
            }

        }
    }


    /**
     * Gets adapter map.
     *
     * @return the adapter map
     */
    static SparseArray<CustomAdsAdapter> getAdapterMap() {
        return mAdapters;
    }

    /**
     * Gets custom ads adapter.
     *
     * @param mediationId the mediation id
     * @return the custom ads adapter
     */
    public static CustomAdsAdapter getCustomAdsAdapter(int mediationId) {
        if (mAdapters != null) {
            return mAdapters.get(mediationId);
        }
        return null;
    }

    /**
     * Gets adapter path with type.
     *
     * @param type the type
     * @param name the name
     * @return the adapter path with type
     */
    public static String getAdapterPathWithType(int type, String name) {
        return MEDIATION_ADAPTER_BASE_PATH.concat(name).concat(getAdType(type));
    }

    private static AdNetwork getAdNetWork(CustomAdsAdapter adapter) {
        if (adapter != null) {
            return new AdNetwork(adapter.getAdNetworkId(), adapter.getMediationVersion(), adapter.getAdapterVersion());
        } else {
            return null;
        }
    }

    /**
     * Create adapter t.
     *
     * @param <T>       the type parameter
     * @param tClass    the t class
     * @param className the class name
     * @return the t
     * @throws Exception the exception
     */
    protected static <T> T createAdapter(Class<T> tClass, String className) throws Exception {
        Class<? extends T> adapterClass = Class.forName(className)
                .asSubclass(tClass);
        Constructor<?> adapterConstructor = adapterClass.getDeclaredConstructor((Class[]) null);
        adapterConstructor.setAccessible(true);
        return (T) adapterConstructor.newInstance();
    }

    private static String getAdapterPath(int mediationType) {
        String path = "";
        switch (mediationType) {
            case MediationInfo.MEDIATION_ID_1:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_1)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_2:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_2)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_3:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_3)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_4:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_4)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_5:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_5)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_6:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_6)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_7:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_7)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_8:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_8)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_9:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_9)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_11:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_11)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_12:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_12)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_13:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_13)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_14:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_14)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_15:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_15)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_17:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_17)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_18:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_18)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_19:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_19)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_20:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_20)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_21:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_21)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_22:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_22)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_23:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_23)).concat(ADAPTER);
                break;

            //plugin
            case MediationInfo.MEDIATION_ID_32:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_32)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_33:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_33)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_34:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_34)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_35:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_35)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_36:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_36)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_37:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_37)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_38:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_38)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_39:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_39)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_40:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_40)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_41:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_41)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_42:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_42)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_43:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_43)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_44:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_44)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_45:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_45)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_46:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_46)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_47:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_47)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_48:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_48)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_49:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_49)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_50:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_50)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_51:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_51)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_52:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_52)).concat(ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_53:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_53)).concat(ADAPTER);
                break;
            default:
                break;
        }
        DeveloperLog.LogD("adapter path is : " + path);
        return path;
    }


    /**
     * Gets ad type.
     *
     * @param adIndex the ad index
     * @return the ad type
     */
    static String getAdType(int adIndex) {
        String adType = "";
        switch (adIndex) {
            case 0:
                adType = CommonConstants.ADTYPE_BANNER;
                break;
            case 1:
                adType = CommonConstants.ADTYPE_NATIVE;
                break;
            default:
                break;
        }
        return adType;
    }

    /**
     * Gets adapter name.
     *
     * @param platName the plat name
     * @return the adapter name
     */
    protected static String getAdapterName(String platName) {
        return new String(Base64.decode(platName, Base64.NO_WRAP),
                Charset.forName(CommonConstants.CHARTSET_UTF8));
    }
}
