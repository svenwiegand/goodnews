package com.gettingmobile.goodnews.itemlist;

import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.text.Html;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.gettingmobile.android.app.DialogFactory;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;
import com.gettingmobile.goodnews.itemview.ClassicItemUrlSharer;
import com.gettingmobile.goodnews.itemview.ItemUrlSharer;
import com.gettingmobile.goodnews.settings.SettingsIntentFactory;
import com.gettingmobile.goodnews.tip.TipDialogHandler;
import com.gettingmobile.goodnews.util.*;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.Item;
import com.gettingmobile.google.reader.ItemState;
import com.gettingmobile.google.reader.ItemTeaserSource;
import com.gettingmobile.google.reader.db.ItemDatabaseAdapter;
import com.gettingmobile.google.reader.db.ItemTagChangeListener;
import com.gettingmobile.text.HtmlUtil;
import com.google.inject.Inject;
import roboguice.RoboGuice;

import java.text.DecimalFormat;
import java.text.NumberFormat;

final class ItemRowViewHandler implements View.OnClickListener, OnCheckedChangeListener, ItemTagChangeListener {
    private static final NumberFormat MBYTE_FORMAT = new DecimalFormat("#,##0");
    private final ItemTimestampFormat timestampFormat;
	private final ItemListActivity activity;
	private final Application app;
    private final ItemUrlSharer itemUrlSharer;
	private View view = null;
    private TextView titleView = null;
    private TextView feedTitleView = null;
    private TextView timestampView = null;
    private TextView labelView = null;
    private TextView teaserView = null;
    private Button offlineIndicator = null;
    private ToggleButton readButton = null;
    private ToggleButton starredButton = null;
    private ToggleButton readListButton = null;
	private Item item = null;
    @Inject
    private SettingsIntentFactory settingsIntentFactory = null;

	public static ItemRowViewHandler getByView(View view) {
		return (ItemRowViewHandler) (view != null ? view.getTag() : null);
	}
	
	public ItemRowViewHandler(ItemListActivity activity) {
		this.activity = activity;
        activity.itemTagChangeAdapter.addListener(this);
		app = activity.getApp();
        timestampFormat = new ItemTimestampFormat(app, false);
        itemUrlSharer = new ClassicItemUrlSharer(activity);
        RoboGuice.getInjector(activity).injectMembers(this);
	}

	public void init(View view, Item item) {
        if (this.view != view) {
            this.view = view;
            titleView = (TextView) view.findViewById(R.id.item_row_title);
            feedTitleView = (TextView) view.findViewById(R.id.item_row_feed_title);
            timestampView = (TextView) view.findViewById(R.id.timestamp);
            labelView = (TextView) view.findViewById(R.id.labels);
            labelView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showTagSelectionDialog();
                }
            });
            offlineIndicator = (Button) view.findViewById(R.id.indicator_offline);
            readButton = (ToggleButton) view.findViewById(R.id.button_read);
            starredButton = (ToggleButton) view.findViewById(R.id.button_starred);
            readListButton = (ToggleButton) view.findViewById(R.id.button_readlist);
            teaserView = (TextView) view.findViewById(R.id.teaser);

            /*
             * handle offline indicator
             */
            offlineIndicator.setOnClickListener(this);

            /*
             * handle read state
             */
            readButton.setOnCheckedChangeListener(this);

            /*
             * handle starred state
             */
            starredButton.setOnCheckedChangeListener(this);

            /*
             * handle read list
             */
            readListButton.setOnCheckedChangeListener(this);
        }
        view.setTag(this);
		this.item = item;
        itemUrlSharer.setItemInfo(item.getAlternate() != null ? item.getAlternate().getHref() : null, item.getTitle());

		/*
		 * item title
		 */
		titleView.setText(Html.fromHtml(ItemTextUtil.getUnformattedItemTitle(app, item)));

		/*
		 * feed title
		 */
        if (!activity.getGroupByFeeds() && !activity.isFeedsList()) {
            String feedTitleText = ItemTextUtil.getItemSpecialFeedTitle(app, item);
            if (feedTitleText == null) {
                feedTitleText = item.getFeedTitle();
            }
            feedTitleView.setText(feedTitleText != null ? HtmlUtil.removeTags(feedTitleText) : "");
            feedTitleView.setVisibility(View.VISIBLE);
        } else {
            feedTitleView.setVisibility(View.GONE);
        }

        /*
         * item time
         */
        timestampView.setText(timestampFormat.format(item.getTimestamp()));

        /*
         * teaser
         */
        final int maxTeaserWordCount = app.getSettings().getTeaserWordCount();
        final String teaser = maxTeaserWordCount > 0 && item.hasTeaser() &&
                        app.getSettings().getFeedTeaserSource(item.getFeedId()) != ItemTeaserSource.NONE ?
                item.getTeaser(maxTeaserWordCount) : null;
        if (teaser != null && teaser.length() > 0) {
            teaserView.setVisibility(View.VISIBLE);
            teaserView.setText(Html.fromHtml(teaser));
        } else {
            teaserView.setVisibility(View.GONE);
        }

        /*
         * choose adequate list button
         */
        starredButton.setVisibility(app.getSettings().shouldShowStarredTag() ? View.VISIBLE : View.GONE);
        readListButton.setVisibility(app.getSettings().shouldShowReadListTag() ? View.VISIBLE : View.GONE);

		updateView();
	}

    public Item getItem() {
        return item;
    }

    public void updateView() {
        activity.itemListAdapter.adjustItem(item);
        updateOfflineIndicator();
        updateViewReadState(item.isRead());
        updateToggleButton(starredButton, item.hasTag(ItemState.STARRED.getId()));
        updateToggleButton(readListButton, item.hasTag(app.getSettings().getLabelReadListId()));
        updateLabelView();
    }

    private void updateToggleButton(ToggleButton btn, boolean state) {
        btn.setOnCheckedChangeListener(null);
        btn.setChecked(state);
        btn.setOnCheckedChangeListener(this);
    }

    private void updateLabelView() {
        TagListViewController.setTags(labelView,
                TagFilter.filterSpecialTagIds(app.getSettings(), item.getTagIds()),
                app.getResources().getColor(R.color.highlight));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.indicator_offline) {
            onOfflineIndicatorClicked();
        }
    }

    @Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.button_read) {
            setRead(isChecked);
        } else if (buttonView.getId() == R.id.button_starred) {
            onStarredCheckedChanged(isChecked);
        } else if (buttonView.getId() == R.id.button_readlist) {
            onReadListCheckedChanged(isChecked);
        }
	}

    private void updateViewReadState(boolean isRead) {
        item.setRead(isRead);
        updateToggleButton(readButton, isRead);
        setTitleViewAppearance(isRead);
        if (isRead) {
            activity.onMarkedItemRead();
        }
    }
    
    private void setTitleViewAppearance(boolean isRead) {
        titleView.setTextAppearance(view.getContext(), app.getThemeUtil().getThemeResource(view,
                isRead ? R.attr.textAppearanceListItem : R.attr.textAppearanceListItemEmphasized));
        switch (app.getSettings().getItemListTitleTextSize()) {
            case SMALLEST:
                setTitleViewSize(R.dimen.title_text_smallest);
                break;
            case SMALLER:
                setTitleViewSize(R.dimen.title_text_smaller);
                break;
            default:
                // no explicit change
        }
    }
    
    private void setTitleViewSize(int dimensionId) {
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, app.getResources().getDimension(dimensionId));        
    }

    private void implicitlyUpdateViewReadState() {
        if (app.getSettings().markReadOnTag()) {
            updateViewReadState(true);
        }
    }

	private void implicitlyMarkRead() {
        if (app.getSettings().markReadOnTag()) {
            markRead(true);
        }
	}

    private void markRead(boolean isRead) {
        activity.itemTagChangeAdapter.markItemRead(item, isRead);
    }
	
	private void setRead(boolean isRead) {
        markRead(isRead);
        updateViewReadState(isRead);
	}

    private void onLabelCheckedChanged(final ElementId labelId, ToggleButton btn, final boolean isChecked) {
        /*
         * at first update controls to give quick visual feedback
         */
        if (isChecked) {
            item.getTagIds().add(labelId);
        } else {
            item.getTagIds().remove(labelId);
        }
        updateToggleButton(btn, isChecked);
        if (isChecked) {
            implicitlyUpdateViewReadState();
        }

        /*
         * now adjust the database
         */
        activity.itemTagChangeAdapter.changeItemTag(item, labelId, isChecked);
        if (isChecked) {
            implicitlyMarkRead();
        }
    }

    private void onStarredCheckedChanged(boolean isStarred) {
        onLabelCheckedChanged(ItemState.STARRED.getId(), starredButton, isStarred);
    }

    private void onReadListCheckedChanged(boolean isOnReadList) {
        onLabelCheckedChanged(app.getSettings().getLabelReadListId(), readListButton, isOnReadList);
    }

    private void onOfflineIndicatorClicked() {
        if (app.getSettings().offlineIndicatorTogglesReadState()) {
            setRead(!item.isRead());
        } else {
            TipDialogHandler.start(activity, app.getTipManager(), "offline_indicator");
        }
    }

    private void updateOfflineIndicator() {
        final int offlineIndicatorDrawableId;
        if (item != null && item.hasContent()) {
            offlineIndicatorDrawableId = app.getThemeUtil().getThemeResource(
                    offlineIndicator, R.attr.offlineContentFullIcon);
        } else if (item != null && item.hasSummary()) {
            offlineIndicatorDrawableId = app.getThemeUtil().getThemeResource(
                    offlineIndicator, R.attr.offlineContentSummaryIcon);
        } else {
            offlineIndicatorDrawableId = app.getThemeUtil().getThemeResource(
                    offlineIndicator, R.attr.offlineContentNoneIcon);
        }
        offlineIndicator.setCompoundDrawablesWithIntrinsicBounds(offlineIndicatorDrawableId, 0, 0, 0);
    }

    /*
     * Context menu handling
     */

    public void onPrepareContextMenu(ContextMenu menu) {
        if (item != null) {
            final String url = item.getAlternate() != null ? item.getAlternate().getHref() : null;
            menu.findItem(R.id.menu_item_browser).setEnabled(url != null && url.length() > 0);

            /*
             * provide delete option is this is a label list
             */
            menu.findItem(R.id.menu_item_blacklist).setVisible(activity.getIntentElementIsStateOrLabel());
        } else {
            menu.clear();
        }
    }

    public boolean onContextItemSelected(int itemId) {
        if (itemId == R.id.menu_item_open) {
            activity.openItem(item);
        } else if (itemId == R.id.menu_item_browser) {
            activity.openItemInBrowser(item);
        } else if (itemId == R.id.menu_item_tags) {
            showTagSelectionDialog();
        } else if (itemId == R.id.menu_item_share) {
            shareItemUrl();
        } else if (itemId == R.id.menu_item_feed_preferences) {
            if (item.getFeedId() != null) {
                activity.startActivity(settingsIntentFactory.createFeedSettingsIntent(item.getFeedId()));
            }
        } else if (itemId == R.id.menu_item_blacklist) {
            blacklistItem();
        } else {
            return false;
        }
        return true;
    }

    protected void showTagSelectionDialog() {
        new TagSelectionDialogBuilder(activity, item, activity.itemTagChangeAdapter).show();
    }

    protected void shareItemUrl() {
        itemUrlSharer.handleAction();
    }

    protected void blacklistItem() {
        DialogFactory.showYesNoDialog(activity, R.string.blacklist_title, R.string.blacklist_message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final SQLiteDatabase db = app.getDbHelper().getDatabase();
                db.beginTransaction();
                try {
                    new ItemDatabaseAdapter().blacklistItem(db, item);
                    db.setTransactionSuccessful();
                    activity.loadView();
                } finally {
                    db.endTransaction();
                }
            }
        });
    }
    
    /*
     * ItemTagChangeListener implementation
     */

    @Override
    public void onItemReadStateChanged(long itemKey, boolean read) {
        if (item != null && item.getKey() == itemKey) {
            updateView();
        }
    }

    @Override
    public void onItemTagChanged(long itemKey, ElementId tag, boolean added) {
        if (item != null && item.getKey() == itemKey) {
            updateView();
        }
    }
}
