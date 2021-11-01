package com.openmediation.sdk.core;

import android.text.TextUtils;
import android.util.SparseArray;

import com.openmediation.sdk.ImpressionManager;
import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.core.runnable.LoadTimeoutRunnable;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.utils.AdRateUtil;
import com.openmediation.sdk.utils.AdtUtil;
import com.openmediation.sdk.utils.DensityUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.InsExecutor;
import com.openmediation.sdk.utils.JsonUtil;
import com.openmediation.sdk.utils.PlacementUtils;
import com.openmediation.sdk.utils.cache.DataCache;
import com.openmediation.sdk.utils.constant.KeyConstants;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.event.EventUploadManager;
import com.openmediation.sdk.utils.helper.WaterFallHelper;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.Configurations;
import com.openmediation.sdk.utils.model.InstanceLoadStatus;
import com.openmediation.sdk.utils.model.Mediation;
import com.openmediation.sdk.utils.model.MediationRule;
import com.openmediation.sdk.utils.model.Placement;
import com.openmediation.sdk.utils.model.Scene;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class InsManager {

    /**
     * Gets init data map.
     *
     * @return the init data map
     */
    public static Map<String, Object> getInitDataMap(BaseInstance insFields) {
        Map<String, Object> dataMap = new HashMap<>();
        if (insFields == null) {
            return dataMap;
        }
        dataMap.put("AppKey", insFields.getAppKey());
        dataMap.put("pid", insFields.getKey());
        if (insFields.getMediationId() == MediationInfo.MEDIATION_ID_7) {
            Configurations config = DataCache.getInstance().getFromMem(KeyConstants.KEY_CONFIGURATION, Configurations.class);
            if (config != null) {
                Map<String, Placement> placements = config.getPls();
                if (placements != null && !placements.isEmpty()) {
                    Set<String> keys = placements.keySet();
                    if (!keys.isEmpty()) {
                        SparseArray<Mediation> mediations = config.getMs();
                        Mediation mediation = mediations.get(insFields.getMediationId());
                        if (mediation != null) {
                            dataMap.put("zoneIds", buildZoneIds(keys, placements, mediation));
                        }
                    }
                }
            }
        }
        return dataMap;
    }

    public static JSONObject buildReportData(BaseInstance insFields) {
        try {
            if (insFields == null) {
                return null;
            }
            JSONObject jsonObject = new JSONObject();
            JsonUtil.put(jsonObject, "pid", insFields.getPlacementId());
            JsonUtil.put(jsonObject, "iid", insFields.getId());
            JsonUtil.put(jsonObject, "mid", insFields.getMediationId());
            CustomAdsAdapter adsAdapter = insFields.getAdapter();
            if (adsAdapter != null) {
                JsonUtil.put(jsonObject, "adapterv", adsAdapter.getAdapterVersion());
                JsonUtil.put(jsonObject, "msdkv", adsAdapter.getMediationVersion());
            }
            JsonUtil.put(jsonObject, "priority", insFields.getPriority());
            Placement placement = PlacementUtils.getPlacement(insFields.getPlacementId());
            if (placement != null) {
                JsonUtil.put(jsonObject, "cs", placement.getCs());
            }
            JsonUtil.put(jsonObject, "abt", insFields.getWfAbt());
            if (insFields.getHb() == 1) {
                JsonUtil.put(jsonObject, "bid", 1);
            }
            BidResponse bidResponse = insFields.getBidResponse();
            if (bidResponse != null) {
                JsonUtil.put(jsonObject, "price", bidResponse.getPrice());
                JsonUtil.put(jsonObject, "cur", bidResponse.getCur());
            }
            JsonUtil.put(jsonObject, "reqId", insFields.getReqId());
            if (insFields.getMediationRule() != null) {
                JsonUtil.put(jsonObject, "ruleId", insFields.getMediationRule().getId());
            }
            return jsonObject;
        } catch (Exception e) {
            DeveloperLog.LogD("buildReportData exception : ", e);
        }
        return null;
    }

    public static JSONObject buildReportDataWithScene(BaseInstance insFields, Scene scene) {
        JSONObject jsonObject = buildReportData(insFields);
        JsonUtil.put(jsonObject, "scene", scene != null ? scene.getId() : 0);
        JsonUtil.put(jsonObject, "ot", DensityUtil.getDirection(AdtUtil.getInstance().getApplicationContext()));
        return jsonObject;
    }

    public static void reportInsLoad(BaseInstance insFields, int eventId) {
        if (insFields == null) {
            return;
        }
        EventUploadManager.getInstance().uploadEvent(eventId, buildReportData(insFields));
    }

    public static void reportInsDestroyed(BaseInstance insFields) {
        if (insFields == null) {
            return;
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_DESTROY, buildReportData(insFields));
    }

    /**
     * takes care of instance load timeout
     *
     * @param listener the listener
     */
    public static void startInsLoadTimer(BaseInstance insFields, LoadTimeoutRunnable.OnLoadTimeoutListener listener) {
        if (insFields == null) {
            return;
        }
        if (insFields.getTimeoutRunnable() == null) {
            LoadTimeoutRunnable timeoutRunnable = new LoadTimeoutRunnable();
            timeoutRunnable.setTimeoutListener(listener);
            insFields.setTimeoutRunnable(timeoutRunnable);
        }
        Placement placement = PlacementUtils.getPlacement(insFields.getPlacementId());
        int timeout = placement != null ? placement.getPt() : 30;
        insFields.setScheduledFuture(InsExecutor.execute(insFields.getTimeoutRunnable(), timeout, TimeUnit.SECONDS));
    }

    /**
     * Cancels instance load timeout
     */
    private static void cancelInsLoadTimer(BaseInstance insFields) {
        if (insFields == null) {
            return;
        }
        if (insFields.getScheduledFuture() != null) {
            ScheduledFuture scheduledFuture = insFields.getScheduledFuture();
            if (!scheduledFuture.isCancelled()) {
                scheduledFuture.cancel(true);
            }
            insFields.setScheduledFuture(null);
        }

        if (insFields.getTimeoutRunnable() != null) {
            InsExecutor.remove(insFields.getTimeoutRunnable());
            insFields.setTimeoutRunnable(null);
        }
    }

    /**
     * On ins init start.
     */
    public static void onInsInitStart(BaseInstance insFields) {
        if (insFields == null) {
            return;
        }
        insFields.setInitStart(System.currentTimeMillis());
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_INIT_START, buildReportData(insFields));
    }

    /**
     * On ins init success.
     */
    public static void onInsInitSuccess(BaseInstance insFields) {
        if (insFields == null) {
            return;
        }
        insFields.setMediationState(BaseInstance.MEDIATION_STATE.INITIATED);
        JSONObject data = buildReportData(insFields);
        if (insFields.getInitStart() > 0) {
            int dur = (int) (System.currentTimeMillis() - insFields.getInitStart()) / 1000;
            JsonUtil.put(data, "duration", dur);
            insFields.setInitStart(0);
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_INIT_SUCCESS, data);
    }

    /**
     * On ins init failed.
     *
     * @param error the error
     */
    public static void onInsInitFailed(BaseInstance insFields, AdapterError error) {
        if (insFields == null) {
            return;
        }
        insFields.setMediationState(BaseInstance.MEDIATION_STATE.INIT_FAILED);
        JSONObject data = buildReportData(insFields);
        if (error != null) {
            JsonUtil.put(data, "code", error.getCode());
            JsonUtil.put(data, "msg", error.getMessage());
        }
        if (insFields.getInitStart() > 0) {
            int dur = (int) (System.currentTimeMillis() - insFields.getInitStart()) / 1000;
            JsonUtil.put(data, "duration", dur);
            insFields.setInitStart(0);
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_INIT_FAILED, data);
    }

    /**
     * On ins load success.
     */
    public static void onInsLoadSuccess(BaseInstance insFields, boolean reload) {
        if (insFields == null) {
            return;
        }
        insFields.setLastLoadStatus(null);
        cancelInsLoadTimer(insFields);
        insFields.setMediationState(BaseInstance.MEDIATION_STATE.AVAILABLE);
        JSONObject data = buildReportData(insFields);
        if (insFields.getLoadStart() > 0) {
            int dur = (int) (System.currentTimeMillis() - insFields.getLoadStart()) / 1000;
            JsonUtil.put(data, "duration", dur);
        }
        if (insFields.getHb() == 1) {
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_PAYLOAD_SUCCESS, data);
        } else {
            if (reload) {
                EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_RELOAD_SUCCESS, data);
            } else {
                EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_LOAD_SUCCESS, data);
            }
        }
    }

    public static void onInsLoadFailed(BaseInstance insFields, AdapterError error, boolean reload) {
        if (insFields == null) {
            return;
        }
        cancelInsLoadTimer(insFields);
        insFields.setMediationState(BaseInstance.MEDIATION_STATE.LOAD_FAILED);
        JSONObject data = buildReportData(insFields);
        if (error != null) {
            JsonUtil.put(data, "code", error.getCode());
            JsonUtil.put(data, "msg", error.getMessage());
        }
        int dur = 0;
        if (insFields.getLoadStart() > 0) {
            dur = (int) (System.currentTimeMillis() - insFields.getLoadStart()) / 1000;
            JsonUtil.put(data, "duration", dur);
        }
        if (insFields.getHb() == 1) {
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_PAYLOAD_FAILED, data);
        } else {
            if (error != null && error.getMessage().contains(ErrorCode.ERROR_TIMEOUT)) {
                EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_LOAD_TIMEOUT, data);
            } else {
                if (reload) {
                    EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_RELOAD_ERROR, data);
                } else {
                    EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_LOAD_ERROR, data);
                }
            }
        }
        setLoadStatus(insFields, dur, error);
    }

    private static void setLoadStatus(BaseInstance insFields, long duration, AdapterError error) {
        if (insFields == null) {
            return;
        }
        if (error != null && error.isLoadFailFromAdn()) {
            InstanceLoadStatus status = new InstanceLoadStatus();
            status.setIid(insFields.getId());
            status.setLts(insFields.getLoadStart());
            status.setDur(duration);
            status.setCode(error.getCode());
            status.setMsg(error.getMessage());
            insFields.setLastLoadStatus(status);
        } else {
            insFields.setLastLoadStatus(null);
        }
    }

    public static void onInsShow(BaseInstance insFields, Scene scene) {
        if (insFields == null) {
            return;
        }
        insFields.setShowStart(System.currentTimeMillis());
        AdRateUtil.onInstancesShowed(insFields.getPlacementId(), insFields.getKey());
        if (scene != null) {
            AdRateUtil.onSceneShowed(insFields.getPlacementId(), scene);
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_SHOW, buildReportDataWithScene(insFields, scene));
    }

    public static void onInsClosed(BaseInstance insFields, Scene scene) {
        if (insFields == null) {
            return;
        }
        insFields.setMediationState(BaseInstance.MEDIATION_STATE.NOT_AVAILABLE);

        JSONObject data = buildReportDataWithScene(insFields, scene);
        if (insFields.getShowStart() > 0) {
            int dur = (int) (System.currentTimeMillis() - insFields.getShowStart()) / 1000;
            JsonUtil.put(data, "duration", dur);
            insFields.setShowStart(0);
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_CLOSED, data);
        insFields.setBidResponse(null);
    }

    public static void onInsClick(BaseInstance insFields, Scene scene) {
        if (insFields == null) {
            return;
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_CLICKED, buildReportDataWithScene(insFields, scene));
    }

    public static void onInsShowSuccess(BaseInstance insFields, Scene scene) {
        if (insFields == null) {
            return;
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_SHOW_SUCCESS, buildReportDataWithScene(insFields, scene));

        ImpressionManager.onInsShowSuccess(insFields, scene);

        TaiChiManager.reportUserRevenue(insFields);
    }

    public static void onInsShowFailed(BaseInstance insFields, AdapterError error, Scene scene) {
        if (insFields == null) {
            return;
        }
        insFields.setMediationState(BaseInstance.MEDIATION_STATE.NOT_AVAILABLE);
        JSONObject data = buildReportDataWithScene(insFields, scene);
        if (error != null) {
            JsonUtil.put(data, "code", error.getCode());
            JsonUtil.put(data, "msg", error.getMessage());
        }
        if (insFields.getShowStart() > 0) {
            int dur = (int) (System.currentTimeMillis() - insFields.getShowStart()) / 1000;
            JsonUtil.put(data, "duration", dur);
            insFields.setShowStart(0);
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_SHOW_FAILED, data);
    }

    /**
     * Gets ins with status.
     *
     * @param instances the instances
     * @param states    the states
     * @return the ins with status
     */
    public static synchronized List<BaseInstance> getInsWithStatus(List<BaseInstance> instances, BaseInstance.MEDIATION_STATE... states) {
        if (instances == null) {
            return null;
        }

        List<BaseInstance> instanceList = new ArrayList<>();
        for (BaseInstance in : instances) {
            for (BaseInstance.MEDIATION_STATE state : states) {
                if (in.getMediationState() == state) {
                    instanceList.add(in);
                }
            }
        }
        return instanceList;
    }

    public static synchronized List<Integer> getInsIdWithStatus(List<BaseInstance> instances, BaseInstance.MEDIATION_STATE... states) {
        if (instances == null) {
            return Collections.emptyList();
        }

        List<Integer> insIdList = new ArrayList<>();
        for (BaseInstance in : instances) {
            for (BaseInstance.MEDIATION_STATE state : states) {
                if (in.getMediationState() == state) {
                    insIdList.add(in.getId());
                }
            }
        }
        return insIdList;
    }

    /**
     * Instance count int.
     *
     * @param instances the instances
     * @param states    the states
     * @return the int
     */
    public static int instanceCount(List<BaseInstance> instances, BaseInstance.MEDIATION_STATE... states) {
        return getInsWithStatus(instances, states).size();
    }

    /**
     * Adds last instances not in the new instances to the end of the new
     *
     * @param lastIns last available instances
     * @param newIns  new instances
     */
    public static void reOrderIns(List<BaseInstance> lastIns, List<BaseInstance> newIns) {
        for (BaseInstance ins : lastIns) {
            if (newIns.contains(ins)) {
                continue;
            }
            ins.setIndex(newIns.size() - 1);
            newIns.add(ins);
        }
    }


    /**
     * Resets instance's state when: init failed, load failed, Capped
     *
     * @param instances the instances
     */
    public static void resetInsStateOnClResponse(List<BaseInstance> instances) {
        if (instances.isEmpty()) {
            return;
        }

        for (BaseInstance in : instances) {
            BaseInstance.MEDIATION_STATE state = in.getMediationState();
            if (state == BaseInstance.MEDIATION_STATE.INIT_FAILED) {
                in.setMediationState(BaseInstance.MEDIATION_STATE.NOT_INITIATED);
            } else if (state == BaseInstance.MEDIATION_STATE.LOAD_FAILED ||
                    state == BaseInstance.MEDIATION_STATE.CAPPED) {
                in.setMediationState(BaseInstance.MEDIATION_STATE.NOT_AVAILABLE);
            } else if (state == BaseInstance.MEDIATION_STATE.NOT_AVAILABLE) {
                in.setObject(null);
            }
        }
    }

    /**
     * Gets ins by id.
     *
     * @param instances  the instances
     * @param instanceId the instance id
     * @return the ins by id
     */
    public static BaseInstance getInsById(List<BaseInstance> instances, String instanceId) {
        if (instances == null || TextUtils.isEmpty(instanceId)) {
            return null;
        }

        for (BaseInstance ins : instances) {
            if (ins == null) {
                continue;
            }
            //extra insId check in case id is the same as other ad networks'
            if (TextUtils.equals(instanceId, String.valueOf(ins.getId()))) {
                return ins;
            }
        }
        return null;
    }

    /**
     * Gets ins by id.
     *
     * @param instances  the instances
     * @param instanceId the instance id
     * @return the ins by id
     */
    public static BaseInstance getInsById(CopyOnWriteArrayList<BaseInstance> instances, int instanceId) {
        if (instances == null || instances.isEmpty()) {
            return null;
        }

        for (BaseInstance ins : instances) {
            if (ins == null) {
                continue;
            }
            //extra insId check in case id is the same as other ad networks'
            if (instanceId == ins.getId()) {
                return ins;
            }
        }
        return null;
    }

    public static List<InstanceLoadStatus> getInstanceLoadStatuses(List<? extends BaseInstance> instanceList) {
        List<InstanceLoadStatus> statusList = null;
        if (instanceList != null && !instanceList.isEmpty()) {
            statusList = new ArrayList<>();
            for (BaseInstance instance : instanceList) {
                if (instance == null || instance.getLastLoadStatus() == null) {
                    continue;
                }
                statusList.add(instance.getLastLoadStatus());
            }
        }
        return statusList;
    }

    public static CopyOnWriteArrayList<BaseInstance> getInstanceList(Placement placement) {
        if (placement == null || placement.getInsMap() == null || placement.getInsMap().size() == 0) {
            return null;
        }
        SparseArray<BaseInstance> insMap = placement.getInsMap();
        CopyOnWriteArrayList<BaseInstance> list = new CopyOnWriteArrayList<>();
        for (int i = 0; i < insMap.size(); i++) {
            BaseInstance instance = insMap.valueAt(i);
            if (instance instanceof BaseInstance) {
                list.add(instance);
            }
        }
        return list;
    }

    /**
     * @param instance instance
     * @return NetworkName
     */
    public static String getNetworkName(BaseInstance instance) {
        if (instance == null) {
            return null;
        }
        Configurations config = DataCache.getInstance().getFromMem(KeyConstants.KEY_CONFIGURATION, Configurations.class);
        if (config == null) {
            return null;
        }
        SparseArray<Mediation> configMs = config.getMs();
        if (configMs == null || configMs.size() == 0 || configMs.get(instance.getMediationId()) == null) {
            return null;
        }
        Mediation mediation = configMs.get(instance.getMediationId());
        return mediation.getNetworkName();
    }

    /**
     * Gets ins by id.
     *
     * @param placement  the placement
     * @param instanceId the instance id
     * @return BaseInstance
     */
    public static BaseInstance getInsById(Placement placement, String instanceId) {
        if (TextUtils.isEmpty(instanceId)) {
            return null;
        }

        if (placement == null) {
            return null;
        }

        CopyOnWriteArrayList<BaseInstance> instanceList = getInstanceList(placement);
        if (instanceList == null || instanceList.isEmpty()) {
            return null;
        }
        for (BaseInstance ins : instanceList) {
            if (ins == null) {
                continue;
            }
            if (TextUtils.equals(instanceId, String.valueOf(ins.getId()))) {
                return ins;
            }
        }
        return null;
    }

    public static boolean isInstanceAvailable(BaseInstance instance) {
        return BaseInstance.MEDIATION_STATE.AVAILABLE == instance.getMediationState();
    }

    public static List<BaseInstance> sort(List<BaseInstance> wfInstances, List<BaseInstance> c2sInstances) {
        if (c2sInstances == null || c2sInstances.isEmpty()) {
            return wfInstances;
        }
        List<BaseInstance> list = new ArrayList<>();
        if (wfInstances == null || wfInstances.isEmpty()) {
            list.addAll(c2sInstances);
            Collections.sort(list);
            return list;
        }

        list.addAll(wfInstances);
        for (BaseInstance instance : c2sInstances) {
            int size = list.size();
            boolean hasInsert = false;
            for (int i = 0; i < size; i++) {
                if (instance.getRevenue() > list.get(i).getRevenue()) {
                    list.add(i, instance);
                    hasInsert = true;
                    break;
                }
            }
            if (!hasInsert) {
                list.add(instance);
            }
        }
        return list;
    }

    /**
     * Gets list ins result.
     *
     * @param clInfo    the cl info
     * @param placement the placement
     * @return the list ins result
     */
    public static List<BaseInstance> getC2SInstances(String reqId, JSONObject clInfo, Placement placement) {

        JSONArray insArray = clInfo.optJSONArray("c2s");
        if (insArray == null || insArray.length() <= 0) {
            return null;
        }

        SparseArray<BaseInstance> insMap = placement.getInsMap();
        if (insMap == null || insMap.size() <= 0) {
            return null;
        }
        boolean cacheAds = PlacementUtils.isCacheAdsType(placement.getT());
        MediationRule mediationRule = WaterFallHelper.getMediationRule(clInfo);
        int abt = clInfo.optInt("abt");
        List<BaseInstance> instancesList = new ArrayList<>();
        for (int i = 0; i < insArray.length(); i++) {
            int insId = insArray.optInt(i);
            BaseInstance instance = insMap.get(insId);
            if (instance != null) {
                instance.setWfAbt(abt);
                instance.setMediationRule(mediationRule);
                instance.setReqId(reqId);
                instance.setRevenue(0);
                instance.setPriority(0);
                instance.setRevenuePrecision(1);
                // clear bid response
                if (!cacheAds) {
                    instance.setBidResponse(null);
                }
                instancesList.add(instance);
            } else {
                reportNoInstance(reqId, placement, mediationRule, insId);
            }
        }
        return instancesList;
    }

    public static List<BaseInstance> getListInsResult(String reqId, JSONObject clInfo, Placement placement) {
        return getListInsResult(reqId, clInfo, placement, 0);
    }

    /**
     * Gets list ins result.
     *
     * @param clInfo    the cl info
     * @param placement the placement
     * @return the list ins result
     */
    public static List<BaseInstance> getListInsResult(String reqId, JSONObject clInfo, Placement placement, int bs) {

        // FixMe
        List<BaseInstance> test = WaterFallHelper.getTestInstanceMap().get(placement.getId());
        //for testing
        if (test != null && test.size() > 0) {
            if (bs > 0) {
                return splitInsByBs(test, bs);
            }
            return splitAbsIns(test);
        }

        JSONArray insArray = clInfo.optJSONArray("ins");
        if (insArray == null || insArray.length() <= 0) {
            return null;
        }

        SparseArray<BaseInstance> insMap = placement.getInsMap();
        if (insMap == null || insMap.size() <= 0) {
            return null;
        }

        MediationRule mediationRule = WaterFallHelper.getMediationRule(clInfo);
        int abt = clInfo.optInt("abt");

        boolean cacheAds = PlacementUtils.isCacheAdsType(placement.getT());
        List<BaseInstance> instancesList = new ArrayList<>();
        for (int i = 0; i < insArray.length(); i++) {
            JSONObject insObject = insArray.optJSONObject(i);
            BaseInstance instance = getInstance(reqId, placement, insObject, insMap, mediationRule, abt);
            if (instance != null) {
                // TODO
//                instance.setIndex(i);
                if (!cacheAds) {
                    instance.setBidResponse(null);
                }
                instancesList.add(instance);
            }
        }
        return instancesList;
    }

    public static List<BaseInstance> splitInsByBs(List<BaseInstance> origin, int bs) {
        if (origin == null) {
            return null;
        }
        int len = origin.size();
        int grpIndex = 0;
        for (int a = 0; a < len; a++) {
            BaseInstance i = origin.get(a);
            i.setIndex(a);

            //when index of instance >= group index, increase group index
            if (bs != 0) {
                if (a >= (grpIndex + 1) * bs) {
                    grpIndex++;
                }

                i.setGrpIndex(grpIndex);

                if (a % bs == 0) {
                    i.setFirst(true);
                }
            }
            i.setObject(null);
        }
        return origin;
    }

    private static BaseInstance getInstance(String reqId, Placement placement, JSONObject insObject,
                                            SparseArray<BaseInstance> insMap, MediationRule mediationRule, int abt) {
        if (insObject != null) {
            int insId = insObject.optInt("id");
            BaseInstance ins = insMap.get(insId);
            if (ins != null) {
                if (ins.getMediationState() != BaseInstance.MEDIATION_STATE.AVAILABLE) {
                    ins.setWfAbt(abt);
                    ins.setMediationRule(mediationRule);
                    ins.setReqId(reqId);
                    ins.setRevenue(insObject.optDouble("r", 0d));
                    ins.setPriority(insObject.optInt("i", -1));
                    ins.setRevenuePrecision(insObject.optInt("rp", -1));
                }
                return ins;
            }
            reportNoInstance(reqId, placement, mediationRule, insId);
        }
        return null;
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

    public static List<BaseInstance> splitAbsIns(List<BaseInstance> origin) {
        //shallow copy!!!
        int len = origin.size();
        for (int a = 0; a < len; a++) {
            BaseInstance i = origin.get(a);

            //resets instance's state if init failed or load failed
            BaseInstance.MEDIATION_STATE state = i.getMediationState();
            if (state == BaseInstance.MEDIATION_STATE.INIT_FAILED) {
                i.setMediationState(BaseInstance.MEDIATION_STATE.NOT_INITIATED);
            } else if (state == BaseInstance.MEDIATION_STATE.LOAD_FAILED) {
                i.setMediationState(BaseInstance.MEDIATION_STATE.NOT_AVAILABLE);
            }
            DeveloperLog.LogD("ins state : " + i.getMediationState());
            if (state != BaseInstance.MEDIATION_STATE.AVAILABLE) {
                i.setObject(null);
            }
        }
        return origin;
    }

    private static void reportNoInstance(String reqId, Placement placement, MediationRule mediationRule, int insId) {
        JSONObject jsonObject = PlacementUtils.placementEventParams(placement.getId());
        JsonUtil.put(jsonObject, "iid", insId);
        JsonUtil.put(jsonObject, "reqId", reqId);
        if (mediationRule != null) {
            JsonUtil.put(jsonObject, "ruleId", mediationRule.getId());
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_NOT_FOUND, jsonObject);
    }
}
