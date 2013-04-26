/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.gettingmobile.rest;

import android.util.Log;
import com.gettingmobile.ApplicationException;
import com.gettingmobile.rest.entity.StringExtractor;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 *
 * @author sven.wiegand
 */
public final class RequestProcessor {
    private final String LOG_TAG = "goodnews.RequestProcessor";
    private static final int KEEP_ALIVE_SECONDS = 60;
    private static final ConnectionKeepAliveStrategy keepAliveStrategy =
            new StaticConnectionKeepAliveStrategy(KEEP_ALIVE_SECONDS);
    private final DefaultHttpClient httpClient;
    private final StringExtractor stringExtractor = new StringExtractor();

    public RequestProcessor() {
        httpClient = new DefaultHttpClient();
        httpClient.setKeepAliveStrategy(keepAliveStrategy);
        
        /*
         * the user agent containing the string "gzip" is required to make google reader return gzip!
         */
        httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "GoodNews for Android (supports gzip)");

        /*
         * configure proxy if set
         */
        final ProxyConfiguration proxyConfig = new ProxyConfiguration();
        if (proxyConfig.hasProxy()) {
            //httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, new HttpHost("10.0.2.2", 8008));
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
                    new HttpHost(proxyConfig.getHost(), proxyConfig.getPort()));
            Log.i(LOG_TAG, "Using proxy " + proxyConfig);
        }
    }

	public void throwExceptionIfApplicable(Request<?> request, HttpResponse response) throws ApplicationException, HttpStatusException {
		final int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode < 300) {
            Log.d(LOG_TAG, "Request succeeded with status code " + statusCode);
			return;
        }
		
		final StringBuilder detailsBuilder = 
			new StringBuilder("Request failed with status code " + statusCode + ". Message: ");
		try {
			detailsBuilder.append(stringExtractor.extract(response.getEntity()));
		} catch (ContentIOException e) {
			detailsBuilder.append("none");
			Log.w(LOG_TAG, "Request failed with status code " + statusCode + " without content");
		}
		detailsBuilder.append("\nRequest: ").append(request.toString());
		final String details = detailsBuilder.toString();
		
		request.throwExceptionIfApplicable(response);
		if (statusCode >= 500) {
			switch (statusCode) {
			case HttpStatus.SC_INTERNAL_SERVER_ERROR:
				throw new ServerErrorException(ServerErrorException.ErrorCode.INTERNAL_SERVER_ERROR, details);
			case HttpStatus.SC_NOT_IMPLEMENTED:
				throw new ServerErrorException(ServerErrorException.ErrorCode.NOT_IMPLEMENTED, details);
			case HttpStatus.SC_SERVICE_UNAVAILABLE:
				throw new ServerErrorException(ServerErrorException.ErrorCode.SERVICE_UNAVAILABLE, details);
			default:
				throw new ServerErrorException(ServerErrorException.ErrorCode.GENERIC_SERVER_ERROR, details);
			}
		} else if (statusCode >= 400) {
			switch (statusCode) {
			case HttpStatus.SC_UNAUTHORIZED:
				throw new BadRequestException(BadRequestException.ErrorCode.UNAUTHORIZED, details);
			case HttpStatus.SC_PAYMENT_REQUIRED:
				throw new BadRequestException(BadRequestException.ErrorCode.PAYMENT_REQUIRED, details);
			case HttpStatus.SC_FORBIDDEN:
				throw new BadRequestException(BadRequestException.ErrorCode.FORBIDDEN, details);
			case HttpStatus.SC_NOT_FOUND:
				throw new BadRequestException(BadRequestException.ErrorCode.NOT_FOUND, details);
			case HttpStatus.SC_GONE:
				throw new BadRequestException(BadRequestException.ErrorCode.GONE, details);
			default:
				throw new BadRequestException(BadRequestException.ErrorCode.GENERIC_REQUEST_ERROR, details);
			}
		} else if (statusCode >= 300) {
			throw new RedirectionException(RedirectionException.ErrorCode.REDIRECTION, details);
		}
	}    

    public <T> T requestResult(Request<T> request) throws ApplicationException, IOException {
        Log.d(LOG_TAG, "Sending request of type " + request.getClass().getSimpleName() + ": " + request.toString());
        final HttpResponse response = httpClient.execute(request.getRequest());
        throwExceptionIfApplicable(request, response);
        return request.processResponse(response);
    }

    /*
     * inner classes
     */
    static class StaticConnectionKeepAliveStrategy implements ConnectionKeepAliveStrategy {
        private final long keepAliveMillis;

        public StaticConnectionKeepAliveStrategy(int seconds) {
            keepAliveMillis = 1000 * seconds;
        }

        @Override
        public long getKeepAliveDuration(HttpResponse httpResponse, HttpContext httpContext) {
            return keepAliveMillis;
        }
    }
}
