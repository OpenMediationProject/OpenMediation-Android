package com.cloudtech.shell.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.cloudtech.shell.utils.SwitchConfig;
import com.cloudtech.shell.utils.YeLog;

import java.util.Arrays;
import java.util.UUID;


/**
 * 调试开关, 内部测试用,不对外开放
 * Created by huangdong on 16/8/31.
 * adb shell am broadcast -a com.cloudtech.ads.Debug --ez LOG true    开启日志
 * "ct"/"fb"/"ad"
 */
public class DebugSwitchReceiver {

    private static final String ACTION = "com.cloudtech.ads.Debug";
    private static final String SWITCH_LOG = "LOG";
    private static boolean hasRegister = false;

    public static void registerReceiver(Context context) {
        if (hasRegister) {     //已经注册,就不用再注册了
            return;
        }
        hasRegister = true;
        YeLog.i("DebugSwitchReceiver >> registerReceiver");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION);
        context.registerReceiver(debugBroadcastReceiver, intentFilter);

    }

    public static void unRegisterReceiver(Context context) {
        try {
            if (hasRegister) {     //已经注册,就不用再注册了
                context.unregisterReceiver(debugBroadcastReceiver);
            }
        } catch (Exception e) {
            Log.e("tjt ", "debugBroadcastReceiver error");
            e.printStackTrace();
        }

    }


    private static BroadcastReceiver debugBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(SWITCH_LOG)) {       //
                boolean logSwitch = intent.getBooleanExtra(SWITCH_LOG, false);
                SwitchConfig.LOG = logSwitch;
                YeLog.i("DebugSwitchReceiver::LogSwitch=" + logSwitch);

            }

        }

    };


}
