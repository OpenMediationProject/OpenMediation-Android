package com.cloudtech.shell.http;

import android.util.Log;

import com.cloudtech.shell.utils.DecodeInputStream;
import com.cloudtech.shell.utils.EncryptionTool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;

/**
 * Created by jiantao.tu on 2018/4/19.
 */
public class HttpUtils {

    private static final String TAG = "HttpUtils";

    private final static int MAX_REDIRECTS = 10;

    public static HttpURLConnection handleConnection(String urlStr, int timeout)
        throws IOException, HttpRedirectException, HttpErrorException {
        HttpURLConnection conn;
        boolean redirected;

        int redirectCount = 0;
        do {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            if (conn instanceof HttpsURLConnection) {
                Log.i(TAG, "handleConnection: https url connection");
                SSLSocketFactory sslSocketFactory = SSLUtils.defaultSSLSocketFactory();
                ((HttpsURLConnection) conn).setSSLSocketFactory(sslSocketFactory);
                HostnameVerifier hostnameVerifier = SSLUtils.defaultHostnameVerifier();
                if (hostnameVerifier != null) {
                    ((HttpsURLConnection) conn).setHostnameVerifier(hostnameVerifier);
                }
            }
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(30000);
            conn.setRequestProperty("Accept-Encoding", "gzip");
            conn.setRequestProperty("CT-Accept-Encoding", "gzip");
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setInstanceFollowRedirects(false);
            int code = conn.getResponseCode();
            if (code >= HTTP_BAD_REQUEST) {
                throw new HttpErrorException("request error code : " + code);
            }
            redirected = code == HTTP_MOVED_TEMP;
            if (redirected) {
                urlStr = conn.getHeaderField("Location");
                redirectCount++;
                conn.disconnect();
            }
            if (redirectCount >= MAX_REDIRECTS) {
                throw new HttpRedirectException("Too many redirects: " +
                    redirectCount);
            }
        } while (redirected);

        return conn;
    }

    public static byte[] handleSuccess(HttpURLConnection conn) throws Exception {
        InputStream is;
        if ("gzip".equals(conn.getContentEncoding())) {
            is = new GZIPInputStream(conn.getInputStream());
        } else {
            is = conn.getInputStream();
        }
        if ("gzip".equals(conn.getHeaderField("CT-Content-Encoding"))) {
            byte[] key = EncryptionTool.key(conn.getURL().toString());
            is = new DecodeInputStream(is, key);//解密
            is = new GZIPInputStream(is);//解压
        }
        byte[] bytes = getBytes(is);
        is.close();
        return bytes;
    }


    private static byte[] getBytes(InputStream is) throws Exception {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int len;
        while ((len = is.read(b, 0, 1024)) != -1) {
            bao.write(b, 0, len);
            bao.flush();
        }
        return bao.toByteArray();
    }

    public static class HttpRedirectException extends Exception {
        public HttpRedirectException(String message) {
            super(message);
        }
    }


    public static class HttpErrorException extends Exception {
        public HttpErrorException(String message) {
            super(message);
        }
    }
}
