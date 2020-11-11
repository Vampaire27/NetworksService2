package com.wwc2.networks.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.R;
import com.wwc2.networks.server.dvr.DvrManagement;
import com.wwc2.networks.server.utils.LogUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DebugActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
    }
}
