package com.gettingmobile.goodnews.home;

import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;
import com.gettingmobile.goodnews.util.ThemeUtil;
import com.gettingmobile.goodnews.widget.ElementRowViewType;
import com.gettingmobile.google.reader.*;

final class HomeRowViewType extends ElementRowViewType {
	private final Application app;
    private final ThemeUtil themeUtil;
	private final OnFolderEdgeButtonClickListener listener;
	
	public HomeRowViewType(Application app, OnFolderEdgeButtonClickListener listener) {
        super(R.layout.tag_row);
		this.app = app;
        themeUtil = app.getThemeUtil();
		this.listener = listener;
	}

    @Override
	public void bindView(View view, Object item) {
        final SortedElement element = (SortedElement) item;

        /*
         * tag the view
         */
        view.setTag(element.getId());

        /*
         * determine correct drawable
         */
		final Drawable image;
        if (element instanceof Tag) {
            final Tag tag = (Tag) element;
            if (tag.isUserLabel() && app.getSettings().getLabelReadListId().equals(tag.getId())) {
                image = themeUtil.getThemeDrawable(view.getContext(), R.attr.listReadListIcon);
            } else if (tag.isUserLabel() && tag.isFeedFolder()) {
                image = themeUtil.getThemeDrawable(view.getContext(), R.attr.listFolderIcon);
            } else if (tag.isUserLabel() && !tag.isFeedFolder()) {
                image = themeUtil.getThemeDrawable(view.getContext(), R.attr.listLabelIcon);
            } else if (tag.getType() == ElementType.STATE && tag.getId().equals(ItemState.STARRED.getId())) {
                image = themeUtil.getThemeDrawable(view.getContext(), R.attr.listStarIcon);
            } else if (tag.getType() == ElementType.STATE && tag.getId().equals(ItemState.READING_LIST.getId())) {
                image = themeUtil.getThemeDrawable(view.getContext(), R.attr.listAllIcon);
            } else {
                image = null;
            }
        } else if (element instanceof Feed) {
            image = themeUtil.getThemeDrawable(view.getContext(), R.attr.listFeedIcon);
        } else {
            image = null;
        }
		((ImageView) view.findViewById(R.id.label_row_image)).setImageDrawable(image);

        /*
         * set texts
         */
		final TextView title = (TextView) view.findViewById(R.id.label_row_title);
		title.setText(Html.fromHtml(element.getTitle()));
		title.setTextAppearance(view.getContext(), themeUtil.getThemeResource(view,
                element.getUnreadCount() > 0 ? R.attr.textAppearanceListItemEmphasized : R.attr.textAppearanceListItem));

		final TextView unreadCount = (TextView) view.findViewById(R.id.label_row_unread_count);
		if (element.getUnreadCount() != 0) {
			unreadCount.setText(Integer.toString(element.getUnreadCount()));
			unreadCount.setVisibility(View.VISIBLE);
		} else {
			unreadCount.setVisibility(View.GONE);
		}

        /*
         * init details button
         */
		final Button detailsButton = (Button) view.findViewById(R.id.folder_edge_button);
        if (element instanceof Tag) {
            final Tag tag = (Tag) element;
            detailsButton.setVisibility(tag.isFeedFolder() || tag.getId().equals(ItemState.READING_LIST.getId()) ?
                    View.VISIBLE : View.INVISIBLE);
            detailsButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onFolderEdgeButtonClick(tag);
                }
            });
        } else {
            detailsButton.setVisibility(View.INVISIBLE);
        }
	}

}
