// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk;

import com.openmediation.sdk.utils.error.Error;

/**
 * SDK init callback, to notify about SDK init results
 */
public interface InitCallback {
    /**
     * called upon SDK init success
     */
    void onSuccess();

    /**
     * called upon SDK init failure
     *
     * @param result failure reason
     */
    void onError(Error result);
}
