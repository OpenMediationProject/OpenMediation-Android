package com.cloudtech.shell.entity;

/**
 * Shadow的插件类型
 * Created by jiantao.tu on 2020/6/19.
 */
public enum PluginType {


    /**
     * shadow manager ,唯一
     */
    MANAGER(1),

    /**
     * shadow runtime+loader ,唯一
     */
    MAIN_PLUGIN(2),

    /**
     * shadow a-plugin,b-plugin,c-plugin ,多个
     */
    OTHER_PLUGIN(0);

    public int getTypeId() {
        return typeId;
    }

    private final int typeId;

    PluginType(int type) {
        typeId = type;
    }


    public static PluginType getPluginType(int typeId) {
        for (PluginType type : PluginType.values()) {
            if (type.typeId == typeId) {
                return type;
            }
        }
        return PluginType.OTHER_PLUGIN;
    }
}
