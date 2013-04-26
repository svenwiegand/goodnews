package com.gettingmobile.google.reader;

import java.util.Set;

public interface TaggedElement {
	Set<ElementId> getTagIds();
	boolean hasTag(ElementId tagId);
}
