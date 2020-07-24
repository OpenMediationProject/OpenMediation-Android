// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.cache;

import android.content.Context;

import java.util.ArrayList;

/**
 * Operations on DB tables
 */
class DataBaseUtil {
    private DataBaseHelper mDataBaseHelper;

    private static class DataBaseUtilHolder {
        private static final DataBaseUtil INSTANCE = new DataBaseUtil();
    }

    public static DataBaseUtil getSingleton() {
        return DataBaseUtilHolder.INSTANCE;
    }


    private DataBaseUtil() {
    }

    /**
     * 
     *
     * @param context
     * @param name
     * @param version
     */
    void init(Context context, String name, int version) {
        mDataBaseHelper = DataBaseHelper.getSingleton(context, name, version);
    }

    /**
     * 
     *
     * @param tableName  
     * @param columnName 
     * @return true:success or false:failure
     */
    boolean createTable(String tableName, String columnName) {
        return mDataBaseHelper.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s (_id INTEGER PRIMARY KEY AUTOINCREMENT,%s)", tableName, columnName));
    }

    //
    boolean insert(String tableName, String columnName, String... values) {
        if (values == null || values.length == 0) {
            return false;
        }
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO ");
        sqlBuilder.append(tableName);
        sqlBuilder.append("(");
        sqlBuilder.append(columnName);
        sqlBuilder.append(")");
        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                sqlBuilder.append(" UNION ALL ");
            }
            sqlBuilder.append("SELECT ");
            sqlBuilder.append(values[i]);
        }
        return mDataBaseHelper.execSQL(sqlBuilder.toString());
    }

    /**
     * 
     *
     * @param tableName 
     * @param wheres    
     * @return true:success or false:failure
     */
    public boolean delete(String tableName, String key, String... wheres) {
        if (wheres == null || wheres.length == 0) {
            return false;
        }
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("DELETE FROM ");
        sqlBuilder.append(tableName);
        sqlBuilder.append(" WHERE ");
        sqlBuilder.append(key);
        sqlBuilder.append(" IN(");
        for (int i = 0; i < wheres.length; i++) {
            sqlBuilder.append("\"");
            sqlBuilder.append(wheres[i]);
            sqlBuilder.append("\"");
            if (i != wheres.length - 1) {
                sqlBuilder.append(",");
            }
        }
        sqlBuilder.append(")");
        return mDataBaseHelper.execSQL(sqlBuilder.toString());
    }

    /**
     * 
     *
     * @param tableName 
     * @param updates   
     * @param wheres    
     * @return true:success or false:failure
     */
    boolean update(String tableName, String[] updates, String... wheres) {
        boolean flage = false;
        if (updates == null || updates.length == 0) {
            return flage;
        }
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("UPDATE ");
        sqlBuilder.append(tableName);
        sqlBuilder.append(" SET ");
        for (int i = 0; i < updates.length; i++) {
            sqlBuilder.append(updates[i]);
            if (i != updates.length - 1) {
                sqlBuilder.append(",");
            }
        }
        if (wheres != null) {
            sqlBuilder.append(" WHERE ");
            for (int i = 0; i < wheres.length; i++) {
                sqlBuilder.append(wheres[i]);
                if (i != wheres.length - 1) {
                    sqlBuilder.append(" AND ");
                }
            }
        }
        flage = mDataBaseHelper.execSQL(sqlBuilder.toString());
        return flage;
    }

    /**
     * 
     *
     * @param tableName  
     * @param columnName 
     * @param wheres     
     * @return Array:1st row contains col names
     */
    public ArrayList<String[]> query(String tableName, String columnName, String... wheres) {
        if (wheres != null && wheres.length != 0) {
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT ");
            sqlBuilder.append(columnName);
            sqlBuilder.append(" FROM ");
            sqlBuilder.append(tableName);
            sqlBuilder.append(" WHERE ");
            for (int i = 0; i < wheres.length; i++) {
                sqlBuilder.append(wheres[0]);
                if (i != wheres.length - 1) {
                    sqlBuilder.append(" AND ");
                }
            }
            return mDataBaseHelper.rawQuery(String.format("SELECT %s FROM %s WHERE %s", columnName, tableName, sqlBuilder.toString()));
        }
        return mDataBaseHelper.rawQuery(String.format("SELECT %s FROM %s", columnName, tableName));
    }
}
