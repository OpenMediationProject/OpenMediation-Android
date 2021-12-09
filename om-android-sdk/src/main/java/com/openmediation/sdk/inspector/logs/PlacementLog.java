/*
 * // Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * // Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.inspector.logs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PlacementLog implements Comparator<BaseLog> {
    private String id;
    private List<BaseLog> logList;

    public PlacementLog(String id) {
        this.id = id;
        logList = new ArrayList<>();
    }

    public void addPlacementLog(BaseLog log) {
        logList.add(log);
    }

    public List<BaseLog> getLogList() {
        return logList;
    }

    public void sort() {
        Collections.sort(logList, this);
    }

    @Override
    public int compare(BaseLog o1, BaseLog o2) {
        long start1 = o1.getStart();
        long start2 = o2.getStart();
        if ((start2 - start1) > 0) {
            return 1;
        }
        return -1;
    }
}
