package com.gettingmobile.google.reader;

import android.net.Uri;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ElementId implements Comparable<ElementId> {
	private static final Pattern patternLabel = Pattern.compile("^user/([0-9]|\\-)+/label/(.*)");
	private static final Pattern patternState = Pattern.compile("^user/([0-9]|\\-)+/state/(.*)");
    private static final Pattern patternSource = Pattern.compile("^user/([0-9]|\\-)+/source/(.*)");
	private static final Pattern patternFeed = Pattern.compile("^feed/.*");
    private static final Pattern patternPopular = Pattern.compile("^pop/.*");
    private static final Pattern patternAlert = Pattern.compile("^user/([0-9]|\\-)+/state/com.google/alerts/(.*)");
	private static final Pattern patternItem = Pattern.compile("tag:google.com,2005:reader/item/(.*)");
	
	private final String id;
	private final String urlEncodedId;
	private final ElementType type;
	
	public static ElementId createUserLabel(String name) {
		return new ElementId("user/-/label/" + name);
	}

    public static ElementId createItemId(long itemReferenceId) {
        return new ElementId(String.format("tag:google.com,2005:reader/item/%016x", itemReferenceId));
    }

	public ElementId(String id) {
		String tmpId = id;
		if (patternItem.matcher(id).matches()) {
			type = ElementType.ITEM;
			urlEncodedId = null;
		} else if (patternFeed.matcher(id).matches() || patternPopular.matcher(id).matches()) {
			type = ElementType.FEED;
			urlEncodedId = null;
		} else {
			Matcher m = patternLabel.matcher(id);
			if (m.matches()) {
				type = ElementType.LABEL;
				tmpId = "user/-/label/" + m.group(2);
				urlEncodedId = "user/-/label/" + Uri.encode(m.group(2));
			} else {
                m = patternAlert.matcher(id);
                if (m.matches()) {
                    type = ElementType.FEED;
                    tmpId = "user/-/state/com.google/alerts/" + m.group(2);
                    urlEncodedId = null;
                } else {
                    m = patternState.matcher(id);
                    if (m.matches()) {
                        type = ElementType.STATE;
                        tmpId = "user/-/state/" + m.group(2);
                        urlEncodedId = null;
                    } else {
                        m = patternSource.matcher(id);
                        if (m.matches()) {
                            type = ElementType.SOURCE;
                            tmpId = "user/-/source/" + m.group(2);
                            urlEncodedId = null;
                        } else {
                            throw new UnknownElementIdTypeException(id);
                        }
                    }
                }
			}
		}
		this.id = tmpId;
	}
	
	public String getId() {
		return id;
	}
	
	public String getUrlEncodedId() {
		return urlEncodedId != null ? urlEncodedId : id;
	}

    public long getItemReferenceId() {
        if (type != ElementType.ITEM)
            throw new IllegalArgumentException("ElementID '" + id + "' is not of type ITEM: " + type.name());

        final Matcher m = patternItem.matcher(id);
        if (!m.matches())
            throw new IllegalArgumentException("ElementID '" + id + "' doesn't match ITEM ID pattern.");

        return new BigInteger(m.group(1), 16).longValue();
    }
	
	public ElementType getType() {
		return type;
	}

	@Override
	public int compareTo(ElementId o) {
		return id.compareTo(o.id) ;
	}

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
		ElementId other = (ElementId) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return id != null ? id : super.toString();
	}
}
