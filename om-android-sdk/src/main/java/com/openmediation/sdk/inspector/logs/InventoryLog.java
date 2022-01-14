/*
 * // Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * // Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.inspector.logs;

import com.openmediation.sdk.inspector.LogConstants;
import com.openmediation.sdk.utils.model.BaseInstance;

public class InventoryLog extends BaseLog {
    private BaseInstance instance;
    private double revenue;
    // inventorySize
    private int inventorySize;
    // availableSize
    private int availableSize;
    public InventoryLog() {
        super(LogConstants.LOG_TAG_INVENTORY);
    }

    public void setInstance(BaseInstance instance) {
        this.instance = instance;
        this.recordTime = System.currentTimeMillis();
        this.revenue = instance.getRevenue();
    }

    public int getInventorySize() {
        return inventorySize;
    }

    public void setInventorySize(int inventorySize) {
        this.inventorySize = inventorySize;
    }

    public int getAvailableSize() {
        return availableSize;
    }

    public void setAvailableSize(int availableSize) {
        this.availableSize = availableSize;
    }

    public double getRevenue() {
        return revenue;
    }

    public BaseInstance getInstance() {
        return instance;
    }
}
