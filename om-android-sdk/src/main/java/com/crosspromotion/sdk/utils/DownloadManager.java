package com.crosspromotion.sdk.utils;

import android.text.TextUtils;

import com.openmediation.sdk.utils.AdtUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.WorkExecutor;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DownloadManager {

    private ConcurrentLinkedQueue<String> mActiveTask;
    private ConcurrentMap<String, List<OnResDownloaded>> mPendingTask;

    private static final class DmHolder {
        private static final DownloadManager INSTANCE = new DownloadManager();
    }

    private DownloadManager() {
        mActiveTask = new ConcurrentLinkedQueue<>();
        mPendingTask = new ConcurrentHashMap<>();
    }

    public static DownloadManager getInstance() {
        return DmHolder.INSTANCE;
    }

    public synchronized void downloadFile(final String url, final OnResDownloaded listener) {
        if (TextUtils.isEmpty(url)) {
            if (listener != null) {
                listener.onCompleted(url, null);
            }
            return;
        }
        if (Cache.existCache(AdtUtil.getApplication(), url)) {
            if (listener != null) {
                listener.onCompleted(url, Cache.getCacheFile(AdtUtil.getApplication(), url, null));
            }
            return;
        }
        WorkExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (DownloadManager.class) {
                        addToPendingTask(url, listener);
                        DeveloperLog.LogD("DownloadManager downloadFile addToPendingTask url is  = " + url);
                        if (mActiveTask.contains(url)) {
                            DeveloperLog.LogD("DownloadManager downloadFile mActiveTask.contains(url) ");
                            return;
                        }
                        mActiveTask.add(url);
                    }
                    File file = ResDownloader.downloadFile(url);
                    synchronized (DownloadManager.class) {
                        callbackPendingTaskFinished(url, file);
                        mActiveTask.remove(url);
                        DeveloperLog.LogD("DownloadManager downloadFile callbackPendingTaskFinished url is  = " + url);
                    }
                } catch (Exception e) {
                    DeveloperLog.LogE("DownloadManager downloadFile exception: " + e);
                    synchronized (DownloadManager.class) {
                        callbackPendingTaskFinished(url, null);
                        mActiveTask.remove(url);
                    }
                }
            }
        });
    }


    private void addToPendingTask(String url, OnResDownloaded listener) {
        synchronized (DownloadManager.class) {
            List<OnResDownloaded> onResDownloadedList = mPendingTask.get(url);
            if (onResDownloadedList == null) {
                onResDownloadedList = new CopyOnWriteArrayList<>();
            }
            onResDownloadedList.add(listener);
            DeveloperLog.LogD("DownloadManager downloadFile addToPendingTask onResDownloadedList.size()  = " + onResDownloadedList.size());
            mPendingTask.put(url, onResDownloadedList);
        }
    }

    private void callbackPendingTaskFinished(String url, File file) {
        synchronized (DownloadManager.class) {
            List<OnResDownloaded> onResDownloadedList = mPendingTask.get(url);
            if (onResDownloadedList != null && !onResDownloadedList.isEmpty()) {
                for (OnResDownloaded onResDownloaded : onResDownloadedList) {
                    if (onResDownloaded != null) {
                        onResDownloaded.onCompleted(url, file);
                    }
                }
                mPendingTask.remove(url);
            }
        }
    }

    public interface OnResDownloaded {
        void onCompleted(String url, File file);
    }
}
