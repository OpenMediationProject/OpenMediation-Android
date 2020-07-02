package com.cloudtech.shell.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cloudtech.shell.entity.ModulePo;
import com.cloudtech.shell.entity.PluginType;

import java.util.ArrayList;
import java.util.List;

import static com.cloudtech.shell.db.ModuleTable.CHECKSUM;
import static com.cloudtech.shell.db.ModuleTable.CLASS_NAME;
import static com.cloudtech.shell.db.ModuleTable.CURRENT_SWITCH;
import static com.cloudtech.shell.db.ModuleTable.DOWNLOAD_URL;
import static com.cloudtech.shell.db.ModuleTable.ID;
import static com.cloudtech.shell.db.ModuleTable.IS_ACTIVE;
import static com.cloudtech.shell.db.ModuleTable.IS_DEL;
import static com.cloudtech.shell.db.ModuleTable.IS_DOWN;
import static com.cloudtech.shell.db.ModuleTable.IS_DYNAMIC;
import static com.cloudtech.shell.db.ModuleTable.MEDIUM;
import static com.cloudtech.shell.db.ModuleTable.METHOD_NAME;
import static com.cloudtech.shell.db.ModuleTable.MODULE_NAME;
import static com.cloudtech.shell.db.ModuleTable.PLUGIN_TYPE;
import static com.cloudtech.shell.db.ModuleTable.SIZED;
import static com.cloudtech.shell.db.ModuleTable.SMALL;
import static com.cloudtech.shell.db.ModuleTable.TABLE_NAME;
import static com.cloudtech.shell.db.ModuleTable.VERSION;


/**
 * Created by huangdong on 17/12/5.
 * antony.huang@yeahmobi.com
 */
public class ModuleDao {

    private static final String TAG = "ModuleDao";

    private boolean isOpen = false;
    private DatabaseOpenHelper databaseOpenHelper;
    private SQLiteDatabase sqLiteDatabase;

    private static ModuleDao moduleDao;

    private ModuleDao(Context context) {
        databaseOpenHelper = new DatabaseOpenHelper(context);
    }

    public synchronized static ModuleDao getInstance(Context context) {
        if (moduleDao == null) {
            moduleDao = new ModuleDao(context);
        }
        return moduleDao;
    }


    private synchronized SQLiteDatabase openDatabase() {
        if (!isOpen) {
            sqLiteDatabase = databaseOpenHelper.getWritableDatabase();
        }
        return sqLiteDatabase;
    }


    private synchronized void closeDatabase() {
        if (isOpen) {
            if (sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }
    }


    /**
     * 向数据库中插入更新数据
     */
    public synchronized void replace(final ModulePo... modules) {
        SQLiteDatabase db = openDatabase();
        if (db == null || modules == null || modules.length == 0) {
            return;
        }
        try {
            db.beginTransaction();
            for (ModulePo module : modules) {
                ContentValues contentValues = new ContentValues();

                contentValues.put(ID, module.getId());

                contentValues.put(MODULE_NAME, module.getModuleName());

                contentValues.put(VERSION, module.getVersion());

                contentValues.put(CHECKSUM, module.getChecksum());

                contentValues.put(CLASS_NAME, module.getClassName());

                contentValues.put(DOWNLOAD_URL, module.getDownloadUrl());

                contentValues.put(IS_DYNAMIC, module.isDynamic());

                contentValues.put(METHOD_NAME, module.getMethodName());

                contentValues.put(IS_DOWN, module.isDown());

                contentValues.put(IS_DEL, module.isDel());

                contentValues.put(IS_ACTIVE, module.isActive());

                contentValues.put(CURRENT_SWITCH, module.getCurrentSwitch());

                contentValues.put(SMALL, module.getSmall());

                contentValues.put(MEDIUM, module.getMedium());

                contentValues.put(SIZED, module.getSized());

                contentValues.put(PLUGIN_TYPE, module.getPluginType().getTypeId());

                db.replace(ModuleTable.TABLE_NAME, null, contentValues);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            closeDatabase();
        }


    }

    public synchronized List<ModulePo> queryOtherAll() {
        List<ModulePo> moduleList = new ArrayList<>();
        SQLiteDatabase db = openDatabase();
        if (db == null) {
            return moduleList;
        }
        StringBuilder sql = new StringBuilder("select * from ")
                .append(ModuleTable.TABLE_NAME)
                .append(" where ")
                .append(PLUGIN_TYPE)
                .append(" = ")
                .append(PluginType.OTHER_PLUGIN.getTypeId())
                .append(" order by ")
                .append(MODULE_NAME)
                .append(" desc, ")
                .append(SMALL)
                .append(" desc, ")
                .append(MEDIUM)
                .append(" desc, ")
                .append(SIZED)
                .append(" desc");
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql.toString(), null);
            if (cursor == null) return moduleList;
            while (cursor.moveToNext()) {
                ModulePo module = getModule(cursor);
                moduleList.add(module);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            closeDatabase();
        }
        return moduleList;
    }

    public synchronized List<ModulePo> queryBaseAll() {
        List<ModulePo> moduleList = new ArrayList<>();
        SQLiteDatabase db = openDatabase();
        if (db == null) {
            return moduleList;
        }
        StringBuilder sql = new StringBuilder("select * from ")
                .append(ModuleTable.TABLE_NAME)
                .append(" where ")
                .append(PLUGIN_TYPE)
                .append(" in ( ")
                .append(PluginType.MANAGER.getTypeId())
                .append(", ")
                .append(PluginType.MAIN_PLUGIN.getTypeId())
                .append(" ) ")
                .append(" order by ")
                .append(PLUGIN_TYPE)
                .append(" asc, ")
                .append(MODULE_NAME)
                .append(" desc, ")
                .append(SMALL)
                .append(" desc, ")
                .append(MEDIUM)
                .append(" desc, ")
                .append(SIZED)
                .append(" desc");
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql.toString(), null);
            if (cursor == null) return moduleList;
            while (cursor.moveToNext()) {
                ModulePo module = getModule(cursor);
                moduleList.add(module);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            closeDatabase();
        }
        return moduleList;
    }

    public synchronized List<ModulePo> queryAll() {
        List<ModulePo> moduleList = new ArrayList<>();
        SQLiteDatabase db = openDatabase();
        if (db == null) {
            return moduleList;
        }
        StringBuilder sql = new StringBuilder("select * from ")
                .append(ModuleTable.TABLE_NAME)
                .append(" order by ")
                .append(MODULE_NAME)
                .append(" desc, ")
                .append(SMALL)
                .append(" desc, ")
                .append(MEDIUM)
                .append(" desc, ")
                .append(SIZED)
                .append(" desc");
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql.toString(), null);
            if (cursor == null) return moduleList;
            while (cursor.moveToNext()) {
                ModulePo module = getModule(cursor);
                moduleList.add(module);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            closeDatabase();
        }
        return moduleList;
    }


    public synchronized ModulePo getByNameAndVersion(String moduleName, String version) {
        SQLiteDatabase db = openDatabase();
        if (db == null) {
            return null;
        }
        StringBuilder sql = new StringBuilder("select * from ")
                .append(TABLE_NAME)
                .append(" where ")
                .append(MODULE_NAME)
                .append(" =? and ")
                .append(VERSION)
                .append(" = ? LIMIT 1");
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql.toString(), new String[]{moduleName, version});
            if (cursor != null && cursor.moveToFirst())
                return getModule(cursor);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            closeDatabase();
        }
        return null;
    }

    public synchronized ModulePo getStaticModuleByName(String moduleName) {
        SQLiteDatabase db = openDatabase();
        if (db == null) {
            return null;
        }
        StringBuilder sql = new StringBuilder("select * from ")
                .append(TABLE_NAME)
                .append(" where ")
                .append(MODULE_NAME)
                .append(" = ? and ")
                .append(IS_DYNAMIC)
                .append(" = 0 LIMIT 1");
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql.toString(), new String[]{moduleName});
            if (cursor != null && cursor.moveToFirst())
                return getModule(cursor);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            closeDatabase();
        }
        return null;
    }

    private ModulePo getModule(Cursor cursor) {
        ModulePo module = new ModulePo();
        module.setId(cursor.getInt(cursor.getColumnIndex(ID)));
        module.setChecksum(cursor.getString(cursor.getColumnIndex(CHECKSUM)));
        module.setClassName(cursor.getString(cursor.getColumnIndex(CLASS_NAME)));
        module.setDownloadUrl(cursor.getString(cursor.getColumnIndex(DOWNLOAD_URL)));
        module.setDynamic(module.getDynamic(cursor.getInt(cursor.getColumnIndex(IS_DYNAMIC))));
        module.setMethodName(cursor.getString(cursor.getColumnIndex(METHOD_NAME)));
        module.setDown(module.getDown(cursor.getInt(cursor.getColumnIndex(IS_DOWN))));
        module.setDel(module.getDel(cursor.getInt(cursor.getColumnIndex(IS_DEL))));
        module.setActive(module.getActive(cursor.getInt(cursor.getColumnIndex(IS_ACTIVE))));
        module.setModuleName(cursor.getString(cursor.getColumnIndex(MODULE_NAME)));
        module.setVersion(cursor.getString(cursor.getColumnIndex(VERSION)));
        module.setCurrentSwitch(cursor.getInt(cursor.getColumnIndex(CURRENT_SWITCH)));
        module.setSmall(cursor.getInt(cursor.getColumnIndex(SMALL)));
        module.setMedium(cursor.getInt(cursor.getColumnIndex(MEDIUM)));
        module.setSized(cursor.getInt(cursor.getColumnIndex(SIZED)));
        module.setPluginType(PluginType.getPluginType(cursor.getInt(cursor.getColumnIndex(PLUGIN_TYPE))));
        return module;
    }

    public synchronized void deleteById(final Integer id) {
        SQLiteDatabase db = openDatabase();
        if (db == null) {
            return;
        }
        try {
            db.delete(TABLE_NAME, ID + " = ?", new String[]{id.toString()});
        } finally {
            closeDatabase();
        }
    }


}
