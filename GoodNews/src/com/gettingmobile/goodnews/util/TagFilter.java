package com.gettingmobile.goodnews.util;

import com.gettingmobile.goodnews.settings.Settings;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.ItemState;
import com.gettingmobile.google.reader.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class TagFilter {
	public static List<Tag> filterSpecialTags(Settings settings, Collection<Tag> tags) {
		final List<Tag> cleaned = new ArrayList<Tag>(tags);
		final ElementId readListId = settings.getLabelReadListId();
		final ElementId starredId = ItemState.STARRED.getId();
		for (Iterator<Tag> it = cleaned.iterator(); it.hasNext(); ) {
			final ElementId tagId = it.next().getId();
			if (readListId.equals(tagId) || starredId.equals(tagId)) {
				it.remove();
			}			
		}
		return cleaned;
	}
	
	public static List<ElementId> filterSpecialTagIds(Settings settings, Collection<ElementId> tagIds) {
		final List<ElementId> cleaned = new ArrayList<ElementId>(tagIds);
		cleaned.remove(settings.getLabelReadListId());
		cleaned.remove(ItemState.STARRED.getId());
		return cleaned;
	}
}
