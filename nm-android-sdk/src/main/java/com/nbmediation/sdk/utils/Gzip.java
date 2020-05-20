// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.utils;

import com.nbmediation.sdk.utils.crash.CrashUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Gzip {
    private Gzip() {
    }

    public static byte[] inGZip(byte[] bytes) {
        byte[] unByte = null;
        if (bytes == null || bytes.length == 0) {
            return unByte;
        }
        ByteArrayOutputStream bos = null;
        GZIPOutputStream gzip = null;
        try {
            bos = new ByteArrayOutputStream();
            gzip = new GZIPOutputStream(bos);
            gzip.write(bytes);
        } catch (Exception e) {
            DeveloperLog.LogD("Gzip", e);
            CrashUtil.getSingleton().saveException(e);
        } finally {
            try {

                if (gzip != null) {
                    gzip.finish();
                    gzip.close();
                }
                if (bos != null) {
                    unByte = bos.toByteArray();
                    bos.close();
                }
            } catch (Exception e) {
                DeveloperLog.LogD("Gzip", e);
                CrashUtil.getSingleton().saveException(e);
            }
        }
        return unByte;
    }

    public static byte[] unGZip(byte[] bytes) {
        byte[] unByte = null;
        if (bytes == null || bytes.length == 0) {
            return unByte;
        }
        ByteArrayInputStream bis = null;
        ByteArrayOutputStream bos = null;
        GZIPInputStream gzip = null;
        try {
            bis = new ByteArrayInputStream(bytes);
            bos = new ByteArrayOutputStream();
            gzip = new GZIPInputStream(bis);
            byte[] buf = new byte[1024];
            int num = -1;
            while ((num = gzip.read(buf, 0, buf.length)) != -1) {
                bos.write(buf, 0, num);
            }
        } catch (Exception e) {
            DeveloperLog.LogD("Gzip", e);
            CrashUtil.getSingleton().saveException(e);
        } finally {
            try {

                if (gzip != null) {
                    gzip.close();
                }
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    unByte = bos.toByteArray();
                    bos.flush();
                    bos.close();
                }
            } catch (Exception e) {
                DeveloperLog.LogD("Gzip", e);
                CrashUtil.getSingleton().saveException(e);
            }
        }
        return unByte;
    }

    public static byte[] compress(byte[] bytes) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(bytes);
            gzip.close();
        } catch (IOException e) {
            DeveloperLog.LogD("Gzip", e);
            CrashUtil.getSingleton().saveException(e);
        }
        return out.toByteArray();
    }
}
