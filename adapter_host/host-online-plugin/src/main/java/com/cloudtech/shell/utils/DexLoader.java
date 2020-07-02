package com.cloudtech.shell.utils;

import android.content.Context;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.cloudtech.shell.entity.ModuleData;
import com.cloudtech.shell.ex.FileCurdError;
import com.cloudtech.shell.listener.DexCloseListener;
import com.cloudtech.shell.receiver.DexCloseBroadcastReceiver;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import dalvik.system.DexClassLoader;

import com.cloudtech.shell.manager.PluginFileManager;

/**
 * Created by jiantao.tu on 2018/4/2.
 */
public class DexLoader {

    private static final String TAG = "DexLoader";

    private static Map<String, DexClassLoader> DEX_LOADS = null;

    public static String getVersion(Context context, String moduleName, String version, String className) throws
            IllegalAccessException, FileCurdError, FileNotFoundException, NoSuchMethodException, InvocationTargetException,
            ClassNotFoundException {
        if (context == null || TextUtils.isEmpty(moduleName)) throw
                new NullPointerException("context or moduleName is null");
        Object obj = executeCall(context, moduleName, version, className, "getVersion", null);
        return obj.toString();
    }

    public static void initialize(Context context, String moduleName, String version, String className, String slot)
            throws FileCurdError, IllegalAccessException, FileNotFoundException, NoSuchMethodException,
            InvocationTargetException, ClassNotFoundException {
        if (context == null || TextUtils.isEmpty(moduleName) || TextUtils.isEmpty(slot)) throw
                new NullPointerException("context or moduleName or slot is null");
        executeCall(context, moduleName, version, className, "initialize", new Class[]{Context.class, String.class},
                context, slot);
    }

    /**
     * 这个方法要在非UI线程上执行
     *
     * @param context
     * @param moduleName
     * @throws TimeoutException
     * @throws FileCurdError
     * @throws InterruptedException
     * @throws IllegalAccessException
     * @throws FileNotFoundException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws ClassNotFoundException
     */
    @Deprecated
    public synchronized static void remove(Context context, final String moduleName, String version, String className) throws
            TimeoutException, FileCurdError, InterruptedException, IllegalAccessException, FileNotFoundException, NoSuchMethodException,
            InvocationTargetException, ClassNotFoundException {
        if (context == null || TextUtils.isEmpty(moduleName))
            throw new NullPointerException("context or moduleName is null");
        final int timeoutNum = 12;
        final int waitCount = 1;
        final CountDownLatch waitDown = new CountDownLatch(waitCount);
        DexCloseBroadcastReceiver receiver = new DexCloseBroadcastReceiver(new DexCloseListener() {
            @Override
            public void onCloseOk() {
                YeLog.i(TAG, "come have closeBroadcast.. moduleName=" + moduleName);
                waitDown.countDown();
            }
        });
        IntentFilter filter = new IntentFilter(DexCloseBroadcastReceiver.ACTION);
        context.registerReceiver(receiver, filter);
        try {
            executeCall(context, moduleName, version, className, "close", new Class[]{Context.class}, context);
        } catch (Throwable e) {
            context.unregisterReceiver(receiver);
            YeLog.i(TAG, "deleteDexFile and unregisterReceiver1 moduleName=" + moduleName);
            throw e;
        }
        waitDown.await(timeoutNum, TimeUnit.SECONDS);
        try {
            if (waitDown.getCount() == waitCount) {
                throw new TimeoutException("listener dex close time out..  moduleName=" +
                        moduleName);
            }
            PluginFileManager.deleteFile(context, moduleName, version, null);
        } finally {
            if (DEX_LOADS != null) DEX_LOADS.remove(moduleName);
            context.unregisterReceiver(receiver);
            YeLog.i(TAG, "deleteDexFile and unregisterReceiver2 moduleName=" + moduleName);
        }
    }

    public static Object executeCall(Context context, String moduleName, String version, String className,
                                     String methodName, Class[] classes, Object... objects)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, FileNotFoundException
            , FileCurdError, ClassNotFoundException {

        ModuleData data = PluginFileManager.getModuleMake(context, moduleName, version, className, methodName, null);
        DexClassLoader dexClassLoader;
        if (DEX_LOADS == null) DEX_LOADS = new HashMap<>();
        if (DEX_LOADS.get(moduleName) == null) {
            File dexOutputFile = PluginFileManager.getCachePath(context);
            dexClassLoader = new DexClassLoader(data.pluginFile.getAbsolutePath(),
                    dexOutputFile.getAbsolutePath(), null, context.getClassLoader());
            DEX_LOADS.put(moduleName, dexClassLoader);
        } else dexClassLoader = DEX_LOADS.get(moduleName);
        Class<?> cl = dexClassLoader.loadClass(data.className);
        Method method;

        final String newMethodName;
        if (methodName == null) {
            newMethodName = data.methodName;
        } else {
            newMethodName = methodName;
        }
        if (classes != null && classes.length > 0) {
            method = cl.getDeclaredMethod(newMethodName, classes);
        } else {
            method = cl.getDeclaredMethod(newMethodName);
        }
        method.setAccessible(true);
        if (objects != null && objects.length > 0) {
            return method.invoke(null, objects);
        } else {
            return method.invoke(null);
        }
    }


    public synchronized static void clean() {
        DEX_LOADS.clear();
        DEX_LOADS = null;
    }

}
