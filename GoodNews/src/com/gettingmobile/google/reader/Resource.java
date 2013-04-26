package com.gettingmobile.google.reader;


public class Resource {
	private String href = null;
	private String mimeType = null;
	private String mediaType = null;
	private String subType = null;

	public Resource() {
	}
	
	public String getHref() {
		return href;
	}
	
	public void setHref(String href) {
		this.href = href;
	}

    public String getHrefFileExtension() {
        if (href == null)
            return null;

        final int lastPeriodIndex = href.lastIndexOf('.');
        return lastPeriodIndex >= 0 && lastPeriodIndex > href.lastIndexOf('/') ?
                href.substring(lastPeriodIndex + 1).toLowerCase() : null;
    }
	
	public String getMimeType() {
		return mimeType;
	}
	
	public String getMediaType() {
		return mediaType;
	}
	
	public String getSubType() {
		return subType;
	}
	
	public boolean isMediaType(String mediaType) {
		return mediaType != null && mediaType.equals(this.mediaType);
	}
	
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
		if (mimeType != null) {
			final int typeSeparator = mimeType.indexOf('/');
			if (typeSeparator >= 0) {
				mediaType = mimeType.substring(0, typeSeparator);
				if (typeSeparator < mimeType.length()) {
					subType = mimeType.substring(typeSeparator + 1);
				} else {
					subType = "";
				}
			} else {
				mediaType = mimeType;
				subType = "";
			}
		} else {
			mediaType = null;
			subType = null;
		}
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Resource)) return false;

        Resource resource = (Resource) o;

        if (href != null ? !href.equals(resource.href) : resource.href != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return href != null ? href.hashCode() : 0;
    }
}
