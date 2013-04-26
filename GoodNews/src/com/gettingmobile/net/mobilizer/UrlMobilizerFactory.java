package com.gettingmobile.net.mobilizer;

import java.util.HashMap;
import java.util.Map;

public class UrlMobilizerFactory {
    private final Map<MobilizerImplementation, UrlMobilizer> mobilizers = new HashMap<MobilizerImplementation, UrlMobilizer>();

    protected UrlMobilizer createMobilizer(MobilizerImplementation implementation) {
        switch (implementation) {
            case GOOGLE:
                return new GoogleUrlMobilizer();
            case READABILITY:
                return new ReadabilityUrlMobilizer();
            default:
                return new NullUrlMobilizer();
        }
    }

    public UrlMobilizer getMobilizer(MobilizerImplementation implementation) {
        UrlMobilizer mobilizer = mobilizers.get(implementation);
        if (mobilizer == null) {
            mobilizer = createMobilizer(implementation);
            mobilizers.put(implementation, mobilizer);
        }
        return mobilizer;
    }
}
