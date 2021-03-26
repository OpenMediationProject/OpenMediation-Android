// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils;

import android.util.Base64;
import android.util.SparseArray;

import com.openmediation.sdk.core.AdapterRepository;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.model.AdNetwork;

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
    protected static final String MEDIATION_ADAPTER_BASE_PATH = CommonConstants.PKG_ADAPTER;
    private static final String ADAPTER = "Adapter";
    private static SparseArray<CustomAdsAdapter> mAdapters = new SparseArray<>();
    private static final SparseArray<String> mAdapterPaths;

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
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_19, getAdapterPath(MediationInfo.MEDIATION_ID_19));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_20, getAdapterPath(MediationInfo.MEDIATION_ID_20));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_21, getAdapterPath(MediationInfo.MEDIATION_ID_21));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_22, getAdapterPath(MediationInfo.MEDIATION_ID_22));
        mAdapterPaths.put(MediationInfo.MEDIATION_ID_23, getAdapterPath(MediationInfo.MEDIATION_ID_23));
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
            try {
                CustomAdsAdapter adapter = createAdapter(CustomAdsAdapter.class, mAdapterPaths.get(mAdapterPaths.keyAt(i)));

                mAdapters.put(adapter.getAdNetworkId(), adapter);
                AdNetwork unityAdNetwork = getAdNetWork(adapter);
                jsonArray.put(unityAdNetwork.toJson());
                // set GDPR Age Gender
                AdapterRepository.getInstance().setCustomParams(adapter);
            } catch (Throwable e) {
//                CrashUtil.getSingleton().saveException(e);
                DeveloperLog.LogD("AdapterUtil getAdns : ", e);
            }
        }
        return jsonArray;
    }

    /**
     * Gets adapter map.
     *
     * @return the adapter map
     */
    public static SparseArray<CustomAdsAdapter> getAdapterMap() {
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
     * @param type        the type
     * @param mediationId the mediationId
     * @return the adapter path with type
     */
    public static String getAdapterPathWithType(int type, int mediationId) {
        return getMediationPath(mediationId).concat(getAdType(type));
    }

    private static AdNetwork getAdNetWork(CustomAdsAdapter adapter) {
        if (adapter != null) {
            return new AdNetwork(adapter.getAdNetworkId(), adapter.getAdapterVersion(), adapter.getMediationVersion());
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
        String path = getMediationPath(mediationType).concat(ADAPTER);
        DeveloperLog.LogD("adapter path is : " + path);
        return path;
    }

    private static String getMediationPath(int mediationType) {
        String path = "";
        switch (mediationType) {
            case MediationInfo.MEDIATION_ID_1:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_1));
                break;
            case MediationInfo.MEDIATION_ID_2:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_2));
                break;
            case MediationInfo.MEDIATION_ID_3:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_3));
                break;
            case MediationInfo.MEDIATION_ID_4:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_4));
                break;
            case MediationInfo.MEDIATION_ID_5:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_5));
                break;
            case MediationInfo.MEDIATION_ID_6:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_6));
                break;
            case MediationInfo.MEDIATION_ID_7:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_7));
                break;
            case MediationInfo.MEDIATION_ID_8:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_8));
                break;
            case MediationInfo.MEDIATION_ID_9:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_9));
                break;
            case MediationInfo.MEDIATION_ID_11:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_11));
                break;
            case MediationInfo.MEDIATION_ID_12:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_12));
                break;
            case MediationInfo.MEDIATION_ID_13:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_13));
                break;
            case MediationInfo.MEDIATION_ID_14:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_14));
                break;
            case MediationInfo.MEDIATION_ID_15:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_15));
                break;
            case MediationInfo.MEDIATION_ID_17:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_17));
                break;
            case MediationInfo.MEDIATION_ID_19:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_19));
                break;
            case MediationInfo.MEDIATION_ID_20:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_20));
                break;
            case MediationInfo.MEDIATION_ID_21:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_21));
                break;
            case MediationInfo.MEDIATION_ID_22:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_22));
                break;
            case MediationInfo.MEDIATION_ID_23:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_23));
                break;
            case MediationInfo.MEDIATION_ID_30:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_30));
                break;
            default:
                break;
        }
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
            case CommonConstants.BANNER:
                adType = CommonConstants.ADTYPE_BANNER;
                break;
            case CommonConstants.NATIVE:
                adType = CommonConstants.ADTYPE_NATIVE;
                break;
            case CommonConstants.SPLASH:
                adType = CommonConstants.ADTYPE_SPLASH;
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
