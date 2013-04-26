package com.gettingmobile.google.reader.db;

import android.database.Cursor;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.Item;
import com.gettingmobile.google.reader.Resource;

import java.util.Date;

import static com.gettingmobile.google.reader.db.ItemTable.*;

public class ItemCursorAdapter extends ElementCursorAdapter<Item> {
    private static final String TAG_ID = "tagId";

    @Override
    public void init(Cursor c) {
        super.init(c);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected Item createEntity() {
        return new Item();
    }

    @Override
    public Item readEntity(Cursor c) {
        final Item item = super.readEntity(c);

        item.setFeedId(new ElementId(c.getString(c.getColumnIndex(FEED_ID))));
        item.setFeedTitle(c.getString(c.getColumnIndex(FEED_TITLE)));
        item.setRead(c.getInt(c.getColumnIndex(READ)) != 0);

        final int tagIdCol = c.getColumnIndex(TAG_ID);
        if (tagIdCol > -1) {
            final String tagId = c.getString(tagIdCol);
            if (tagId != null) {
                item.getTagIds().add(new ElementId(tagId));
            }
        }

        final long timestamp = c.getLong(c.getColumnIndex(TIMESTAMP));
        item.setTimestamp(timestamp > 0 ? new Date(timestamp) : null);

        final String alternateHref = c.getString(c.getColumnIndex(ALTERNATE_HREF));
        if (alternateHref != null) {
            final Resource alternate = new Resource();
            alternate.setHref(alternateHref);
            alternate.setMimeType(c.getString(c.getColumnIndex(ALTERNATE_MIME_TYPE)));
            item.setAlternate(alternate);
        }

        item.setAuthor(c.getString(c.getColumnIndex(AUTHOR)));

        final int summaryColumnIndex = c.getColumnIndex(SUMMARY);
        if (summaryColumnIndex >= 0) {
            item.setSummary(c.getString(summaryColumnIndex));
        }
        final int contentColumnIndex = c.getColumnIndex(CONTENT);
        if (contentColumnIndex >= 0) {
            item.setContent(c.getString(contentColumnIndex));
        }
        item.setHasSummary(c.getInt(c.getColumnIndex(HAS_SUMMARY)) > 0);
        item.setHasContent(c.getInt(c.getColumnIndex(HAS_CONTENT)) > 0);
        item.setIsExternalContent(c.getInt(c.getColumnIndex(IS_EXTERNAL_CONTENT)) > 0);
        item.setHasImages(c.getInt(c.getColumnIndex(HAS_IMAGES)) > 0);

        final int teaserColumnIndex = c.getColumnIndex(TEASER);
        if (teaserColumnIndex >= 0) {
            item.setTeaser(c.getString(teaserColumnIndex));
        }

        return item;
    }

    @Override
    public void readEntityJoin(Item entity, Cursor c) {
        super.readEntityJoin(entity, c);

        final String tagId = c.getString(c.getColumnIndex(TAG_ID));
        if (tagId != null) {
            entity.getTagIds().add(new ElementId(tagId));
        }
    }
}
