// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.utils;

import java.nio.charset.Charset;
import java.security.MessageDigest;

public class Encrypter {
    private Encrypter() {
    }

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    public static String md5(String data) {
        return md5(data, UTF_8);
    }

    public static String md5(String data, Charset charset) {
        return byte2hex(encrypt("MD5", data.getBytes(charset)));
    }

    public static byte[] encrypt(String algorithm, byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            return md.digest(data);
        } catch (Exception gse) {
            throw new RuntimeException(gse);
        }
    }

    public static String byte2hex(byte[] bytes) {
        StringBuilder sign = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex);
        }
        return sign.toString();
    }
}