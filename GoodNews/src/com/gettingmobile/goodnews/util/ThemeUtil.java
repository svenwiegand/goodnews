package com.gettingmobile.goodnews.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.View;
import com.gettingmobile.goodnews.Application;

public class ThemeUtil {
    private final Application app;

    public ThemeUtil(Application app) {
        this.app = app;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    public int getThemeResource(View view, int attr) {
        return getThemeResource(view.getContext(), attr);
    }

    public int getThemeResource(Context context, int attr) {
        final TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        final int resId = ta.getResourceId(0, 0);
        if (resId == 0)
            throw new Resources.NotFoundException();

        return resId;
    }

    public Drawable getThemeDrawable(View view, int attr) {
        return getThemeDrawable(view.getContext(), attr);
    }

    public Drawable getThemeDrawable(Context context, int attr) {
        return app.getResources().getDrawable(getThemeResource(context, attr));
    }

    public int getThemeColor(Context context, int attr) {
        return app.getResources().getColor(getThemeResource(context, attr));
    }

    public String getThemeColorWebString(Context context, int attr) {
        final int color = getThemeColor(context, attr);
        final StringBuilder colorString = new StringBuilder(Integer.toHexString(color & 0x00FFFFFF));
        while (colorString.length() < 6) {
            colorString.insert(0, '0');
        }
        colorString.insert(0, '#');
        return colorString.toString();
    }


    /*
      * helpers
      */

    protected Drawable getDrawable(int resId) {
        return app.getResources().getDrawable(resId);
    }
}
