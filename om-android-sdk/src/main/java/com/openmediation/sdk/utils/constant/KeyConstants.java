// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.constant;


import com.openmediation.sdk.utils.device.DeviceUtil;

/**
 * KeyConstants
 */
public interface KeyConstants {

    /**
     * The constant KEY_CONFIGURATION.
     */
    String KEY_CONFIGURATION = "Config";
    /**
     * The constant KEY_APP_KEY.
     */
    String KEY_APP_KEY = "AppKey";

    /**
     * The constant KEY_APP_CHANNEL.
     */
    String KEY_APP_CHANNEL = "AppChannel";

    String KEY_INIT_HOST = "InitHost";

    String KEY_DISPLAY_SCENE = "display_sceneName";
    String KEY_DISPLAY_ABT = "display_abt";
    /**
     * The accumulated value of the user ad revenue
     */
    String KEY_REVENUE = "revenue";

    /**
     * The interface Request.
     */
    interface Request {
        /**
         * The constant KEY_API_VERSION.
         */
        String KEY_API_VERSION = "v";
        /**
         * The constant KEY_PLATFORM.
         */
        String KEY_PLATFORM = "plat";
        /**
         * The constant KEY_SDK_VERSION.
         */
        String KEY_SDK_VERSION = "sdkv";
        /**
         * The constant KEY_APP_KEY.
         */
        String KEY_APP_KEY = "k";
    }

    /**
     * Keys for server request body json+gzip+aes
     */
    interface RequestBody {
        /**
         * The constant KEY_TS.
         */
        /*@see System.currentTimeMillis() long      app time in mills 1567479919643  must */
        String KEY_TS = "ts";

        /**
         * The constant KEY_FLT.
         */
        String KEY_FLT = "flt";

        /**
         * The constant KEY_FIT.
         */
        String KEY_FIT = "fit";

        /**
         * The constant KEY_ZO.
         */
        /*@see DeviceUtil.getTimeZoneOffset    int       TimeZoneOffset in minutes,e.g. UTC+0800 zo=480 must */
        String KEY_ZO = "zo";

        /**
         * The constant KEY_TZ.
         */
        /*@see DeviceUtil.getTimeZone   String  localTimeZone.name,TimeZone.getDefault().getID()    Asia/Shanghai must*/
        String KEY_TZ = "tz";

        /**
         * The constant KEY_SESSION.
         */
        /*String    session ID, UUID generated at App init   BBD6E1CD-8C4B-40CB-8A62-4BBC7AFE07D6  must*/
        String KEY_SESSION = "session";

        /**
         * The constant KEY_UID.
         */
        /*String    unique user ID by SDK   BBD6E1CD-8C4B-40CB-8A62-4BBC7AFE07D6*/
        String KEY_UID = "uid";

        /**
         * The constant KEY_DID.
         */
        /*String    device ID, combined with dtype  BBD6E1CD-8C4B-40CB-8A62-4BBC7AFE07D6*/
        String KEY_DID = "did";

        /**
         * The constant KEY_DTYPE.
         */
        /*int       device type, 1:IDFA, 2:GAID, 3:FBID, 4:HUAWEIID   1*/
        String KEY_DTYPE = "dtype";

        /**
         * The constant KEY_JB.
         */
        /*int      jailbreak;//  0:normal, 1:jailbreak   1*/
        String KEY_JB = "jb";

        /**
         * The constant KEY_LANG.
         */
        /*@see DeviceUtil.getLanguageCountry String    device language code   en_US*/
        String KEY_LANG = "lang";

        /**
         * The constant KEY_LANG_NAME.
         */
        /*@see DeviceUtil.getLanguage String    device language name   English*/
        String KEY_LANG_NAME = "langname";

        /**
         * The constant KEY_LCOUNTRY.
         */
        /*@see DeviceUtil.getCountry  String   [[NSLocale currentLocale]localeIdentifier],Locale.getCountry()     CN*/
        String KEY_LCOUNTRY = "lcountry";

        /**
         * The constant KEY_BUNDLE.
         */
        /*@see AdtUtil.getApplication().getPackageName String   app bunle name  com.xxx.xxx */
        String KEY_BUNDLE = "bundle";

        /**
         * The constant KEY_MAKE.
         */
        /*@see Build.MANUFACTURER   String    device make  samsung*/
        String KEY_MAKE = "make";

        /**
         * The constant KEY_BRAND.
         */
        /*@see Build.BRAND    String    device brand  samsung*/
        String KEY_BRAND = "brand";

        /**
         * The constant KEY_MODEL.
         */
        /*@see Build.MODEL  String    device model  iPhone10,3*/
        String KEY_MODEL = "model";

        /**
         * The constant KEY_OSV.
         */
        /*@see Build.VERSION.RELEASE  String    OS version   12.1*/
        String KEY_OSV = "osv";

        /**
         * The constant KEY_BUILD.
         */
        /*@see Build.DEVICE  String    system build #, Android: ro.build.display.id   16A366*/
        String KEY_BUILD = "build";

        /**
         * The constant KEY_APPK.
         */
        /*APP KEY*/
        String KEY_APPK = "appk";

        /**
         * The constant KEY_APPV.
         */
        /*@see DeviceUtil.getVersionName String    App version   1.0*/
        String KEY_APPV = "appv";

        /**
         * The constant KEY_W.
         */
        /*@see DensityUtil.getPhoneWidth int  DisplayMetrics.widthPixels    1028 */
        String KEY_W = "width";

        /**
         * The constant KEY_H.
         */
        /*@see DensityUtil.getPhoneHeight int    DisplayMetrics.heightPixels  2094*/
        String KEY_H = "height";

        /**
         * The constant KEY_LIP.
         */
        /*String   LAN ip   192.168.1.101*/
        String KEY_LIP = "lip";

        /**
         * The constant KEY_CONT.
         */
        /*@see NetworkChecker.getNetworkType  int       ConnectionType   4*/
        String KEY_CONT = "contype";

        /**
         * The constant KEY_CARRIER.
         */
        /*@see NetworkChecker.getNetworkOperator  String    NetworkOperatorName with mccmnc NetworkOperatorName   46002China Mobile*/
        String KEY_CARRIER = "carrier";

        /**
         * The constant KEY_FM.
         */
        /*@see Density.getFm  int     free storage size in MB     17799*/
        String KEY_FM = "fm";

        /**
         * The constant KEY_BATTERY.
         */
        /*int       in percentage    52*/
        String KEY_BATTERY = "battery";

        /**
         * The constant KEY_BTCH.
         */
        /*int       charging battery, 0:No,1:Yes    1*/
        String KEY_BTCH = "btch";

        /**
         * The constant KEY_LOWP.
         */
        /*int       low power, 0:No,1:Yes*/
        String KEY_LOWP = "lowp";

        String KEY_LCY = "lcy";

        /**********************   for Config(init) server API     */
        String KEY_ADNS = "adns";           //List<AdNetwork>  mediated AdNetworks
        String KEY_BTIME = "btime";
        String KEY_RAM = "ram";
        String KEY_TDM = "tdm";
        String KEY_IFGP = "ifgp";
        /**
         * The constant KEY_ANDROID.
         */
        String KEY_ANDROID = "android";    //Android   for Android-only

        /************************     for cl,lr server API        */
        /*int32     placement ID      2345*/
        String KEY_PID = "pid";
        /**
         * The constant KEY_ACT.
         */
        /*int8      load activation type, [1:init,2:interval,3:adclose,4:manual]*/
        String KEY_ACT = "act";

        /**
         * The constant KEY_SCENE.
         */
        String KEY_SCENE = "scene";

        /************************     for cl server API       */
        /*float     IAP, inAppPurchase               1*/
        String KEY_IAP = "iap";
        /*string    AppIDs to be banned, sperated by comma      com.bb,com.ee*/
        String KEY_BA = "ba";
        /**
         * The constant KEY_IMPRTIMES.
         */
        /*int32      placement Impression Times for the day   5*/
        String KEY_IMPRTIMES = "imprTimes";

        /************************     for cl server API       */
        /*AdNetworkID*/
        String KEY_MID = "mid";
        /**
         * The constant KEY_IID.
         */
        /*InstanceID*/
        String KEY_IID = "iid";

        /**
         * The constant KEY_TYPE.
         */
        String KEY_TYPE = "type";

        String KEY_BID = "bid";
        /**
         * The constant KEY_CHANNEL.
         */
        String KEY_CHANNEL = "cnl";

        String KEY_CDID = "cdid";

        /*Age*/
        String KEY_AGE = "age";
        /*Gender*/
        String KEY_GENDER = "gender";

        /*Regs*/
        String KEY_REGS = "regs";
        /*GDPR*/
        String KEY_GDPR = "gdpr";
        /*COPPA*/
        String KEY_COPPA = "coppa";
        /*CCPA*/
        String KEY_CCPA = "ccpa";

        String KEY_GAID = "AdvertisingId";

        /**
         * AppsFlyer ID
         */
        String KEY_AF_ID = "afid";

        /*int32     noGooglePlay flag, 0 or missing: GP installed, 1:GP not installed   1*/
        String KEY_NG = "ng";

        /*EXT*/
        String KEY_EXT = "ext";

        /*TAGS*/
        String KEY_TAGS = "tags";

        /*AuctionID*/
        String KEY_REQ_ID = "reqId";

        /*Mediation Rule ID*/
        String KEY_RULE_ID = "ruleId";

        /*Instance Impression Revenue*/
        String KEY_INSTANCE_REVENUE = "revenue";

        /*Instance Revenue Precision*/
        String KEY_INSTANCE_PRECISION = "rp";

        /*Instance Priority*/
        String KEY_INSTANCE_PRIORITY = "ii";
    }

    /**
     * The interface Ad network.
     */
    interface AdNetwork {
        /**
         * The constant KEY_MID.
         */
        String KEY_MID = "mid";             // int       AdNetwork ID       1
        /**
         * The constant KEY_ADAPTER_V.
         */
        String KEY_ADAPTER_V = "adapterv";//String      AdNetwork Adapter Version      1.0.1
        /**
         * The constant KEY_MSDKV.
         */
        String KEY_MSDKV = "msdkv";        //String     AdNetwork SDK Version             3.2.1
    }

    /**
     * Keys for server request body json+gzip+aes, server init API
     */
    interface Android {
        /**
         * The constant KEY_DEVICE.
         */
        /*@see Build.Device     string     Build.DEVICE                           lteub*/
        String KEY_DEVICE = "device";

        /**
         * The constant KEY_PRODUCE.
         */
        /*@see Build.PRODUCT     string     Build.PRODUCT                          a6plteub*/
        String KEY_PRODUCE = "product";

        /**
         * The constant KEY_SD.
         */
        /* int        [0,1,2]                      2*/
        String KEY_SD = "screen_density";

        /**
         * The constant KEY_SS.
         */
        /* int       [1,2,3,4]                     2*/
        String KEY_SS = "screen_size";

        /**
         * The constant KEY_CPU_ABI.
         *
         * @see DeviceUtil#getSystemProperties DeviceUtil#getSystemProperties;
         * @see KeyConstants.Device#KEY_RO_CPU_ABI KeyConstants.Device#KEY_RO_CPU_ABI string     ro.product.cpu.abi       armeabi-v7a
         */
        String KEY_CPU_ABI = "cpu_abi";

        /**
         * The constant KEY_CPU_ABI2.
         *
         * @see DeviceUtil#getSystemProperties DeviceUtil#getSystemProperties;
         * @see KeyConstants.Device#KEY_RO_CPU_ABI2 KeyConstants.Device#KEY_RO_CPU_ABI2 string     ro.product.cpu.abi2                    armeabi
         */
        String KEY_CPU_ABI2 = "cpu_abi2";

        /**
         * The constant KEY_CPU_ABI_LIST.
         *
         * @see DeviceUtil#getSystemProperties DeviceUtil#getSystemProperties;
         * @see KeyConstants.Device#KEY_RO_CPU_ABI_LIST KeyConstants.Device#KEY_RO_CPU_ABI_LIST string     ro.product.cpu.abilist
         */
        String KEY_CPU_ABI_LIST = "cpu_abilist";

        /**
         * The constant KEY_CPU_ABI_LIST_32.
         *
         * @see DeviceUtil#getSystemProperties DeviceUtil#getSystemProperties;
         * @see KeyConstants.Device#KEY_RO_CPU_ABI_LIST_32 KeyConstants.Device#KEY_RO_CPU_ABI_LIST_32 string    ro.product.cpu.abilist32
         */
        String KEY_CPU_ABI_LIST_32 = "cpu_abilist32";

        /**
         * The constant KEY_CPU_ABI_LIST_64.
         *
         * @see DeviceUtil#getSystemProperties DeviceUtil#getSystemProperties;
         * @see KeyConstants.Device#KEY_RO_CPU_ABI_LIST_64 KeyConstants.Device#KEY_RO_CPU_ABI_LIST_64 string    ro.product.cpu.abilist64
         */
        String KEY_CPU_ABI_LIST_64 = "cpu_abilist64";

        /**
         * The constant KEY_API_LEVEL.
         *
         * @see DeviceUtil#getSystemProperties DeviceUtil#getSystemProperties;
         * @see KeyConstants.Device#KEY_RO_CPU_ABI_LIST_64 KeyConstants.Device#KEY_RO_CPU_ABI_LIST_64 int32     Android API Level                       26
         */
        String KEY_API_LEVEL = "api_level";

        /**
         * The constant KEY_D_DPI.
         */
        /*@see getDensityDpi int32      DisplayMetrics.densityDpi              420*/
        String KEY_D_DPI = "d_dpi";

        /**
         * The constant KEY_DIM_SIZE.
         */
        /*int32     WebViewBridge.getScreenMetrics().size   2*/
        String KEY_DIM_SIZE = "dim_size";

        /**
         * The constant KEY_XDP.
         */
        /*@see DensityUtil.getXDpi  string    DisplayMetrics.xdpi          268.941*/
        String KEY_XDP = "xdp";

        /**
         * The constant KEY_YDP.
         */
        /*@see DensityUtil.getYDpi  string     DisplayMetrics.ydpi         268.694*/
        String KEY_YDP = "ydp";

        /**
         * The constant KEY_DFPID.
         */
        /*@see DeviceUtil.getUniquePsuedoId     string     deviceFingerPrintId, getUniquePsuedoId*/
        String KEY_DFPID = "dfpid";

        /**
         * The constant KEY_TIME_ZONE.
         */
        /*@see DeviceUtil.getTimeZone string     TimeZone.getDefault().getID()         Asia/Shanghai*/
        String KEY_TIME_ZONE = "time_zone";

        /**
         * The constant KEY_ARCH.
         *
         * @see DeviceUtil#getSystemProperties DeviceUtil#getSystemProperties;
         * @see KeyConstants.Device#KEY_RO_ARCH KeyConstants.Device#KEY_RO_ARCH
         * @see Device#KEY_RO_ARCH Device#KEY_RO_ARCHstring  ro.arch
         */
        String KEY_ARCH = "arch";

        /**
         * The constant KEY_CHIPNAME.
         *
         * @see DeviceUtil#getSystemProperties DeviceUtil#getSystemProperties;
         * @see KeyConstants.Device#KEY_RO_CHIPNAME KeyConstants.Device#KEY_RO_CHIPNAME string     ro.chipname
         */
        String KEY_CHIPNAME = "chipname";

        /**
         * The constant KEY_BRIDGE.
         *
         * @see DeviceUtil#getSystemProperties DeviceUtil#getSystemProperties;
         * @see KeyConstants.Device#KEY_RO_BRIDGE KeyConstants.Device#KEY_RO_BRIDGE string     ro.dalvik.vm.native.bridge
         */
        String KEY_BRIDGE = "bridge";

        /**
         * The constant KEY_BRIDGE_EXEC.
         *
         * @see DeviceUtil#getSystemProperties DeviceUtil#getSystemProperties;
         * @see KeyConstants.Device#KEY_RO_BRIDGE_EXEC KeyConstants.Device#KEY_RO_BRIDGE_EXEC string     ro.enable.native.bridge.exec
         */
        String KEY_BRIDGE_EXEC = "bridge_exec";

        /**
         * The constant KEY_ZYGOTE.
         *
         * @see DeviceUtil#getSystemProperties DeviceUtil#getSystemProperties;
         * @see KeyConstants.Device#KEY_RO_ZYGOTE KeyConstants.Device#KEY_RO_ZYGOTE string     ro.zygote
         */
        String KEY_ZYGOTE = "zygote";

        /**
         * The constant KEY_MOCK_LOCATION.
         *
         * @see DeviceUtil#getSystemProperties DeviceUtil#getSystemProperties;
         * @see KeyConstants.Device#KEY_RO_MOCK_LOCATION KeyConstants.Device#KEY_RO_MOCK_LOCATION string     ro.allow.mock.location
         */
        String KEY_MOCK_LOCATION = "mock_location";

        /**
         * The constant KEY_ISA_ARM.
         *
         * @see DeviceUtil#getSystemProperties DeviceUtil#getSystemProperties;
         * @see KeyConstants.Device#KEY_RO_ISA_ARM KeyConstants.Device#KEY_RO_ISA_ARM string     ro.dalvik.vm.isa.arm
         */
        String KEY_ISA_ARM = "isa_arm";

        /**
         * The constant KEY_BUILD_USER.
         *
         * @see DeviceUtil#getSystemProperties DeviceUtil#getSystemProperties;
         * @see KeyConstants.Device#KEY_RO_BUILD_USER KeyConstants.Device#KEY_RO_BUILD_USER string     ro.build.user
         */
        String KEY_BUILD_USER = "build_user";

        /**
         * The constant KEY_KERNEL_QEMU.
         *
         * @see DeviceUtil#getSystemProperties DeviceUtil#getSystemProperties;
         * @see KeyConstants.Device#KEY_RO_KERNEL_QEMU KeyConstants.Device#KEY_RO_KERNEL_QEMU string     ro.kernel.qemu
         */
        String KEY_KERNEL_QEMU = "kernel_qemu";

        /**
         * The constant KEY_HARDWARE.
         *
         * @see DeviceUtil#getSystemProperties DeviceUtil#getSystemProperties;
         * @see KeyConstants.Device#KEY_RO_HARDWARE KeyConstants.Device#KEY_RO_HARDWARE string     ro.hardware
         */
        String KEY_HARDWARE = "hardware";

        /**
         * The constant KEY_NATIVE_BRIDGE.
         *
         * @see DeviceUtil#getSystemProperties DeviceUtil#getSystemProperties;
         * @see KeyConstants.Device#KEY_NATIVE_BRIDGE KeyConstants.Device#KEY_NATIVE_BRIDGE string     persist.sys.nativebridge
         */
        String KEY_NATIVE_BRIDGE = "nativebridge";

        /**
         * The constant KEY_ISA_X86_FEATURES.
         */
        /*string     dalvik.vm.isa.x86.features*/
        String KEY_ISA_X86_FEATURES = "isax86_features";

        /**
         * The constant KEY_ISA_X86_VARIANT.
         */
        /*string     dalvik.vm.isa.x86.variant*/
        String KEY_ISA_X86_VARIANT = "isa_x86_variant";

        /**
         * The constant KEY_ISA_ARM_FEATURES.
         */
        /*string     dalvik.vm.isa.arm.features*/
        String KEY_ISA_ARM_FEATURES = "isa_arm_features";

        /**
         * The constant KEY_ISA_ARM_VARIANT.
         */
        /*string     dalvik.vm.isa.arm.variant*/
        String KEY_ISA_ARM_VARIANT = "isa_arm_variant";

        /**
         * The constant KEY_ISA_ARM64_FEATURES.
         */
        /*string     dalvik.vm.isa.arm64.features*/
        String KEY_ISA_ARM64_FEATURES = "isa_arm64_features";

        /**
         * The constant KEY_ISA_ARM64_VARIANT.
         */
        /*string     dalvik.vm.isa.arm64.variant*/
        String KEY_ISA_ARM64_VARIANT = "isa_arm64_variant";

        /**
         * The constant KEY_SENSOR_SIZE.
         */
        /*@see DeviceUtil.getSensorSize     int32                                    18*/
        String KEY_SENSOR_SIZE = "sensor_size";

        /**
         * The constant KEY_SENSORS.
         */
        /*@see DeviceUtil.getSensorList     Array of Sensors              */
        String KEY_SENSORS = "sensors";

        /**
         * The constant KEY_FB_ID.
         */
        /*@see DeviceUtil.getFacebookId     string     FacebookID*/
        String KEY_FB_ID = "fb_id";

        /**
         * The constant KEY_AS.
         */
        String KEY_AS = "as";
    }

    /**
     * Keys for SystemProperties
     */
    interface Device {
        /**
         * The constant KEY_RO_CPU_ABI.
         */
        /*@see DeviceUtil.getSystemProperties  string     ro.product.cpu.abi                     armeabi-v7a*/
        String KEY_RO_CPU_ABI = "ro.product.cpu.abi";

        /**
         * The constant KEY_RO_CPU_ABI2.
         */
        /*@see DeviceUtil.getSystemProperties  string     ro.product.cpu.abi2                    armeabi*/
        String KEY_RO_CPU_ABI2 = "ro.product.cpu.abi2";

        /**
         * The constant KEY_RO_CPU_ABI_LIST.
         */
        /*@see DeviceUtil.getSystemProperties  string     ro.product.cpu.abilist*/
        String KEY_RO_CPU_ABI_LIST = "ro.product.cpu.abilist";

        /**
         * The constant KEY_RO_CPU_ABI_LIST_32.
         */
        /*@see DeviceUtil.getSystemProperties  string    ro.product.cpu.abilist32*/
        String KEY_RO_CPU_ABI_LIST_32 = "ro.product.cpu.abilist";

        /**
         * The constant KEY_RO_CPU_ABI_LIST_64.
         */
        /*@see DeviceUtil.getSystemProperties  string    ro.product.cpu.abilist64*/
        String KEY_RO_CPU_ABI_LIST_64 = "ro.product.cpu.abilist64";

        /**
         * The constant KEY_RO_ARCH.
         */
        /*@see DeviceUtil.getSystemProperties  string    ro.arch*/
        String KEY_RO_ARCH = "ro.arch";

        /**
         * The constant KEY_RO_CHIPNAME.
         */
        /*@see DeviceUtil.getSystemProperties  string    ro.chipname*/
        String KEY_RO_CHIPNAME = "ro.chipname";

        /**
         * The constant KEY_RO_BRIDGE.
         */
        /*@see DeviceUtil.getSystemProperties  string    ro.dalvik.vm.native.bridge*/
        String KEY_RO_BRIDGE = "ro.dalvik.vm.native.bridge";

        /**
         * The constant KEY_RO_BRIDGE_EXEC.
         */
        /*@see DeviceUtil.getSystemProperties  string    ro.enable.native.bridge.exec*/
        String KEY_RO_BRIDGE_EXEC = "ro.enable.native.bridge.exec";

        /**
         * The constant KEY_RO_ZYGOTE.
         */
        /*@see DeviceUtil.getSystemProperties  string    ro.zygote*/
        String KEY_RO_ZYGOTE = "ro.zygote";

        /**
         * The constant KEY_RO_MOCK_LOCATION.
         */
        /*@see DeviceUtil.getSystemProperties  string    ro.allow.mock.location*/
        String KEY_RO_MOCK_LOCATION = "ro.allow.mock.location";

        /**
         * The constant KEY_RO_ISA_ARM.
         */
        /*@see DeviceUtil.getSystemProperties  string    ro.dalvik.vm.isa.arm*/
        String KEY_RO_ISA_ARM = "ro.dalvik.vm.isa.arm";

        /**
         * The constant KEY_ISA_ARM_FEATURES.
         */
        String KEY_ISA_ARM_FEATURES = "dalvik.vm.isa.arm.features";

        /**
         * The constant KEY_ISA_ARM_VARIANT.
         */
        String KEY_ISA_ARM_VARIANT = "dalvik.vm.isa.arm.variant";

        /**
         * The constant KEY_ISA_X86_FEATURES.
         */
        String KEY_ISA_X86_FEATURES = "dalvik.vm.isa.x86.features";

        /**
         * The constant KEY_ISA_X86_VARIANT.
         */
        String KEY_ISA_X86_VARIANT = "dalvik.vm.isa.x86.variant";

        /**
         * The constant KEY_ISA_ARM64_FEATURES.
         */
        String KEY_ISA_ARM64_FEATURES = "dalvik.vm.isa.arm64.features";

        /**
         * The constant KEY_ISA_ARM64_VARIANT.
         */
        String KEY_ISA_ARM64_VARIANT = "dalvik.vm.isa.arm64.variant";

        /**
         * The constant KEY_RO_BUILD_USER.
         */
        /*@see DeviceUtil.getSystemProperties  string    ro.build.user*/
        String KEY_RO_BUILD_USER = "ro.build.user";

        /**
         * The constant KEY_RO_KERNEL_QEMU.
         */
        /*@see DeviceUtil.getSystemProperties  string    ro.kernel.qemu*/
        String KEY_RO_KERNEL_QEMU = "ro.kernel.qemu";

        /**
         * The constant KEY_RO_HARDWARE.
         */
        /*@see DeviceUtil.getSystemProperties  string    ro.hardware*/
        String KEY_RO_HARDWARE = "ro.hardware";

        /**
         * The constant KEY_NATIVE_BRIDGE.
         */
        /*@see DeviceUtil.getSystemProperties  string    persist.sys.nativebridge*/
        String KEY_NATIVE_BRIDGE = "persist.sys.nativebridge";
    }

    interface Response {
        /*String AppName */
        String KEY_APP_NAME = "app_name";
        /*int32     AppFileSize     0*/
        String KEY_APP_SIZE = "app_size";
        /*int32     AppRating count     1393613*/
        String KEY_RATING_COUNT = "rating_count";
        /*Array of string carousel images URLs       */
        String KEY_IMGS = "imgs";
        /*int8      shows adt mark, 0:hide,1:show*/
        String KEY_MK = "mk";
        /*int32    in seconds*/
        String KEY_EXPIRE = "expire";
        /*Object of PlayIn          PlayIn*/
        String KEY_PI = "pi";
    }
}
