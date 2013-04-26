package com.gettingmobile.google.reader;


public abstract class SortedElement extends Element implements Comparable<SortedElement> {
	private String sortId = null;
	private int unreadCount = 0;
    protected int rootSortOrder = Integer.MAX_VALUE;

    @Override
	public boolean isRead() {
		return getUnreadCount() == 0;
	}

	public String getSortId() {
		return sortId;
	}
	
	public void setSortId(String sortId) {
		this.sortId = sortId;
	}

    public int getRootSortOrder() {
        return rootSortOrder;
    }

    public void setRootSortOrder(int rootSortOrder) {
        this.rootSortOrder = rootSortOrder;
    }

	public int getUnreadCount() {
		return unreadCount;
	}
	
	public void setUnreadCount(int unreadCount) {
		this.unreadCount = unreadCount;
	}

    @Override
    public int compareTo(SortedElement other) {
        int result = rootSortOrder - other.rootSortOrder;
        if (result == 0) {
            result = getTitle().compareToIgnoreCase(other.getTitle());
        }
        return result;
    }
}
