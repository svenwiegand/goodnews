package com.gettingmobile.goodnews.settings;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.preference.ListPreference;
import android.preference.Preference;
import com.gettingmobile.android.app.DialogFactory;
import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;
import com.gettingmobile.goodnews.storage.StorageProvider;
import com.gettingmobile.goodnews.storage.StorageProviderFactory;

import java.io.IOException;
import java.text.MessageFormat;

class DatabaseStorageProviderChangeListener implements Preference.OnPreferenceChangeListener {
    private final ActionContext<Application> context;

    public DatabaseStorageProviderChangeListener(ActionContext<Application> context) {
        this.context = context;
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, Object o) {
        final StorageProvider src = context.getApp().getSettings().getDatabaseStorageProvider();
        final StorageProvider dest = StorageProviderFactory.createStorageProvider(
                context.getApp(), StorageProvider.Storage.valueOf(o.toString()));
        if (src.getType() == dest.getType())
            return false; // nothing changed

        DialogFactory.buildContinueDialog(
                context.getActivity(), R.string.pref_database_move,
                R.string.pref_database_move_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        perform((ListPreference) preference, src, dest);
                    }
                }
        ).show();
        return false;
    }

    protected void perform(final ListPreference preference, final StorageProvider src, final StorageProvider dest) {
        context.showWaitDialog();
        new AsyncTask<Object, Object, Exception>() {
            @Override
            protected Exception doInBackground(Object... objects) {
                try {
                    context.getApp().getDbHelper().moveOpenDatabase(src, dest);
                    return null;
                } catch (IOException ex) {
                    return ex;
                }
            }

            @Override
            protected void onPostExecute(Exception o) {
                context.dismissWaitDialog();
                if (o == null) {
                    context.getApp().getSettings().setDatabaseStorageProvider(dest);
                    preference.setValue(dest.getType().name());
                } else {
                    DialogFactory.showErrorDialog(context.getActivity(), R.string.pref_database_move,
                            MessageFormat.format(context.getApp().getString(R.string.pref_database_move_failed),
                                    o.getLocalizedMessage()));
                }
                super.onPostExecute(o);
            }

            @Override
            protected void onCancelled() {
                context.dismissWaitDialog();
                super.onCancelled();
            }
        }.execute();
    }
}
