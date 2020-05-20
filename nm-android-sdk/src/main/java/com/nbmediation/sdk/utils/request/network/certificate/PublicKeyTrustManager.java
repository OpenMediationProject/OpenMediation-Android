// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.utils.request.network.certificate;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.X509TrustManager;

public class PublicKeyTrustManager implements X509TrustManager {

    private final List<String> trustKeys = Arrays.asList(
            "S7g1rSNao6g2EUBbylVSMu8TqeOAgZPlUeWoCEQE6G8=",
            "qo1QyzYCUCM6TTpkflyWle2ERuNQ8q7/99oCt1RmDgk=",
            "qiYwp7YXsE0KKUureoyqpQFubb5gSDeoOoVxn6tmfrU=");

    public PublicKeyTrustManager() {
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (chain.length == 0) {
            throw new IllegalArgumentException("null or zero-length certificate chain");
        }

        boolean wasPinFound = false;
        for (X509Certificate cert : chain) {
            PublicKeyPin keyPin = new PublicKeyPin(cert);
            String pin = keyPin.toString();
            if (trustKeys.contains(pin)) {
                wasPinFound = true;
                break;
            }
        }
        if (!wasPinFound) {
            throw new CertificateException("Certificate is not in trusted list (" + trustKeys.toString() + ")");
        }
    }


    @Override
    public X509Certificate[] getAcceptedIssuers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "PublicKeyTrustManager [trustKeys=" + trustKeys.toString() + "]";
    }

}
