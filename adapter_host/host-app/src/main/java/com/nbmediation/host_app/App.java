package com.nbmediation.host_app;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.cloudtech.shell.SdkShell;


/**
 * Created by jiantao.tu on 2020/6/10.
 */
public class App extends Application {


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        InitApplication.onApplicationCreate(this);
    }


}
