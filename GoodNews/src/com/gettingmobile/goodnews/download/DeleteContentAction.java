package com.gettingmobile.goodnews.download;

import android.database.sqlite.SQLiteDatabase;
import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;
import com.gettingmobile.goodnews.settings.PreferenceAction;
import com.gettingmobile.google.reader.Item;
import com.gettingmobile.io.IOUtils;

public class DeleteContentAction extends PreferenceAction {
    public DeleteContentAction() {
        super(R.string.pref_content_delete, R.string.pref_content_delete_confirm,
                R.string.pref_content_delete_failed);
    }

    @Override
    protected void asyncPerform(ActionContext<? extends Application> context) throws Throwable {
        final SQLiteDatabase db = context.getApp().getDbHelper().getDatabase();
        db.beginTransaction();
        try {
            new ItemDownloadInfoDatabaseAdapter().clearSummarysAndContent(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        IOUtils.deleteRecursive(context.getApp().getSettings().getContentStorageProvider().getDirectory(
                Item.STORAGE_CATEGORY));
    }
}
