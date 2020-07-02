    package com.cloudtech.shell.utils;

    import android.os.Build;

    import com.cloudtech.shell.BuildConfig;

    public class SwitchConfig {



        /**
         * 日志开关
         */
        public static Boolean LOG = BuildConfig.DEBUG;   //TODO:  日志开关,一直关着就行


        /**
         * 存储responseDate开关
         */
        public static final Boolean DEBUG_USE_EMULATOR = Build.FINGERPRINT.startsWith("generic") || Build.FINGERPRINT.startsWith("Android");


    }
