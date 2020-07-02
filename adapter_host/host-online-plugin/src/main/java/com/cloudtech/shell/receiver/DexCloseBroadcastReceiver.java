package com.cloudtech.shell.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cloudtech.shell.listener.DexCloseListener;
import com.cloudtech.shell.utils.YeLog;

/**
 * Created by jiantao.tu on 2018/4/8.
 */
@Deprecated
public class DexCloseBroadcastReceiver extends BroadcastReceiver {


    private DexCloseListener listener;

    public DexCloseBroadcastReceiver(DexCloseListener listener) {
        this.listener = listener;
    }

    public final static String ACTION = "com.yeahmobi.DexCloseBroadcastReceiver";

    public final static String PARAMS_KEY = "data";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION.equals(intent.getAction())) {
            YeLog.i("DexCloseBroadcastReceiver go-go-go package="+context.getPackageName());
            listener.onCloseOk();
        }
    }
}