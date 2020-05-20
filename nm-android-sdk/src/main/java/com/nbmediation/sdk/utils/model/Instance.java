// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.utils.model;

import android.app.Activity;
import android.util.SparseArray;

import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.utils.InsExecutor;
import com.nbmediation.sdk.utils.JsonUtil;
import com.nbmediation.sdk.utils.PlacementUtils;
import com.nbmediation.sdk.utils.cache.DataCache;
import com.nbmediation.sdk.utils.constant.KeyConstants;
import com.nbmediation.sdk.utils.event.EventId;
import com.nbmediation.sdk.utils.event.EventUploadManager;
import com.nbmediation.sdk.core.runnable.LoadTimeoutRunnable;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * One Instance for one Adapter
 */
public class Instance extends BaseInstance {

    private Instance.MEDIATION_STATE mMediationState;
    private LoadTimeoutRunnable mTimeoutRunnable;
    private ScheduledFuture mScheduledFuture;


    /**
     * On resume.
     *
     * @param activity the activity
     */
    public void onResume(Activity activity) {
        if (mAdapter != null) {
            mAdapter.onResume(activity);
        }
    }

    /**
     * On pause.
     *
     * @param activity the activity
     */
    public void onPause(Activity activity) {
        if (mAdapter != null) {
            mAdapter.onPause(activity);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Sets mediation state.
     *
     * @param state the state
     */
    public void setMediationState(MEDIATION_STATE state) {
        mMediationState = state;
    }

    /**
     * Gets mediation state.
     *
     * @return the mediation state
     */
    public MEDIATION_STATE getMediationState() {
        return mMediationState;
    }

    /**
     * Is caped boolean.
     *
     * @return the boolean
     */
    public boolean isCaped() {
        return getMediationState() == MEDIATION_STATE.CAPPED;
    }

    /**
     * takes care of instance load timeout
     *
     * @param listener the listener
     */
    protected void startInsLoadTimer(LoadTimeoutRunnable.OnLoadTimeoutListener listener) {
        if (mTimeoutRunnable == null) {
            mTimeoutRunnable = new LoadTimeoutRunnable();
            mTimeoutRunnable.setTimeoutListener(listener);
        }
        Placement placement = PlacementUtils.getPlacement(mPlacementId);
        int timeout = placement != null ? placement.getPt() : 30;
        mScheduledFuture = InsExecutor.execute(mTimeoutRunnable, timeout, TimeUnit.SECONDS);
    }

    /**
     * Gets init data map.
     *
     * @return the init data map
     */
    protected Map<String, Object> getInitDataMap() {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("AppKey", getAppKey());
        dataMap.put("pid", key);
        if (getMediationId() == MediationInfo.MEDIATION_ID_7) {
            Configurations config = DataCache.getInstance().getFromMem(KeyConstants.KEY_CONFIGURATION, Configurations.class);
            if (config != null) {
                Map<String, Placement> placements = config.getPls();
                if (placements != null && !placements.isEmpty()) {
                    Set<String> keys = placements.keySet();
                    if (!keys.isEmpty()) {
                        SparseArray<Mediation> mediations = config.getMs();
                        Mediation mediation = mediations.get(getMediationId());
                        if (mediation != null) {
                            dataMap.put("zoneIds", buildZoneIds(keys, placements, mediation));
                        }
                    }
                }
            }
        }
        return dataMap;
    }

    /**
     * On ins init start.
     */
    protected void onInsInitStart() {
        mInitStart = System.currentTimeMillis();
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_INIT_START, buildReportData());
    }

    /**
     * On ins init success.
     */
    protected void onInsInitSuccess() {
        setMediationState(MEDIATION_STATE.INITIATED);
        JSONObject data = buildReportData();
        if (mInitStart > 0) {
            int dur = (int) (System.currentTimeMillis() - mInitStart) / 1000;
            JsonUtil.put(data, "duration", dur);
            mInitStart = 0;
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_INIT_SUCCESS, data);
    }

    /**
     * On ins init failed.
     *
     * @param error the error
     */
    protected void onInsInitFailed(String error) {
        setMediationState(MEDIATION_STATE.INIT_FAILED);
        JSONObject data = buildReportData();
        JsonUtil.put(data, "msg", error);
        if (mInitStart > 0) {
            int dur = (int) (System.currentTimeMillis() - mInitStart) / 1000;
            JsonUtil.put(data, "duration", dur);
            mInitStart = 0;
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_INIT_FAILED, data);
    }

    @Override
    public void onInsClosed(Scene scene) {
        setMediationState(MEDIATION_STATE.NOT_AVAILABLE);
        super.onInsClosed(scene);
    }

    /**
     * On ins load success.
     */
    protected void onInsLoadSuccess() {
        cancelInsLoadTimer();
        setMediationState(MEDIATION_STATE.AVAILABLE);
        JSONObject data = buildReportData();
        if (mLoadStart > 0) {
            int dur = (int) (System.currentTimeMillis() - mLoadStart) / 1000;
            JsonUtil.put(data, "duration", dur);
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_LOAD_SUCCESS, data);
    }

    @Override
    public void onInsLoadFailed(String error) {
        setMediationState(MEDIATION_STATE.LOAD_FAILED);
        cancelInsLoadTimer();
        super.onInsLoadFailed(error);
    }

    /**
     * On ins show success.
     *
     * @param scene the scene
     */
    protected void onInsShowSuccess(Scene scene) {
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_SHOW_SUCCESS, buildReportDataWithScene(scene));
    }

    /**
     * On ins show failed.
     *
     * @param error the error
     * @param scene the scene
     */
    protected void onInsShowFailed(String error, Scene scene) {
        setMediationState(MEDIATION_STATE.NOT_AVAILABLE);
        JSONObject data = buildReportDataWithScene(scene);
        JsonUtil.put(data, "msg", error);
        if (mShowStart > 0) {
            int dur = (int) (System.currentTimeMillis() - mShowStart) / 1000;
            JsonUtil.put(data, "duration", dur);
            mShowStart = 0;
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_SHOW_FAILED, data);
    }

    /**
     * Cancels instance load timeout
     */
    private void cancelInsLoadTimer() {
        if (mScheduledFuture != null) {
            if (!mScheduledFuture.isCancelled()) {
                mScheduledFuture.cancel(true);
            }
            mScheduledFuture = null;
        }

        if (mTimeoutRunnable != null) {
            InsExecutor.remove(mTimeoutRunnable);
            mTimeoutRunnable = null;
        }
    }

    private static List<String> buildZoneIds(Set<String> keys
            , Map<String, Placement> placements, Mediation mediation) {
        List<String> instanceKeys = new ArrayList<>();
        for (String key : keys) {
            Placement p = placements.get(key);
            if (p == null) {
                continue;
            }
            SparseArray<BaseInstance> instances = p.getInsMap();
            if (instances != null && instances.size() > 0) {
                int size = instances.size();
                for (int i = 0; i < size; i++) {
                    BaseInstance mp = instances.valueAt(i);
                    if (mp == null) {
                        continue;
                    }
                    //mp doesn't belong to the AdNetwork
                    if (mp.getMediationId() != mediation.getId()) {
                        continue;
                    }
                    instanceKeys.add(mp.getKey());
                }
            }
        }
        return instanceKeys;
    }

    /**
     * The enum Mediation state.
     */
    public enum MEDIATION_STATE {
        /**
         * mediation not yet initialized; sets instance's state to after SDK init is done
         */
        NOT_INITIATED(0),
        /**
         * set after initialization failure
         */
        INIT_FAILED(1),
        /**
         * set after initialization success
         */
        INITIATED(2),
        /**
         * set after load success
         */
        AVAILABLE(3),
        /**
         * set after load failure
         */
        NOT_AVAILABLE(4),

        /**
         * Capped per session mediation state.
         */
        CAPPED_PER_SESSION(5),
        /**
         * set after initialization starts
         */
        INIT_PENDING(6),
        /**
         * set after load starts
         */
        LOAD_PENDING(7),

        /**
         * set after load fails
         */
        LOAD_FAILED(8),
        /**
         * Capped per day mediation state.
         */
        CAPPED_PER_DAY(9),

        /**
         * set in the case of frequency control
         */
        CAPPED(10);

        private int mValue;

        MEDIATION_STATE(int value) {
            this.mValue = value;
        }

        /**
         * Gets value.
         *
         * @return the value
         */
        public int getValue() {
            return this.mValue;
        }
    }
}
