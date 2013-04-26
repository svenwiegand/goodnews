package com.gettingmobile.goodnews.home;

import com.gettingmobile.google.reader.db.EntityListCursor;

import java.util.List;

final class HomeListItemCursor extends EntityListCursor<HomeListItem> {
    public HomeListItemCursor(List<HomeListItem> entities) {
        super(entities);
    }

    @Override
    protected long getEntityId(HomeListItem entity) {
        return entity.id;
    }
}
