package com.gettingmobile.android.app.actions;

public class MarketDetailsAction extends MarketAction {
    public static final String DETAILS_URL = BASE_URL + "details?id=";

    public MarketDetailsAction(String packageName) {
        super(DETAILS_URL + packageName);
    }
}
