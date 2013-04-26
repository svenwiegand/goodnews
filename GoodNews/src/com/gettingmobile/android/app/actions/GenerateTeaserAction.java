package com.gettingmobile.android.app.actions;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.settings.Settings;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.Item;
import com.gettingmobile.google.reader.db.ItemCursor;
import com.gettingmobile.google.reader.db.ItemDatabaseAdapter;

import java.util.HashMap;
import java.util.Map;

public final class GenerateTeaserAction extends AbstractAction<Application> {
    private final ElementId feedId;

    public GenerateTeaserAction(ElementId feedId) {
        this.feedId = feedId;
    }

    public GenerateTeaserAction() {
        this(null);
    }
    
    @Override
    public boolean onFired(final ActionContext<? extends Application> context) {
        generateTeasers(context);
        return true;
    }

    private void generateTeasers(final ActionContext<? extends Application> context) {
        context.showWaitDialog();
        //noinspection unchecked
        new AsyncTask<Object, Integer, Boolean>() {
            @Override
            protected Boolean doInBackground(Object... ignore) {
                final Settings settings = context.getApp().getSettings();
                final SQLiteDatabase db = context.getApp().getDbHelper().getDatabase();

                final ItemDatabaseAdapter itemAdapter = new ItemDatabaseAdapter();
                final ItemCursor c = feedId != null ?
                        itemAdapter.cursorActiveWithContentWithoutTags(db, feedId) :
                        itemAdapter.cursorActiveWithContentWithoutTags(db);
                //publishProgress(c.getCount(), 0);

                final int blockSize = 100;
                final Map<Long, String> teaserByKey = new HashMap<Long, String>(blockSize);
                if (c.moveToFirst()) {
                    while (!c.isAfterLast()) {
                        teaserByKey.clear();
                        int i = 0;
                        do {
                            final Item item = c.getEntity();
                            item.createTeaser(
                                    settings.getFeedTeaserSource(item.getFeedId()), 
                                    settings.getFeedTeaserStartChar(item.getFeedId()),
                                    settings.getContentStorageProvider());
                            teaserByKey.put(item.getKey(), item.getTeaser());
                        } while (c.moveToNext() && ++i < blockSize);

                        /*
                         * write block
                         */
                        db.beginTransaction();
                        try {
                            for (Map.Entry<Long, String> teaser : teaserByKey.entrySet()) {
                                itemAdapter.updateTeaser(db, teaser.getKey(), teaser.getValue());
                            }
                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }
                        //publishProgress(c.getCount(), c.getPosition());
                    }
                }
                return true;  //To change body of implemented methods use File | Settings | File Templates.
            }

//            @Override
//            protected void onProgressUpdate(Integer... values) {
//                final int max = values[0];
//                final int progress = values[1];
//                context.setProgress(max, progress);
//            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                context.dismissWaitDialog();
            }
        }.execute();
    }
}
