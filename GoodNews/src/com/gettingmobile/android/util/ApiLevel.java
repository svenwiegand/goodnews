package com.gettingmobile.android.util;

import android.os.Build;
import android.util.Log;

public class ApiLevel {
    public static final int V_4_0 = 14;
    public static final int V_3_0 = 11;
    public static final int V_2_3 = 9;
	public static final int V_2_2 = 8;
	public static final int V_2_1 = 7;
	public static final int V_2_0_1 = 6;
	public static final int V_2_0 = 5;
	public static final int V_1_6 = 4;
	public static final int V_1_5 = 3;
	
	public static int getApiLevel() {
		Log.d(ApiLevel.class.getSimpleName(), "API Level is '" + Build.VERSION.SDK_INT + "'");
		return Build.VERSION.SDK_INT;
	}
	
	public static boolean isAtLeast(int apiLevel) {
		return getApiLevel() >= apiLevel;
	}

    public static boolean isAtMost(int apiLevel) {
        return getApiLevel() <= apiLevel;
    }

    public static boolean isBelow(int apiLevel) {
        return getApiLevel() < apiLevel;
    }
}
