package com.wwc2.networks.server.view;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.wwc2.networks.R;

public class WarnDialog extends Dialog {

    public WarnDialog(Context context) {
        super(context);
    }

    public WarnDialog(Context context, int theme) {
        super(context, theme);
    }

    public static class Builder {
        private Context context;

        public Builder(Context context) {
            this.context = context;
        }

        public WarnDialog create() {
            LayoutInflater inflater = LayoutInflater.from(context);
            final WarnDialog dialog = new WarnDialog(context,
                    R.style.DialogStyle);
            View layout = inflater.inflate(R.layout.layout_warn_dialog, null);
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            dialog.setContentView(layout, params);
            return dialog;
        }
    }
}
