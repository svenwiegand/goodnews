package com.gettingmobile.goodnews.storage;

import android.content.Context;

import java.io.File;

public class ExternalStorageProvider extends AbstractExternalStorageProvider {
    public ExternalStorageProvider(Context context) {
        super(context);
    }

    @Override
    public File getDirectory(String category) {
        final File dir = new File(context.getExternalFilesDir(null), category);
        dir.mkdirs();
        return dir;
    }
}
