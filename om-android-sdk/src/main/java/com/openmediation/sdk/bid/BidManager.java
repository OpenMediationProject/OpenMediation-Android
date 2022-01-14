// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.bid;

import android.content.Context;
import android.util.SparseArray;

import com.openmediation.sdk.banner.AdSize;
import com.openmediation.sdk.core.InsManager;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.utils.AdapterUtil;
import com.openmediation.sdk.utils.AdtUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.crash.CrashUtil;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.event.EventUploadManager;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.Configurations;
import com.openmediation.sdk.utils.model.Placement;
import com.openmediation.sdk.utils.request.network.AdRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BidManager {

    private final ConcurrentHashMap<String, List<BaseInstance>> mBidInstances;

    private static final class BidHolder {
        private static final BidManager INSTANCE = new BidManager();
    }

    private BidManager() {
        mBidInstances = new ConcurrentHashMap<>();
    }

    public static BidManager getInstance() {
        return BidHolder.INSTANCE;
    }

    public ConcurrentHashMap<String, List<BaseInstance>> getBidInstances() {
        return mBidInstances;
    }

    public void initBid(Context context, Configurations config) {
        if (config == null) {
            return;
        }

        Map<String, Placement> placementMap = config.getPls();
        if (placementMap == null || placementMap.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Placement> placementEntry : placementMap.entrySet()) {
            if (placementEntry == null) {
                continue;
            }
            List<BaseInstance> bidInstances = new ArrayList<>();
            SparseArray<BaseInstance> insMap = placementEntry.getValue().getInsMap();

            if (insMap == null || insMap.size() <= 0) {
                continue;
            }

            int size = insMap.size();
            for (int i = 0; i < size; i++) {
                BaseInstance instance = insMap.valueAt(i);
                if (instance == null) {
                    continue;
                }

                if (instance.getHb() == 1) {
                    CustomAdsAdapter adapter = AdapterUtil.getCustomAdsAdapter(instance.getMediationId());
                    if (adapter != null) {
                        try {
                            adapter.initBid(context, BidUtil.makeBidInitInfo(config, instance.getMediationId()));
                            bidInstances.add(instance);
                        } catch (Throwable throwable) {
                            DeveloperLog.LogE("initBid error: " + throwable.toString());
                            CrashUtil.getSingleton().saveException(throwable);
                        }
                    }
                }
            }
            if (bidInstances.size() > 0) {
                mBidInstances.put(placementEntry.getKey(), bidInstances);
            }
        }
    }

    public void c2sBid(Context context, List<BaseInstance> bidInstances, String placementId, String reqId, int adType, AdSize adSize, BidResponseCallback callback) {
        BidC2SManager.getInstance().bid(context, bidInstances, placementId, reqId, adType, adSize, callback);
    }

    public void s2sBid(Context context, String placementId, String reqId, int adType, BidResponseCallback callback) {
        BidS2SManager.getInstance().bid(context, placementId, reqId, adType, callback);
    }

    public void notifyWin(BaseInstance instance) {
        CustomAdsAdapter adapter = AdapterUtil.getCustomAdsAdapter(instance.getMediationId());
        if (adapter != null) {
            adapter.notifyWin(instance.getKey(), null);
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_BID_WIN, InsManager.buildReportData(instance));
        }

    }

    void notifyWin(String url, BaseInstance instance) {
        AdRequest.get().url(url).performRequest(AdtUtil.getInstance().getApplicationContext());
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_BID_WIN, InsManager.buildReportData(instance));
    }

    public void notifyLose(BaseInstance instance, int reason) {
        CustomAdsAdapter adapter = AdapterUtil.getCustomAdsAdapter(instance.getMediationId());
        if (adapter != null) {
            adapter.notifyLose(instance.getKey(), makeNotifyMap(reason));
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_BID_LOSE, InsManager.buildReportData(instance));
        }
    }

    private Map<String, Object> makeNotifyMap(int reason) {
        Map<String, Object> map = new HashMap<>();
        map.put(BidConstance.BID_NOTIFY_REASON, reason);
        return map;
    }

    void notifyLose(String url, BaseInstance instance) {
        AdRequest.get().url(url).performRequest(AdtUtil.getInstance().getApplicationContext());
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_BID_LOSE, InsManager.buildReportData(instance));
    }

}
