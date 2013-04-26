package com.gettingmobile.android.app.actions;

import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import com.gettingmobile.android.app.DialogFactory;
import com.gettingmobile.goodnews.R;

public class MarketAction extends AbstractAction<Application> {
    public static final String BASE_URL = "market://";
    protected final String url;

    public static boolean isMarketUrl(String url) {
        return url != null && url.startsWith(BASE_URL);
    }
    
    public MarketAction(String url) {
        this.url = url;
    }

    @Override
    public boolean onFired(ActionContext<? extends Application> context) {
        showMarket(context);
        return true;
    }

    protected void showMarket(ActionContext<? extends Application> context) {
        try {
            context.getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException ex) {
            handleMarketNotAvailable(context);
        }
    }
    
    protected void handleMarketNotAvailable(ActionContext<? extends Application> context) {
        DialogFactory.showErrorDialog(context.getActivity(),
                R.string.market_not_found, context.getActivity().getString(R.string.market_not_found_msg));
    }
}
