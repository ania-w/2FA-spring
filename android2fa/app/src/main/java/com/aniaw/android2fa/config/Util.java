package com.aniaw.android2fa.config;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Util {
    public static String getProperty(String key, Context context) throws IOException {
        Properties properties = new Properties();
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open("application.properties");
        properties.load(inputStream);
        return properties.getProperty(key);
    }

    public static void setProperty(String key, String value, Context context) throws IOException {
        Properties properties = new Properties();
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open("application.properties");
        properties.load(inputStream);
        properties.setProperty(key, value);
    }
}