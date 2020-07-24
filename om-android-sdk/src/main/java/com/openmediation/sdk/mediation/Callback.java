// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mediation;

public abstract class Callback {

    protected abstract void onInstanceClick(String instanceKey, String instanceId);

    protected void onInsReady(String instanceKey, String instanceId, Object o) {
    }

    protected void onInsError(String instanceKey, String instanceId, String error) {
    }
    protected void onInsError(String instanceKey, String instanceId, AdapterError error) {
    }

    protected void onInsShowSuccess(String instanceKey, String instanceId) {
    }
    protected void onInsShowFailed(String instanceKey, String instanceId, AdapterError error) {
    }
    protected void onInsClosed(String instanceKey, String instanceId) {
    }
    protected void onInsTick(String instanceKey, String instanceId, long millisUntilFinished) {
    }
}
