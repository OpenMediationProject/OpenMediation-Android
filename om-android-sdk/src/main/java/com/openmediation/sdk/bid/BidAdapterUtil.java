// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.bid;

import android.util.SparseArray;

import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.utils.AdapterUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.crash.CrashUtil;

final class BidAdapterUtil extends AdapterUtil {
    private static final String BID_ADAPTER = "BidAdapter";

    private static SparseArray<BidAdapter> mBidAdapters = new SparseArray<>();
    private static SparseArray<String> mBidAdapterPaths;

    static {
        mBidAdapterPaths = new SparseArray<>();
        mBidAdapterPaths.put(MediationInfo.MEDIATION_ID_1, getBidAdapterPath(MediationInfo.MEDIATION_ID_1));
        mBidAdapterPaths.put(MediationInfo.MEDIATION_ID_3, getBidAdapterPath(MediationInfo.MEDIATION_ID_3));
        mBidAdapterPaths.put(MediationInfo.MEDIATION_ID_5, getBidAdapterPath(MediationInfo.MEDIATION_ID_5));
        mBidAdapterPaths.put(MediationInfo.MEDIATION_ID_14, getBidAdapterPath(MediationInfo.MEDIATION_ID_14));
        mBidAdapterPaths.put(MediationInfo.MEDIATION_ID_17, getBidAdapterPath(MediationInfo.MEDIATION_ID_17));
        mBidAdapterPaths.put(MediationInfo.MEDIATION_ID_19, getBidAdapterPath(MediationInfo.MEDIATION_ID_19));
        mBidAdapterPaths.put(MediationInfo.MEDIATION_ID_23, getBidAdapterPath(MediationInfo.MEDIATION_ID_23));
    }

    static BidAdapter getBidAdapter(int mediationId) {
        try {
            if (mBidAdapters == null) {
                mBidAdapters = new SparseArray<>();
            }

            if (mBidAdapters.get(mediationId) != null) {
                return mBidAdapters.get(mediationId);
            } else {
                BidAdapter bidAdapter = createAdapter(BidAdapter.class, mBidAdapterPaths.get(mediationId));
                mBidAdapters.put(mediationId, bidAdapter);
                return bidAdapter;
            }
        } catch (Throwable e) {
//            CrashUtil.getSingleton().saveException(e);
            DeveloperLog.LogD("AdapterUtil getBidAdapter mediationId : " + mediationId, e);
        }
        return null;
    }

    static boolean hasBidAdapter(int mediationId) {
        return mBidAdapters != null && mBidAdapters.get(mediationId) != null;
    }

    private static String getBidAdapterPath(int mediation) {
        String path = "";
        switch (mediation) {
            case MediationInfo.MEDIATION_ID_1:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_1)).concat(BID_ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_3:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_3)).concat(BID_ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_5:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_5)).concat(BID_ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_14:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_14)).concat(BID_ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_17:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_17)).concat(BID_ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_19:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_19)).concat(BID_ADAPTER);
                break;
            case MediationInfo.MEDIATION_ID_23:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_23)).concat(BID_ADAPTER);
                break;
            default:
                break;
        }
        DeveloperLog.LogD("adapter path is : " + path);
        return path;
    }
}
