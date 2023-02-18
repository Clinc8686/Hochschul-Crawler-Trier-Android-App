package de.clinc8686.hochschul_crawler;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {
    @SuppressLint({"BatteryLife", "UnsafeProtectedBroadcastReceiver"})
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences("de.clinc8686.qishochschulcrawler", Context.MODE_PRIVATE);
        int interval = prefs.getInt("interval", 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Crawler_Service.class);
        i.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pi = PendingIntent.getBroadcast(context, 8686, i, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pi);

        HomeFragment.bootCompletedReceiver = this;
    }
}