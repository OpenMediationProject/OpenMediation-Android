package com.cloudtech.shell.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jiantao.tu on 2018/5/23.
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {

    final static String DB_NAME = "cloudtech_shell_loader.db";

    final static int VERSION_NUM = 1;


    DatabaseOpenHelper(Context context) {
        super(context, DB_NAME, null, VERSION_NUM);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ModuleTable.CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}


