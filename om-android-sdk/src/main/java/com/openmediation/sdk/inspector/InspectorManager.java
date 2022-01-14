/*
 * // Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * // Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.inspector;

import android.text.TextUtils;

import com.openmediation.sdk.core.OmManager;
import com.openmediation.sdk.inspector.logs.InitLog;
import com.openmediation.sdk.inspector.logs.InventoryLog;
import com.openmediation.sdk.inspector.logs.PlacementLog;
import com.openmediation.sdk.inspector.logs.SettingsLog;
import com.openmediation.sdk.inspector.logs.WaterfallLog;
import com.openmediation.sdk.utils.model.BaseInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InspectorManager {

    private List<InitLog> mInitLogs;
    private List<SettingsLog> mSettingsLogs;
    private ConcurrentMap<String, List<WaterfallLog>> mWfLogs;
    private ConcurrentMap<String, List<InventoryLog>> mInventoryLogs;
    private InspectorNotifyListener mListener;
    private boolean enable = false;

    private static final class ImHolder {
        private static final InspectorManager INSTANCE = new InspectorManager();
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isEnable() {
        return enable;
    }

    private InspectorManager() {
        mInitLogs = new ArrayList<>();
        mSettingsLogs = new ArrayList<>();
        mWfLogs = new ConcurrentHashMap<>();
        mInventoryLogs = new ConcurrentHashMap<>();
    }

    public static InspectorManager getInstance() {
        return ImHolder.INSTANCE;
    }

    public void setInspectorNotifyListener(InspectorNotifyListener listener) {
        mListener = listener;
    }

    public void notifyWaterfallChanged(String placementId, WaterfallLog log) {
        if (enable && mListener != null) {
            mListener.notifyWaterfallChanged(placementId, log);
        }
    }

    public PlacementLog getPlacementLog(String placementId) {
        if (!enable) {
            return null;
        }
        return buildPlacementLog(placementId);
    }

    public void addInitLog(InitLog initLog) {
        if (initLog != null && initLog.getEventTag() == LogConstants.INIT_FAILED) {
            mInitLogs.add(initLog);
            return;
        }
        if (!enable) {
            return;
        }
        mInitLogs.add(initLog);
    }

    public void addSettingsLog(SettingsLog settingsLog) {
//        if (!enable) {
//            return;
//        }
        mSettingsLogs.add(settingsLog);
    }

    public void addWaterfallLog(String placementId, WaterfallLog waterfallLog) {
        if (!enable) {
            return;
        }
        List<WaterfallLog> waterfallLogs = mWfLogs.get(placementId);
        if (waterfallLogs == null) {
            waterfallLogs = new ArrayList<>();
        }
        if (waterfallLogs.contains(waterfallLog)) {
            return;
        }
        waterfallLogs.add(waterfallLog);
        mWfLogs.put(placementId, waterfallLogs);
    }

    public void addInventoryLog(boolean isInventory, String placementId, InventoryLog inventoryLog) {
        if (!isInventory) {
            return;
        }
        if (!enable) {
            return;
        }
        List<InventoryLog> inventoryLogs = mInventoryLogs.get(placementId);
        if (inventoryLogs == null) {
            inventoryLogs = new ArrayList<>();
        }
        inventoryLog.setInventorySize(getInventorySize(placementId));
        List<BaseInstance> instances = getAvailableInstance(placementId);
        if (instances != null && !instances.isEmpty()) {
            inventoryLog.setAvailableSize(instances.size());
        }
        inventoryLogs.add(inventoryLog);
        mInventoryLogs.put(placementId, inventoryLogs);
    }

    public int getInventorySize(String placementId) {
        return OmManager.getInstance().getInventorySize(placementId);
    }

    public List<BaseInstance> getAvailableInstance(String placementId) {
        return OmManager.getInstance().getAvailableInstance(placementId);
    }

    public int getIntervalTime(String placementId) {
        return OmManager.getInstance().getIntervalTime(placementId);
    }

    public InitLog buildInitLog(int eventId, String detail) {
        InitLog initLog = new InitLog();
        initLog.setEventTag(eventId);
        if (!TextUtils.isEmpty(detail)) {
            initLog.setDetail(detail);
        }
        return initLog;
    }

    private PlacementLog buildPlacementLog(String placementId) {
        PlacementLog placementLog = new PlacementLog(placementId);
        if (!mInitLogs.isEmpty()) {
            for (InitLog initLog : mInitLogs) {
                placementLog.addPlacementLog(initLog);
            }
        }
        if (!mSettingsLogs.isEmpty()) {
            for (SettingsLog settingsLog : mSettingsLogs) {
                placementLog.addPlacementLog(settingsLog);
            }
        }
        List<WaterfallLog> waterfallLogs = mWfLogs.get(placementId);
        if (waterfallLogs != null && !waterfallLogs.isEmpty()) {
            for (WaterfallLog waterfallLog : waterfallLogs) {
                placementLog.addPlacementLog(waterfallLog);
            }
        }
        List<InventoryLog> inventoryLogs = mInventoryLogs.get(placementId);
        if (inventoryLogs != null && !inventoryLogs.isEmpty()) {
            for (InventoryLog inventoryLog : inventoryLogs) {
                placementLog.addPlacementLog(inventoryLog);
            }
        }
        placementLog.sort();
        return placementLog;
    }
}
