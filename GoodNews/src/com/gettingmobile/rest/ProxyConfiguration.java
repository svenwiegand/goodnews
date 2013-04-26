package com.gettingmobile.rest;

public class ProxyConfiguration {
    public static final String PROPERTY_HOST = "http.proxyHost";
    public static final String PROPERTY_PORT = "http.proxyPort";

    private final String host;
    private final int port;

    public ProxyConfiguration() {
        host = System.getProperty(PROPERTY_HOST);
        port = Integer.getInteger(PROPERTY_PORT, 0);
    }

    public boolean hasProxy() {
        return host != null;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }
}
