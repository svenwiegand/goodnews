package com.gettingmobile.google.reader;

import com.gettingmobile.google.reader.rest.StreamContentOrder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Feed extends SortedElement implements TaggedElement {
	private Set<ElementId> tags = new TreeSet<ElementId>();
	private String htmlUrl = null;
    private Map<ElementId, Integer> sortOrderInTag = null;
	
	public Feed() {
	}
	
	@Override
	public Set<ElementId> getTagIds() {
		return tags;
	}
	
	@Override
	public boolean hasTag(ElementId tagId) {
		return tags.contains(tagId);
	}

	public String getHtmlUrl() {
		return htmlUrl;
	}
	
	public void setHtmlUrl(String htmlUrl) {
		this.htmlUrl = htmlUrl;
	}

    public void clearSortOrder() {
        if (sortOrderInTag != null) {
            sortOrderInTag.clear();
        }
        rootSortOrder = Integer.MAX_VALUE;
    }

    public void setSortOrder(ElementId tag, int sortOrder) {
        if (sortOrderInTag == null) {
            sortOrderInTag = new HashMap<ElementId, Integer>();
        }
        sortOrderInTag.put(tag, sortOrder);
    }

    public void setSortOrder(StreamContentOrder sortOrder) {
        clearSortOrder();

        final String sortId = getSortId();
        if (sortId != null) {
            if (!tags.isEmpty()) {
                for (ElementId tag : tags) {
                    setSortOrder(tag, sortOrder.getSortOrder(tag, sortId));
                }
            } else {
                final Map<String, Integer> sortIdOrder = sortOrder.getSortIdOrder(ItemState.ROOT.getId());
                final Integer s = sortIdOrder != null ? sortIdOrder.get(sortId) : null;
                if (s != null) {
                    rootSortOrder = s;
                }
            }
        }
    }

    public int getSortOrder(ElementId tagId) {
        final Integer sortOrder = sortOrderInTag != null ? sortOrderInTag.get(tagId) : null;
        return sortOrder != null ? sortOrder : Integer.MAX_VALUE;
    }
}