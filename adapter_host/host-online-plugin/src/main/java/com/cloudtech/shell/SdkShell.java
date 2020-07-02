package com.cloudtech.shell;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Keep;

import com.cloudtech.shell.gp.GpsHelper;
import com.cloudtech.shell.receiver.DebugSwitchReceiver;
import com.cloudtech.shell.utils.ContextHolder;
import com.cloudtech.shell.utils.PreferencesUtils;
import com.nbmediation.sdk.core.SdkShellCallback;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Vincent
 * Email:jingwei.zhang@yeahmobi.com
 */
@Keep
public class SdkShell {

    public static Handler handler = null;

    private static AtomicBoolean isInit = new AtomicBoolean(false);

    public synchronized static void initialize(final Context context, final String slot) {
        initialize(context, slot, null);
    }

    public synchronized static void initialize(final Context context, final String slot, SdkShellCallback callback) {
        if (isInit.compareAndSet(false, true)) {
            if (handler == null) handler = new Handler(Looper.getMainLooper());
            PreferencesUtils.initPrefs(context);
            ContextHolder.init(context);
            PreferencesUtils.putSlotId(slot);
            DebugSwitchReceiver.registerReceiver(context);
            GpsHelper.startLoadGaid();
             /*
                    final int interval = PreferencesUtils.getIntervalSecond();
                    SdkImpl.settingFrequency(interval);
                     */
            SdkImpl.init(callback);
        }
    }



    public synchronized static void close() {
//        DexLoader.clean();
//        ContextHolder.clean();
//        MethodBuilderFactory.clean();
//        PreferencesUtils.clean();
//        ThreadPoolProxy.clean();
//        TimingTaskManager.clean();
//        handler = null;
        Context context = ContextHolder.getGlobalAppContext();
        if (context == null) return;
        DebugSwitchReceiver.unRegisterReceiver(context);
    }


}
