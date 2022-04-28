package de.clinc8686.hochschul_crawler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;
import android.util.TypedValue;
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
        ((TextView) view.findViewById(R.id.header)).setAutoSizeTextTypeUniformWithConfiguration(27, 54, 4, TypedValue.COMPLEX_UNIT_SP);

        ((EditText) view.findViewById(R.id.et_name)).setAutoSizeTextTypeUniformWithConfiguration(24, 48, 4, TypedValue.COMPLEX_UNIT_SP);
        ((EditText) view.findViewById(R.id.et_name)).setPadding(50, 10, 50, 10);

        ((EditText) view.findViewById(R.id.et_password)).setAutoSizeTextTypeUniformWithConfiguration(24, 48, 4, TypedValue.COMPLEX_UNIT_SP);
        ((EditText) view.findViewById(R.id.et_password)).setPadding(50, 10, 50, 10);

        ((Button) view.findViewById(R.id.btn_login)).setAutoSizeTextTypeUniformWithConfiguration(12, 24, 4, TypedValue.COMPLEX_UNIT_SP);
        ((Button) view.findViewById(R.id.btn_login)).setPadding(50, 10, 50, 10);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)(width/12.5f), (int)(width/12.5f));
        params.topMargin = (int) (width/20.76923);
        ((ProgressBar) view.findViewById(R.id.progressBarLogin)).setLayoutParams(params);

        ((RadioButton) view.findViewById(R.id.radioButtonTrier)).setAutoSizeTextTypeUniformWithConfiguration(12, 24, 1, TypedValue.COMPLEX_UNIT_SP);
        ((RadioButton) view.findViewById(R.id.radioButtonTrier)).setTextColor(Color.WHITE);

        //((RadioButton) view.findViewById(R.id.radioButtonAachen)).setAutoSizeTextTypeUniformWithConfiguration(12, 24, 1, TypedValue.COMPLEX_UNIT_SP);
        //((RadioButton) view.findViewById(R.id.radioButtonAachen)).setTextColor(Color.WHITE);

        //((RadioButton) view.findViewById(R.id.radioButtonKoblenz)).setAutoSizeTextTypeUniformWithConfiguration(12, 24, 1, TypedValue.COMPLEX_UNIT_SP);
        //((RadioButton) view.findViewById(R.id.radioButtonKoblenz)).setTextColor(Color.WHITE);

        ((TextView) view.findViewById(R.id.appCloseText)).setAutoSizeTextTypeUniformWithConfiguration(12, 24, 4, TypedValue.COMPLEX_UNIT_SP);
        ((TextView) view.findViewById(R.id.loggingstatus_text)).setAutoSizeTextTypeUniformWithConfiguration(12, 24, 4, TypedValue.COMPLEX_UNIT_SP);

        ((TextView) view.findViewById(R.id.intervall_text)).setAutoSizeTextTypeUniformWithConfiguration(12, 24, 4, TypedValue.COMPLEX_UNIT_SP);

        ((TextView) view.findViewById(R.id.loginHint)).setAutoSizeTextTypeUniformWithConfiguration(12, 24, 4, TypedValue.COMPLEX_UNIT_SP);
        ((TextView) view.findViewById(R.id.text_seekbar_minute)).setAutoSizeTextTypeUniformWithConfiguration(12, 24, 4, TypedValue.COMPLEX_UNIT_SP);
        ((TextView) view.findViewById(R.id.text_qis_abschaltung)).setAutoSizeTextTypeUniformWithConfiguration(12, 24, 4, TypedValue.COMPLEX_UNIT_SP);
        //((TextView) view.findViewById(R.id.license)).setAutoSizeTextTypeUniformWithConfiguration(12, 24, 4, TypedValue.COMPLEX_UNIT_SP);
        ((TextView) view.findViewById(R.id.swiperight)).setAutoSizeTextTypeUniformWithConfiguration(12, 24, 4, TypedValue.COMPLEX_UNIT_SP);
    }
}