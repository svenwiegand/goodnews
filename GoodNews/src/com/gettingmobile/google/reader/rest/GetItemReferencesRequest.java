package com.gettingmobile.google.reader.rest;

import com.gettingmobile.google.Authenticator;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.ItemState;
import com.gettingmobile.rest.ContentIOException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;

public final class GetItemReferencesRequest extends AuthenticatedReaderRequest<HttpGet, ItemReferenceStream> {
	private final static ItemReferenceStreamExtractor itemReferenceStreamExtractor = new ItemReferenceStreamExtractor();

    private static String createUri(Collection<ElementId> tags, ItemState exclude, int maxCountPerTag, long startTime,
                                    boolean includeAllDirectStreamIds) {
        final int maxLength = MAX_URI_LENGTH - BASE_URI.length();
        final StringBuilder uri = new StringBuilder("/api/0/stream/items/ids?output=json");
        uri.append("&n=").append(maxCountPerTag);
        if (startTime > 0) {
            uri.append("&ot=").append(startTime);
        }
        if (exclude != null) {
            uri.append("&xt=").append(exclude.getId().getId());
        }
        if (includeAllDirectStreamIds) {
            uri.append("&includeAllDirectStreamIds=true");
        }
        for (Iterator<ElementId> it = tags.iterator(); it.hasNext(); ) {
            final ElementId tagId = it.next();
            if ((uri.length() + tagId.getId().length() + 3) < maxLength) {
                uri.append("&s=").append(tagId.getUrlEncodedId());
                it.remove();
            } else {
                break;
            }
        }
        return uri.toString();
    }

    /**
     * Constructs the request
     * @param authenticator the authenticator to be used
     * @param tags the tags to be requested references for. As many tags as allowed for a reasonable URI length will
     * be taken and deleted from this collection. So the tags remaining in the collection after the call have not
     * been requested!
     * @param exclude {@link ItemState} of items to be excluded from the list or {@code null} if no excludes are requested.
     * @param maxCountPerTag maximum number of item references to be returned per tag.
     * @param startTime unix timestamp in seconds specifying the minimum change time of the items to be returned.
     * @param includeAllDirectStreamIds http://groups.google.com/group/fougrapi/browse_thread/thread/96bc41e878d2a0c/ab11fd7f9858462a
     * @throws URISyntaxException if the URI is malformed
     */
	public GetItemReferencesRequest(
            Authenticator authenticator, Collection<ElementId> tags, ItemState exclude,
            int maxCountPerTag, long startTime, boolean includeAllDirectStreamIds)
		throws URISyntaxException {
		super(createUri(tags, exclude, maxCountPerTag, startTime, includeAllDirectStreamIds), authenticator);
	}

    public GetItemReferencesRequest(Authenticator authenticator, Collection<ElementId> tags, int maxCountPerTag)
            throws URISyntaxException {
        this(authenticator, tags, null, maxCountPerTag, 0, false);
    }

	@Override
	public ItemReferenceStream processResponse(HttpResponse response) throws ContentIOException {
		return itemReferenceStreamExtractor.extract(response.getEntity());
	}

	@Override
	protected HttpGet createRequest() {
		return new HttpGet();
	}

}
