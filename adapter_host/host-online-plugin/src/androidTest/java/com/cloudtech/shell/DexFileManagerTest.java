package com.cloudtech.shell;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.cloudtech.shell.entity.ModuleData;
import com.cloudtech.shell.ex.FileCurdError;
import com.cloudtech.shell.ex.MD5ValidException;
import com.cloudtech.shell.manager.AssetsFileManager;
import com.cloudtech.shell.manager.PluginFileManager;
import com.cloudtech.shell.utils.PreferencesUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class DexFileManagerTest {

    private Context appContext;

    private final static String DEST_DIR_NAME = "bin";

    private final static String ZIP_FILE_NAME = "test.zip";

    private static final String MODULE_NAME = "test";

    @Before
    public void before() throws InterruptedException, IOException {
        appContext = InstrumentationRegistry.getTargetContext();
        PreferencesUtils.initPrefs(appContext);
        Thread.sleep(5000);
        File file = new File(appContext.getDir(DEST_DIR_NAME, Context.MODE_PRIVATE), ZIP_FILE_NAME);
        AssetsFileManager.copyAssets(appContext, ZIP_FILE_NAME, file, "700");
    }

    @Test
    public void saveZipToDexFileTest() {
        // Context of the app under test.
        File zipFile = new File(appContext.getDir(DEST_DIR_NAME, Context.MODE_PRIVATE),
                ZIP_FILE_NAME);
        ModuleData data = new ModuleData();
        data.methodName = "initialize";
        data.className = "com.cloudtech.shell.test.CTService";
        data.moduleName = "test";
        try {
            PluginFileManager.saveFile(appContext, zipFile, data.moduleName,
                    "1.0.0","9f6776efcbe4611f6f6a3b3c603952e3");
        } catch (FileCurdError fileCurdError) {
            fileCurdError.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MD5ValidException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
