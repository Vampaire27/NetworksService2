package com.wwc2.networks.server.view;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.R;
import com.wwc2.networks.server.provider.sharedpreference.SPUtils;
import com.wwc2.networks.server.utils.Config;

public class UrlDialog extends Dialog {

    public UrlDialog(Context context) {
        super(context);
    }

    public UrlDialog(Context context, int theme) {
        super(context, theme);
    }

    public static class Builder {
        private Context context;
        private EditText s_edit;
        private String style;

        public Builder(Context context, String style) {
            this.context = context;
            this.style = style;
        }

        public UrlDialog create() {
            LayoutInflater inflater = LayoutInflater.from(context);

            final UrlDialog dialog = new UrlDialog(context,
                    R.style.DialogStyle);

            View layout = inflater.inflate(R.layout.layout_url_dialog, null);
            s_edit = (EditText) layout.findViewById(R.id.s_edit);

            RelativeLayout sure_layout = (RelativeLayout) layout
                    .findViewById(R.id.sure_layout);
            sure_layout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.dismiss();

                    String value = s_edit.getText().toString();
                    Log.i("UrlDialog", "---value=" + value + ",,,---style=" + style);
                    if(value != null && !value.equals("")){
                        SPUtils.put(CarServiceClient.getContext(),
                                Config.SYS_INTERFACE, value);
                    }
                }
            });

            RelativeLayout quit_layout = (RelativeLayout) layout
                    .findViewById(R.id.quit_layout);
            quit_layout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            dialog.setContentView(layout, params);
            dialog.getWindow().setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            return dialog;
        }
    }
}
