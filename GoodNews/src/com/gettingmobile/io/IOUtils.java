/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.gettingmobile.io;

import android.util.Log;

import java.io.*;

/**
 * @author sven.wiegand
 */
public class IOUtils {
    private static final String LOG_TAG = "goodnews.IOUtils";

    public static void closeQuietly(Closeable o) {
        if (o != null) {
            try {
                o.close();
            } catch (IOException ex) {
                // ignore
            } catch (NullPointerException ex) {
                /*
                 * see issue #204: avoid NullPointerException at
                 * org.apache.harmony.luni.internal.net.www.protocol.http.HttpURLConnectionImpl$ChunkedInputStream.readChunkSize(HttpURLConnectionImpl.java:405)
                 */
            }
        }
    }

    public static void delete(File file) throws IOException {
        if (file.exists() && !file.delete())
            throw new IOException("Failed to delete file " + file);
    }

    public static void deleteIgnore(File file) {
        file.delete();
    }

    public static void deleteRecursive(File file) throws IOException {
        if (file.isDirectory()) {
            final File[] files = file.listFiles(new SimpleFileFilter(true, true, true));
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteRecursive(f);
                    } else {
                        deleteIgnore(f);
                    }
                }
            }
        }
        delete(file);
    }

    public static void copy(File src, File dest) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new BufferedInputStream(new FileInputStream(src));
            out = new BufferedOutputStream(new FileOutputStream(dest));

            for (int b = in.read(); b > -1; b = in.read()) {
                out.write(b);
            }
        } finally {
            closeQuietly(in);
            closeQuietly(out);
        }
    }

    public static void move(File src, File dest) throws IOException {
        try {
            copy(src, dest);
            if (!src.delete())
                throw new IOException("Failed to delete " + src.getAbsolutePath());
        } catch (IOException ex) {
            delete(dest);
        }
    }

    public static void touch(File file) throws IOException {
        if (!file.exists())
            throw new FileNotFoundException("File does not exist: " + file.getAbsolutePath());

        if (!file.setLastModified(System.currentTimeMillis()))
            throw new IOException("Failed to touch file: " + file.getAbsolutePath());
    }

    public static void touchIgnore(File file) {
        try {
            touch(file);
        } catch (IOException ex) {
            Log.w(LOG_TAG, "touch failed", ex);
        }
    }

    public static void ensureDirExists(File dir) throws IOException {
        if (!dir.exists()) {
            if (!dir.mkdirs())
                throw new IOException("Failed to create directory " + dir.getAbsolutePath());
        }
    }
}
