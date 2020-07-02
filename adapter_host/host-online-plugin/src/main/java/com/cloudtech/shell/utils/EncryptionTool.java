package com.cloudtech.shell.utils;// package EncryptionTool;

import android.net.Uri;
import android.text.TextUtils;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by jiantao.tu on 2018/5/2.
 */
public class EncryptionTool {

    public static byte[] hash(String data, String key) {
        byte[] bytesKey = key.getBytes();
        final SecretKeySpec secretKey = new SecretKeySpec(bytesKey,
            "HmacSHA256");
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            return mac.doFinal(data.getBytes());
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (InvalidKeyException e) {
            return null;
        }
//        byte[] rs = new byte[0];
//        if (bytes != null) {
//            rs = new byte[bytes.length / 2];
//            for (int i = 0; i < rs.length; i++) {
//                rs[i] = bytes[i * 2];
//            }
//        }

    }

    public static String byte2hex(final byte[] b) {
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

    /**
     * Rijindael的解密算法
     *
     * @param password：解密密码
     * @param content：解密内容的byte数组
     * @return
     */
    private static byte[] decrypt(byte[] password, byte[] content) throws Exception {
        Rijndael rijndael = new Rijndael();
        rijndael.makeKey(password, 256);
        byte[] ct = new byte[16];
        rijndael.decrypt(content, ct);
        return ct;
    }

    /**
     * bytes字符串转换为Byte值
     *
     * @param hexString Byte字符串，每个Byte之间没有分隔符
     * @return byte[]
     */
    public static byte[] hexStr2Bytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    public static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * 解密算法
     *
     * @return
     */
    public static String decrypt1(String content,  String password) throws Exception {

        //如果解密内容和解密密码无效就不进行解密，直接返回null
//        if (TextUtils.isEmpty(contentByte) || TextUtils.isEmpty(passwordByte)) {
//            return null;
//        }

        //将解密的内容转成字节数组(因为加密之后的是十六进制的字符串)
        byte[] contentByte = hexStr2Bytes(content);
        byte[] passwordByte = hexStr2Bytes(password);
        //得到密文的块数
        int block = contentByte.length / 16;

        ArrayList<byte[]> ct = new ArrayList<>();//密文的Bytes块列表
        ArrayList<byte[]> pt = new ArrayList<>();//明文的Bytes块列表

        /**将密文进行分块，并且存到ct列表中**/
        for (int i = 0; i < block; i++) {
            byte[] temp = new byte[16];
            for (int j = 0; j < 16; j++) {
                temp[j] = contentByte[i * 16 + j];
            }
            ct.add(temp);
            temp = null;
        }

        /**将密文块进行解密，然后存到pt列表中**/
        for (int i = 0; i < ct.size(); i++) {
            pt.add(decrypt(passwordByte, ct.get(i)));
        }

        //得到明文的长度
        for (int i = 0; i < pt.get(pt.size() - 1).length; i++) {
            pt.get(pt.size() - 1)[i] -= 48;
        }

        StringBuilder lenStr = new StringBuilder();
        byte[] lenByte = pt.get(pt.size() - 1);
        for (int i = 0; i < lenByte.length; i++) {
            lenStr.append(String.valueOf(lenByte[i]));
        }
        int lens = Integer.valueOf(lenStr.toString());

        /**取得有效的明文长度数据*/
        byte[] contentBytes = new byte[pt.size() * 16];
        int location = 0;
        for (int i = 0; i < pt.size(); i++) {
            byte[] temp = pt.get(i);
            for (int j = 0; j < temp.length; j++) {
                contentBytes[location] = temp[j];
                location++;
            }
            temp = null;
        }
        return new String(contentBytes, 0, lens);
    }

    public static byte[] key(String url) {
        Uri uri = Uri.parse(url);
        String time = uri.getQueryParameter("time");
        String queryStr = uri.getQuery();
        return hash(queryStr, time);
    }

    @Deprecated
    public static void main(String[] args) {
//        String now = String.format("%1$s", System.currentTimeMillis());
        String url = "http://54.169.152.119:8888/sdkupdate/?vs=subscription,1,1.0.1,subscription,4,1.0.0,promote,1,1.0.1,promote,4,1.0.0&pkg=com" +
            ".cloudtech.dexshell&slot=246&aid=a6d6a39210e4f9e7&gaid=c98ad329-2f0b-49dd-af94-ba28e0faa8ba&version=1.0.0&net=8&time=1527680726474";
//        String str = String.format(url, "js,1.1.0,houjie,1.2.0","com.mobi","247",
//            "sdfdsfd", "ssss", now);

        String queryStr = URI.create(url).getQuery();
//        String key = hash(queryStr, "1527680726474");
//        System.out.println(key);
        try {
//            String resultData = decrypt
//                ("5573d9fdceddb8386cb08faac5337082b1f779e72d97b9d98abfdba591b3452958104cc8abed8dfa36886d4b62f403e975d535a32007f97922c8e53f563c4c335d9f04bb6a14e6d47905ad67f794430ecd26058f4ebc061bb6ae8b70bf4c09639734ad899f38f1a3268a12a5b87d7b84435166180911b34eab1e5e2cdb275fb1801215754feb1cde9c08580ca7f87a87e6fefe1723aba79eddb6d79e4cbb1423048be561e1390f9108d549330485a438413fdfce36fced9566a4a8a41b6365e6954420e4ee9dafcc47f3e17324c091dc3f861be3c1140125fd7430299d2648323d5d000f62a23198dbc9acc2f1a440229cf5577a10767a07755397c8b61ff1176b1d0c3c6a0b07a43b3fd882cd566afc12c243eaa1b854f1c3d0b77347ee7151810c7f0b307736ba6a424e0228ba1cc2f864be608ca2cd002891e7205a6e83eaf528467df4e7d61c47f26ea3a035e550b0d9ee7a2935cb4c2c49cd93860411eb41073c2d93f0ad9ae01f4b1bf72065bd0c350f49802b98c81bca3f273060518a5dcc79699d97c67e9aace952bf5e8e3a9d9f09795a206351aa638bde980b274a14df04e167febdcd343e0eecde285e3fbd1d6557750163df158174d00fd0e6ebbe260440c3d3bf84a75564013769f17dcd1dd164eedd2cebe06ce1cb6cc740b49d4ae22b82e59bdad990a714215a9b9ab3b80a8531d1f3ace5a8bbd61bb3e4e633ca9eefa9ec8d8eebdab68d1528e2b9a7eec4b758c9e099e80fee62f3c540961e3baee7afcee3b02100f7d16cac0b57e4b5f21df68a202aaf53bb75f0ec5af2", key);
//            System.out.println(resultData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
