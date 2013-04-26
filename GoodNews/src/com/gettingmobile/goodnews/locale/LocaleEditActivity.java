package com.gettingmobile.goodnews.locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.gettingmobile.goodnews.R;
import com.twofortyfouram.locale.BreadCrumber;

public abstract class LocaleEditActivity extends Activity {
    private final int titleId;

    public LocaleEditActivity(int titleId) {
        this.titleId = titleId;
    }

    /*
     * lifecycle
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(BreadCrumber.generateBreadcrumb(this, getIntent(), getString(titleId)));

        /*
         * if savedInstanceState == null, then we are entering the Activity directly from Locale and we need to check whether the
         * Intent has forwarded a Bundle extra (e.g. whether we editing an old setting or creating a new one)
         */
        if (savedInstanceState == null) {
            final Bundle settings = getIntent().getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);

            /*
             * the forwardedBundle would be null if this was a new setting
             */
            if (settings != null) {
                loadSettings(settings);
            }
        }
    }

    /*
     * logic
     */

    protected abstract void loadSettings(Bundle settings);
    protected abstract String saveSettings(Bundle settings);

    public void finish(boolean save) {
        if (save) {
            final Bundle settings = new Bundle();
            final String blurb = saveSettings(settings);
            if (blurb == null)
                    return;

            /*
             * Build the return Intent, into which we'll put all the required extras
             */
            final Intent returnIntent = new Intent();
            returnIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE, settings);
            returnIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB, blurb);
            setResult(RESULT_OK, returnIntent);
        } else {
            setResult(RESULT_CANCELED);
        }

        super.finish();
    }

    @Override
    public void finish() {
        finish(true);
    }

    /*
     * menu handling
     */

    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.locale_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_cancel) {
            finish(false);
        } else if (item.getItemId() == R.id.menu_ok) {
            finish(true);
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
