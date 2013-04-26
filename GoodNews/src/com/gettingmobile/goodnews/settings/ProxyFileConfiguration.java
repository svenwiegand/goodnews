package com.gettingmobile.goodnews.settings;

import android.content.Context;
import android.util.Log;
import com.gettingmobile.goodnews.storage.StorageProvider;
import com.gettingmobile.goodnews.storage.StorageProviderFactory;
import com.gettingmobile.rest.ProxyConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ProxyFileConfiguration {
    private static final String LOG_TAG = "goodnews.ProxyFileConfiguration";

    public static void init(Context context) {
        final StorageProvider storage =
                StorageProviderFactory.createStorageProvider(context, StorageProvider.Storage.EXTERNAL);
        final File proxyConfigFile = storage.getFile("prefs", "proxy");
        if (proxyConfigFile.exists()) {
            final Properties proxyConfig = new Properties();
            try {
                final InputStream proxyConfigStream = new FileInputStream(proxyConfigFile);
                try {
                    proxyConfig.load(proxyConfigStream);
                } finally {
                    proxyConfigStream.close();
                }
                final String host = proxyConfig.getProperty("host");
                final int port = Integer.decode(proxyConfig.getProperty("port", "0"));
                Log.i(LOG_TAG, "Found proxy configuration: " + host + ":" + port);
                System.setProperty(ProxyConfiguration.PROPERTY_HOST, host);
                System.setProperty(ProxyConfiguration.PROPERTY_PORT, Integer.toString(port));
                Log.i(LOG_TAG, "Stored proxy configuration: " + System.getProperty(ProxyConfiguration.PROPERTY_HOST) + ":" + System.getProperty(ProxyConfiguration.PROPERTY_PORT));
            } catch (IOException ex) {
                Log.e(LOG_TAG, "Failed to read proxy configuration file from " + proxyConfigFile, ex);
            } catch (NumberFormatException ex) {
                Log.e(LOG_TAG, "Invalid proxy port: " + proxyConfig.getProperty("port"));
            }
        } else {
            Log.i(LOG_TAG, "No proxy configuration found at " + proxyConfigFile);
        }
    }
}
