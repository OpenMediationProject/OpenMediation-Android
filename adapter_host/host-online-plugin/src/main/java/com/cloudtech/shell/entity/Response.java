package com.cloudtech.shell.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vincent
 * Email:jingwei.zhang@yeahmobi.com
 */
public class Response {
    public int frequency;

    public List<Module> modules = new ArrayList<>();

    public static class Module {
        public String version;

        public String download_url;

        public String checksum;

        public int switchVal;

        public String className;

        public String methodName;

        public String moduleName;

        public PluginType pluginType;


        @Override
        public String toString() {
            return "Module{" +
                    "version='" + version + '\'' +
                    ", download_url='" + download_url + '\'' +
                    ", checksum='" + checksum + '\'' +
                    ", switchVal=" + switchVal +
                    ", className='" + className + '\'' +
                    ", methodName='" + methodName + '\'' +
                    ", moduleName='" + moduleName + '\'' +
                    ", pluginType='" + pluginType + '\'' +
                    '}';
        }
    }


    @Override
    public String toString() {
        return "Response{" +
                "frequency=" + frequency +
                ", modules=" + modules +
                '}';
    }
}
