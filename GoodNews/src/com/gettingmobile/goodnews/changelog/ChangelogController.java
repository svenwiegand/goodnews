package com.gettingmobile.goodnews.changelog;

import com.gettingmobile.goodnews.Activity;
import com.gettingmobile.goodnews.Application;

import java.util.Calendar;

public final class ChangelogController {
    public static boolean automaticallyShowChangelogIfApplicable(Application app, Activity activity) {
        final int currentVersionCode = app.getSettings().getCurrentVersionCode();
        final int versionCode = app.getPackageInfo().versionCode;
        if (versionCode > currentVersionCode) {
            boolean showChangelog = app.getSettings().shouldShowChangelogAutomatically();
            if (showChangelog && currentVersionCode == 0) {
                /*
                 * we are started for the first time with the version code settings, so lets determine whether it is
                 * a new installation (in which case we do not want to show the changelog) or an old installation.
                 */
                final Calendar threshold = Calendar.getInstance();
                threshold.add(Calendar.MINUTE, -10);
                showChangelog = app.getSettings().getInstallTime().before(threshold);
            }

            app.getSettings().setPreviousVersionCode(currentVersionCode);
            app.getSettings().setCurrentVersionCode(versionCode);
            if (showChangelog) {
                ChangelogDialogHandler.start(activity);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
