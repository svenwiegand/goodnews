package com.gettingmobile.goodnews;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.sqlite.SQLiteDatabase;
import android.widget.ListView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.gettingmobile.android.app.DialogFactory;

public abstract class ElementListActivity extends Activity {
	protected abstract ListView getListView();

    protected ElementListActivity() {
        super();
    }

    protected ElementListActivity(int tipGroupId) {
        super(tipGroupId);
    }

    /*
     * live cycle handler
     */

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeCursor();
    }

	@Override
	public void loadView() {
		super.loadView();
        invalidateOptionsMenu();
	}
	
	@Override
	public void updateView() {
		super.updateView();
        invalidateOptionsMenu();
	}

    protected abstract void closeCursor();

	/*
	 * control handling
	 */

	protected abstract boolean getHideRead();
	protected abstract void setHideReadSetting(boolean hideRead);

    /**
     * Called when the hide read status changed, but before the view is reloaded.
     * @param hideRead specifies whether the status has been set or reset.
     * @return true if the view should be reloaded, false if reloading should be skipped.
     */
    protected boolean onSetHideRead(boolean hideRead) {
        return true;
    }
	
	protected final void setHideRead(boolean hideRead) {
        setHideReadSetting(hideRead);
        invalidateOptionsMenu();
        if (onSetHideRead(hideRead)) {
            loadView();
        }
	}

	/*
	 * command handling
	 */
	
	protected abstract boolean requiresMarkReadConfirmationDialog();
	
	@Override
	protected int getOptionsMenuResourceId() {
		return R.menu.list_options;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_show_new).setVisible(!getHideRead());
		menu.findItem(R.id.menu_show_all).setVisible(getHideRead());
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() ==  R.id.menu_mark_read) {
			onMarkRead();
        } else if (item.getItemId() == R.id.menu_show_new) {
			setHideRead(true);
        } else if (item.getItemId() == R.id.menu_show_all) {
			setHideRead(false);
		} else {
            return super.onOptionsItemSelected(item);
        }
        return true;
	}

    protected abstract void markAllRead(SQLiteDatabase db);
	
	protected void markAllRead() {
        final SQLiteDatabase db = getDb();
        db.beginTransaction();
        try {
            markAllRead(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        getApp().onItemTagsChanged();
        onMarkedRead();
	}
	
	protected void onMarkRead() {
		if (requiresMarkReadConfirmationDialog()) {
			confirmMarkRead();
		} else {
			markAllRead();
		}
	}
	
	protected void confirmMarkRead() {
		DialogFactory.buildYesNoDialog(this, R.string.confirm_mark_read, R.string.confirm_mark_read_msg,
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        markAllRead();
                    }
                }).show();
	}
	
	protected void onMarkedRead() {
        loadView();
	}
}
