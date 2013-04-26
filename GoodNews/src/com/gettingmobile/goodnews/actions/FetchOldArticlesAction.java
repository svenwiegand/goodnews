package com.gettingmobile.goodnews.actions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import com.gettingmobile.android.app.DialogFactory;
import com.gettingmobile.android.app.actions.AbstractAction;
import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;
import com.gettingmobile.goodnews.sync.SyncService;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.ItemRequestSpecification;
import com.gettingmobile.google.reader.db.ItemRequestSpecificationDatabaseAdapter;

public final class FetchOldArticlesAction extends AbstractAction<Application> {
    private final ElementId streamId;
    
    public FetchOldArticlesAction(ElementId streamId) {
        this.streamId = streamId;
    }

    @Override
    public boolean onFired(final ActionContext<? extends Application> actionContext) {
        final ViewGroup view = (ViewGroup) LayoutInflater.from(
                actionContext.getActivity()).inflate(R.layout.fetch_old, null);
        prepareDialog(actionContext.getApp(), view);

        new AlertDialog.Builder(actionContext.getActivity()).
                setTitle(R.string.fetch_old_title).
                setView(view).
                setNegativeButton(R.string.cancel, null).
                setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        processDialog(actionContext.getApp(), actionContext.getActivity(), view);
                        dialogInterface.dismiss();
                    }
                }).
                show();
        return true;
    }
    
    private void prepareDialog(Application app, ViewGroup view) {
        /*
         * set default max ages
         */
        final int defaultAgeInDays = app.getSettings().getFetchOldMaxAge();
        final int[] agesInDays = view.getContext().getResources().getIntArray(R.array.fetch_old_max_age_choice_values);
        int index = 0;
        while (index < agesInDays.length && defaultAgeInDays > agesInDays[index]) {
            ++index;
        }
        if (index > agesInDays.length)
            index = agesInDays.length - 1;
        
        final Spinner maxAgeSpinner = (Spinner) view.findViewById(R.id.max_age);
        maxAgeSpinner.setSelection(index);
        
        /*
         * set default max count
         */
        final EditText maxCountTextEdit = (EditText) view.findViewById(R.id.max_count);
        maxCountTextEdit.setText(Integer.toString(app.getSettings().getFetchOldMaxCount()));
        
    }

    private void processDialog(Application app, Activity activity, ViewGroup view) {
        /*
         * get max ages
         */
        final Spinner maxAgeSpinner = (Spinner) view.findViewById(R.id.max_age);
        final int[] agesInDays = view.getContext().getResources().getIntArray(R.array.fetch_old_max_age_choice_values);
        final int ageInDays = agesInDays[maxAgeSpinner.getSelectedItemPosition()];
        app.getSettings().setFetchOldMaxAge(ageInDays);
        
        /*
         * get max count
         */
        final EditText maxCountTextEdit = (EditText) view.findViewById(R.id.max_count);
        final String maxCountStr = maxCountTextEdit.getText().toString();
        final int maxCount = (maxCountStr != null && maxCountStr.length() > 0) ? Integer.parseInt(maxCountStr) : 0;
        app.getSettings().setFetchOldMaxCount(maxCount);
        
        /*
         * persist the request
         */
        final SQLiteDatabase db = app.getDbHelper().getDatabase();
        db.beginTransaction();
        try {
            new ItemRequestSpecificationDatabaseAdapter().insertOrUpdate(db,
                    new ItemRequestSpecification(streamId, ageInDays, maxCount));
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        
        /*
         * sync now?
         */
        final CheckBox syncCheckBox = (CheckBox) view.findViewById(R.id.sync);
        if (syncCheckBox.isChecked()) try {
            final SyncService syncService = app.getSyncService().getService();
            syncService.startFullSync();
        } catch (IllegalStateException ex) {
            if (!activity.isFinishing()) {
                DialogFactory.showConfirmationDialog(activity,
                        R.string.fetch_old_title, R.string.fetch_old_sync_already_running, R.string.close);
            }
        }
    }
}
