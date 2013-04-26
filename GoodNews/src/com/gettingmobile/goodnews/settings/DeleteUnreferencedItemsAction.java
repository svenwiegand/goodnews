package com.gettingmobile.goodnews.settings;

import android.database.sqlite.SQLiteDatabase;
import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;
import com.gettingmobile.goodnews.sync.CleanupService;
import com.gettingmobile.google.reader.db.ItemDatabaseAdapter;

public class DeleteUnreferencedItemsAction extends PreferenceAction {
    public DeleteUnreferencedItemsAction() {
        super(R.string.pref_delete_unreferenced, R.string.pref_delete_unreferenced_confirm);
    }

    @Override
    protected void asyncPerform(ActionContext<? extends Application> context) {
        final SQLiteDatabase db = context.getApp().getDbHelper().getDatabase();
        db.beginTransaction();
        try {
            final ItemDatabaseAdapter itemAdapter = new ItemDatabaseAdapter();
            itemAdapter.deleteReadUnreferencedItems(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        context.getApp().getDbHelper().tryVacuum();
        CleanupService.start(context.getApp());
    }
}
