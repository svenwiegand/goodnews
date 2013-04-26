package com.gettingmobile.goodnews.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.*;
import android.text.InputType;
import com.gettingmobile.android.app.actions.Action;
import com.gettingmobile.android.app.actions.GenerateTeaserAction;
import com.gettingmobile.android.app.settings.BooleanOverride;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;
import com.gettingmobile.google.reader.*;
import com.gettingmobile.google.reader.db.FeedDatabaseAdapter;

public final class ElementSettingsActivity extends AbstractSettingsActivity {
    public static final String EXTRA_KEY_BASE = "com.gettingmobile.goodnews.";
    public static final String EXTRA_KEY_ELEMENT_ID = EXTRA_KEY_BASE + "ELEMENT_ID";
    public static final String EXTRA_KEY_ELEMENT_TITLE = EXTRA_KEY_BASE + "ELEMENT_TITLE";

    public ElementSettingsActivity() {
    }

    /*
     * create helpers
     */

    private static Intent createStandardIntent(Context packageContext) {
        return new Intent(packageContext, ElementSettingsActivity.class);
    }

    public static Intent createElementSettingsIntent(Context packageContext, ElementId elementId, String elementTitle) {
        final Intent intent;
        if (elementId != null) {
            intent = createStandardIntent(packageContext);
            intent.putExtra(EXTRA_KEY_ELEMENT_ID, elementId.getId());
            intent.putExtra(EXTRA_KEY_ELEMENT_TITLE, elementTitle);
        } else {
            intent = null;
        }
        return intent;
    }

    public static Intent createFeedSettingsIntent(Application app, ElementId elementId) {
        return createElementSettingsIntent(app, elementId,
                new FeedDatabaseAdapter().readTitle(app.getDbHelper().getDatabase(), elementId));
    }

    /*
     * lifecycle management
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * populate preferences based on intent
         */
        final ElementId elementId = getIntentElementId();
        if (elementId.getType() == ElementType.FEED) {
            populateFeedPreferences();
        } else if ((elementId.getType() == ElementType.LABEL) || (ItemState.STARRED.getId().equals(elementId))) {
            populateLabelPreferences();
        }
    }

    /*
     * helpers
     */

    protected ElementId getIntentElementId() {
        final Bundle extras = getIntent().getExtras();
        final String elementIdText = extras != null ? extras.get(EXTRA_KEY_ELEMENT_ID).toString() : null;
        return elementIdText != null ? new ElementId(elementIdText) : null;
    }

    protected String getIntentElementTitle() {
        final Bundle extras = getIntent().getExtras();
        return extras != null ? extras.getString(EXTRA_KEY_ELEMENT_TITLE) : null;
    }

    protected PreferenceCategory createCategory(PreferenceScreen screen, int titleId) {
        final PreferenceCategory c = new PreferenceCategory(this);
        c.setTitle(titleId);
        screen.addPreference(c);
        return c;
    }

    protected ListPreference createListPreference(String key, String title, int summaryId, int dialogTitleId,
                                                  int entriesId, int entryValuesId, Object defaultValue) {
        final ListPreference lp = new ListPreference(this);
        lp.setKey(key);
        lp.setTitle(title);
        lp.setSummary(summaryId);
        lp.setDialogTitle(dialogTitleId);
        lp.setEntries(entriesId);
        lp.setEntryValues(entryValuesId);
        lp.setDefaultValue(defaultValue);
        return lp;
    }

    protected CheckBoxPreference createCheckBoxPreference(String key, String title, int summaryId, boolean defaultValue) {
        final CheckBoxPreference p = new CheckBoxPreference(this);
        p.setKey(key);
        p.setTitle(title);
        p.setSummary(summaryId);
        p.setDefaultValue(defaultValue);
        return p;
    }

    protected ListPreference createBooleanOverridePreference(String key, int titleId, int summaryId) {
        return createListPreference(key, getString(titleId), summaryId, titleId,
                R.array.boolean_override, R.array.boolean_override_values, BooleanOverride.GLOBAL.name());
    }
    
    protected EditTextPreference createEditTextPreference(String key, int titleId, int summaryId, 
                                                          int inputType, Object defaultValue) {
        final EditTextPreference p = new EditTextPreference(this);
        p.setKey(key);
        p.setTitle(getString(titleId));
        p.setSummary(summaryId);
        p.getEditText().setInputType(inputType);
        p.getEditText().setSingleLine();
        p.setDefaultValue(defaultValue.toString());
        return p;
    }
    
    protected Preference createActionPreference(String key, int titleId, int summaryId, 
                                                Action<? extends android.app.Application> action) {
        final Preference p = new Preference(this);
        p.setKey(key);
        p.setTitle(titleId);
        p.setSummary(summaryId);
        registerAction(key, action);
        return p;
    }

    protected void populateFeedPreferences() {
        final ElementId feedId = getIntentElementId();
        assert feedId.getType() == ElementType.FEED;

        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(this);
        screen.setKey(feedId.getId() + "_screen");
        screen.setTitle(getIntentElementTitle());

        /*
         * sync settings
         */
        final PreferenceCategory sync = createCategory(screen, R.string.pref_sync);

        // create ignore setting
        sync.addPreference(createCheckBoxPreference(
                feedId.getId() + "_ignore_unread", getString(R.string.pref_feed_ignore_unread),
                R.string.pref_feed_ignore_unread_summary, false));

        // create auto add to list setting
        sync.addPreference(createListPreference(
                feedId.getId() + "_autolist", getString(R.string.pref_feed_autolist), R.string.pref_feed_autolist_summay,
                R.string.pref_feed_autolist, R.array.boolean_override, R.array.boolean_override_values,
                BooleanOverride.GLOBAL.name()));       

        /*
         * teaser settings
         */
        final PreferenceCategory teaser = createCategory(screen, R.string.pref_teaser);

        // create teaser setting
        teaser.addPreference(createListPreference(
                feedId.getId() + "_teaser_source",
                getString(R.string.pref_teaser_source), R.string.pref_teaser_source_summary,
                R.string.pref_teaser_source, R.array.pref_teaser_source_choice, R.array.pref_teaser_source_choice_values,
                ItemTeaserSource.PREFER_SUMMARY.name()));
        
        // create teaser offset setting
        //noinspection PointlessBitwiseExpression
        teaser.addPreference(createEditTextPreference(
                feedId.getId() + "_teaser_start_char",
                R.string.pref_teaser_start_char, R.string.pref_teaser_start_char_summary,
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL, 0));

        // create teaser generate action
        teaser.addPreference(createActionPreference("teaser_generate",
                R.string.pref_teaser_generate, R.string.pref_teaser_generate_summary,
                new GenerateTeaserAction(feedId)));
        
        /*
         * user interface settings
         */
        final PreferenceCategory ui = createCategory(screen, R.string.pref_ui);
        
        // create item display setting
        ui.addPreference(createListPreference(
                feedId.getId() + "_item_view",
                getString(R.string.pref_feeds_item_view), R.string.pref_feeds_item_view_summary,
                R.string.pref_item_view_choice, R.array.pref_feed_item_view_choice,
                R.array.pref_feed_item_view_choice_values, "GLOBAL"));

        // create scale images setting
        ui.addPreference(createBooleanOverridePreference(
                feedId.getId() + "_scale_images", R.string.pref_scale_images, R.string.pref_scale_images_summary));

        /*
         * news reading settings
         */
        final PreferenceCategory newsReading = createCategory(screen, R.string.pref_news_reading);

        // create mobilizer setting
        newsReading.addPreference(createListPreference(
                feedId.getId() + "_mobilizer",
                getString(R.string.pref_mobilizer), R.string.pref_mobilizer_summary,
                R.string.pref_mobilizer_choice_title, R.array.pref_feed_mobilizer_choice,
                R.array.pref_feed_mobilizer_choice_values, Settings.GLOBAL));

        // create offline reading setting
        newsReading.addPreference(createListPreference(
                feedId.getId() + "_offline_content",
                getString(R.string.pref_offline_reading), R.string.pref_offline_content_summary,
                R.string.pref_offline_content_choice_title, R.array.pref_feed_offline_content_choice,
                R.array.pref_feed_offline_content_choice_values, Settings.GLOBAL));

        // create content treatment settings
        newsReading.addPreference(createListPreference(
                feedId.getId() + "_summary_treatment",
                getString(R.string.pref_feed_summary_treatment), R.string.pref_feed_summary_treatment_summary,
                R.string.pref_feed_summary_treatment_choice_title, R.array.pref_feed_content_treatment_choice,
                R.array.pref_feed_content_treatment_choice_values, ItemContentTreatment.TREAT_AS_SUMMARY.name()));
        newsReading.addPreference(createListPreference(
                feedId.getId() + "_content_treatment",
                getString(R.string.pref_feed_content_treatment), R.string.pref_feed_content_treatment_summary,
                R.string.pref_feed_content_treatment_choice_title, R.array.pref_feed_content_treatment_choice,
                R.array.pref_feed_content_treatment_choice_values, ItemContentTreatment.TREAT_AS_CONTENT.name()));

        setPreferenceScreen(screen);
    }

    protected void populateLabelPreferences() {
        final ElementId tagId = getIntentElementId();
        assert tagId.getType() == ElementType.LABEL;

        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(this);
        screen.setKey(tagId.getId() + "_screen");
        screen.setTitle(getIntentElementTitle());

        screen.addPreference(createCheckBoxPreference(
                tagId.getId() + "_sync", getString(R.string.pref_tag_sync), R.string.pref_tag_sync_summary, true));

        setPreferenceScreen(screen);
    }
}
