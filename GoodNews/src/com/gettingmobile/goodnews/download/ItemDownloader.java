package com.gettingmobile.goodnews.download;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.storage.TempFileFactory;
import com.gettingmobile.google.reader.Item;
import com.gettingmobile.google.reader.Resource;
import com.gettingmobile.google.reader.db.ItemDatabaseAdapter;
import com.gettingmobile.io.CharacterSet;
import com.gettingmobile.io.IOUtils;
import com.gettingmobile.net.mobilizer.UrlMobilizer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ItemDownloader {
    private static final String LOG_TAG = "goodnews.ItemDownloader";
    private static final Pattern CHARACTER_ENCODING_PATTERN = Pattern.compile("http-equiv=\"content-type\"\\s+content=\"text/html;\\s*charset=(\\S+)\"", Pattern.CASE_INSENSITIVE);
    protected static final ItemDownloadInfoDatabaseAdapter itemDownloadAdapter = new ItemDownloadInfoDatabaseAdapter();
    private final ItemDatabaseAdapter itemAdapter = new ItemDatabaseAdapter();
    private final Application app;
    private final Queue<Item> items;
    private final DownloadProgress progress;
    private boolean cancel = false;

    public ItemDownloader(Application app, Queue<Item> items, DownloadProgress progress) {
        this.app = app;
        this.items = items;
        this.progress = progress;
    }

    public void shutdown() {
        cancel = true;
    }

    private String readResource(String sourceUrl) throws DownloadException {
        HttpURLConnection connection = null;
        try {
            /*
             * build connection
             */
            connection = (HttpURLConnection) new URL(sourceUrl).openConnection();
            connection.connect();

            /*
             * check for content type
             */
            final String contentType = connection.getContentType();
            if (contentType != null && !contentType.toLowerCase().startsWith("text") && !contentType.toLowerCase().contains("html"))
                throw new DownloadException(DownloadException.ErrorCode.UNEXPECTED_RESOURCE_TYPE);

            /*
             * load and save content
             */
            return readResource(connection);
        } catch (IOException ex) {
            throw new DownloadException(DownloadException.ErrorCode.GENERIC, ex);
        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }

    private String readResource(HttpURLConnection connection) throws IOException {
        final String encoding = connection.getContentEncoding();
        Log.d(LOG_TAG, "Detected the following encoding from the response header: " + encoding);
        if (encoding != null) {
            /*
             * we know the encoding, so we can directly read into memory
             */
            BufferedReader input = null;
            try {
                input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                return readResource(input);
            } finally {
                IOUtils.closeQuietly(input);
            }
        } else {
            /*
             * we do not yet know the encoding, so we need to determine it. 
             * Therefore we download the resource to a local file at first, guess the encoding then and 
             * read from the local file with the correct encoding afterwards. 
             */
            final File tmpFile = TempFileFactory.create(app.getSettings().getContentStorageProvider(), "resourceDownload");
            try {
                /*
                 * download to local file
                 */
                InputStream input = null;
                OutputStream output = null;
                try {
                    input = new BufferedInputStream(connection.getInputStream());
                    output = new BufferedOutputStream(new FileOutputStream(tmpFile));
                    for (int c = input.read(); c > -1; c = input.read()) {
                        output.write(c);
                    }
                } finally {
                    IOUtils.closeQuietly(input);
                    IOUtils.closeQuietly(output);
                }
                
                /*
                 * guess encoding from file and read it
                 */
                BufferedReader fileInput = null;
                try {
                    fileInput = new BufferedReader(
                            new InputStreamReader(new FileInputStream(tmpFile), guessFileEncoding(tmpFile)));
                    return readResource(fileInput);
                } finally {
                    IOUtils.closeQuietly(fileInput);
                }
            } finally {
                IOUtils.deleteIgnore(tmpFile);
            }
        }
    }
    
    private String readResource(BufferedReader reader) throws IOException {
        try {
            final StringBuilder resource = new StringBuilder();
            for (int c = reader.read(); c > -1; c = reader.read()) {
                resource.append((char) c);
            }
            return resource.toString();
        } catch (OutOfMemoryError error) {
            Log.e(LOG_TAG, "out of memory!", error);
            throw new IOException("out of memory");
        }
    }
    
    private String guessFileEncoding(File file) throws IOException {
        /*
         * we need to determine the encoding. Lets assume it's a HTML file,
         * then lets look for the character encoding pattern.
         *
         * Though this muss not be true we assume the character encoding to be defined on a single line to safe
         * memory.
         *
         * Further on we are not checking whether the encoding header is enclosed in a comment -- we simply take
         * the first one.
         */
        final BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                final Matcher m = CHARACTER_ENCODING_PATTERN.matcher(line);
                if (m.find()) {
                    final String encoding = m.group(1);
                    Log.d(LOG_TAG, "Determined encoding from file content: " + encoding);
                    return encoding;
                }
            }
        } finally {
            IOUtils.closeQuietly(reader);
        }
        
        /*
         * didn't find the encoding, so lets assume UTF-8
         */
        Log.d(LOG_TAG, "Failed to determine encoding from file content. Assuming UTF-8");
        return CharacterSet.UTF8;
    }        

    protected void downloadItem(Item item) {
        /*
         * fetch the full item from the database 
         */
        final SQLiteDatabase db = app.getDbHelper().getDatabase();
        item = itemAdapter.readFullByKey(db, item.getKey());
        if (item == null) {
            return;
        }
        try {
            item.loadIfRequired(app.getSettings().getContentStorageProvider());
        } catch (IOException ex) {
            Log.e(LOG_TAG, "Failed to load item", ex);
        }

        /*
         * determine whether to download content
         */
        final OfflineContentType offlineContentType = app.getSettings().getOfflineContentType(item.getFeedId());
        final Resource alternate = item.getAlternate();
        final boolean hasValidAlternate = alternate != null && alternate.getHref() != null && alternate.getHref().length() > 0;
        final boolean hasValidContentAlternate = hasValidAlternate;
        if (offlineContentType.wantsText() && !item.hasContent() && hasValidContentAlternate) {
            /*
             * download item content
             */
            final boolean scaleImages = app.getSettings().scaleImages(item.getFeedId());
            final UrlMobilizer mobilizer = app.getSettings().getUrlMobilizer(item.getFeedId());
            final String url = mobilizer.mobilize(item.getAlternate().getHref(), scaleImages);
            Log.i(LOG_TAG, "Looking at item " + item + " with alternate " + url);
    
            /*
             * download the item
             */
            try {
                item.setContent(readResource(url));
                item.setIsExternalContent(true);
    
                db.beginTransaction();
                try {
                    itemDownloadAdapter.writeContent(db, item, app.getSettings().storeContentInFiles());
                    item.saveIfApplicable(app.getSettings().storeContentInFiles(), app.getSettings().getContentStorageProvider());
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                Log.i(LOG_TAG, "Successfully downloaded item " + item + " with alternate " + item.getAlternate().getHref());
            } catch (Exception ex) {
                Log.e(LOG_TAG,
                        "Failed to download item " + item + " from " + url, ex);
            }
        }
        
        /*
         * determine whether to download images
         */
        if (offlineContentType.wantsImages() && !item.hasImages() && (item.hasSummary() || item.hasContent())) {
            Log.i(LOG_TAG, "Looking at images for item " + item + " with pageUrl ");

            /*
             * build page url
             */
            final String pageUrl = hasValidAlternate ? alternate.getHref() : null;

            try {
                final File itemDir = item.getDirectory(app.getSettings().getContentStorageProvider());
                final boolean scaleImages = app.getSettings().scaleImages(item.getFeedId());
                final int displaySmallSize = Math.min(app.getResources().getDisplayMetrics().widthPixels,
                        app.getResources().getDisplayMetrics().heightPixels);
                final int displayLargeSize = Math.max(app.getResources().getDisplayMetrics().widthPixels,
                        app.getResources().getDisplayMetrics().heightPixels);

                /*
                 * inline images in summary if applicable
                 */
                if (item.hasSummary() && item.getSummary() != null) {
                    item.setSummary(processImages(itemDir, "s", pageUrl, item.getSummary(),
                            displaySmallSize, displayLargeSize, scaleImages));
                }
                
                /*
                 * inline images in content if applicable
                 */
                if (item.hasContent() && item.getContent() != null) {
                    item.setContent(processImages(itemDir, "c", pageUrl, item.getContent(),
                            displaySmallSize, displayLargeSize, scaleImages));
                }
                item.setHasImages(true);

                /*
                 * persist result
                 */
                db.beginTransaction();
                try {
                    itemDownloadAdapter.writeContent(db, item, app.getSettings().storeContentInFiles());
                    item.saveIfApplicable(app.getSettings().storeContentInFiles(), app.getSettings().getContentStorageProvider());
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                Log.i(LOG_TAG, "Successfully downloaded images for " + item);
            } catch (IOException ex) {
                Log.e(LOG_TAG, "Failed to download images", ex);
            }
        }
        progress.increase();
    }
    
    private String processImages(File itemDir, String prefix, String pageUrl, String html,
                                 int displaySmallSize, int displayLargeSize, boolean downscale) throws IOException {
        return new ImageProcessor(itemDir, prefix, pageUrl, html, displaySmallSize, displayLargeSize, downscale).
                getPageWithInlineImages();
    }

    public void run() {
        while (!cancel && !items.isEmpty()) {
            final Item item = items.poll();
            if (item != null) {
                downloadItem(item);
            }
        }
    }
}
