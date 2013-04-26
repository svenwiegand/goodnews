package com.gettingmobile.goodnews.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

abstract class InfoDialogBuilder  {
	protected final Context context;
	private final AlertDialog.Builder builder;
	private final int titleId;
	private final int iconId;
    private final Integer neutralButtonTextId;
    private final Integer positiveButtonTextId;
    private final Integer negativeButtonTextId;
    private final InfoDialogListener listener;
	
	public InfoDialogBuilder(Context context, int titleId, int iconId,
                             Integer neutralButtonTextId, Integer positiveButtonTextId, Integer negativeButtonTextId,
                             InfoDialogListener listener) {
		this.context = context;
		this.titleId = titleId;
		this.iconId = iconId;
        this.neutralButtonTextId = neutralButtonTextId;
        this.positiveButtonTextId = positiveButtonTextId;
        this.negativeButtonTextId = negativeButtonTextId;
        this.listener = listener;
		builder = new AlertDialog.Builder(context);
	}
	
	protected abstract String buildMessage();

	public void show() {
		final SpannableString s = new SpannableString(Html.fromHtml(buildMessage()));
		Linkify.addLinks(s, Linkify.WEB_URLS);

        final Listener l = new Listener(listener);
		builder
			.setTitle(titleId)
			.setIcon(iconId)
            .setMessage(s);
        if (neutralButtonTextId != null) {
            builder.setNeutralButton(neutralButtonTextId, l);
        }
        if (positiveButtonTextId != null) {
            builder.setPositiveButton(positiveButtonTextId, l);
        }
        if (negativeButtonTextId != null) {
            builder.setNegativeButton(negativeButtonTextId, l);
        }

        final AlertDialog dlg = builder.show();
        dlg.setOnDismissListener(l);
		final TextView msgView = (TextView) dlg.findViewById(android.R.id.message);
		if (msgView != null) {
			msgView.setMovementMethod(LinkMovementMethod.getInstance());
			msgView.setLinkTextColor(0xffffffff);
		}
	}

    class Listener implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener {
        private final InfoDialogListener listener;

        public Listener(InfoDialogListener listener) {
            this.listener = listener;
        }

        @Override
        public void onClick(DialogInterface dlg, int btnId) {
            if (listener != null) {
                final int dismissType;
                switch (btnId) {
                    case AlertDialog.BUTTON_POSITIVE:
                        dismissType = InfoDialogListener.DISMISS_TYPE_POSITIVE;
                        break;
                    case AlertDialog.BUTTON_NEGATIVE:
                        dismissType = InfoDialogListener.DISMISS_TYPE_NEGATIVE;
                        break;
                    default:
                        dismissType = InfoDialogListener.DISMISS_TYPE_NEUTRAL;
                }
                listener.onDismissInfoDialog(dlg, dismissType);
            }
        }

        @Override
        public void onDismiss(DialogInterface dlg) {
            onClick(dlg, AlertDialog.BUTTON_NEUTRAL);
        }
    }
}
