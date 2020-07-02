package com.cloudtech.shell;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.cloudtech.shell.ex.DataFormatException;
import com.cloudtech.shell.ex.FileCurdError;
import com.cloudtech.shell.ex.NotMakeException;
import com.cloudtech.shell.listener.DexCloseListener;
import com.cloudtech.shell.receiver.DexCloseBroadcastReceiver;
import com.cloudtech.shell.utils.DexLoader;
import com.cloudtech.shell.utils.PreferencesUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeoutException;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class DexLoaderTest {

    private static final String TAG="DexLoaderTest";

    private static final String MODULE_NAME="test";

    private Context appContext;

    @Before
    public void before() throws InterruptedException {
        appContext = InstrumentationRegistry.getTargetContext();
        Thread.sleep(5000);
        PreferencesUtils.initPrefs(appContext);
    }

    @Test
    public void getVersion() {
        Log.i(TAG,"getVersion");
        try {
            try {
                String version=DexLoader.getVersion(appContext,MODULE_NAME,"1.0.0","com.cloudtech.shell.test.CTService");
                Log.i(TAG,"version="+version);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (FileCurdError fileCurdError) {
            fileCurdError.printStackTrace();
        }
    }

    @Test
    public void initialize()  {
        Log.i(TAG,"initialize");
        try {
            try {
                DexLoader.initialize(appContext,MODULE_NAME,"1.0.0","com.cloudtech.shell.test.CTService","246");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (FileCurdError fileCurdError) {
            fileCurdError.printStackTrace();
        }
    }

    @Test
    public void close(){
        try {
            try {
                DexLoader.remove(appContext,MODULE_NAME,"1.0.0","com.cloudtech.shell.test.CTService");
            } catch (TimeoutException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (FileCurdError fileCurdError) {
            fileCurdError.printStackTrace();
        }
        Log.i(TAG,"close");
    }


    @Test
    public void sendBroadcast() throws InterruptedException {
        DexCloseBroadcastReceiver receiver = new DexCloseBroadcastReceiver(new DexCloseListener() {
            @Override
            public void onCloseOk() {
                Log.i("tjt","广播进来了");
            }
        });
        IntentFilter filter = new IntentFilter(DexCloseBroadcastReceiver.ACTION);
        appContext.registerReceiver(receiver, filter);
        Intent intent=new Intent();
//        intent.setComponent(new ComponentName(appContext.getPackageName(),"com.cloudtech.shell" +
//                ".DexCloseBroadcastReceiver"));
//        intent.setAction(DexCloseBroadcastReceiver.ACTION);
        intent.setAction(DexCloseBroadcastReceiver.ACTION);
        Log.i("tjt",appContext.getPackageName());
        appContext.sendBroadcast(intent);
        Thread.sleep(20000);
//        context=null;
    }
}
