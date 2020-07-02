package com.cloudtech.shell;// package EncryptionTool;

import java.io.File;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by jiantao.tu on 2018/5/2.
 */
public class CheckSum {

    public static String hash(String data, String key) {
        byte[] bytesKey = key.getBytes();
        final SecretKeySpec secretKey = new SecretKeySpec(bytesKey,
            "HmacSHA256");
        byte[] bytes;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            bytes = mac.doFinal(data.getBytes());
        } catch (NoSuchAlgorithmException e) {
            bytes = null;
        } catch (InvalidKeyException e) {
            bytes = null;
        }
        byte[] rs = new byte[0];
        if (bytes != null) {
            rs = new byte[bytes.length / 2];
            for (int i = 0; i < rs.length; i++) {
                rs[i] = bytes[i * 2];
            }
        }
        return byte2hex(rs);
    }

    private static String byte2hex(final byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (byte aB : b) {
            stmp = (Integer.toHexString(aB & 0xFF));
            if (stmp.length() == 1)
                hs.append("0").append(stmp);
            else
                hs.append(stmp);
        }
        return hs.toString();
    }

    public static void main(String[] args) {
        String now = String.format("%1$s", System.currentTimeMillis());
        String url = "http://www.baidu.com?vs=%1$s&pkg=%2$s&slot=%3$s&aid=%4$s&gaid=%5$s&time=%6$s";
        String str = String.format(url, "js,1.1.0,houjie,1.2.0","com.mobi","247",
            "sdfdsfd", "ssss", now);
        String queryStr = URI.create(str).getQuery();

        System.out.println("encrypt key: "+ hash(queryStr, now));
    }

}
