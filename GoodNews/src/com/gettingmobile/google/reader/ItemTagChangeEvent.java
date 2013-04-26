package com.gettingmobile.google.reader;


public class ItemTagChangeEvent {
	private long id = 0;
	private ElementId feedId = null;
	private ElementId itemId = null;
	private TagChangeOperation operation = null;
	private ElementId tagId = null;
	
	public ItemTagChangeEvent() {
	}
    
    public ItemTagChangeEvent(ElementId itemId, ElementId feedId, TagChangeOperation operation, ElementId tag) {
        this.feedId = feedId;
        this.itemId = itemId;
        this.operation = operation;
        this.tagId = tag;
    }
	
	public ItemTagChangeEvent(Item item, TagChangeOperation operation, ElementId tag) {
        this(item.getId(), item.getFeedId(), operation, tag);
	}
    
    public ItemTagChangeEvent(Item item, boolean add, ElementId tag) {
        this(item, add ? TagChangeOperation.ADD : TagChangeOperation.REMOVE, tag);
    }

    public ItemTagChangeEvent(ElementId itemId, ElementId feedId, boolean add, ElementId tag) {
        this(itemId, feedId, add ? TagChangeOperation.ADD : TagChangeOperation.REMOVE, tag);
    }

	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public ElementId getFeedId() {
		return feedId;
	}
	
	public void setFeedId(ElementId feedId) {
		this.feedId = feedId;
	}
	
	public ElementId getItemId() {
		return itemId;
	}
	
	public void setItemId(ElementId itemId) {
		this.itemId = itemId;
	}

    public boolean isAddOperation() {
        return operation == TagChangeOperation.ADD;
    }

    public boolean isRemoveOperation() {
        return operation == TagChangeOperation.REMOVE;
    }
	
	public TagChangeOperation getOperation() {
		return operation;
	}
	
	public void setOperation(TagChangeOperation operation) {
		this.operation = operation;
	}
	
	public ElementId getTagId() {
		return tagId;
	}
	
	public void setTagId(ElementId tagId) {
		this.tagId = tagId;
	}

    @Override
    public String toString() {
        return "ItemTagChangeEvent[id=" + id + "; feedId=" + feedId + "; itemId=" + itemId +
                "; operation=" + operation + "; tagId=" + tagId + "]";
    }
}
