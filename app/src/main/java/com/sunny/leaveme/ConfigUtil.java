package com.sunny.leaveme;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by sunnyl on 2016/9/5.
 */
public class ConfigUtil {
    private final static String TAG = "ConfigUtil";
    private final static String CONF_FILE_DIR = "leaveme";
    private final static String CONF_FILE_NAME = "leaveme.properties";

    public static Properties loadConfig(Context context) {
        if (!isConfigFileExist()) {
            if (!saveToSdcard(context)) {
                return null;
            }
        }

        String dirPath = getConfDir();
        Properties properties = new Properties();
        try {
            FileInputStream s = new FileInputStream(dirPath + "/" + CONF_FILE_NAME);
            properties.load(s);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return properties;
    }

    public static boolean saveConfig(Context context, Properties properties) {
        if (!isConfigFileExist()) {
            return false;
        }

        String dirPath = getConfDir();
        try {
            File file = new File(dirPath + "/" + CONF_FILE_NAME);
            if (!file.exists())
                file.createNewFile();
            FileOutputStream s = new FileOutputStream(file);
            properties.store(s, "");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static boolean isConfigFileExist() {
        String dirPath;
        if ((dirPath = getConfDir()) != null) {
            try {
                File file = new File(dirPath + "/" + CONF_FILE_NAME);
                if (!file.exists()) {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            return true;
        } else {
            return false;
        }
    }

    private static boolean saveToSdcard(Context context) {
        String dirPath;
        if ((dirPath = getConfDir()) != null) {
            try {
                File file = new File(dirPath + "/" + CONF_FILE_NAME);
                if (!file.exists()) {
                    AssetManager assetManager = context.getAssets();
                    InputStream ins = assetManager.open(CONF_FILE_NAME);
                    FileOutputStream fos = new FileOutputStream(file);
                    byte[] buffer = new byte[8192];
                    int count = 0;
                    while ((count = ins.read(buffer)) > 0) {
                        fos.write(buffer, 0, count);
                    }
                    fos.close();
                    ins.close();
                } else {
                    Log.e(TAG, "File exist");
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            return true;
        } else {
            return false;
        }
    }

    private static String getConfDir() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            String dirPath = Environment.getExternalStorageDirectory()
                    .getPath() + CONF_FILE_DIR;

            try {
                File dir = new File(dirPath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
            } catch (Exception e) {
                Log.e(TAG, "Cannot create directory in SD card");
                e.printStackTrace();
                return null;
            }

            return dirPath;
        } else {
            Log.e(TAG, "No SD Card");
            return null;
        }
    }
}
