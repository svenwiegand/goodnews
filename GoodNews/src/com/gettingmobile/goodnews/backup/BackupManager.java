package com.gettingmobile.goodnews.backup;

import com.gettingmobile.android.util.ApiLevel;
import com.gettingmobile.goodnews.Application;

public class BackupManager {
    public static void init(Application app) {
        if (ApiLevel.isAtLeast(8))
            new StandardBackupManager(app);
    }
}
