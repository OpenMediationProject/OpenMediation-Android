/*
 * // Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * // Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.inspector.logs;

import com.openmediation.sdk.inspector.LogConstants;

import java.util.HashMap;
import java.util.Map;

public class SettingsLog extends BaseLog {
    private Map<String, Object> tags = new HashMap<>();
    private String userId;

    public SettingsLog() {
        super(LogConstants.LOG_TAG_SETTINGS);
    }

    public void setTags(Map<String, Object> customTags) {
        if (customTags != null) {
            for (Map.Entry<String, Object> entry : customTags.entrySet()) {
                this.tags.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public Map<String, Object> getTags() {
        return tags;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
