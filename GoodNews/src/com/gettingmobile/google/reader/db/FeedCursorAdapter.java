package com.gettingmobile.google.reader.db;

import android.database.Cursor;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.Feed;

public class FeedCursorAdapter extends SortedElementCursorAdapter<Feed> {
    private static final String TAG_ID = "tagId";
    int htmlUrlCol;
    int rootSortOrderCol;
    int tagIdCol;

    public void init(Cursor c) {
        super.init(c);
        
        htmlUrlCol = c.getColumnIndex(FeedTable.HTML_URL);
        rootSortOrderCol = c.getColumnIndex(FeedTable.ROOT_SORT_ORDER);
        tagIdCol = c.getColumnIndex(TAG_ID);
    }

    @Override
    protected Feed createEntity() {
        return new Feed();
    }

    @Override
    public Feed readEntity(Cursor c) {
        final Feed feed = super.readEntity(c);

        feed.setHtmlUrl(c.getString(htmlUrlCol));
        feed.setRootSortOrder(c.getInt(rootSortOrderCol));

        if (tagIdCol >= 0) {
            final String tagId = c.getString(tagIdCol);
            if (tagId != null) {
                feed.getTagIds().add(new ElementId(c.getString(tagIdCol)));
            }
        }

        return feed;
    }

    @Override
    public void readEntityJoin(Feed entity, Cursor c) {
        super.readEntityJoin(entity, c);

        final ElementId tagId = new ElementId(c.getString(tagIdCol));
        entity.getTagIds().add(tagId);
        entity.setSortOrder(tagId, c.getInt(sortIdCol));
    }
}
