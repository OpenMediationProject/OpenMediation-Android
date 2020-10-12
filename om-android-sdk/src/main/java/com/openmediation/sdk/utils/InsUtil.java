// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils;

import android.text.TextUtils;

import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.Instance;
import com.openmediation.sdk.utils.model.InstanceLoadStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The type Ins util.
 */
public class InsUtil {

    /**
     * Gets ins with status.
     *
     * @param instances the instances
     * @param states    the states
     * @return the ins with status
     */
    public static synchronized List<Instance> getInsWithStatus(List<Instance> instances, Instance.MEDIATION_STATE... states) {
        if (instances == null) {
            return Collections.emptyList();
        }

        List<Instance> instanceList = new ArrayList<>();
        for (Instance in : instances) {
            for (Instance.MEDIATION_STATE state : states) {
                if (in.getMediationState() == state) {
                    instanceList.add(in);
                }
            }
        }
        return instanceList;
    }

    public static synchronized List<Integer> getInsIdWithStatus(List<Instance> instances, Instance.MEDIATION_STATE... states) {
        if (instances == null) {
            return Collections.emptyList();
        }

        List<Integer> insIdList = new ArrayList<>();
        for (Instance in : instances) {
            for (Instance.MEDIATION_STATE state : states) {
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
    public static int instanceCount(List<Instance> instances, Instance.MEDIATION_STATE... states) {
        return getInsWithStatus(instances, states).size();
    }

    /**
     * Adds last instances not in the new instances to the end of the new
     *
     * @param lastIns last available instances
     * @param newIns  new instances
     */
    public static void reOrderIns(List<Instance> lastIns, List<Instance> newIns) {
        for (Instance ins : lastIns) {
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
    public static void resetInsStateOnClResponse(List<Instance> instances) {
        if (instances.isEmpty()) {
            return;
        }

        for (Instance in : instances) {
            Instance.MEDIATION_STATE state = in.getMediationState();
            if (state == Instance.MEDIATION_STATE.INIT_FAILED) {
                in.setMediationState(Instance.MEDIATION_STATE.NOT_INITIATED);
            } else if (state == Instance.MEDIATION_STATE.LOAD_FAILED ||
                    state == Instance.MEDIATION_STATE.CAPPED) {
                in.setMediationState(Instance.MEDIATION_STATE.NOT_AVAILABLE);
            } else if (state == Instance.MEDIATION_STATE.NOT_AVAILABLE) {
                in.setObject(null);
                in.setStart(0);
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
    public static BaseInstance getInsById(BaseInstance[] instances, String instanceId) {
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
    public static Instance getInsById(CopyOnWriteArrayList<Instance> instances, int instanceId) {
        if (instances == null || instances.isEmpty()) {
            return null;
        }

        for (Instance ins : instances) {
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
}
