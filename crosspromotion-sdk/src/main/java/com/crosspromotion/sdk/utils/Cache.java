// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.utils;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.openmediation.sdk.utils.AdtUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.IOUtil;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.request.network.Headers;
import com.openmediation.sdk.utils.request.network.Response;

import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public final class Cache {

    private static final String FILE_DIR_NAME = "omnetwork";//cache dir name
    private static final long FREE_SD_SPACE_NEEDED_TO_CACHE = 100 * 1024 * 1024L;//Cache dir max size
    private static final long MIN_CACHE_INTERVAL = 60 * 60 * 1000L;//min cache time
    private static final long MAX_CACHE_INTERVAL = 24 * MIN_CACHE_INTERVAL;//max cache time

    private static String[] HEADERS = new String[]{
            CommonConstants.KEY_CACHE_CONTROL,
            CommonConstants.KEY_CONTENT_TYPE,
            CommonConstants.KEY_ETAG,
            CommonConstants.KEY_LAST_MODIFIED,
            CommonConstants.KEY_LOCATION,
            CommonConstants.KEY_CONTENT_LENGTH
    };

    public static void init() {
        if (AdtUtil.getApplication() == null) {
            return;
        }
        createCacheRootDir(AdtUtil.getApplication());
        freeSpaceIfNeeded(AdtUtil.getApplication());
    }

    /**
     * Gets default cache root dir
     */
    private static File getRootDir(Context context) {
        File root = context.getCacheDir();
        String path = root.getAbsolutePath() + File.separator + FILE_DIR_NAME;

        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * checks if url is in cache
     */
    public static boolean existCache(Context context, String url) {
        try {
            if (context == null) {
                if (AdtUtil.getApplication() == null) {
                    return false;
                } else {
                    context = AdtUtil.getApplication();
                }
            }
            File rootDir = getRootDir(context);
            File content;
            File header;
            if (url.startsWith("http")) {
                Map<String, String> urlFields = getUrlFields(url);
                String filePath = urlFields.get("path");
                String fileName = urlFields.get("name");
                if (TextUtils.isEmpty(fileName)) {
                    return false;
                }
                content = new File(rootDir, filePath + fileName);
                header = new File(rootDir, filePath + fileName.concat(CommonConstants.FILE_HEADER_SUFFIX));
            } else {
                Uri tmp = Uri.parse(url);
                if (TextUtils.isEmpty(tmp.getPath())) {
                    return false;
                }
                content = new File(tmp.getPath());
                header = new File(tmp.getPath().concat(CommonConstants.FILE_HEADER_SUFFIX));
            }
            if (!header.exists() || !content.exists()) {
                return false;
            }
            if (header.length() > 0) {
                try {
                    String contentLength = getValueFromFile(header, CommonConstants.KEY_CONTENT_LENGTH);
                    if (!TextUtils.isEmpty(contentLength)) {
                        int length = Integer.parseInt(contentLength);
                        if (content.length() != length) {
                            return false;
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            updateFileTime(header);
            updateFileTime(content);

            long interval = getRequestInterval(header);
            long maxAge = getMaxAge(header);

            return interval < maxAge;
        } catch (Exception e) {
            DeveloperLog.LogD("Cache", e);
            return false;
        }
    }

    /**
     * caches file
     */
    public static boolean saveFile(Context context, String url, Response response) {
        if (context == null) {
            return false;
        }
        boolean success;
        try {
            success = saveContent(context, url, response);
            if (success) {
                saveHeaderFields(context, url, response);
            }
        } catch (Exception e) {
            success = false;
            DeveloperLog.LogD("Cache", e);
        }
        return success;
    }

    /**
     * caches header fields
     */
    public static void saveHeaderFields(Context context, String url, Response response) throws Exception {
        Headers headers = response.headers();
        if (headers == null || headers.isEmpty()) {
            return;
        }
        File rootDir = getRootDir(context);
        Map<String, String> urlFields = getUrlFields(url);
        String filePath = urlFields.get("path");
        String fileName = urlFields.get("name").concat(CommonConstants.FILE_HEADER_SUFFIX);
        File header = makeFilePath(rootDir, filePath, fileName);
        if (header.length() > 0) {
            header.delete();
            header = makeFilePath(rootDir, filePath, fileName);
        }

        JSONObject object = new JSONObject();
        for (String s : HEADERS) {
            if (headers.containsKey(s)) {
                String value = headers.get(s).get(0);
                String tmp = value.split(";")[0];
                object.put(s, tmp.trim());
            }
        }

        object.put(CommonConstants.KEY_REQUEST_TIME, String.valueOf(System.currentTimeMillis()));
        IOUtil.writeToFile(object.toString().getBytes(Charset.forName("utf-8")), header);
    }

    /**
     * caches file content
     */
    private static boolean saveContent(Context context, String url, Response response) throws Exception {
        File rootDir = getRootDir(context);
        Map<String, String> urlFields = getUrlFields(url);
        String filePath = urlFields.get("path");
        String fileName = urlFields.get("name");

        if (TextUtils.isEmpty(fileName)) {
            return false;
        }
        File result = makeFilePath(rootDir, filePath, fileName);
        if (result.length() > 0) {//if exists, delete it
            result.delete();
            result = makeFilePath(rootDir, filePath, fileName);
        }
        int contentLength = (int) response.headers().getContentLength();//only image and video res have value ,otherwise value = -1

        return convertToResult(result, response, contentLength);
    }

    /**
     * gets cache file by url and suffix
     */
    public static File getCacheFile(Context context, String url, String suffix) {
        File dir = getRootDir(context);
        File result;
        if (url.startsWith("http")) {
            Map<String, String> urlFields = getUrlFields(url);
            String filePath = urlFields.get("path");
            String fileName = urlFields.get("name");
            if (TextUtils.isEmpty(fileName)) {
                return null;
            }
            if (!TextUtils.isEmpty(suffix)) {
                fileName = fileName.concat(CommonConstants.FILE_HEADER_SUFFIX);
            }

            result = makeFilePath(dir, filePath, fileName, false);
        } else {
            Uri tmp = Uri.parse(url);
            if (TextUtils.isEmpty(tmp.getPath())) {
                return null;
            }
            if (TextUtils.isEmpty(suffix)) {
                result = new File(tmp.getPath());
            } else {
                result = new File(tmp.getPath().concat(CommonConstants.FILE_HEADER_SUFFIX));
            }
        }
        DeveloperLog.LogD("result:" + result.toString());
        updateFileTime(result);
        return result;
    }

    /**
     * Returns value from file for key
     */
    public static String getValueFromFile(File file, String key) throws Exception {
        updateFileTime(file);
        InputStream inputStream = IOUtil.getFileInputStream(file);
        if (inputStream == null) {
            return "";
        }
        String values = IOUtil.toString(inputStream);
        if (TextUtils.isEmpty(values)) {
            return "";
        }
        String tmp = values.substring(values.indexOf("{"), values.lastIndexOf("}") + 1);
        JSONObject object = new JSONObject(tmp);
        return object.optString(key);
    }

    /**
     * updates file's last modify time
     */
    private static void updateFileTime(File file) {
        if (file != null && file.exists()) {
            long newModifiedTime = System.currentTimeMillis();
            file.setLastModified(newModifiedTime);
        }
    }

    /**
     * Stores received data to file
     */
    private static boolean convertToResult(File result, Response response, int length) throws
            Exception {
        boolean success;
        InputStream in = response.body().stream();
        IOUtil.writeToFile(in, result);
        IOUtil.closeQuietly(in);
        if (length <= 0) {
            success = result.length() > 0;
        } else {
            success = result.length() == length;
        }
        return success;
    }

    /**
     * creates cache root dir
     */
    private static void createCacheRootDir(Context context) {
        File root = context.getCacheDir();

        String path = root.getAbsolutePath() + File.separator + FILE_DIR_NAME;

        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * checks free space, if not enough in cache left, deletes files by LRU
     */
    private static void freeSpaceIfNeeded(Context context) {
        File dir = getRootDir(context);
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        int dirSize = 0;
        for (File file : files) {
            dirSize += file.length();
        }
        // if the dir size is larger than 100MB
        // frees 40% space
        if (dirSize > FREE_SD_SPACE_NEEDED_TO_CACHE) {
            // delete 40% files by LRU
            int removeFactor = (int) ((0.4 * files.length) + 1);
            // sort the files by modify time
            Arrays.sort(files, new FileLastModifySort());
            // delete files
            for (int i = 0; i < removeFactor; i++) {
                files[i].delete();
            }
        }
    }

    /**
     * gets request interval
     */
    private static long getRequestInterval(File header) throws Exception {
        String requestTime = getValueFromFile(header, CommonConstants.KEY_REQUEST_TIME);
        if (TextUtils.isEmpty(requestTime)) {
            return 0;
        }
        return System.currentTimeMillis() - Long.parseLong(requestTime);
    }

    /**
     * gets cache max-age
     */
    private static long getMaxAge(File header) throws Exception {
        long maxAge = 0;
        String cacheControl = getValueFromFile(header, CommonConstants.KEY_CACHE_CONTROL);
        if (!TextUtils.isEmpty(cacheControl)) {
            if (cacheControl.contains(CommonConstants.KEY_MAX_AGE)) {
                String[] tmp = cacheControl.split(",");
                for (String s : tmp) {
                    if (s.contains(CommonConstants.KEY_MAX_AGE)) {
                        maxAge = Long.parseLong(s.split("=")[1]) * 1000;
                    }
                }
            }
        }

        if (maxAge > MAX_CACHE_INTERVAL) {
            return MAX_CACHE_INTERVAL;
        } else if (maxAge < MIN_CACHE_INTERVAL) {
            return MIN_CACHE_INTERVAL;
        } else {
            return maxAge;
        }
    }

    private static Map<String, String> getUrlFields(String url) {
        Map<String, String> urlFields = new HashMap<>();
        Uri uri = Uri.parse(url);
        String host = uri.getHost();
        String md5Host = Encrypter.md5(host);
        DeveloperLog.LogD("url host : " + host);
        String path = uri.getPath();
        if (!TextUtils.isEmpty(path)) {
            String filePath = path.substring(0, path.lastIndexOf("/"));
            DeveloperLog.LogD("url path : " + filePath);
            String fileName = path.substring(path.lastIndexOf("/"));
            DeveloperLog.LogD("url name : " + fileName);
            urlFields.put("path", md5Host.concat(filePath));
            urlFields.put("name", fileName);
        }
        return urlFields;
    }

    private static File makeFilePath(File root, String filePath, String fileName) {
        return makeFilePath(root, filePath, fileName, true);
    }

    private static File makeFilePath(File root, String filePath, String fileName, boolean create) {
        File file = null;
        makeRootDirectory(root, filePath);
        try {
            file = new File(root, filePath + fileName);
            if (create && !file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            DeveloperLog.LogE("error:", e);
        }
        return file;
    }

    /**
     * 生成文件夹
     */
    public static void makeRootDirectory(File root, String filePath) {
        try {
            File file = new File(root, filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
        } catch (Exception e) {
            DeveloperLog.LogE("error:", e);
        }
    }

    /**
     * Compares with lastModified
     */
    private static class FileLastModifySort implements Comparator<File> {
        public int compare(File arg0, File arg1) {
            if (arg0.lastModified() > arg1.lastModified()) {
                return 1;
            } else if (arg0.lastModified() == arg1.lastModified()) {
                return 0;
            } else {
                return -1;
            }
        }
    }
}
