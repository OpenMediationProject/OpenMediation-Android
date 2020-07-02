package com.cloudtech.shell.manager;

import android.content.Context;
import android.text.TextUtils;

import com.cloudtech.shell.Constants;
import com.cloudtech.shell.db.ModuleDao;
import com.cloudtech.shell.entity.ModulePo;
import com.cloudtech.shell.entity.PluginType;
import com.cloudtech.shell.entity.Response;
import com.cloudtech.shell.http.DownloadManager;
import com.cloudtech.shell.utils.ContextHolder;
import com.cloudtech.shell.shadow.ShadowLoader;
import com.cloudtech.shell.utils.YeLog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by jiantao.tu on 2018/5/23.
 */
public class ModuleManager {

    public static Map<String, ModulePo> activeMap = new HashMap<>();

    private static boolean isManagerActive = false;

    private static boolean isMainActive = false;

    public static void saveModules(Response responseVO) {

        Context context = ContextHolder.getGlobalAppContext();
        if (context == null) return;

        List<ModulePo> list = new ArrayList<>();

        for (Response.Module module : responseVO.modules) {

            if (TextUtils.isEmpty(module.moduleName) || TextUtils.isEmpty(module.version)) {
                YeLog.e("saveModules continue. moduleName=" + module.moduleName + ",version=" + module.version);
                continue;
            }
            boolean isDown = (Constants.DOWNLOAD & module.switchVal) > 0;

            boolean isDel = (Constants.DELETE & module.switchVal) > 0;

            boolean isActive = (Constants.ACTIVE & module.switchVal) > 0;

            ModulePo modulePo = ModuleDao.getInstance(context).getByNameAndVersion(module
                    .moduleName, module.version);
            /*
            如果本地有则更新，没有则新建
             */
            if (modulePo == null) {

                int small, medium, sized;
                try {
                    String[] vs = module.version.split("\\.");
                    small = Integer.parseInt(vs[0]);
                    medium = Integer.parseInt(vs[1]);
                    sized = Integer.parseInt(vs[2]);
                } catch (Exception e) {
                    YeLog.e(e);
                    continue;
                }

                modulePo = new ModulePo(module.moduleName
                        , module.version
                        , true
                        , module.download_url
                        , module.checksum
                        , module.className
                        , module.methodName
                        , isDown
                        , isDel
                        , isActive
                        , 0
                        , small
                        , medium
                        , sized
                        , module.pluginType);
            } else {
                if (modulePo.isDown() == isDown
                        && modulePo.isActive() == isActive
                        && modulePo.isDel() == isDel) {
                    continue;
                }
            }
            modulePo.setDown(isDown);
            modulePo.setDel(isDel);
            modulePo.setActive(isActive);
            list.add(modulePo);
        }
        if (list.size() == 0) return;

        ModulePo[] modules = new ModulePo[list.size()];
        list.toArray(modules);
        ModuleDao.getInstance(context).replace(modules);
    }

    public static void applyModules() {
        final Context context = ContextHolder.getGlobalAppContext();
        if (context == null) return;
        List<ModulePo> moduleBaseList = ModuleDao.getInstance(context).queryBaseAll();
        applyModules(moduleBaseList);
        List<ModulePo> moduleOtherList = ModuleDao.getInstance(context).queryOtherAll();
        applyModules(moduleOtherList);
    }

    public static void applyModules(List<ModulePo> moduleList) {
        for (ModulePo module : moduleList) {
            if (module.isDown()) {

                YeLog.i("ready to execute the download. " + getModuleAndVersionStr(module));

                download(module);

            } else if (module.isActive()) {
                if (activeMap.containsKey(module.getModuleName())) continue;

                YeLog.i("ready to execute the active. " + getModuleAndVersionStr(module));

                if (active(module))
                    activeMap.put(module.getModuleName(), module);

            } else if (module.isDel()) {

                YeLog.i("ready to execute the delete. " + getModuleAndVersionStr(module));

                delete(module);

            }
        }
    }


    private static boolean active(ModulePo module) {
        try {
            boolean isManagerPlugin = module.getPluginType() == PluginType.MANAGER;
            boolean isMainPlugin = module.getPluginType() == PluginType.MAIN_PLUGIN;
            if (!isManagerPlugin && !isManagerActive) {
                YeLog.i("active fail. manager module not ready,pluginType="
                        + module.getPluginType() + " info=" + getModuleAndVersionStr(module));
                return false;
            }
            if (!isManagerPlugin && !isMainPlugin && !isMainActive) {
                YeLog.i("active fail. manager module not ready,pluginType="
                        + module.getPluginType() + " info=" + getModuleAndVersionStr(module));
                return false;
            }
            final Context context = ContextHolder.getGlobalAppContext();
            if (context == null) throw new NullPointerException("context is null. " + getModuleAndVersionStr(module));
            /*
            激活
             */
//            DexLoader.initialize(context, module.getModuleName(), module.getVersion(), module.getClassName(), PreferencesUtils.getSlotId());
            ShadowLoader.loadPlugin(context, module);

            /*
            记录执行成功
             */
            module.setCurrentSwitch(Constants.ACTIVE);
            ModuleDao.getInstance(context).replace(module);

            if (module.getPluginType() == PluginType.MANAGER) {
                isManagerActive = true;
            } else if (module.getPluginType() == PluginType.MAIN_PLUGIN) {
                isMainActive = true;
            }
            YeLog.i("active success. " + getModuleAndVersionStr(module));
        } catch (Throwable e) {
            YeLog.e(e);
            return false;
        }
        return true;
    }


    private static void delete(ModulePo module) {
        try {
            final Context context = ContextHolder.getGlobalAppContext();
            if (context == null)
                throw new NullPointerException("context is null. " + getModuleAndVersionStr(module));

            if (!module.isDynamic()) {
                YeLog.i("native module,not delete. " + getModuleAndVersionStr(module));
                return;
            }
            
            /*
            删除文件
             */
            PluginFileManager.deleteFile(context, module.getModuleName(), module.getVersion(), module.getPluginType());

            /*
            删除数据库
             */
            ModuleDao.getInstance(context).deleteById(module.getId());
            YeLog.i("delete success. " + getModuleAndVersionStr(module));
        } catch (Throwable e) {
            YeLog.e(e);
        }
    }

    private static void download(ModulePo module) {
        try {
            final Context context = ContextHolder.getGlobalAppContext();
            if (context == null)
                throw new NullPointerException("context is null. " + getModuleAndVersionStr(module));
            /*
            验证本地文件是否存在
             */
            File file = PluginFileManager.getFilePath(context, module.getModuleName(), module.getVersion(), module.getPluginType());
            if (file.exists()) {
                YeLog.i("Files already exist and are not downloaded. " + getModuleAndVersionStr(module));
                return;
            }

            /*
            是否下载链接为空 or 是否是静态模块
             */
            if (TextUtils.isEmpty(module.getDownloadUrl()) || !module.isDynamic()) {
                YeLog.e("downloadUrl is null or module is static. " + getModuleAndVersionStr(module));
                return;
            }

            /*
            下载
             */
            file = PluginFileManager.createFile(context, UUID.randomUUID().toString() + ".zip");
            if (!DownloadManager.download(module.getDownloadUrl(), file)) {
                YeLog.e("download failure. " + getModuleAndVersionStr(module));
                return;
            }

            /*
            解压
             */
            PluginFileManager.saveFile(context, file, module.getModuleName(), module.getVersion(), module.getChecksum(), module.getPluginType());
            /*
            记录执行成功
             */
            module.setCurrentSwitch(Constants.DOWNLOAD);
            ModuleDao.getInstance(context).replace(module);
            YeLog.i("download success. " + getModuleAndVersionStr(module));
        } catch (Throwable e) {
            YeLog.e(e);
        }
    }

    private static String getModuleAndVersionStr(ModulePo modulePo) {
        return "moduleName=" + modulePo.getModuleName() + ",version=" + modulePo.getVersion();
    }

}
