package com.cloudtech.shell.manager;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.cloudtech.shell.entity.ModuleData;
import com.cloudtech.shell.entity.PluginType;
import com.cloudtech.shell.ex.FileCurdError;
import com.cloudtech.shell.ex.MD5ValidException;
import com.cloudtech.shell.utils.MD5;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by jiantao.tu on 2018/4/2.
 */
public class PluginFileManager {

    private final static char SEPARATOR = '$';

    public static String getFileName(String moduleName, String version, PluginType type) {
        String suffix;
        if (type == PluginType.MANAGER) {
            suffix = ".apk";
        } else {
            suffix = ".zip";
        }
        return moduleName + "_" + version + suffix;
    }

    public static File getFilePath(Context context, String moduleName, String version, PluginType type) {
        if (context == null || moduleName == null)
            throw new NullPointerException("context or moduleName is null");
        return getFilePath(context, getFileName(moduleName, version, type));
    }


    public static File getFilePath(Context context, String fileName) {
        if (context == null || fileName == null)
            throw new NullPointerException("context or fileName is null");
        File dexDir = context.getDir("libs", Context.MODE_PRIVATE);
        return new File(dexDir, "cloudmobi/" + fileName);
    }

    public static File getCachePath(Context context) throws FileCurdError {
        if (context == null)
            throw new NullPointerException("context is null");
        if (Build.VERSION.SDK_INT >= 21) {
            return context.getCodeCacheDir();
        } else {
            File dexDir = context.getDir("cache", Context.MODE_PRIVATE);
            File file = new File(dexDir, UUID.randomUUID().toString());
            if (!file.exists()) {
                if (!file.mkdirs()) throw new FileCurdError("create folder error.");
            } else {
                if (!file.isDirectory()) {
                    if (!file.delete()) throw new FileCurdError("create folder error.");
                    if (!file.mkdirs()) {
                        throw new FileCurdError("file already exist,but not folders!");
                    }
                }
            }
            return file;
        }
    }


    /**
     * 创建插件文件
     *
     * @param fileName 插件名称
     * @throws IOException
     */
    public static File createFile(Context context, String fileName, String version, PluginType type) throws
            IOException, FileCurdError {
        return createFile(context, getFileName(fileName, version, type));
    }


    public static File createFile(Context context, String fileName) throws IOException,
            FileCurdError {
        if (context == null || fileName == null)
            throw new NullPointerException("context or fileName is null");
        File file = getFilePath(context, fileName);
        File fileParent = file.getParentFile();
        if (!fileParent.exists()) {
            if (!fileParent.mkdirs()) {
                throw new RuntimeException("create folder error. fileName=" + fileName);
            }
        }
        if (file.exists()) {
            if (!file.delete()) throw new FileCurdError("delete file error. fileName=" + fileName);
        }
        if (!file.createNewFile())
            throw new FileCurdError("create file error. fileName=" + fileName);
        return file;
    }


    public static void saveFile(Context context, File zipFile, String moduleName
            , String version, String checksum, PluginType type) throws FileCurdError, NoSuchAlgorithmException, IOException
            , MD5ValidException, NullPointerException {
        if (context == null || moduleName == null || zipFile == null || checksum == null) {
            throw new NullPointerException("context or zipFile or data or checksum is null. moduleName=" + moduleName);
        }
        ZipInputStream zipInputStream = null;
        BufferedInputStream inputStream = null;
        File fileOut = null;
        try {
            String renameStr = getFileName(moduleName, version, type);
            String fileOutMd5 = MD5.getFileMD5(zipFile);
            if (TextUtils.isEmpty(checksum) || !checksum.equals(fileOutMd5)) {
                throw new MD5ValidException();
            }
            if (type != PluginType.MANAGER) {
                if (!rename(zipFile, getFileName(moduleName, version, type))) {
                    throw new FileCurdError("file rename error. moduleName=" + moduleName);
                } else {
                    return;
                }
            }
            zipInputStream = new ZipInputStream(new FileInputStream(zipFile));//输入源zip路
            inputStream = new BufferedInputStream(zipInputStream);
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.getName().endsWith(".apk") && !entry.isDirectory()) {
                    String uuid = UUID.randomUUID().toString();
                    fileOut = createFile(context, uuid);
                    if (null != fileOut) {
                        FileOutputStream out = null;
                        BufferedOutputStream bout = null;
                        try {
                            out = new FileOutputStream(fileOut);
                            bout = new BufferedOutputStream(out);
                            int b;
                            while ((b = inputStream.read()) != -1) {
                                bout.write(b);
                            }
                        } finally {
                            if (bout != null) bout.close();
                            if (out != null) out.close();
                        }
                    }
                    break;
                }
            }
            if (null != fileOut) {
                if (!rename(fileOut, renameStr)) {
                    throw new FileCurdError("file rename error. moduleName=" + moduleName);
                }
            } else {
                throw new NullPointerException("fileOut is null. moduleName=" + moduleName);
            }
        } catch (IOException | MD5ValidException | FileCurdError | NoSuchAlgorithmException e) {
            if (null != fileOut && fileOut.exists()) {
                fileOut.delete();
            }
            throw e;
        } finally {
            if (zipFile.exists()) {
                zipFile.delete();
            }
            if (inputStream != null) {
                inputStream.close();
                inputStream = null;
            }
            if (zipInputStream != null) {
                zipInputStream.close();
                zipInputStream = null;
            }
        }
    }


//    public static void setDexModuleMake(Context context, ModuleData data) throws
//        FileNotFoundException,
//        PreferencesCommitError {
//        if (context == null || data == null)
//            throw new NullPointerException("context or data is null");
//        File file = getDexFilePath(context, data.moduleName,data.version);
//        if (!file.exists()) {
//            throw new FileNotFoundException("this is not exists file , set dex make error  " +
//                "moduleName=" + data.moduleName);
//        }
//        boolean result = PreferencesUtils.putString(data.moduleName, data.className + SEPARATOR +
//            data.methodName);
//        if (!result) throw new PreferencesCommitError();
//    }


    public static ModuleData getModuleMake(Context context, String moduleName, String version
            , String className, String methodName,PluginType pluginType) throws FileNotFoundException {

        if (context == null || moduleName == null)
            throw new NullPointerException("context or module is null");

        File file = getFilePath(context, moduleName, version,pluginType);
        if (!file.exists()) {
            throw new FileNotFoundException("This is not exists file , get plugin make " +
                    "error moduleName=" + moduleName);
        }

        ModuleData data = new ModuleData();
        data.version = version;
        data.moduleName = moduleName;
        data.pluginFile = file;
        data.className = className;
        data.methodName = methodName;
        return data;

    }

    public static File getPluginFile(Context context, String moduleName, String version, PluginType type) throws
            FileNotFoundException {
        if (context == null || moduleName == null)
            throw new NullPointerException("context or module is null");
        File file = getFilePath(context, moduleName, version, type);

        if (!file.exists()) {
            throw new FileNotFoundException("This is not exists file , get dex make " +
                    "error moduleName=" + moduleName);
        }
        return file;
    }


    public static void deleteFile(Context context, String moduleName, String version, PluginType type) throws
            FileCurdError {
        if (context == null || moduleName == null)
            throw new NullPointerException("context or module is null");
        File file = getFilePath(context, moduleName, version, type);
        if (file.exists()) {
            if (!file.delete()) throw new FileCurdError("delete file error.");
        }
    }


    private static boolean rename(File outFile, String newFileName) {
        String c = outFile.getParent();
        File mm = new File(c + File.separator + newFileName);
        return outFile.renameTo(mm);
    }


}
