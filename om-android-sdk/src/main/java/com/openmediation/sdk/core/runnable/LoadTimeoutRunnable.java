// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.runnable;

import com.openmediation.sdk.utils.HandlerUtil;

/**
 * Instance's ads load timeout runnable, when timeout occurs, call{@link OnLoadTimeoutListener} to notify Instance
 *
 * 
 */
public class LoadTimeoutRunnable implements Runnable {

    private OnLoadTimeoutListener mListener;

    public LoadTimeoutRunnable() {
    }

    public void setTimeoutListener(OnLoadTimeoutListener listener) {
        mListener = listener;
    }

    @Override
    public void run() {
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.onLoadTimeout();
                }
            }
        });
    }

    /**
     * Timeout interface
     */
    public interface OnLoadTimeoutListener {
        /**
         * called when instance's loading times out
         */
        void onLoadTimeout();
    }
}
