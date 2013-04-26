package com.gettingmobile.goodnews.download;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.Item;
import com.gettingmobile.google.reader.Resource;
import com.gettingmobile.google.reader.Tag;
import com.gettingmobile.google.reader.db.AbstractDatabaseAdapter;
import com.gettingmobile.google.reader.db.TagDatabaseAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.gettingmobile.google.reader.db.ItemTable.*;

final class ItemDownloadInfoDatabaseAdapter extends AbstractDatabaseAdapter<Item> {
    private static final TagDatabaseAdapter tagAdapter = new TagDatabaseAdapter();

    public ItemDownloadInfoDatabaseAdapter() {
        super(TABLE_NAME);
    }

    public List<Item> readItemDownloadInfosRequiringDownloads(SQLiteDatabase db) {
        return readList(db.query(TABLE_NAME, new String[] { 
                KEY, ID, FEED_ID, ALTERNATE_HREF, ALTERNATE_MIME_TYPE, HAS_CONTENT, HAS_SUMMARY, HAS_IMAGES,
                IS_EXTERNAL_CONTENT },
                "(hasContent=0 AND NOT alternateHref IS NULL AND alternateHref<>'') OR " +
                "(hasImages=0 AND (hasContent<>0 OR hasSummary<>0))", null, null, null, null));
    }

    public List<Item> readItemDownloadInfosRequiringDownloads(SQLiteDatabase db, ElementId tagId) {
        final Tag tag = tagAdapter.readById(db, tagId);
        if (tag == null)
            return new ArrayList<Item>(0);

        return readList(db.rawQuery(
                "SELECT i._id _id, i.id id, feedId, alternateHref, alternateMimeType, hasContent, hasSummary, hasImages, " +
                        "isExternalContent " +
                "FROM item i " +
                "LEFT JOIN itemTag it ON it.itemKey=i._id " +
                "WHERE it.tagKey=? AND " +
                "  ((hasContent=0 AND NOT alternateHref IS NULL AND alternateHref<>'') OR " +
                "  (hasImages=0 AND (hasContent<>0 OR hasSummary<>0)))",
                new String[] { Long.toString(tag.getKey()) }));
    }

    public int readItemDownloadInfosRequiringDownloadCount(SQLiteDatabase db, ElementId tagId) {
        final Tag tag = tagAdapter.readById(db, tagId);
        if (tag == null)
            return 0;

        final Cursor c = db.rawQuery(
                "SELECT COUNT(*) " +
                "FROM item i " +
                "LEFT JOIN itemTag it ON it.itemKey=i._id " +
                "WHERE it.tagKey=? AND " +
                "  ((hasContent=0 AND NOT alternateHref IS NULL AND alternateHref<>'') OR " +
                "  (hasImages=0 AND (hasContent<>0 OR hasSummary<>0)))",
                new String[] { Long.toString(tag.getKey()) });
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }

    public void writeContent(SQLiteDatabase db, Item item, boolean ignoreSummaryAndContent) {
        final ContentValues values = new ContentValues();
        values.put(HAS_CONTENT, item.hasContent());
        values.put(HAS_SUMMARY, item.hasSummary());
        values.put(IS_EXTERNAL_CONTENT, item.isExternalContent());
        values.put(HAS_IMAGES, item.hasImages());
        if (!ignoreSummaryAndContent) {
            values.put(SUMMARY, item.canStoreSummaryInDb() ? item.getSummary() : null);
            values.put(CONTENT, item.canStoreContentInDb() ? item.getContent() : null);
        }
        db.update(TABLE_NAME, values, KEY + "=" + Long.toString(item.getKey()), null);
    }

    public void clearSummarysAndContent(SQLiteDatabase db) {
        final ContentValues values = new ContentValues();
        values.put(HAS_CONTENT, false);
        values.put(HAS_SUMMARY, false);
        db.update(TABLE_NAME, values, null, null);
    }

    @Override
    protected void setRowValues(SQLiteDatabase db, ContentValues columns, Item entity, Bundle parameters) {
        // we will not write complete entities -- so we can leave this empty
    }

    @Override
    protected void attachRowId(Item entity, long id) {
        entity.setKey(id);
    }

    @Override
    protected Item create() {
        return new Item();
    }

    @Override
    public long getRowKey(Cursor c) {
        return getRowKey(c, KEY);
    }

    @Override
    public Item readCurrent(Cursor c) {
        final Item item = super.readCurrent(c);

        item.setKey(getRowKey(c));
        item.setId(new ElementId(c.getString(c.getColumnIndex(ID))));
        item.setFeedId(new ElementId(c.getString(c.getColumnIndex(FEED_ID))));
        final int contentCol = c.getColumnIndex(CONTENT);
        if (contentCol >= 0) {
            item.setContent(c.getString(contentCol));
        }
        item.setHasContent(c.getInt(c.getColumnIndex(HAS_CONTENT)) > 0);
        final int summaryCol = c.getColumnIndex(SUMMARY);
        if (summaryCol >= 0) {
            item.setSummary(c.getString(summaryCol));
        }
        item.setHasSummary(c.getInt(c.getColumnIndex(HAS_SUMMARY)) > 0);
        item.setIsExternalContent(c.getInt(c.getColumnIndex(IS_EXTERNAL_CONTENT)) > 0);
        item.setHasImages(c.getInt(c.getColumnIndex(HAS_IMAGES)) > 0);

        final String alternateHref = c.getString(c.getColumnIndex(ALTERNATE_HREF));
        if (alternateHref != null) {
            final Resource alternate = new Resource();
            alternate.setHref(alternateHref);
            alternate.setMimeType(c.getString(c.getColumnIndex(ALTERNATE_MIME_TYPE)));
            item.setAlternate(alternate);
        }

        return item;
    }
}
