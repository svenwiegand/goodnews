package com.gettingmobile.goodnews.storage;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Equivalent to {@link ExternalStorageProvider} but for API levels lower than 8.
 */
public class OldExternalStorageProvider extends AbstractExternalStorageProvider {
    public OldExternalStorageProvider(Context context) {
        super(context);
    }

    @Override
    public File getDirectory(String category) {
        final File dir = new File(Environment.getExternalStorageDirectory(),
                "Android/data/" + context.getPackageName() + "/files/" + category);
        dir.mkdirs();
        return dir;
    }
}
