package com.gettingmobile.google.reader;


import com.gettingmobile.google.reader.db.Table;

public abstract class Element {
	private long key = Table.INVALID_ID;
	private ElementId id;
	private String title;
	
	public Element() {
	}
	
	public long getKey() {
		return key;
	}

	public void setKey(long key) {
		this.key = key;
	}

	public ElementId getId() {
		return id;
	}
	
	public void setId(ElementId id) {
		this.id = id;
	}
	
	/**
	 * Shortcut for {@code getId().getType()}.
	 * @return the item's type.
	 */
	public ElementType getType() {
		return id.getType();
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public abstract boolean isRead();

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Element other = (Element) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return id != null ? id.toString() : super.toString();
	}
}
