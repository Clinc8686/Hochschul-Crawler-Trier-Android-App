package de.clinc8686.hochschul_crawler.ui;

import android.content.Context;
import android.graphics.Point;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class Message {
    public Message(Context context, String text) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display screensize = wm.getDefaultDisplay();
        Point size = new Point();
        screensize.getSize(size);

        SpannableStringBuilder biggerText = new SpannableStringBuilder(text);
        biggerText.setSpan(new RelativeSizeSpan(size.y/2000.f), 0, text.length(), 0);
        Toast.makeText(context, biggerText, Toast.LENGTH_LONG).show();
    }
}
