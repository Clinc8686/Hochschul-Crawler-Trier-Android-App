package de.clinc8686.hochschul_crawler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

public class DisplayHomeMetrics {
    View view;
    int width, height;
    DisplayHomeMetrics(Context context, View view) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display screensize = wm.getDefaultDisplay();
        this.view = view;
        Point size = new Point();
        screensize.getSize(size);
        this.width = size.x;
        this.height = size.y;
        createMetrics();
    }

    @SuppressLint("ResourceType")
    private void createMetrics() {
        ((TextView) view.findViewById(R.id.header)).setTextSize(width/46.f);

        ((EditText) view.findViewById(R.id.et_name)).setTextSize(width/45.f);
        ((EditText) view.findViewById(R.id.et_name)).setPadding(100, 30, 100, 30);

        ((EditText) view.findViewById(R.id.et_password)).setTextSize(width/45.f);
        ((EditText) view.findViewById(R.id.et_password)).setPadding(100, 30, 100, 30);

        ((Button) view.findViewById(R.id.btn_login)).setTextSize(width/61.6f);
        ((Button) view.findViewById(R.id.btn_login)).setPadding(100, 30, 100, 30);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)(width/12.5f), (int)(width/12.5f));
        params.topMargin = (int) (width/20.76923);
        ((ProgressBar) view.findViewById(R.id.progressBarLogin)).setLayoutParams(params);

        ((RadioButton) view.findViewById(R.id.radioButtonTrier)).setTextSize(width/60.4f);
        ((RadioButton) view.findViewById(R.id.radioButtonTrier)).setTextColor(Color.WHITE);
        ((RadioButton) view.findViewById(R.id.radioButtonAachen)).setTextSize(width/60.4f);
        ((RadioButton) view.findViewById(R.id.radioButtonAachen)).setTextColor(Color.WHITE);
        ((RadioButton) view.findViewById(R.id.radioButtonKoblenz)).setTextSize(width/60.4f);
        ((RadioButton) view.findViewById(R.id.radioButtonKoblenz)).setTextColor(Color.WHITE);

        ((TextView) view.findViewById(R.id.appCloseText)).setTextSize(width/61.6f);
        ((TextView) view.findViewById(R.id.loggingstatus_text)).setTextSize(width/61.6f);

        ((TextView) view.findViewById(R.id.intervall_text)).setTextSize(width/77.f);

        ((TextView) view.findViewById(R.id.loginHint)).setTextSize(width/77.f);
        ((TextView) view.findViewById(R.id.text_seekbar_minute)).setTextSize(width/77.f);
        ((TextView) view.findViewById(R.id.text_qis_abschaltung)).setTextSize(width/77.f);
        ((TextView) view.findViewById(R.id.license)).setTextSize(width/80.f);
        ((TextView) view.findViewById(R.id.swiperight)).setTextSize(width/80.f);
    }
}