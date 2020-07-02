package com.cloudtech.shell.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Created by jiantao.tu on 2018/6/1.
 */
public class DecodeInputStream extends InputStream {


    private InputStream is;

    private byte[] password;

    private byte[] bytes = new byte[0];

    private int index = 0;

    private int count = 0;


    public DecodeInputStream(InputStream inputStream, byte[] password) throws Exception {
        this.is = inputStream;
        this.password = password;
        init();
    }

    private void init() throws Exception {
        int len;
        final int CACHE_COUNT = 1024;
        byte[] b = new byte[CACHE_COUNT];
        int totalLen = 0;
        while ((len = is.read(b, 0, CACHE_COUNT)) != -1) {
            bytes = Arrays.copyOf(bytes, totalLen + len);
            System.arraycopy(b, 0, bytes, totalLen, len);
            totalLen += len;
        }
        bytes = decode(bytes);
        count = bytes.length;
    }

    private byte[] decode(byte[] bytes) throws Exception {
        final int blockLen = 16;
        int block = bytes.length / blockLen;
        byte[][] cp = new byte[block][];//明文的Bytes块列表
        int length = 0;
        /* 将密文块进行解密，然后存到cp 二维数组中 */
        for (int i = 0; i < block; i++) {
            /* 以16字节分割为块 */
            byte[] temp = new byte[blockLen];
            System.arraycopy(bytes, i * blockLen, temp, 0, blockLen);

            /* 解密操作 */
            byte[] deBytes = decrypt(temp);

            /* 对最后一块数据进行特殊去冗余处理 */
            if (i == block - 1) {
                for (int x = 0; x < deBytes.length; x++) {
                    deBytes[x] -= 48;
                }
            }
            cp[i] = deBytes;
            length += deBytes.length;
        }

        /* 获取加密时最后一个分块存储的字节长度 */
        StringBuilder lenStr = new StringBuilder();
        byte[] lenByte = cp[cp.length - 1];
        for (byte aLenByte : lenByte) {
            lenStr.append(String.valueOf(aLenByte));
        }
        int lens = Integer.parseInt(lenStr.toString());

        /* 把二维数组转成一维数组返回 */
        byte[] outBytes = new byte[length];
        for (int i = 0; i < cp.length; i++) {
            System.arraycopy(cp[i], 0, outBytes, i * blockLen, cp[i].length);
        }
        return Arrays.copyOf(outBytes, lens);
    }

    private byte[] decrypt(byte[] content) throws Exception {
        Rijndael rijndael = new Rijndael();
        rijndael.makeKey(password, 256);
        byte[] ct = new byte[16];
        rijndael.decrypt(content, ct);
        return ct;
    }


    @Override
    public int read() {
        return (index < count) ? (bytes[index++] & 0xff) : -1;
    }


    @Override
    public void close() throws IOException {
        if (is != null) is.close();
    }
}