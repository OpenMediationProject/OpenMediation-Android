package com.nbmediation.sdk.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


/**
 * 调试开关, 内部测试用,不对外开放
 * adb shell am broadcast -a com.nbmediation.sdk.Debug --ez LOG true    开启日志
 * adb shell am broadcast -a com.nbmediation.sdk.Debug --ez TEST true   开启测试模式
 */
public class DebugSwitchApi {

    private static final String ACTION = "com.nbmediation.sdk.Debug";
    private static final String SWITCH_LOG = "LOG";
    private static final String SWITCH_TEST = "TEST";
    private static boolean hasRegister = false;

    public static void registerReceiver(Context context) {
        if (hasRegister) {         //已经注册,就不用再注册了
            return;
        }
        hasRegister = true;
        AdLog.getSingleton().LogD("DebugSwitchApi >> registerReceiver");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION);
        context.registerReceiver(debugBroadcastReceiver, intentFilter);

    }

    public static void unRegisterReceiver(Context context) {
        try {
            context.unregisterReceiver(debugBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private static BroadcastReceiver debugBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.hasExtra(SWITCH_LOG)) {       //
                boolean logSwitch = intent.getBooleanExtra(SWITCH_LOG, false);
                AdLog.getSingleton().LogD("DebugSwitchApi::LogSwitch %s", "--" + logSwitch);

                AdLog.getSingleton().isDebug(logSwitch);
                DeveloperLog.enableDebug(context, logSwitch);
            } else if (intent.hasExtra(SWITCH_TEST)) {
                boolean isDebug = intent.getBooleanExtra(SWITCH_TEST, false);
                AdLog.getSingleton().LogD("DebugSwitchApi::TestSwitch %s", "--" + isDebug);
                DeveloperLog.enableDebug(context, isDebug);

            }
        }

    };

}