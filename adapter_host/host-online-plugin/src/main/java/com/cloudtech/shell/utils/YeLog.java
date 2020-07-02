package com.cloudtech.shell.utils;

import android.content.Context;
import android.os.Process;
import android.util.Log;

import com.cloudtech.shell.BuildConfig;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public class YeLog {

    private final static String TAG = "CT_v" + BuildConfig.VERSION_NUMBER;    //当前版本号

    public static String LOG_FILE_NAME = "shellLog.log";

    public static File getLogPath(Context context) {
        if (context == null)
            throw new NullPointerException("context or fileName is null");
        File dexDir = context.getDir("cache", Context.MODE_PRIVATE);
        return new File(dexDir, "cloudmobi/" + LOG_FILE_NAME);
//        File cacheDirectory = Environment.getExternalStorageDirectory();
//        String fileName = LOG_FILE_NAME;
//        return new File(cacheDirectory, fileName);
    }

    private YeLog() {

    }

    private static String getLogTag(String tag) {
        return tag == null ? TAG : tag;
    }

    public static void dp(String format, String... args) {
        if (SwitchConfig.LOG) {
            i(String.format(format, (Object[]) args));
        }
    }


    public static void ip(String format, String... args) {
        if (SwitchConfig.LOG) {
            i(String.format(format, (Object[]) args));
        }
    }


    public static void ep(String format, String... args) {
        if (SwitchConfig.LOG) {
            e(String.format(format, (Object[]) args));
        }
    }

    public static void i(String msg) {
        if (SwitchConfig.LOG) {
            if (msg != null) {
                String key = getLogTag(TAG);
                String content = msg;
                unify(key, content);
                Log.i(key, content);
            }
        }
    }


    public static void d(String msg) {

        if (SwitchConfig.LOG) {
            if (msg != null) {
                String key = getLogTag(TAG);
                String content = msg;
                unify(key, content);
                Log.d(key, content);
            }
        }
    }


    public static void w(Exception ex) {
        if (SwitchConfig.LOG) {
            if (ex != null) {
                String key = getLogTag(TAG);
                String content = getStackTraceString(ex);
                unify(key, content);
                Log.w(key, content);
            }
        }
    }

    public static void w(String msg) {
        if (SwitchConfig.LOG) {
            if (msg != null) {
                String key = getLogTag(TAG);
                String content = msg;
                unify(key, content);
                Log.w(key, content);
            }
        }
    }

    public static void e(String msg) {
        if (SwitchConfig.LOG) {
            if (msg != null) {
                String key = getLogTag(TAG);
                String content = msg;
                unify(key, content);
                Log.e(key, content);
            }
        }
    }

    public static void e(Throwable tr) {
        if (SwitchConfig.LOG) {
            if (tr != null) {
                String key = getLogTag(TAG);
                String content = getStackTraceString(tr);
                unify(key, content);
                Log.e(key, content);
            }
        }
    }

    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        /* Throwable t = tr;
        while (t != null) {
            if (t instanceof UnknownHostException) {
                return "";
            }
            t = t.getCause();
        }*/

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }


    public static void i(String tag, String msg) {
        if (SwitchConfig.LOG) {
            if (msg != null) {
                String key = getLogTag(tag);
                String content = msg;
                unify(key, content);
                Log.i(key, content);
            }
        }
    }


    public static void d(String tag, String msg) {
        if (SwitchConfig.LOG) {
            if (msg != null) {
                String key = getLogTag(tag);
                String content = msg;
                unify(key, content);
                Log.d(key, content);
            }
        }
    }


    public static void w(String tag, String msg) {
        if (SwitchConfig.LOG) {
            if (msg != null) {
                String key = getLogTag(tag);
                String content = msg;
                unify(key, content);
                Log.w(key, content);
            }
        }
    }


    public static void e(String tag, String msg) {
        if (SwitchConfig.LOG) {
            if (msg != null) {
                String key = getLogTag(tag);
                String content = msg;
                unify(key, content);
                Log.e(key, content);
            }
        }
    }


    private static void write(File logFile, String msg) {

        try {
            File fileParent = logFile.getParentFile();
            if (!fileParent.exists()) {
                if (!fileParent.mkdirs()) {
                    throw new RuntimeException("create folder error. fileName=" + logFile.getName());
                }
            }
            if (!logFile.exists())
                logFile.createNewFile();
            FileOutputStream output = new FileOutputStream(logFile, true);
            InputStream input = new ByteArrayInputStream(msg.getBytes());
            byte b[] = new byte[1024];
            int j;
            while ((j = input.read(b)) != -1) {
                output.write(b, 0, j);
            }
            output.flush();
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeLog(String msg) {
        Date data = new Date();
        String dateStr = TimeUtils.getDateToString(data, TimeUtils.YMD);
        String time = TimeUtils.getDateToString(data, TimeUtils.HMS);
        Context context = ContextHolder.getGlobalAppContext();
        if (context == null) return;
        File logFile = getLogPath(context);
        write(logFile, dateStr + " " + time + "\t\t"+"pid="+ Process.myPid()+"," + msg+"\n");
    }


    private static void unify(final String tag, final String msg) {
        writeLog(msg);
    }


}
