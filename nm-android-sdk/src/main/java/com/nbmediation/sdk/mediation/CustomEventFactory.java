// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mediation;


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
}
