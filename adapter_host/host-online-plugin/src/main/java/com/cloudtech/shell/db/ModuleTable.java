package com.cloudtech.shell.db;

/**
 * Created by jiantao.tu on 2018/5/23.
 */
public class ModuleTable {

    public final static String TABLE_NAME = "module";

    public final static String ID = "id";

    public final static String MODULE_NAME = "moduleName";

    public final static String VERSION = "version";

    public final static String IS_DYNAMIC = "isDynamic";

    public final static String DOWNLOAD_URL = "downloadUrl";

    public final static String CHECKSUM = "checksum";

    public final static String CLASS_NAME = "className";

    public final static String METHOD_NAME = "methodName";

    public final static String IS_DOWN = "isDown";

    public final static String IS_DEL = "isDel";

    public final static String IS_ACTIVE = "isActive";

    public final static String CURRENT_SWITCH = "currentSwitch";

    public final static String SMALL = "small";

    public final static String MEDIUM = "medium";

    public final static String SIZED = "sized";

    public final static String PLUGIN_TYPE = "pluginType";

    public static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME
            + "("
            + ID + " integer PRIMARY KEY AUTOINCREMENT, "
            + MODULE_NAME + " TEXT, "
            + VERSION + " TEXT, "
            + IS_DYNAMIC + " integer, "
            + DOWNLOAD_URL + " TEXT, "
            + CHECKSUM + " TEXT, "
            + CLASS_NAME + " TEXT, "
            + METHOD_NAME + " TEXT, "
            + CURRENT_SWITCH + " integer, "
            + SMALL + " integer, "
            + MEDIUM + " integer, "
            + SIZED + " integer, "
            + IS_DOWN + " integer, "
            + IS_DEL + " integer, "
            + IS_ACTIVE + " integer, "
            + PLUGIN_TYPE + " integer);";
}
