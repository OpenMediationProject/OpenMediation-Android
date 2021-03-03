// Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.model;

import com.openmediation.sdk.utils.DeveloperLog;

import org.json.JSONObject;

public class MediationRule {
    // Rule ID
    private final int id;
    // Rule Name
    private final String name;
    // Rule Type, 0:Auto,1:Manual
    private final int type;
    // Rule Priority, Only available when type is Manual
    private final int priority;

    private MediationRule(JSONObject jsonObject) {
        this.id = jsonObject.optInt("id", -1);
        this.name = jsonObject.optString("n");
        this.type = jsonObject.optInt("t", -1);
        this.priority = jsonObject.optInt("i", -1);
    }

    public static MediationRule create(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        try {
            return new MediationRule(jsonObject);
        } catch (Exception e) {
            DeveloperLog.LogE(e.toString());
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public String toString() {
        return "MediationRule{" +
                "id=" + id +
                ", name=" + name +
                ", type=" + type +
                ", priority=" + priority +
                '}';
    }
}