// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mediation;


import java.lang.reflect.Constructor;

public class CustomEventFactory {

    public static CustomBannerEvent createBanner(String className) throws Exception {
        Class<? extends CustomBannerEvent> bannerClass = Class.forName(className)
                .asSubclass(CustomBannerEvent.class);
        Constructor<?> bannerConstructor = bannerClass.getDeclaredConstructor((Class[]) null);
        bannerConstructor.setAccessible(true);
        return (CustomBannerEvent) bannerConstructor.newInstance();
    }

    public static CustomNativeEvent createNative(String className) throws Exception {
        Class<? extends CustomNativeEvent> nativeClass = Class.forName(className)
                .asSubclass(CustomNativeEvent.class);
        Constructor<?> nativeConstructor = nativeClass.getDeclaredConstructor((Class[]) null);
        nativeConstructor.setAccessible(true);
        return (CustomNativeEvent) nativeConstructor.newInstance();
    }

    public static CustomSplashEvent createSplash(String className) throws Exception {
        Class<? extends CustomSplashEvent> splashClass = Class.forName(className)
                .asSubclass(CustomSplashEvent.class);
        Constructor<?> splashConstructor = splashClass.getDeclaredConstructor((Class[]) null);
        splashConstructor.setAccessible(true);
        return (CustomSplashEvent) splashConstructor.newInstance();
    }
}
