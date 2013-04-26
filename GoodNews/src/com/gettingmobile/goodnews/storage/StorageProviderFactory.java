package com.gettingmobile.goodnews.storage;

import android.content.Context;
import com.gettingmobile.android.util.ApiLevel;

public class StorageProviderFactory {
    public static StorageProvider createExternalStorageProvider(Context context) {
        return ApiLevel.getApiLevel() >= 8 ?
                new ExternalStorageProvider(context) : new OldExternalStorageProvider(context);
    }

    public static StorageProvider createInternalStorageProvider(Context context) {
        return new InternalStorageProvider(context);
    }

    public static StorageProvider createStorageProvider(Context context, StorageProvider.Storage storage) {
        switch (storage) {
            case INTERNAL:
                return createInternalStorageProvider(context);
            case EXTERNAL:
                return createExternalStorageProvider(context);
            default:
                /*
                 * cannot happen
                 */
                assert false;
                return null;
        }
    }

}
