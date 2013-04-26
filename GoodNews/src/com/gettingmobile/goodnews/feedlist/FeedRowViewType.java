package com.gettingmobile.goodnews.feedlist;

import android.text.Html;
import android.view.View;
import android.widget.TextView;
import com.gettingmobile.goodnews.Activity;
import com.gettingmobile.goodnews.R;
import com.gettingmobile.goodnews.widget.ElementRowViewType;
import com.gettingmobile.google.reader.Feed;

public class FeedRowViewType extends ElementRowViewType {
    private final Activity activity;

    public FeedRowViewType(Activity activity) {
        super(R.layout.feed_row);
        this.activity = activity;
    }

    @Override
	public void bindView(View view, Object item) {
        final Feed feed = (Feed) item;
		final TextView title = (TextView) view.findViewById(R.id.feed_row_title);
		title.setText(Html.fromHtml(feed.getTitle()));
		title.setTextAppearance(view.getContext(), activity.getApp().getThemeUtil().getThemeResource(view,
				feed.getUnreadCount() > 0 ? R.attr.textAppearanceListItemEmphasized : R.attr.textAppearanceListItem));

		final TextView unreadCount = (TextView) view.findViewById(R.id.feed_row_unread_count);
		if (feed.getUnreadCount() != 0) {
			unreadCount.setText(Integer.toString(feed.getUnreadCount()));
			unreadCount.setVisibility(View.VISIBLE);
		} else {
			unreadCount.setVisibility(View.GONE);
		}
        view.setTag(feed);
	}

}
