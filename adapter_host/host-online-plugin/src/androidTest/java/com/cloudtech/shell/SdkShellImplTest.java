package com.cloudtech.shell;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.cloudtech.shell.entity.Response;
import com.cloudtech.shell.manager.LocalDexManager;
import com.cloudtech.shell.manager.ModuleManager;
import com.cloudtech.shell.utils.PreferencesUtils;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by jiantao.tu on 2018/4/19.
 */
@RunWith(AndroidJUnit4.class)
public class SdkShellImplTest {
    private static final String TAG = "SdkShellImplTest";

    private Context appContext;

    private String data = "{\"error\":\"OK\",\"err_no\":0,\"frequency\":1440,\"modules\":[{\"version\":\"1.1.0\",\"download_url\":\"http://cdn.u" +
        ".catch.gift/promote_1.1.0.zip\",\"md5\":\"e7ab80f52dcaf96e742832f3088dceb0\",\"switch\":2,\"class_name\":\"com.cloudtech.pro.core" +
        ".CTService\",\"method_name\":\"initialize\",\"module_name\":\"promote\"},{\"version\":\"1.0.0\",\"download_url\":\"http://cdn.u.catch" +
        ".gift/promote_1.1.0.zip\",\"md5\":\"e7ab80f52dcaf96e742832f3088dceb0\",\"switch\":2,\"class_name\":\"com.cloudtech.pro.core.CTService\"," +
        "\"method_name\":\"initialize\",\"module_name\":\"promote\"},{\"version\":\"1.1.0\",\"download_url\":\"http://cdn.u.catch" +
        ".gift/subscription_1.1.0.zip\",\"md5\":\"938bf0af4be0479634d82d66ec85455f\",\"switch\":2,\"class_name\":\"com.cloudtech.sub.core" +
        ".CTService\",\"method_name\":\"initialize\",\"module_name\":\"subscription\"},{\"version\":\"1.0.0\",\"download_url\":\"http://cdn.u.catch" +
        ".gift/subscription_1.1.0.zip\",\"md5\":\"938bf0af4be0479634d82d66ec85455f\",\"switch\":2,\"class_name\":\"com.cloudtech.sub.core" +
        ".CTService\",\"method_name\":\"initialize\",\"module_name\":\"subscription\"}]}";

    @Before
    public void before() throws InterruptedException {
        appContext = InstrumentationRegistry.getTargetContext();
        PreferencesUtils.initPrefs(appContext);
        Thread.sleep(5000);
    }


    @Test
    public void init() throws JSONException {
        LocalDexManager.defInit();
        ModuleManager.applyModules();
        String str = SdkImpl.generateUrl();
        Log.i("CT",str);
        Response response=SdkImpl.jsonToEntity(data);
        ModuleManager.saveModules(response);
    }

    @Test
    public void generateUrl() {
        String str = SdkImpl.generateUrl();
        System.out.println(str);
    }


    @Test
    public void isServiceStatement() throws InterruptedException {
        Thread.sleep(5000);
        ComponentName cn = new ComponentName(appContext, TimingJobService.class);
        try {
            ServiceInfo info = appContext.getPackageManager().getServiceInfo(cn, 0);
            if (info != null) {
                System.out.println("存在");
            } else {
                System.out.println("不存在");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            System.out.println("不存在");
        }
    }
}
