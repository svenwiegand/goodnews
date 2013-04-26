package com.gettingmobile.google.reader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A tag can represent a label/folder or a state.
 * 
 * @author sven.wiegand
 */
public final class Tag extends SortedElement {
	private static final Pattern patternTitle = Pattern.compile(".*/(.*)$");
	private boolean isFeedFolder = false;

    public Tag() {
	}
	
	public Tag(ElementId id) {
		setId(id);
	}

    public Tag(ElementId id, boolean isFeedFolder) {
        this(id);
        setFeedFolder(isFeedFolder);
    }

    public static String getTitleById(ElementId id) {
		final Matcher titleMatcher = patternTitle.matcher(id.getId());
		if (titleMatcher.matches()) {
			return titleMatcher.group(1);
		} else {
			throw new IllegalArgumentException("Title of ID '" + id.getId() + "' is unknown");
		}		
	}
	
	@Override
	public void setId(ElementId id) {
		super.setId(id);
		setTitle(getTitleById(id));
	}
	
	/**
	 * Returns whether the tag is a user defined label (this is also true for user defined folders).
	 * @return whether the tag is a user defined label (this is also true for user defined folders).
	 */
	public boolean isUserLabel() {
		return ElementType.LABEL.equals(getType());
	}

	/**
	 * Returns whether this is a user defined label and there are feeds tagged with this label.
	 * @return whether this is a user defined label and there are feeds tagged with this label.
	 */
	public boolean isFeedFolder() {
		return isFeedFolder;
	}

	public void setFeedFolder(boolean isFeedFolder) {
		this.isFeedFolder = isFeedFolder;
	}
	
	/**
	 * Indicates whether the tag with the specified id is used by this application. This indicates for example whether
	 * the tag can be found in the database or not.
	 * @param tagId the id of the tag to check.
	 * @return whether the tag with the specified id is used by this application.
	 */
	public static boolean isUsed(ElementId tagId) {
		return ItemState.STARRED.getId().equals(tagId) || ElementType.LABEL.equals(tagId.getType());
	}
	
	public boolean isUsed() {
		return isUsed(getId());
	}
}
