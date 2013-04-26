package com.gettingmobile.goodnews.itemview;

import com.gettingmobile.android.util.ApiLevel;

class ItemWebViewInitializerFactory {
    public static ItemWebViewInitializer createItemWebViewInitializer() {
        if (ApiLevel.isAtLeast(11)) {
            return new ItemWebViewInitializer11();
        } else {
            return new ItemWebViewInitializer();
        }
    }
}
