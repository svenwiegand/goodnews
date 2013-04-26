package com.gettingmobile.rest.entity;

import android.util.Log;
import org.apache.http.Header;
import org.apache.http.HttpEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public abstract class AbstractEntityExtractor<T> implements EntityExtractor<T> {
    protected static boolean isGZipped(HttpEntity entity) {
        final Header h = entity.getContentEncoding();
        return h != null && h.getValue().equalsIgnoreCase("gzip");
    }

    protected static InputStream getContent(HttpEntity entity) throws IOException {
        final boolean gzipped = isGZipped(entity);
        Log.d(AbstractEntityExtractor.class.getSimpleName(),
                gzipped ? "processing gzipped content" : "processing uncompressed content");
        return gzipped ? new GZIPInputStream(entity.getContent()) : entity.getContent();
    }
}
