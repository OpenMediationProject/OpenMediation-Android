package com.cloudtech.shell;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.cloudtech.shell.ex.FileCurdError;
import com.cloudtech.shell.manager.PluginFileManager;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * Created by jiantao.tu on 2018/5/29.
 */
@RunWith(AndroidJUnit4.class)
public class ClassloaderTest {


    @Test
    public void test1() {
        Context context = InstrumentationRegistry.getTargetContext();
        try {
//            TestBean bean1=new TestBean("",23);
//            Log.w("tjt", "TestBean.classloader=" + bean1.getClass().getClassLoader().toString());
            String path = context.getClassLoader().toString();
            Thread.sleep(5000);
            Log.w("tjt -1", path);

            File dexOutputFile = PluginFileManager.getCachePath(context);
            DexClassLoader dexClassLoader = new DexClassLoader("/data/user/0/com.cloudtech.shell.test/app_libs/cloudmobi/test_1.0.0.dex",
                dexOutputFile.getAbsolutePath(), null, context.getClassLoader());
            Class<?> cl = dexClassLoader.loadClass("com.cloudtech.shell.test.CTService");

            final String newMethodName = "getTest";
            Method method = cl.getDeclaredMethod(newMethodName);
            method.setAccessible(true);
            Object bean =  method.invoke(null);
            Log.w("tjt", "dexClassLoader.classloader=" + cl.getClassLoader().toString());
//            Log.w("tjt", "bean.name=" + bean.getName() + ",bean.age=" + bean.getAge());
            Log.w("tjt", "TestBean.classloader=" + bean.getClass().getClassLoader().toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (FileCurdError fileCurdError) {
            fileCurdError.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
