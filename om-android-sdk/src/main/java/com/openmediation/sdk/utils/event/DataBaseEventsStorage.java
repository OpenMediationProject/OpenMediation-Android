// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.event;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.cache.DataBaseHelper;
import com.openmediation.sdk.utils.crash.CrashUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * DB helper for saving events and reporting; different table from key-value in the same DB
 */
public class DataBaseEventsStorage extends DataBaseHelper {

    private static DataBaseEventsStorage mInstance;
    private static final String DB_TABLE_NAME = "events";
    private static final String DB_COLUMN_EVENT = "event";

    private DataBaseEventsStorage(Context context, String databaseName, int databaseVersion) {
        super(context, databaseName, databaseVersion);
    }

    public static synchronized DataBaseEventsStorage getInstance(Context context, String databaseName, int databaseVersion) {
        if (mInstance == null) {
            mInstance = new DataBaseEventsStorage(context, databaseName, databaseVersion);
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        mSQLiteDatabase = db;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_NAME);
        this.onCreate(db);
    }

    /**
     *
     */
    void createTable() {
        try {
            getWritableDatabase();
            mSQLiteDatabase.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s (_id INTEGER PRIMARY KEY AUTOINCREMENT,%s)",
                    DB_TABLE_NAME, DB_COLUMN_EVENT));
        } catch (Exception e) {
            CrashUtil.getSingleton().saveException(e);
        }
    }

    synchronized void addEvent(Event event) {
        if (event != null) {
            try {
                getWritableDatabase();
                if (!isRead()) {
                    return;
                }
                ContentValues values = getContentValuesForEvent(event);
                mSQLiteDatabase.insert(DB_TABLE_NAME, null, values);
            } catch (Throwable throwable) {
                DeveloperLog.LogE("Exception while saving events: ", throwable);
            }
        }
    }

    synchronized List<Event> loadEvents() {
        List<Event> events = new ArrayList<>();
        Cursor cursor = null;
        try {
            getReadableDatabase();
            if (!isRead()) {
                return events;
            }
            cursor = mSQLiteDatabase.query(DB_TABLE_NAME, null, null, null
                    , null, null, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    String data = cursor.getString(cursor.getColumnIndex(DB_COLUMN_EVENT));
                    Event event = new Event(data);
                    events.add(event);
                    cursor.moveToNext();
                }
                cursor.close();
            }
        } catch (Throwable throwable) {
            DeveloperLog.LogE("Exception while loading events: ", throwable);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        DeveloperLog.LogE("loading events: " + events.size());
        return events;
    }

    synchronized void clearEvents(ConcurrentLinkedQueue<Event> events) {
        try {
            DeveloperLog.LogE("clearing events: " + events.size());
            getWritableDatabase();
            if (!isRead()) {
                return;
            }
            mSQLiteDatabase.beginTransaction();
            for (Event event : events) {
                mSQLiteDatabase.delete(DB_TABLE_NAME, DB_COLUMN_EVENT + "=?", new String[]{event.toJson()});
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            DeveloperLog.LogE("Exception while clearing events: ", throwable);
        } finally {
            mSQLiteDatabase.endTransaction();
        }
    }

    private ContentValues getContentValuesForEvent(Event event) {
        ContentValues values = null;
        if (event != null) {
            values = new ContentValues(4);
            values.put(DB_COLUMN_EVENT, event.toJson());
        }

        return values;
    }
}
