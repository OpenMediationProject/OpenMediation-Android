package com.cloudtech.shell.manager;

import android.content.Context;
import android.content.res.AssetManager;

import com.cloudtech.shell.utils.YeLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jiantao.tu on 2018/4/9.
 */
public class AssetsFileManager {

    public static void copyAssets(Context context, String assetsFilename, File file, String mode)
            throws IOException, InterruptedException {
        AssetManager manager = context.getAssets();
        final InputStream is = manager.open(assetsFilename);
        copyFile(file, is, mode);
    }

    private static void copyFile(File file, InputStream is, String mode) throws IOException,
            InterruptedException {
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        final String abspath = file.getAbsolutePath();
        final FileOutputStream out = new FileOutputStream(file);
        byte buf[] = new byte[1024];
        int len;
        while ((len = is.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        is.close();
        Runtime.getRuntime().exec("chmod " + mode + " " + abspath).waitFor();
    }

    /**
     * 判断assets文件夹下的文件是否存在
     *
     * @return false 不存在    true 存在
     */
    public static boolean isFileExists(Context context,String filename) {
        AssetManager assetManager = context.getAssets();
        try {
            String[] names = assetManager.list("");
            for (String name : names) {
                YeLog.i(name);
                if (name.equals(filename.trim())) {
//                    YeLog.i(filename + "存在");
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
//            YeLog.i(filename + "不存在");
            return false;
        }
//        YeLog.i(filename + "不存在");
        return false;
    }
}
