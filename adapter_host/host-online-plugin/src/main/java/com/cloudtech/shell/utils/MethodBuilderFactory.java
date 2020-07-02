package com.cloudtech.shell.utils;

public class MethodBuilderFactory {
    private static MethodBuilderFactory instance = null;

    @Deprecated // for testing
    public static void setInstance(MethodBuilderFactory factory) {
        instance = factory;
    }

    public static Reflection.MethodBuilder create(Object object, String methodName) {
        if (instance == null) instance = new MethodBuilderFactory();
        return instance.internalCreate(object, methodName);
    }

    protected Reflection.MethodBuilder internalCreate(Object object, String methodName) {
        if (instance == null) instance = new MethodBuilderFactory();
        return new Reflection.MethodBuilder(object, methodName);
    }

    public static void clean(){
        instance=null;
    }
}

