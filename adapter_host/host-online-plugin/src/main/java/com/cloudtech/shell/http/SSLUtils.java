package com.cloudtech.shell.http;

import android.os.Build;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

public class SSLUtils {

    private static final HostnameVerifier HOSTNAME_VERIFIER = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    public static SSLSocketFactory defaultSSLSocketFactory() {
        return new CompatSSLSocketFactory();
    }

    public static SSLSocketFactory fixSSLLowerThanLollipop(SSLSocketFactory socketFactory) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP &&
            !(socketFactory instanceof CompatSSLSocketFactory)) {
            socketFactory = new CompatSSLSocketFactory(socketFactory);
        }
        return socketFactory;
    }

    public static HostnameVerifier defaultHostnameVerifier() {
        return HOSTNAME_VERIFIER;
    }

}