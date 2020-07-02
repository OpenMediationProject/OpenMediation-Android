package com.cloudtech.shell.http;

import com.cloudtech.shell.utils.ThreadPoolProxy;
import com.cloudtech.shell.utils.YeLog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.zip.GZIPInputStream;

/**
 * Created by jiantao.tu on 2018/4/19.
 */
public class DownloadManager {

    private final static int timeOut = 10000;

    public static void downloadAsync(final String url, final File saveDir, final DownloadListener
            listener) {
        ThreadPoolProxy.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                download(url,saveDir,listener);
            }
        });

    }

    public static boolean download(final String url, final File saveDir){
        return download(url,saveDir,null);
    }

    public static boolean download(final String url, final File saveDir, final DownloadListener
        listener){
        RandomAccessFile accessFile = null;
        try {
            HttpURLConnection conn = HttpUtils.handleConnection(url, timeOut);
            InputStream is;
            if ("gzip".equals(conn.getContentEncoding())) {
                is = new GZIPInputStream(conn.getInputStream());
            } else {
                is = conn.getInputStream();
            }
            byte[] buffer = new byte[512];
            long len;
            accessFile = new RandomAccessFile(saveDir, "rw");
            while ((len = is.read(buffer)) != -1) {
                accessFile.write(buffer, 0, (int) len);
            }
        } catch (Exception e) {
            YeLog.e(e);
            if (listener != null) {
                if (saveDir.exists())saveDir.delete();
                listener.onFailure(url);
            }
            return false;
        } finally {
            if (accessFile != null) {
                try {
                    accessFile.close();
                } catch (IOException e) {
                   YeLog.e(e);
                }
            }
        }
        if (listener != null) listener.onComplete(url);
        return true;
    }

    public interface DownloadListener {
        void onComplete(String url);

        void onFailure(String url);
    }
}
