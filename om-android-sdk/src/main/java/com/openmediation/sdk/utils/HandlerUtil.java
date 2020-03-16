// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * The type Handler util.
 */
public class HandlerUtil {
    private HandlerUtil() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * The interface On receive message listener.
     */
    public interface OnReceiveMessageListener {
        /**
         * Handler message.
         *
         * @param msg the msg
         */
        void handlerMessage(Message msg);
    }

    /**
     * The type Handler holder.
     */
    public static class HandlerHolder extends Handler {
        /**
         * The M listener weak reference.
         */
        WeakReference<OnReceiveMessageListener> mListenerWeakReference;

        /**
         * Attention: implement this in Activity or classes within Activity. Do NOT use anonymous class for it may be GC'ed.
         *
         * @param listener the listener
         */
        public HandlerHolder(OnReceiveMessageListener listener) {
            this(listener, Looper.myLooper());
        }

        /**
         * Instantiates a new Handler holder.
         *
         * @param listener the listener
         * @param looper   the looper
         */
        public HandlerHolder(OnReceiveMessageListener listener, Looper looper) {
            super(looper);
            if (listener != null) {
                mListenerWeakReference = new WeakReference<>(listener);
            }
        }

        @Override
        public void handleMessage(Message msg) {
            if (mListenerWeakReference != null) {
                OnReceiveMessageListener listener = mListenerWeakReference.get();
                if (listener != null) {
                    listener.handlerMessage(msg);
                }
            }
        }
    }

    /**
     * Run on ui thread.
     *
     * @param runnable the runnable
     */
    public static void runOnUiThread(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            new HandlerHolder(null, Looper.getMainLooper()).postDelayed(runnable, 0);
        }
    }
}
