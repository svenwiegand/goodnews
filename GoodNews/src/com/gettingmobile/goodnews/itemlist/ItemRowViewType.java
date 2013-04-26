package com.gettingmobile.goodnews.itemlist;

import android.view.View;
import com.gettingmobile.goodnews.R;
import com.gettingmobile.goodnews.widget.ElementRowViewType;
import com.gettingmobile.google.reader.Item;

final class ItemRowViewType extends ElementRowViewType {
	private final ItemListActivity activity;

	public ItemRowViewType(ItemListActivity activity) {
        super(R.layout.item_row);
		this.activity = activity;
	}

	@Override
	public void bindView(View view, final Object o) {
        final Item item = (Item) o;
		ItemRowViewHandler h = ItemRowViewHandler.getByView(view);
		if (h == null) {
			h = new ItemRowViewHandler(activity);
            h.init(view, item);
        } else {
            h.init(view, item);
        }
	}

}
