package de.clinc8686.hochschul_crawler;

import android.content.Context;
import android.widget.Toast;

public class Message {
    Message(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }
}
