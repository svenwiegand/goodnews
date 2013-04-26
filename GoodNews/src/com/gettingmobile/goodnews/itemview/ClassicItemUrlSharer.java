package com.gettingmobile.goodnews.itemview;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.util.Log;
import com.gettingmobile.goodnews.R;

public final class ClassicItemUrlSharer extends ItemUrlSharer {
    public ClassicItemUrlSharer(Activity activity) {
        super(activity);
    }

    @Override
    public boolean handleAction() {
        try {
            activity.startActivity(
                    Intent.createChooser(intent, activity.getString(R.string.share_item_target_title)));
        } catch (ActivityNotFoundException ex) {
            Log.e(LOG_TAG, "Failed to send URL", ex);
        }
        return true;
    }
}
