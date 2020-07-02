package com.cloudtech.shell.manager;

import android.content.Context;

import com.cloudtech.shell.BuildConfig;
import com.cloudtech.shell.db.ModuleDao;

import com.cloudtech.shell.entity.ModulePo;
import com.cloudtech.shell.entity.PluginType;
import com.cloudtech.shell.ex.FileCurdError;
import com.cloudtech.shell.ex.MD5ValidException;
import com.cloudtech.shell.utils.ContextHolder;
import com.cloudtech.shell.utils.YeLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by jiantao.tu on 2018/5/18.
 */
public class LocalDexManager {

    private static boolean IS_DEF_MANAGER_INIT = false;

    private static boolean IS_DEF_MAIN_PLUGIN_INIT = false;

    public static void defInit() {
        final Context context = ContextHolder.getGlobalAppContext();
        if (context == null) return;
        defInitManager(context);
        defInitMainPlugin(context);
    }

    private static void defInitManager(final Context context) {
        if (IS_DEF_MANAGER_INIT || context == null || !AssetsFileManager.isFileExists(context,
                BuildConfig.managerFileName)) {

            return;
        }
        try {
            execute(context
                    , BuildConfig.managerModuleName
                    , BuildConfig.managerVersion
                    , BuildConfig.managerMd5
                    , BuildConfig.managerClassName
                    , BuildConfig.managerMethodName
                    , BuildConfig.managerFileName
                    , PluginType.MANAGER);
            IS_DEF_MANAGER_INIT = true;
        } catch (Throwable e) {
            YeLog.e(e);
        }

    }


    private static void defInitMainPlugin(final Context context) {
        if (IS_DEF_MAIN_PLUGIN_INIT || context == null || !AssetsFileManager.isFileExists(context,
                BuildConfig.mainFileName)) {

            return;
        }
        try {
            execute(context
                    , BuildConfig.mainModuleName
                    , BuildConfig.mainVersion
                    , BuildConfig.mainMd5
                    , BuildConfig.mainClassName
                    , BuildConfig.mainMethodName
                    , BuildConfig.mainFileName
                    , PluginType.MAIN_PLUGIN);
            IS_DEF_MAIN_PLUGIN_INIT = true;
        } catch (Throwable e) {
            YeLog.e(e);
        }

    }

    private static void execute(Context context, String moduleName, String version, String checksum
            , String className, String methodName, String fileName, PluginType pluginType)
            throws FileCurdError, IOException, InterruptedException, MD5ValidException, NoSuchAlgorithmException {
        ModulePo module = ModuleDao.getInstance(context).getStaticModuleByName(moduleName);

        boolean isUnzip = true;//是否需要解压
        boolean isPersist = true;//是否需要持久化
        if (module != null) {
             /*
             本地有记录则不持久化
            */
            if (module.getVersion().equals(version)) {
                /*
                 检查本地文件是否存在,存在-不解压
                */
                try {
                    PluginFileManager.getPluginFile(context, module.getModuleName(), module.getVersion(), pluginType);
                    isUnzip = false;
                } catch (FileNotFoundException ignored) {
                }
                isPersist = false;
            } else {
                /*
                  如果有以前本地相同模块不同版本的记录删除
                */
                ModuleDao.getInstance(context).deleteById(module.getId());
                PluginFileManager.deleteFile(context, module.getModuleName(), module.getVersion(), pluginType);

                /*
                监测是否有服务端下发的相同版本，有则删除，以目前的本地模块为主
                 */
                ModulePo moduleCloud = ModuleDao.getInstance(context).getByNameAndVersion(moduleName, version);
                if (moduleCloud != null) {
                    ModuleDao.getInstance(context).deleteById(moduleCloud.getId());
                    PluginFileManager.deleteFile(context, moduleCloud.getModuleName(), moduleCloud.getVersion(), pluginType);
                }
            }
        }
        if (isUnzip) {
            File subFile = PluginFileManager.createFile(context, fileName);
            AssetsFileManager.copyAssets(context, fileName, subFile, "700");
            PluginFileManager.saveFile(context, subFile, moduleName, version, checksum, pluginType);
        }

        if (isPersist) {
            int small, medium, sized;
            try {
                String[] vs = version.split("\\.");
                small = Integer.parseInt(vs[0]);
                medium = Integer.parseInt(vs[1]);
                sized = Integer.parseInt(vs[2]);
            } catch (Exception e) {
                YeLog.e(e);
                return;
            }
            ModulePo modulePo = new ModulePo
                    (moduleName
                            , version
                            , false
                            , null
                            , checksum
                            , className
                            , methodName
                            , false
                            , false,
                            true
                            , 0
                            , small
                            , medium
                            , sized
                            , pluginType);
            ModuleDao.getInstance(context).replace(modulePo);
        }
    }

}