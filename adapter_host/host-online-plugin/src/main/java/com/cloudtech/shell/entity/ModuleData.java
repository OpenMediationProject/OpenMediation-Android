package com.cloudtech.shell.entity;

import java.io.File;
import java.io.Serializable;

/**
 * Created by jiantao.tu on 2018/4/2.
 */
public class ModuleData implements Serializable{

    public String moduleName;

    public String className;

    public String methodName;

    public String version;

    public File pluginFile;

    public ModuleData() { }

    public ModuleData(String moduleName, String className, String methodName) {
        this.moduleName = moduleName;
        this.className = className;
        this.methodName = methodName;
    }
}
