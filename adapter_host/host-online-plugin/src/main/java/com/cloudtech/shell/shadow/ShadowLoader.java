package com.cloudtech.shell.shadow;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.WorkerThread;
import android.view.View;

import com.cloudtech.shell.Constants;
import com.cloudtech.shell.entity.ModuleData;
import com.cloudtech.shell.entity.ModulePo;
import com.cloudtech.shell.manager.PluginFileManager;
import com.tencent.shadow.dynamic.host.EnterCallback;
import com.tencent.shadow.dynamic.host.PluginManager;

import java.io.FileNotFoundException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by jiantao.tu on 2020/6/26.
 */
public class ShadowLoader {

    @WorkerThread
    public static void loadPlugin(Context context, ModulePo module)
            throws FileNotFoundException, TimeoutException, InterruptedException {
        ModuleData data = PluginFileManager.getModuleMake(context,
                module.getModuleName(), module.getVersion(), module.getClassName(), module.getMethodName(), module.getPluginType());
        switch (module.getPluginType()) {
            case MANAGER:
                loadManagerPlugin(context, data);
                break;
            case MAIN_PLUGIN:
                loadMainPlugin(context, data);
                break;
            case OTHER_PLUGIN:
                loadOtherPlugin(context, data);
                break;
        }
    }

    private static void loadManagerPlugin(Context context, ModuleData data) {
        InitApplication.onApplicationCreate(context, data.pluginFile);
    }

    private static void loadMainPlugin(Context context, ModuleData data) throws InterruptedException, TimeoutException {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_PLUGIN_ZIP_PATH, data.pluginFile.getAbsolutePath());
        PluginManager pluginManager = InitApplication.getPluginManager();
        final int timeoutNum = 4;
        final int waitCount = 1;
        final CountDownLatch waitDown = new CountDownLatch(waitCount);
        pluginManager.enter(context, Constants.FROM_ID_LOAD_MAIN, bundle, new EnterCallback() {
                    @Override
                    public void onShowLoadingView(View view) {
                    }

                    @Override
                    public void onCloseLoadingView() {

                    }

                    @Override
                    public void onEnterComplete() {
                        waitDown.countDown();
                    }
                }
        );
        waitDown.await(timeoutNum, TimeUnit.SECONDS);
        if (waitDown.getCount() == waitCount) {
            throw new TimeoutException("loadMainPlugin time out..  moduleName=" +
                    data.moduleName);
        }
    }

    private static void loadOtherPlugin(Context context, ModuleData data) throws TimeoutException, InterruptedException {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_PLUGIN_ZIP_PATH, data.pluginFile.getAbsolutePath());
        bundle.putString(Constants.KEY_PLUGIN_PART_KEY, data.moduleName);
        PluginManager pluginManager = InitApplication.getPluginManager();
        final int timeoutNum = 8;
        final int waitCount = 1;
        final CountDownLatch waitDown = new CountDownLatch(waitCount);

        pluginManager.enter(context, Constants.FROM_ID_LOAD_PLUGIN, bundle, new EnterCallback() {
                    @Override
                    public void onShowLoadingView(View view) {
                    }

                    @Override
                    public void onCloseLoadingView() {

                    }

                    @Override
                    public void onEnterComplete() {
                        waitDown.countDown();
                    }
                }
        );
        waitDown.await(timeoutNum, TimeUnit.SECONDS);
        if (waitDown.getCount() == waitCount) {
            throw new TimeoutException("loadOtherPlugin time out..  moduleName=" +
                    data.moduleName);
        }
    }
}
