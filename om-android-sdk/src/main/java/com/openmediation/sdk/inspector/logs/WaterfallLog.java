/*
 * // Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * // Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.inspector.logs;

import com.openmediation.sdk.inspector.LogConstants;
import com.openmediation.sdk.utils.model.BaseInstance;

import java.util.ArrayList;
import java.util.List;

public class WaterfallLog extends BaseLog {
    private String mediationRuleName;
    private int mediationRuleId;
    private List<InstanceLog> instanceLogs;
    private List<BaseInstance> unloadInstance;

    public WaterfallLog() {
        super(LogConstants.LOG_TAG_WATERFALL);
        instanceLogs = new ArrayList<>();
    }

    public void addInsLog(InstanceLog instanceLog) {
        if (!instanceLogs.contains(instanceLog)) {
            instanceLogs.add(instanceLog);
        }
    }

    public List<InstanceLog> getInstanceLogs() {
        return instanceLogs;
    }

    public void setMediationRuleId(int mediationRuleId) {
        this.mediationRuleId = mediationRuleId;
    }

    public int getMediationRuleId() {
        return mediationRuleId;
    }

    public void setMediationRuleName(String mediationRuleName) {
        this.mediationRuleName = mediationRuleName;
    }

    public String getMediationRuleName() {
        return mediationRuleName;
    }

    public void setUnloadInstance(List<BaseInstance> unloadInstance) {
        this.unloadInstance = unloadInstance;
    }

    public void removeFromUnloadInstance(BaseInstance instance) {
        if (unloadInstance != null) {
            unloadInstance.remove(instance);
        }
    }

    public List<BaseInstance> getUnloadInstance() {
        return unloadInstance;
    }
}
