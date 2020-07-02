package com.tencent.shadow.sample.loader;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;

import com.cloudtech.shell.shadow.share.LoadPluginCallback;
import com.tencent.shadow.core.common.InstalledApk;
import com.tencent.shadow.core.load_parameters.LoadParameters;
import com.tencent.shadow.core.loader.ShadowPluginLoader;
import com.tencent.shadow.core.loader.classloaders.PluginClassLoader;
import com.tencent.shadow.core.loader.exceptions.LoadPluginException;
import com.tencent.shadow.core.loader.infos.PluginParts;
import com.tencent.shadow.core.loader.managers.ComponentManager;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;

import static android.content.pm.PackageManager.GET_META_DATA;

/**
 * 这里的类名和包名需要固定
 * com.tencent.shadow.sdk.pluginloader.PluginLoaderImpl
 */
public class SamplePluginLoader extends ShadowPluginLoader {

    private final static String TAG = "shadow";

    private ComponentManager componentManager;

    public SamplePluginLoader(Context hostAppContext) {
        super(hostAppContext);
        componentManager = new SampleComponentManager(hostAppContext);
    }

    @Override
    public ComponentManager getComponentManager() {
        return componentManager;
    }

    @NotNull
    @Override
    public Future<?> loadPlugin(@NotNull final InstalledApk installedApk) {
        LoadParameters loadParameters = getLoadParameters(installedApk);
        final String partKey = loadParameters.partKey;

        LoadPluginCallback.getCallback().beforeLoadPlugin(partKey);

        final Future<?> future = super.loadPlugin(installedApk);

        getMExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    future.get();
                    PluginParts pluginParts = getPluginParts(partKey);
                    String packageName = pluginParts.getApplication().getPackageName();
                    ApplicationInfo applicationInfo = pluginParts.getPluginPackageManager().getApplicationInfo(packageName, GET_META_DATA);
                    PluginClassLoader classLoader = pluginParts.getClassLoader();
                    Resources resources = pluginParts.getResources();

                    LoadPluginCallback.getCallback().afterLoadPlugin(partKey, applicationInfo, classLoader, resources);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        return future;
    }


}
