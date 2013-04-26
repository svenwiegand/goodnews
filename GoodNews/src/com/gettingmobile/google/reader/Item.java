package com.gettingmobile.google.reader;

import android.util.Log;
import com.gettingmobile.Security.Hash;
import com.gettingmobile.goodnews.storage.StorageProvider;
import com.gettingmobile.io.Base16;
import com.gettingmobile.io.Base64;
import com.gettingmobile.io.CharacterSet;
import com.gettingmobile.io.IOUtils;
import com.gettingmobile.text.HtmlUtil;
import com.gettingmobile.text.Teaser;

import java.io.*;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

/**
 * An item represents a single entry in a feed. This class encapsulates basic information about a feed item without
 * it's content.
 * 
 * @author sven.wiegand
 */
public final class Item extends Element implements TaggedElement {
    private static final String LOG_TAG = "goodnews.Item";
    public static final String STORAGE_CATEGORY = "content";
    public static final String FILE_NAME_SUMMARY = "summary";
    public static final String FILE_NAME_CONTENT = "content";
    public static final int MAX_TEASER_WORDS = 100;
    public static final int MAX_DB_SIZE = 1024 * 1024; // 1 MB
	private ElementId feedId = null;
    private String feedTitle = null;
	private Date timestamp = null;
	private boolean read = false;
	private Set<ElementId> tags = new TreeSet<ElementId>();
	private Resource alternate = null;
	private String author = null;
	private boolean hasSummary = false;
    private String summary = null;
	private boolean hasContent = false;
    private String content = null;
    private boolean isExternalContent = false;
    private boolean hasImages = false;
    private String teaser = null;
    private String directoryName = null;

	public Item() {
	}
	
	@Override
	public boolean isRead() {
		return read;
	}
	
	public void setRead(boolean read) {
		this.read = read;
	}

	public ElementId getFeedId() {
		return feedId;
	}
	
	public void setFeedId(ElementId feedId) {
		assert feedId.getType() == ElementType.FEED;
		
		this.feedId = feedId;
	}

    public String getFeedTitle() {
        return feedTitle;
    }

    public void setFeedTitle(String feedTitle) {
        this.feedTitle = feedTitle;
    }

    public Date getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public Set<ElementId> getTagIds() {
		return tags;
	}
	
	@Override
	public boolean hasTag(ElementId tagId) {
		return tags.contains(tagId);
	}

	public Resource getAlternate() {
		return alternate;
	}
	
	public void setAlternate(Resource alternate) {
		this.alternate = alternate;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}

	public boolean hasSummary() {
		return hasSummary;
	}

	public void setHasSummary(boolean hasSummary) {
		this.hasSummary = hasSummary;
	}

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
        hasSummary = !isEmpty(summary) && !summary.equals(getTitle());
    }

    public boolean canStoreSummaryInDb() {
        return summary == null || summary.length() < MAX_DB_SIZE;
    }

    public boolean hasContent() {
		return hasContent;
	}

	public void setHasContent(boolean hasContent) {
		this.hasContent = hasContent;
	}

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        hasContent = !isEmpty(content) && !content.equals(getTitle()) && !content.equals(summary);
    }

    public boolean canStoreContentInDb() {
        return content == null || content.length() < MAX_DB_SIZE;
    }

    public boolean isExternalContent() {
        return isExternalContent;
    }

    public void setIsExternalContent(boolean externalContent) {
        isExternalContent = externalContent;
    }

    public boolean hasImages() {
        return hasImages;
    }

    public void setHasImages(boolean hasImages) {
        this.hasImages = hasImages;
    }

    public boolean hasTeaser() {
        return !isEmpty(teaser);
    }
    
    public String getTeaser() {
        return teaser;
    }
    
    public String getTeaser(int maxWords) {
        return teaser != null ? Teaser.tease(teaser, 0, maxWords, HtmlUtil.ELLIPSIS) : null;
    }

    public void setTeaser(String teaser) {
        this.teaser = teaser;
    }
    
    public void createTeaser(ItemTeaserSource sourceType, int startChar, StorageProvider storageProvider) {
        if (sourceType != ItemTeaserSource.NONE) {
            try {
                loadIfRequired(storageProvider);
                final String source = (sourceType == ItemTeaserSource.PREFER_SUMMARY && hasSummary) ? summary : content;
                if (source != null) {
                    teaser = Teaser.tease(HtmlUtil.toText(source), startChar, MAX_TEASER_WORDS, HtmlUtil.ELLIPSIS);
                } else {
                    teaser = null;
                }
            } catch (IOException ex) {
                teaser = null;
            }
        } else {
            teaser = null;
        }
    }

    public void updateContentFlags() {
        setSummary(summary);
        setContent(content);
    }

    public void processContentTreatment(ItemContentTreatment summaryTreatment, ItemContentTreatment contentTreatment) {
        String newSummary = null;
        String newContent = null;

        switch (summaryTreatment) {
            case IGNORE:
                break;
            case TREAT_AS_SUMMARY:
                newSummary = summary;
                break;
            case TREAT_AS_CONTENT:
                newContent = summary;
                break;
        }
        switch (contentTreatment) {
            case IGNORE:
                break;
            case TREAT_AS_SUMMARY:
                newSummary = content;
                break;
            case TREAT_AS_CONTENT:
                newContent = content;
                break;
        }

        setSummary(newSummary);
        setContent(newContent);
    }

    /*
     * file system serialization
     */

    public static String convertFromOldDirectoryName(String name) throws IOException {
        final byte[] data = Base64.decode(name.replace('-', '/'));
        return Base16.encode(data);
    }

    public static String convertToOldDirectoryName(String name) {
        final byte[] data = Base16.decode(name);
        return Base64.encode(data).replace('/', '-');
    }

    public static String getDirectoryName(ElementId itemId) {
        return Base16.encode(Hash.getInstance().create(itemId.toString()));
    }

    public String getDirectoryName() {
        if (directoryName == null) {
            if (getId() == null || getId().getId().length() == 0)
                throw new IllegalStateException("The item must be initialized before accessing file operations.");

            directoryName = getDirectoryName(getId());
        }
        return directoryName;
    }

    public static File getDirectory(StorageProvider storageProvider, String dirName) {
        return new File(storageProvider.getDirectory(STORAGE_CATEGORY),
                "/" + dirName.charAt(0) + "/" + dirName.charAt(1) + "/" + dirName);
    }

    public File getDirectory(StorageProvider storageProvider) {
        return getDirectory(storageProvider, getDirectoryName());
    }

    private File getOldDirectory(StorageProvider storageProvider, File dir) {
        return new File(storageProvider.getDirectory(STORAGE_CATEGORY), convertToOldDirectoryName(dir.getName()));
    }

    private File getDirectoryWithFallback(StorageProvider storageProvider) {
        final File dir = getDirectory(storageProvider);
        return dir.exists() ? dir : getOldDirectory(storageProvider, dir);
    }

    protected void save(File dir, String fileName, String str) throws IOException {
        final File file = new File(dir, fileName);
        final Writer w = new BufferedWriter(new FileWriter(file));
        try {
            w.write(str);
        } finally {
            IOUtils.closeQuietly(w);
        }
    }

    public void saveIfApplicable(boolean storeContentInFiles, StorageProvider storageProvider) throws IOException {
        if (hasSummary || hasContent) {
            final File dir = getDirectory(storageProvider);
            if (summary != null && (storeContentInFiles || !canStoreSummaryInDb())) {
                Log.i(LOG_TAG, "Storing item summary to " + dir.getAbsolutePath());
                IOUtils.ensureDirExists(dir);
                save(dir, FILE_NAME_SUMMARY, summary);
            }
            if (content != null && (storeContentInFiles || !canStoreContentInDb())) {
                Log.i(LOG_TAG, "Storing item content to " + dir.getAbsolutePath());
                IOUtils.ensureDirExists(dir);
                save(dir, FILE_NAME_CONTENT, content);
            }
        }
    }

    public String load(File dir, String fileName) throws IOException {
        final File f = new File(dir, fileName);
        if (f.exists()) {
            final BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), CharacterSet.UTF8));
            try {
                final StringBuilder sb = new StringBuilder();
                for (int c = r.read(); c >= 0; c = r.read()) {
                    sb.append((char) c);
                }
                return sb.toString();
            } finally {
                IOUtils.closeQuietly(r);
            }
        } else {
            return null;
        }
    }

    public Item loadIfRequired(StorageProvider storageProvider) throws IOException {
        if (storageProvider != null && ((hasSummary && isEmpty(summary)) || (hasContent && isEmpty(content)))) {
            final File dir = getDirectoryWithFallback(storageProvider);
            if (hasSummary && isEmpty(summary)) {
                Log.d(LOG_TAG, "Loading item summary from " + dir.getAbsolutePath());
                summary = load(dir, FILE_NAME_SUMMARY);
            }
            if (hasContent && isEmpty(content)) {
                Log.d(LOG_TAG, "Loading item content from " + dir.getAbsolutePath());
                content = load(dir, FILE_NAME_CONTENT);
            }
        }
        return this;
    }

    /*
     * helpers
     */
    
    private static boolean isEmpty(String string) {
        return string == null || string.length() == 0;
    }
}
