package de.clinc8686.hochschul_crawler.crawler;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;

@SuppressLint("BatteryLife")
public class Alarm {
    private  Context context;

    public Alarm(Context context, int value) {
        this.context = context;

        PowerManager powerManager = (PowerManager) this.context.getSystemService(Context.POWER_SERVICE);
        Intent intentFlag = new Intent();
        intentFlag.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (powerManager.isIgnoringBatteryOptimizations(context.getPackageName())) {
            intentFlag.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        } else {
            intentFlag.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intentFlag.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intentFlag);
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intentCrawlerClass = new Intent(context, Crawler_Service.class);
        intentCrawlerClass.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pi = PendingIntent.getBroadcast(context, 8686, intentCrawlerClass, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), (value * 60) * 1000, pi);
    }

    public static void stopAlarm(Context context) {
        Intent intent = new Intent(context, Crawler_Service.class).setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        PendingIntent sender = PendingIntent.getBroadcast(context, 8686, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        sender.cancel();
    }

    public static boolean checkAlarm(Context context) {
        Intent intent = new Intent(context, Crawler_Service.class).setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        @SuppressLint("UnspecifiedImmutableFlag") boolean alarmUp = PendingIntent.getBroadcast(context, 8686, intent, PendingIntent.FLAG_NO_CREATE) != null;
        return alarmUp;
    }

    public static boolean checkIntent(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
    }
}