package com.nbmediation.sdk.utils;

import android.util.SparseArray;

import java.lang.ref.WeakReference;

public class ApplicationCache {

    private static SparseArray<WeakReference<Object>> cache = new SparseArray<>();

    public static Object get(int requestId) {
        WeakReference<Object> ref = cache.get(requestId);
        if (ref == null || ref.get() == null) {
            AdLog.getSingleton().LogE("ApplicationCache::no item found");
            return null;
        } else {
            return ref.get();
        }
    }

    public synchronized static void put(int key, Object params) {
        cache.put(key, new WeakReference<>(params));
    }
}
