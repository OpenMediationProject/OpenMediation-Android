package com.cloudtech.shell.http;

import android.os.Build;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class CompatSSLSocketFactory extends SSLSocketFactory {

    private static final String PROTOCOL_ARRAY_LOWER[] = {"SSLv3", "TLSv1"};
    private static final String PROTOCOL_ARRAY[] = {"SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"};

    private static final X509TrustManager DEFAULT_TRUST_MANAGERS = new X509TrustManager() {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws
                                                                                 CertificateException {
            // Trust.
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws
                                                                                 CertificateException {
            // Trust.
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    };

    private static void setSupportProtocolAndCipherSuites(Socket socket) {
        if (socket instanceof SSLSocket) {
            // https://developer.android.com/about/versions/android-5.0-changes.html#ssl
            // TODO: 2017/12/27 handshake SSLSocket
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                ((SSLSocket) socket).setEnabledProtocols(PROTOCOL_ARRAY_LOWER);
            } else {
                ((SSLSocket) socket).setEnabledProtocols(PROTOCOL_ARRAY);
            }
        }
    }

    private SSLSocketFactory delegate;

    public CompatSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{DEFAULT_TRUST_MANAGERS}, new SecureRandom());
            delegate = sslContext.getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw new AssertionError(); // The system has no TLS. Just give up.
        }
    }

    public CompatSSLSocketFactory(SSLSocketFactory factory) {
        this.delegate = factory;
    }


    @Override
    public String[] getDefaultCipherSuites() {
        return delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws
                                                                                   IOException {
        Socket ssl = delegate.createSocket(s, host, port, autoClose);
        setSupportProtocolAndCipherSuites(ssl);
        return ssl;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        Socket ssl = delegate.createSocket(host, port);
        setSupportProtocolAndCipherSuites(ssl);
        return ssl;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws
                                                                                            IOException {
        Socket ssl = delegate.createSocket(host, port, localHost, localPort);
        setSupportProtocolAndCipherSuites(ssl);
        return ssl;
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        Socket ssl = delegate.createSocket(host, port);
        setSupportProtocolAndCipherSuites(ssl);
        return ssl;
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws
                                                                                                       IOException {
        Socket ssl = delegate.createSocket(address, port, localAddress, localPort);
        setSupportProtocolAndCipherSuites(ssl);
        return ssl;
    }

    @Override
    public Socket createSocket() throws IOException {
        Socket ssl = delegate.createSocket();
        setSupportProtocolAndCipherSuites(ssl);
        return ssl;
    }
}
