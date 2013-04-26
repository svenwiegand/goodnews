package com.gettingmobile.goodnews.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;



public class TagSelectionActivity extends Activity {
	private static TagSelectionDialogBuilder dlgBuilder = null;
	private static OnDismissListener dismissListener = null;
	
	public static void start(TagSelectionDialogBuilder dlgBuilder, Activity activity) {
		TagSelectionActivity.dlgBuilder = dlgBuilder;
		TagSelectionActivity.dismissListener = dismissListener;
		final Intent intent = new Intent(activity, TagSelectionActivity.class);
		activity.startActivity(intent);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		dlgBuilder.showDialog(this).setOnDismissListener(new DlgDismissListener());
	}
	
	/*
	 * inner classes
	 */
	
	class DlgDismissListener implements OnDismissListener {
		@Override
		public void onDismiss(DialogInterface dialog) {
			finish();
		}
	}
}
