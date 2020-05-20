// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.utils.cache;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nbmediation.sdk.utils.DeveloperLog;
import com.nbmediation.sdk.utils.crash.CrashUtil;

import java.util.ArrayList;

/**
 * Operations on DB
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    /**
     * The M sq lite database.
     */
    protected SQLiteDatabase mSQLiteDatabase;
    private static DataBaseHelper instance;

    /**
     * Gets singleton.
     *
     * @param context the context
     * @param name    the name
     * @param version the version
     * @return the singleton
     */
    public static synchronized DataBaseHelper getSingleton(Context context, String name, int version) {
        if (instance == null) {
            instance = new DataBaseHelper(context, name, version);
        }
        return instance;
    }

    /**
     * Instantiates a new Data base helper.
     *
     * @param context the context
     * @param name    the name
     * @param version the version
     */
    public DataBaseHelper(Context context, String name, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        this.mSQLiteDatabase = sqLiteDatabase;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }

    /**
     * Exec sql boolean.
     *
     * @param sql the sql
     * @return boolean
     */
    public boolean execSQL(String sql) {
        boolean flage = false;
        getWritableDatabase();
        if (!isRead()) {
            return flage;
        }
        try {
            mSQLiteDatabase.execSQL(sql);
            flage = true;
        } catch (Exception e) {
            DeveloperLog.LogD("DataBaseHelper", e);
            CrashUtil.getSingleton().saveException(e);
        } finally {
            close();
        }
        return flage;
    }

    /**
     * Raw query array list.
     *
     * @param sql the sql
     * @return array list
     */
    ArrayList<String[]> rawQuery(String sql) {
        Cursor cursor = null;
        ArrayList<String[]> dataList = new ArrayList<>();
        try {
            getWritableDatabase();
            if (!isRead()) {
                return dataList;
            }
            cursor = mSQLiteDatabase.rawQuery(sql, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int index = cursor.getColumnCount();
                    if (dataList.size() == 0) {
                        String[] values = new String[index];
                        for (int i = 0; i < index; i++) {
                            values[i] = cursor.getColumnName(i);
                        }
                        dataList.add(values);
                    }
                    String[] values = new String[index];
                    for (int i = 0; i < index; i++) {
                        values[i] = cursor.getString(i);
                    }
                    dataList.add(values);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            DeveloperLog.LogD("DataBaseHelper", e);
            CrashUtil.getSingleton().saveException(e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            close();
        }
        return dataList;
    }

    /**
     *
     */
    @Override
    public synchronized void close() {
        if (isRead()) {
            mSQLiteDatabase.close();
        }
    }

    /**
     * Is read boolean.
     *
     * @return boolean
     */
    public boolean isRead() {
        boolean flage = false;
        if (mSQLiteDatabase != null && mSQLiteDatabase.isOpen()) {
            flage = true;
        }
        return flage;
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        mSQLiteDatabase = super.getReadableDatabase();
        return mSQLiteDatabase;
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        mSQLiteDatabase = super.getWritableDatabase();
        return mSQLiteDatabase;
    }
}
