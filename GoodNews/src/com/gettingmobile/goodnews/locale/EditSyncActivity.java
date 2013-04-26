package com.gettingmobile.goodnews.locale;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import com.gettingmobile.goodnews.R;

import static com.gettingmobile.goodnews.locale.Constants.*;

public class EditSyncActivity extends LocaleEditActivity {
    private Spinner actionSpinner = null;
    private RadioGroup syncTypeGroup = null;

    public EditSyncActivity() {
        super(R.string.locale_title);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        setContentView(R.layout.locale_edit_sync);
        actionSpinner = (Spinner) findViewById(R.id.locale_action);
        actionSpinner.setOnItemSelectedListener(new OnActionSelectedListener());
        syncTypeGroup = (RadioGroup) findViewById(R.id.locale_sync_choice);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void loadSettings(Bundle settings) {
        final int action = settings.getInt(INTENT_EXTRA_ACTION, -1);
        if (action == ACTION_SYNC) {
            actionSpinner.setSelection(action);
        } else {
            actionSpinner.setSelection(0);
        }

        final int syncType = settings.getInt(INTENT_EXTRA_SYNC_TYPE, SYNC_TYPE_FULL);
        syncTypeGroup.check(syncType == SYNC_TYPE_PUSH ? R.id.locale_sync_choice_push : R.id.locale_sync_choice_full);
        updateSyncTypeVisibility();
    }

    @Override
    protected String saveSettings(Bundle settings) {
        /*
         * store settings
         */
        final int action = actionSpinner.getSelectedItemPosition();
        settings.putInt(INTENT_EXTRA_ACTION, action);

        final int syncType = syncTypeGroup.getCheckedRadioButtonId() == R.id.locale_sync_choice_push ?
                SYNC_TYPE_PUSH : SYNC_TYPE_FULL;
        settings.putInt(INTENT_EXTRA_SYNC_TYPE, syncType);

        /*
         * build blurb
         */
        final String syncTypeBlurb = getString(syncType == SYNC_TYPE_PUSH ?
                R.string.locale_blurb_sync_type_push : R.string.locale_blurb_sync_type_full);
        return String.format(getString(R.string.locale_blurb_sync, syncTypeBlurb));
    }

    /*
     * control handling
     */

    protected void updateSyncTypeVisibility() {
        final int action = actionSpinner.getSelectedItemPosition();
        syncTypeGroup.setVisibility(action == ACTION_SYNC ? View.VISIBLE : View.INVISIBLE);
    }

    /*
     * inner classes
     */

    final class OnActionSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            updateSyncTypeVisibility();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            updateSyncTypeVisibility();
        }
    }
}