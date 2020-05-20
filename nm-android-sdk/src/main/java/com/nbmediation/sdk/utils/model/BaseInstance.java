// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.utils.model;

import android.text.TextUtils;

import com.nbmediation.sdk.mediation.CustomAdsAdapter;
import com.nbmediation.sdk.utils.AdRateUtil;
import com.nbmediation.sdk.utils.AdtUtil;
import com.nbmediation.sdk.utils.DensityUtil;
import com.nbmediation.sdk.utils.DeveloperLog;
import com.nbmediation.sdk.utils.JsonUtil;
import com.nbmediation.sdk.utils.PlacementUtils;
import com.nbmediation.sdk.utils.error.ErrorCode;
import com.nbmediation.sdk.utils.event.EventId;
import com.nbmediation.sdk.utils.event.EventUploadManager;

import org.json.JSONObject;

public class BaseInstance extends Frequency {
    //Instances Id
    protected int id;

    //Mediation Id
    protected int mediationId;
    //placement key
    protected String key;
    //placement template path
    private String path;
    //group index
    private int grpIndex;
    //own index
    protected int index;
    //is 1st in the group
    private boolean isFirst;

    //data for instance storage
    private Object object;

    private long start;

    private String appKey;

    private int hb;

    private int hbt;
    private BID_STATE bidState;
    private int wfAbt;

    protected long mInitStart;
    protected long mLoadStart;
    protected long mShowStart;

    protected String mPlacementId;

    protected CustomAdsAdapter mAdapter;

    public BaseInstance() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getMediationId() {
        return mediationId;
    }

    public void setMediationId(int mediationId) {
        this.mediationId = mediationId;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setGrpIndex(int grpIndex) {
        this.grpIndex = grpIndex;
    }

    public int getGrpIndex() {
        return grpIndex;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Object getObject() {
        return object;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getStart() {
        return start;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setPlacementId(String placementId) {
        mPlacementId = placementId;
    }

    public String getPlacementId() {
        return mPlacementId;
    }

    public void setHb(int hb) {
        this.hb = hb;
    }

    public int getHb() {
        return hb;
    }

    public void setHbt(int hbt) {
        this.hbt = hbt;
    }

    public int getHbt() {
        return hbt;
    }

    public void setBidState(BID_STATE bidState) {
        this.bidState = bidState;
    }

    public BID_STATE getBidState() {
        return bidState;
    }

    public void setAdapter(CustomAdsAdapter adapter) {
        mAdapter = adapter;
    }

    public CustomAdsAdapter getAdapter() {
        return mAdapter;
    }

    public void setWfAbt(int wfAbt) {
        this.wfAbt = wfAbt;
    }

    public int getWfAbt() {
        return wfAbt;
    }

    @Override
    public String toString() {
        return "Ins{" +
                "id=" + id +
                ", index=" + index +
                ", pid=" + mPlacementId +
                ", mId=" + mediationId +
                '}';
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mediationId;
        result = prime * result + (TextUtils.isEmpty(key) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        } else if (this == obj) {
            return true;
        }
        BaseInstance other = (BaseInstance) obj;
        return TextUtils.equals(key, other.key) && id == other.id;
    }

    public JSONObject buildReportData() {
        try {
            JSONObject jsonObject = new JSONObject();
            JsonUtil.put(jsonObject, "pid", mPlacementId);
            JsonUtil.put(jsonObject, "iid", id);
            JsonUtil.put(jsonObject, "mid", mediationId);
            if (mAdapter != null) {
                JsonUtil.put(jsonObject, "adapterv", mAdapter.getAdapterVersion());
                JsonUtil.put(jsonObject, "msdkv", mAdapter.getMediationVersion());
            }
            JsonUtil.put(jsonObject, "priority", index);
            Placement placement = PlacementUtils.getPlacement(mPlacementId);
            if (placement != null) {
                JsonUtil.put(jsonObject, "cs", placement.getCs());
            }
            JsonUtil.put(jsonObject, "abt", wfAbt);
            return jsonObject;
        } catch (Exception e) {
            DeveloperLog.LogD("buildReportData exception : ", e);
        }
        return null;
    }

    public JSONObject buildReportDataWithScene(Scene scene) {
        JSONObject jsonObject = buildReportData();
        JsonUtil.put(jsonObject, "scene", scene != null ? scene.getId() : 0);
        JsonUtil.put(jsonObject, "ot", DensityUtil.getDirection(AdtUtil.getApplication()));
        return jsonObject;
    }

    public void reportInsLoad() {
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_LOAD, buildReportData());
    }

    public void reportInsDestroyed() {
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_DESTROY, buildReportData());
    }

    public void onInsLoadFailed(String error) {
        JSONObject data = buildReportData();
        JsonUtil.put(data, "msg", error);
        if (mLoadStart > 0) {
            int dur = (int) (System.currentTimeMillis() - mLoadStart) / 1000;
            JsonUtil.put(data, "duration", dur);
        }
        if (!TextUtils.isEmpty(error) && error.contains(ErrorCode.ERROR_TIMEOUT)) {
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_LOAD_TIMEOUT, data);
        } else {
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_LOAD_ERROR, data);
        }
    }

    public void onInsShow(Scene scene) {
        mShowStart = System.currentTimeMillis();
        AdRateUtil.onInstancesShowed(mPlacementId, key);
        if (scene != null) {
            AdRateUtil.onSceneShowed(mPlacementId, scene);
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_SHOW, buildReportDataWithScene(scene));
    }

    public void onInsClosed(Scene scene) {
        JSONObject data = buildReportDataWithScene(scene);
        if (mShowStart > 0) {
            int dur = (int) (System.currentTimeMillis() - mShowStart) / 1000;
            JsonUtil.put(data, "duration", dur);
            mShowStart = 0;
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_CLOSED, data);
    }

    public void onInsClick(Scene scene) {
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_CLICKED, buildReportDataWithScene(scene));
    }

    public enum BID_STATE {
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
        BID_SUCCESS(3),
        /**
         * set after initialization starts
         */
        INIT_PENDING(4),
        /**
         * set after load starts
         */
        BID_PENDING(5),

        /**
         * set after load fails
         */
        BID_FAILED(6),

        NOT_BIDDING(7);

        private int mValue;

        BID_STATE(int value) {
            this.mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }
    }
}
