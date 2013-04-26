package com.gettingmobile.goodnews.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.database.sqlite.SQLiteDatabase;
import com.gettingmobile.android.util.ApiLevel;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.Item;
import com.gettingmobile.google.reader.ItemState;
import com.gettingmobile.google.reader.Tag;
import com.gettingmobile.google.reader.db.ItemTagChangeDatabaseAdapter;
import com.gettingmobile.google.reader.db.TagDatabaseAdapter;

import java.util.List;

public final class TagSelectionDialogBuilder implements OnMultiChoiceClickListener {
	private final Activity activity;
	private final Application app;
	private final Item item;
	private final ElementId[] tagIds;
	private final String[] tagTitles;
	private final boolean[] tagSelections;
    private final ItemTagChangeDatabaseAdapter itemTagChangeAdapter;

	public TagSelectionDialogBuilder(
            Activity activity, Item item, ItemTagChangeDatabaseAdapter itemTagChangeAdapter) {
		this.activity = activity;
		this.app = (Application) activity.getApplication();
		this.item = itemTagChangeAdapter.adjustItemTags(item);
        this.itemTagChangeAdapter = itemTagChangeAdapter;

		/*
		 * init label list
		 */
		final SQLiteDatabase db = app.getDbHelper().getDatabase();
		final List<Tag> tags = 
			TagFilter.filterSpecialTags(app.getSettings(), new TagDatabaseAdapter().readUserLabelsIgnoreFolders(db));
		
		final int offset = 3;
		tagIds = new ElementId[tags.size() + offset];
		tagTitles = new String[tags.size() + offset];
		tagSelections = new boolean[tags.size() + offset];
		for (int i = offset; i < tagIds.length; ++i) {
			final Tag tag = tags.get(i - offset);
			tagIds[i] = tag.getId();
			tagTitles[i] = tag.getTitle();
			tagSelections[i] = item.hasTag(tag.getId());
		}
		
		tagIds[0] = ItemState.READ.getId();
		tagTitles[0] = app.getString(R.string.tag_read);
		tagSelections[0] = item.isRead();
		
		tagIds[1] = app.getSettings().getLabelReadListId();
		tagTitles[1] = Tag.getTitleById(tagIds[1]);
		tagSelections[1] = item.hasTag(tagIds[1]);
		
		tagIds[2] = ItemState.STARRED.getId();
		tagTitles[2] = app.getString(R.string.tag_starred);
		tagSelections[2] = item.hasTag(tagIds[2]);
	}
	
	protected AlertDialog showDialog(Activity parent) {
		final AlertDialog.Builder b = new AlertDialog.Builder(parent);
		b.setTitle(R.string.select_item_tags);
		b.setNeutralButton(app.getString(R.string.close), null);
		b.setMultiChoiceItems(tagTitles, tagSelections, this);
		
		final AlertDialog dlg = b.create();
		dlg.show();
        return dlg;
	}
	
	public void show() {
		if (ApiLevel.isAtLeast(ApiLevel.V_2_1)) {
			showDialog(activity);
		} else {
			TagSelectionActivity.start(this, activity);
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which, boolean isChecked) {
		final ElementId tagId = tagIds[which];
		if (ItemState.READ.getId().equals(tagId)) {
			itemTagChangeAdapter.markItemRead(item, isChecked);
		} else {
			if (isChecked) {
				itemTagChangeAdapter.addItemTag(item, tagId);
			} else {
				itemTagChangeAdapter.removeItemTag(item, tagId);
			}
		}
	}
}
