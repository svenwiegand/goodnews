package com.gettingmobile.goodnews.settings;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.gettingmobile.goodnews.R;

public class EditTagNamePreference extends EditTextPreference {
    private static final char[] INVALID_CHARACTERS = { '"', '<', '>', '?', '&', '/', '\\', '^', ','};
    private static final String INVALID_CHARACTERS_LIST;

    static {
        final StringBuilder sb = new StringBuilder("\"");
        for (char c : INVALID_CHARACTERS) {
            sb.append(' ').append(c);
        }
        INVALID_CHARACTERS_LIST = sb.toString();
    }

    public EditTagNamePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EditTagNamePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTagNamePreference(Context context) {
        super(context);
    }

    @Override
    protected void onAddEditTextToDialogView(View dialogView, EditText editText) {
        super.onAddEditTextToDialogView(dialogView, editText);
        editText.addTextChangedListener(new TagNameWatcher());
    }

    protected void toastInputError() {
        final String text = String.format(
                getContext().getText(R.string.invalid_tag_name_characters).toString(), INVALID_CHARACTERS_LIST);
        Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
    }

    /*
     * inner classes
     */

    class TagNameWatcher implements TextWatcher {
        private boolean inTextChange = false;

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // nothing to be done
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // nothing to be done
        }

        @Override
        public void afterTextChanged(Editable editable) {
            final boolean topLevelCall = !inTextChange;
            inTextChange = true;
            try {
                /*
                 * find first invalid character position and delete it. This will trigger the method again, so that all
                 * invalid characters will be deleted.
                 */
                final String text = editable.toString();
                for (char c : INVALID_CHARACTERS) {
                    final int index = text.indexOf(c);
                    if (index > -1) {
                        editable.delete(index, index + 1);
                        if (topLevelCall) {
                            toastInputError();
                        }
                        break;
                    }
                }
            } finally {
                inTextChange = false;
            }
        }
    }
}
