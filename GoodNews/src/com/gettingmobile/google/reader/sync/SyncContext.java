package com.gettingmobile.google.reader.sync;

import android.content.Context;
import com.gettingmobile.google.Authenticator;
import com.gettingmobile.google.reader.db.DatabaseHelper;

public interface SyncContext {
    Context getContext();
	SyncSettings getSettings();
	Authenticator getAuthenticator();
	DatabaseHelper getDbHelper();
}
