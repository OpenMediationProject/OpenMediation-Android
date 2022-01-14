/*
 * // Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * // Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.inspector;

import com.openmediation.sdk.inspector.logs.WaterfallLog;

public interface InspectorNotifyListener {
    void notifyWaterfallChanged(String placementId, WaterfallLog log);
}
