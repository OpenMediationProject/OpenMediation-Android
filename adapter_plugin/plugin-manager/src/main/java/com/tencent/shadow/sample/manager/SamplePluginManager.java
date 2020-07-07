package com.tencent.shadow.sample.manager;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import com.tencent.shadow.core.manager.installplugin.InstalledPlugin;
import com.tencent.shadow.dynamic.host.EnterCallback;
import com.tencent.shadow.dynamic.loader.PluginServiceConnection;
import com.tencent.shadow.sample.plugin.IMyAidlInterface;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SamplePluginManager extends FastPluginManager {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private Context mCurrentContext;

    public SamplePluginManager(Context context) {
        super(context);
        mCurrentContext = context;
    }

    /**
     * @return PluginManager实现的别名，用于区分不同PluginManager实现的数据存储路径
     */
    @Override
    protected String getName() {
        return "sample-manager";
    }

    /**
     * @return demo插件so的abi
     */
    @Override
    public String getAbi() {
        return Build.CPU_ABI;
    }

    /**
     * @return 宿主中注册的PluginProcessService实现的类名
     */
    @Override
    protected String getPluginProcessServiceName() {
        return "com.cloudtech.shell.shadow.MainPluginProcessService";
    }

    @Override
    public void enter(final Context context, long fromId, Bundle bundle, final EnterCallback callback) {
        if (fromId == Constant.FROM_ID_LOAD_MAIN) {
            onLoadMain(bundle, callback);
        } else if (fromId == Constant.FROM_ID_LOAD_PLUGIN) {
            onLoadPlugin(bundle, callback);
        } else if (fromId == Constant.FROM_ID_START_ACTIVITY) {
            onStartActivity(context, bundle, callback);
        } else if (fromId == Constant.FROM_ID_DELETE_PLUGIN) {

        } else if (fromId == Constant.FROM_ID_CALL_SERVICE) {
            callPluginService(context);
        } else {
            throw new IllegalArgumentException("不认识的fromId==" + fromId);
        }
    }

    private void onLoadMain(Bundle bundle, final EnterCallback callback) {
        final String pluginZipPath = bundle.getString(Constant.KEY_PLUGIN_ZIP_PATH);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    InstalledPlugin installedPlugin = install(pluginZipPath);//安装，这个调用是阻塞的
                    loadPluginLoaderAndRuntime(installedPlugin.UUID);//加载
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                    if (e.getMessage() != null)
                        Log.e("SamplePluginManager", e.getMessage());
                }
                if (callback != null) {
                    Handler uiHandler = new Handler(Looper.getMainLooper());
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onEnterComplete();
                        }
                    });
                }
            }
        });
    }

    private void onLoadPlugin(Bundle bundle, final EnterCallback callback) {
        final String pluginZipPath = bundle.getString(Constant.KEY_PLUGIN_ZIP_PATH);
        final String partKey = bundle.getString(Constant.KEY_PLUGIN_PART_KEY);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    install(pluginZipPath);//安装，这个调用是阻塞的
                    loadPlugin(partKey);//加载
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                    if (e.getMessage() != null)
                        Log.e("SamplePluginManager", e.getMessage());
                }
                if (callback != null) {
                    Handler uiHandler = new Handler(Looper.getMainLooper());
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onEnterComplete();
                        }
                    });
                }
            }
        });
    }

    private void onStartActivity(final Context context, Bundle bundle, final EnterCallback callback) {
        final String pluginZipPath = bundle.getString(Constant.KEY_PLUGIN_ZIP_PATH);
        final String partKey = bundle.getString(Constant.KEY_PLUGIN_PART_KEY);
        final String className = bundle.getString(Constant.KEY_ACTIVITY_CLASSNAME);
        if (className == null) {
            throw new NullPointerException("className == null");
        }
        final Bundle extras = bundle.getBundle(Constant.KEY_EXTRAS);

//        if (callback != null) {
//            final View view = LayoutInflater.from(mCurrentContext).inflate(R.layout.activity_load_plugin, null);
//            callback.onShowLoadingView(view);
//        }

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    install(pluginZipPath);//安装，这个调用是阻塞的
                    loadPlugin(partKey);//加载

                    //调转
                    Intent pluginIntent = new Intent();
                    pluginIntent.setClassName(
                            context.getPackageName(),
                            className
                    );
                    if (extras != null) {
                        pluginIntent.replaceExtras(extras);
                    }
                    startPluginActivity(context, pluginIntent);
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                    if (e.getMessage() != null)
                        Log.e("SamplePluginManager", e.getMessage());
                }
                if (callback != null) {
                    Handler uiHandler = new Handler(Looper.getMainLooper());
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onEnterComplete();
                        }
                    });
                }
            }
        });
    }


    private InstalledPlugin install(String pluginZipPath) throws Exception {
        return installPlugin(pluginZipPath, null, true);//这个调用是阻塞的
    }

    private void callPluginService(final Context context) {
        final String pluginZipPath = "/data/local/tmp/plugin-debug.zip";
        final String partKey = "sample-plugin";
        final String className = "com.tencent.shadow.sample.plugin.MyService";

        Intent pluginIntent = new Intent();
        pluginIntent.setClassName(context.getPackageName(), className);

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    InstalledPlugin installedPlugin
                            = installPlugin(pluginZipPath, null, true);//这个调用是阻塞的

                    loadPlugin(partKey);

                    Intent pluginIntent = new Intent();
                    pluginIntent.setClassName(context.getPackageName(), className);

                    boolean callSuccess = mPluginLoader.bindPluginService(pluginIntent, new PluginServiceConnection() {
                        @Override
                        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                            IMyAidlInterface iMyAidlInterface = IMyAidlInterface.Stub.asInterface(iBinder);
                            try {
                                String s = iMyAidlInterface.basicTypes(1, 2, true, 4.0f, 5.0, "6");
                                Log.i("SamplePluginManager", "iMyAidlInterface.basicTypes : " + s);
                            } catch (RemoteException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName componentName) {
                            throw new RuntimeException("onServiceDisconnected");
                        }
                    }, Service.BIND_AUTO_CREATE);

                    if (!callSuccess) {
                        throw new RuntimeException("bind service失败 className==" + className);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
