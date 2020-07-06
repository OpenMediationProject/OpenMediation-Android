package com.nbmediation.sdk.mobileads;

import android.content.Context;

import com.bun.miitmdid.core.JLibrary;
import com.bun.miitmdid.core.MdidSdkHelper;
import com.bun.supplier.IIdentifierListener;
import com.bun.supplier.IdSupplier;
import com.nbmediation.sdk.utils.AdLog;

import static com.bun.miitmdid.core.ErrorCode.INIT_ERROR_DEVICE_NOSUPPORT;
import static com.bun.miitmdid.core.ErrorCode.INIT_ERROR_LOAD_CONFIGFILE;
import static com.bun.miitmdid.core.ErrorCode.INIT_ERROR_MANUFACTURER_NOSUPPORT;
import static com.bun.miitmdid.core.ErrorCode.INIT_ERROR_RESULT_DELAY;
import static com.bun.miitmdid.core.ErrorCode.INIT_HELPER_CALL_ERROR;

/**
 * Created by jiantao.tu on 2020/3/23.
 */
public class MDIDHandler {

    public static String MDID = null;

    private static final String MDID_ID_KEY = "mdid_id_key";

    public static void init(Context context) {
        try {
            JLibrary.InitEntry(context);
            int code = MdidSdkHelper.InitSdk(context, true, new IIdentifierListener() {
                @Override
                public void OnSupport(boolean b, IdSupplier idSupplier) {
                    if (idSupplier == null) {
                        return;
                    }
                    String oaid = idSupplier.getOAID();
                    if (oaid.equals("NO")) {
                        oaid = "";
                    }
                    MDID = oaid;
                    AdLog.getSingleton().LogE("mdid: value=" + MDID);
                }
            });
            switch (code) {
                case INIT_ERROR_DEVICE_NOSUPPORT://不支持的设备
                    AdLog.getSingleton().LogE("mdid 不支持的设备");
                    break;
                case INIT_ERROR_LOAD_CONFIGFILE://加载配置文件出错
                    AdLog.getSingleton().LogE("mdid 加载配置文件出错");
                    break;
                case INIT_ERROR_MANUFACTURER_NOSUPPORT://不支持的设备厂商
                    AdLog.getSingleton().LogE("mdid 不支持的设备厂商");
                    break;
                case INIT_ERROR_RESULT_DELAY://获取接口是异步的，结果会在回调中返回，回调执行的回调可能在工作线程
                    AdLog.getSingleton().LogE("mdid 获取接口是异步的，结果会在回调中返回，回调执行的回调可能在工作线程");
                    break;
                case INIT_HELPER_CALL_ERROR://反射调用出错
                    AdLog.getSingleton().LogE("mdid 反射调用出错");
                    break;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static String getMdid() {
        return MDID;
    }
}
