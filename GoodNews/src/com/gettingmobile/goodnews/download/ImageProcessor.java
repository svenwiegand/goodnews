package com.gettingmobile.goodnews.download;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;
import com.gettingmobile.io.IOUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ImageProcessor {
    private static final String LOG_TAG = "goodnews.ImageProcessor";
	public static final int HTTPTIMEOUT = 5000;
    private static final Pattern PATTERN_IMG = Pattern.compile("(?i)<img [^>]*>");
   	private static final Pattern PATTERN_SRC = Pattern.compile("(?i)(?<=src=\")[^\"]*(?=\")");
   	private static final Pattern PATTERN_ATTR = Pattern.compile("(?i)(?<=( ))[a-z]*?=\"[^\"]*\"");
   	private static final Pattern PATTERN_A_TAG = Pattern.compile("(?i)<[/]?a[ >]");
    private final File itemDir;
    private final String prefix;
    private int imageCounter = 0;
	private final URL baseUrl;
	private final URL pathUrl;
	private String html;
    private final int displaySmallSize;
	private final int displayLargeSize;
    private final boolean downscale;
	
	/** 
	 * For testing purposes only, see ImageProcessor(URL pageUrl, StringBuilder html, int displayLargeSize)
     * @param itemDir directory to store images in
     * @param prefix name prefix for image files
     * @param pageUrl The URL of the original page
   	 * @param html The html document of the original page
     * @param displaySmallSize the size of the shorter edge of the display in pixels.
   	 * @param displayLargeSize the size of the longer edge of the display in pixels.
     * @param downscale whether to scale the image down to the display or not. 
	 */
	public ImageProcessor(File itemDir, String prefix, String pageUrl, String html,
                          int displaySmallSize, int displayLargeSize, boolean downscale) {
        this.itemDir = itemDir;
        this.prefix = prefix;
		this.baseUrl = getBaseUrl(pageUrl, false);
		this.pathUrl = getBaseUrl(pageUrl, true);
		this.html = html;
        this.displaySmallSize = displaySmallSize;
		this.displayLargeSize = displayLargeSize;
        this.downscale = downscale;
	}
	
	/**
	 * Get the base URL of the given base URL
	 * @param pageUrl URL of the web page
	 * @param withPath if the path should be included, or just the server name i.e. http://www.dom.com/path/ or just http://www.dom.com/
	 * @return The url of the path of the webpage, minus the file name, ending with /
	 */
	public static URL getBaseUrl(String pageUrl, boolean withPath) {
        try {
            if (pageUrl == null)
                return null;
            
            final URL url = new URL(pageUrl);
            final int i = url.getPath().lastIndexOf("/");
            final String path = (i > -1 ? url.getPath().substring(0, i) : "");
            if (!withPath) {
                return new URL(url.getProtocol()+"://"+url.getAuthority() + "/");
            } else {
                return new URL(url.getProtocol()+"://"+url.getAuthority() + path + "/");
            }
        } catch (MalformedURLException ex) {
            Log.w(LOG_TAG, "malformed url " + pageUrl, ex);
            return null;
        }
	}

	/**
	 * Method that does the actual work, finds all img tags using regex,
	 * all html outside of img tags is just appended to the output StringBuilder.
     * @throws java.io.IOException if it went wrong
     */
	public void processImages() throws IOException {
        try {
            int lastTagEnd = 0; //Last end of img tag regex match, initially start of string
            boolean lastFragmentEndedWithinLink = false;
            final StringBuilder htmlOut = new StringBuilder(); //Somewhere to put our new html doc
            final Matcher matcher = PATTERN_IMG.matcher(html);
            while(matcher.find()) {
                // Append all non-img tag content since last match
                final String htmlSinceLastImg = html.substring(lastTagEnd, matcher.start());
                htmlOut.append(htmlSinceLastImg);
                lastTagEnd = matcher.end(); //set the last tag end to end of current match
                String imgTag = matcher.group(0); // the <img....> tag
                lastFragmentEndedWithinLink = endsWithinLinkTag(htmlSinceLastImg, lastFragmentEndedWithinLink);
                imgTag = processImage(imgTag, !lastFragmentEndedWithinLink);
                htmlOut.append(imgTag);
            }
            htmlOut.append(html.substring(lastTagEnd)); // finally append all non-img data since the last match
            html = htmlOut.toString();
        } catch (OutOfMemoryError ex) {
            // not much we can do here but forwarding the error to be gracefully handled
            throw new IOException("Failed to download images because I'm out of memory");
        }
	}
	
	/**
     * Process a single image tag.
	 * @param imgTag The original img tag to create replacement for with inline/embedded image
	 * @param createLinkToOrigImg Whether or not to wrap img tag in link to the original image
	 * @return a string for the new img tag, i.e. <img src="data:image/png;base64,asdasdlkjlasjd..." alt="other attributes are preserved"/>
     * @throws java.io.IOException if it went wrong
	 */
	private String processImage(String imgTag, boolean createLinkToOrigImg) throws IOException {
		String imgOut = imgTag;
		final Matcher matcher = PATTERN_SRC.matcher(imgTag);
		if(matcher.find()) {
            final String sourceRel = matcher.group(0);
			try {
                final URL imageUrl;
				if(sourceRel.contains("://")) {
                    imageUrl = new URL(sourceRel); //img src on different site
                } else if(sourceRel.startsWith("/")) {
                    imageUrl = (baseUrl != null) ? new URL(baseUrl, sourceRel) : null;
                } else {
                    imageUrl = (pathUrl != null) ? new URL(pathUrl, sourceRel) : null; //img src relative to page url.
                }
                if (imageUrl != null) {
                    // Get the resized image and encode into Base64
                    final ImageInfo imageInfo = fetchAndResizeImage(imageUrl, downscale ? displayLargeSize : 0);
                    if(imageInfo != null) {
                        // Create the new tag with the image data, and applicable attributes from original img tag
                        final StringBuilder newTag = new StringBuilder();
                        newTag.append("<img");

                        /*
                         * only include image dimensions (width and height attribute) if they are remarkable smaller
                         * than the display, so that we can do CSS downscaling when displaying the item in all other cases.
                         * (we are deviding the size by two to ensure that the image is really not getting to large, if the
                         * item view contains borders or stuff like that).
                         */
                        final int maxExplicitDimensionSize = displaySmallSize / 2;
                        if (imageInfo.width < maxExplicitDimensionSize && imageInfo.height < maxExplicitDimensionSize) {
                            newTag.append(" width=\"").append(imageInfo.width).
                                    append("\" height=\"").append(imageInfo.height).append('"');
                        }
                        final String imageSrc = imageInfo.fileName;
                        Log.i(LOG_TAG, "Setting image source to " + imageSrc);
                        newTag.append(" src=\"").append(imageSrc).append('"');

                        /*
                         * append all other image attributes to the new tag
                         */
                        final Matcher matcher2 = PATTERN_ATTR.matcher(imgTag);
                        while(matcher2.find()) {
                            final String a = matcher2.group(0).toLowerCase();
                            if(!a.startsWith("src") && !a.startsWith("height") && !a.startsWith("width")) {
                                newTag.append(" ").append(matcher2.group(0));
                            }
                        }
                        newTag.append("/>");
                        Log.d(LOG_TAG, "dimensions of new image: width="+ imageInfo.width+", height="+ imageInfo.height);

                        if(createLinkToOrigImg) {
                            newTag.insert(0, "<a href=\"" + imageUrl.toExternalForm() + "\">").append("</a>");
                        }

                        imgOut = newTag.toString();
                    }
                }
			} catch(MalformedURLException ex) {
				Log.w(LOG_TAG, "ignoring image with invalid path " + sourceRel);
			} 
		}
		return imgOut;
	}

	/** 
     * Fetches the image in the URL, resizes it to the max size set in maxPixelSize in constructor,
	 * saves it to the base directory and returns the relative path name.
	 * @param imageUrl the URL of the image to fetch.
     * @param maxPixelSize The maximum size, width or height, of the page, or 0 to return unresized image
     * @return the path of the image relative to the base dir.
     * @throws java.io.IOException if an error occured fetching the image.
	 */
	public ImageInfo fetchAndResizeImage(URL imageUrl, int maxPixelSize) throws IOException {
		Log.i(LOG_TAG, "Opening imageUrl: " + imageUrl.toExternalForm());
        final String fileName = prefix + Integer.toString(imageCounter++);
        
		ImageInfo imageInfo = null;
        
		// set up the HttpURLConnection
		final URLConnection connection = imageUrl.openConnection();
        if (connection instanceof HttpURLConnection) {
            final HttpURLConnection conn = (HttpURLConnection) connection;
            try {
                conn.setRequestMethod("GET");
                conn.setReadTimeout(HTTPTIMEOUT);
                conn.setConnectTimeout(HTTPTIMEOUT);
                conn.setDoInput(true);
                conn.connect();
                // Check if server responds ok 200
                // may be we should follow 301/302 redirects here in the future?
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    final ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                    final InputStream is = conn.getInputStream();
                    try {
                        byte[] buf = new byte[256]; int i;
                        while((i=is.read(buf))>-1) {
                            baos1.write(buf, 0, i);
                        }
                    } finally {
                        IOUtils.closeQuietly(is);
                    }
                    final byte[] origImgBytes = baos1.toByteArray();
                    Log.d(LOG_TAG, "Size of original image: " + origImgBytes.length);
    
                    if(maxPixelSize <=0 ) {
                        // Resize disabled, just get the image dimensions and return raw array in Image
                        writeImage(fileName, origImgBytes);
                        imageInfo = new ImageInfo(fileName, origImgBytes);
                    } else try {
                        // A maximum size is set, might have to resize if original size exceeds max
                        // We'll assume so and just load the entire image, converting it to PNG in the process
                        final Bitmap origImg = BitmapFactory.decodeByteArray(origImgBytes, 0, origImgBytes.length);
                        if(origImg == null) {
                            Log.w(LOG_TAG, "Could not create image from connection inputstream");
                        } else {
                            final int origWidth = origImg.getWidth();
                            final int origHeight = origImg.getHeight();
    
                            // Is resize needed?
                            final boolean resize = origWidth > maxPixelSize || origHeight  >maxPixelSize;
                            if (!resize) {
                                writeImage(fileName, origImgBytes);
                                imageInfo = new ImageInfo(fileName, origImgBytes);
                            } else {
                                // Image not within maxSize
                                // Find ratios for height and width
                                final float ratioW = ((float)maxPixelSize)/origWidth;
                                final float ratioH = ((float)maxPixelSize)/origHeight;
                                // Both sides must be < displayLargeSize, so get the smallest of the ratios
                                final float ratio = (ratioW<ratioH ? ratioW : ratioH);
    
                                // Calculate the new widths
                                imageInfo = new ImageInfo(
                                        fileName, Math.round(ratio * origWidth), Math.round(ratio * origHeight), resize);
    
                                // Resize the image and mark the old for garbage collection
                                Bitmap retImg = Bitmap.createScaledBitmap(origImg, imageInfo.width, imageInfo.height, true);
                                origImg.recycle();
    
                                // Save the final image to byte array in PNG format, then mark image for garbage collection
                                final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
                                retImg.compress(CompressFormat.PNG, 50, baos); // PNG format ignores image quality param
                                retImg.recycle();
                                final byte[] newImgBytes = baos.toByteArray();
                                Log.i(LOG_TAG, "Size of resized image: " + newImgBytes.length);
                                writeImage(fileName, newImgBytes);
                            }
                        }
                    } catch (OutOfMemoryError error) {
                        Log.w(LOG_TAG, "Encountered out of memory while processing image " + imageUrl);
                        imageInfo = new ImageInfo(fileName, origImgBytes);
                    }
                } else {
                    /*
                     * non-success return code
                     */
                    Log.w(LOG_TAG, "Failed to download image with return code " + conn.getResponseCode());
                }
            } catch (NullPointerException ex) {
                // HttpURLConnection.connect() throws this in some strange situations :-(
                throw new IOException("NullPointerException while trying to connect");
            } catch (IndexOutOfBoundsException ex) {
                // HttpURLConnection.connect() throws this in some strange situations :-(
                throw new IOException("IndexOutOfBoundsException while trying to connect");
            } finally {
                conn.disconnect();
            }
        }
		return imageInfo;
	}

    private void writeImage(String fileName, byte[] image) throws IOException {
        IOUtils.ensureDirExists(itemDir);

        final File file = new File(itemDir, fileName);
        OutputStream out = null;
        try {
            Log.i(LOG_TAG, "Writing image of " + image.length + " bytes to " + file);
            out = new FileOutputStream(file);
            out.write(image);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }
	
	/**
	 * Tests if the html fragment given ends within a link tag &lt;a&gt;
	 * @param html The fragment of html to test
	 * @param previousFragmentEndedInLink ???
     * @return If the fragment ends within an &lt;a&gt;&lt;/a&gt; or not
	 */
	public static boolean endsWithinLinkTag(String html, boolean previousFragmentEndedInLink) {
		boolean inLinkTag = previousFragmentEndedInLink;
		Matcher matcher = ImageProcessor.PATTERN_A_TAG.matcher(html);
		while(matcher.find()) {
			String tag = matcher.group(0).toLowerCase();
			if(tag.startsWith("</a")) {
                inLinkTag = false;
            } else if(tag.startsWith("<a")) {
                inLinkTag = true;
            }
		}
		return inLinkTag;
	}

	public String getPageWithInlineImages() throws IOException {
		processImages();
		return html;
	}
	
	/**
	 * Data transfer class for transfer of resized image with metadata
	 * @author Tor
	 *
	 */
	static class ImageInfo {
		public final String fileName;
		public final int width;
		public final int height;
		public final boolean wasResized;
		
		public ImageInfo(String fileName, int width, int height, boolean wasResized) {
            this.fileName = fileName;
			this.width = width;
			this.height = height;
			this.wasResized = wasResized;
		}
        
        public ImageInfo(String fileName, byte[] image) {
            this.fileName = fileName;

            final Options opts = new Options();
            opts.inJustDecodeBounds=true;
            BitmapFactory.decodeByteArray(image, 0, image.length, opts);
            width = opts.outWidth;
            height = opts.outHeight;
            wasResized = false;
        }
	}
}
