package de.clinc8686.hochschul_crawler;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.time.LocalDateTime;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {
    private static String password;
    private static String username;
    private static boolean login = false;
    private static int value = 60;
    @SuppressLint("StaticFieldLeak")
    private ProgressBar progressBarLogin;
    private long timestampTimeout = 0;
    private CheckBox checkBoxTrier, checkBoxAachen, checkBoxKoblenz;
    private LinearLayout linearlayoutcheckboxes;
    private static String checkbox = "checkBoxTrier";
    private EditText et_name;
    private Button btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutObjects();

        boolean service_status = checkAlarm(); //checkService();
        if (service_status) {
            MainActivity.login = true;
            btn_login.setText("Logout");
            loginsuccess();
        }

        if (!login) {
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        finishAndRemoveTask();
                        break;
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Datenschutzerklärung & Einwilligung zur Verarbeitung personenbezogener Daten")
                    .setMessage(getString(R.string.privacypolicy))
                    .setPositiveButton("Ich willige diesem ein", dialogClickListener)
                    .setNegativeButton("Ich lehne ab", dialogClickListener).show();
        }

        btn_login.setOnClickListener(view -> {
            progressBarLogin.setVisibility(View.VISIBLE);
            EditText et_password = findViewById(R.id.et_password);
            MainActivity.username = et_name.getText().toString();
            MainActivity.password = et_password.getText().toString();
            linearlayoutcheckboxes.setVisibility(View.GONE);

            if (!MainActivity.login) {
                if (et_name.getText().toString().equals("") || et_password.getText().toString().equals("")) {
                    createToastMessage("Benutzerkennung oder Passwort leer!");
                    runOnUiThread(() -> {
                        loginfailed();
                        progressBarLogin.setVisibility(View.GONE);
                        linearlayoutcheckboxes.setVisibility(View.VISIBLE);
                        MainActivity.login = false;
                    });
                } else {
                    LocalDateTime localdatetime = LocalDateTime.now();
                    if (localdatetime.getHour() >= 1 && localdatetime.getHour() <= 5) {
                         createToastMessage("Login failed: QIS zwischen 0 und 5 Uhr nicht erreichbar!");

                        runOnUiThread(() -> {
                            loginfailed();
                            progressBarLogin.setVisibility(View.GONE);
                            linearlayoutcheckboxes.setVisibility(View.VISIBLE);
                            MainActivity.login = false;
                        });
                    } else {
                        checkFirstLogin();
                        if (!checkIntent()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("Hinweis!");
                            builder.setMessage("Ohne die folgende Berechtigung kann die App nicht optimal im Hintergrund funktionieren.");
                            builder.setCancelable(true);
                            builder.setNeutralButton(android.R.string.ok,
                                    (dialog, id) -> {
                                        startAlarm();
                                        dialog.cancel();
                                    });

                            AlertDialog alert = builder.create();
                            alert.show();
                        } else {
                            startAlarm();
                        }
                    }
                }
            } else {
                //Beim neustarten der App, prüfen ob Service noch läuft, weil Anmeldung nicht mehr vorhanden ist. Ggf. Service stoppen
                if (checkAlarm()) {
                    stopAlarm();
                }

                try {
                    new NotificationChannel("Hochschul-Crawler", "Hochschul-Crawler", NotificationManager.IMPORTANCE_HIGH);
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(54295);
                } catch (Exception ignored) {}

                loginfailed();
                progressBarLogin.setVisibility(View.GONE);
                linearlayoutcheckboxes.setVisibility(View.VISIBLE);
                MainActivity.login = false;
                btn_login.setText("Login");
                MainActivity.username = "";
                MainActivity.password = "";
                SharedPreferences prefs = getSharedPreferences(getApplicationContext().getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
                prefs.edit().clear().apply();

                createToastMessage("Service wurde gestoppt & Logindaten entfernt.");
            }
        });

        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressWarnings("FieldMayBeFinal")
            TextView text_seekbar_minute = findViewById(R.id.text_seekbar_minute);
            @Override
            public void onProgressChanged(SeekBar seekBar, int progessValue, boolean b) {
                MainActivity.value = progessValue;
                text_seekbar_minute.setText("Alle " + MainActivity.value + " Minuten wird aktualisiert.\n" +
                        "Geschätzte Datennutzung im Monat: \n" + dataUsage());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                text_seekbar_minute.setText("Alle " + MainActivity.value + " Minuten wird aktualisiert.\n" +
                        "Geschätzte Datennutzung im Monat: " + dataUsage());
            }

            @SuppressLint("DefaultLocale")
            private String dataUsage() {
                double calculatedDataUsage = ((170.0 * (((60.0/MainActivity.value)*19.0)*30.0))/1000.0);
                if (calculatedDataUsage >= 1000) {
                    calculatedDataUsage = calculatedDataUsage / 1000.0;
                    return String.format("%.2f", calculatedDataUsage ) + " GByte";
                } else {
                    return (int) calculatedDataUsage + " MByte";
                }
            }
        });

        checkBoxTrier.setOnCheckedChangeListener((compoundButton, b) -> {
            if (checkBoxTrier.isChecked()) {
                checkBoxKoblenz.setChecked(false);
                checkBoxAachen.setChecked(false);
                checkbox = "checkBoxTrier";
                et_name.setHint("Benutzerkennung");
            } else if (!checkBoxAachen.isChecked() && !checkBoxTrier.isChecked() && !checkBoxKoblenz.isChecked()) {
                //checkBoxAachen.setChecked(true);
                checkBoxTrier.setChecked(true);
            }
        });

        checkBoxKoblenz.setOnCheckedChangeListener((compoundButton, b) -> {
            if (checkBoxKoblenz.isChecked()) {
                /*checkBoxTrier.setChecked(false);
                checkBoxAachen.setChecked(false);
                checkbox = "checkBoxKoblenz";
                et_name.setHint("HRZ-Login");*/

                checkBoxTrier.setChecked(true);
                checkBoxKoblenz.setChecked(false);
                createToastMessage("Es werden noch Tester für Koblenz gesucht! Melde dich bei: hochschulcrawler@gmail.com");
            } else if (!checkBoxAachen.isChecked() && !checkBoxTrier.isChecked() && !checkBoxKoblenz.isChecked()) {
                //checkBoxKoblenz.setChecked(true);
                checkBoxTrier.setChecked(true);
            }
        });

        checkBoxAachen.setOnCheckedChangeListener((compoundButton, b) -> {
            if (checkBoxAachen.isChecked()) {
                /*checkBoxTrier.setChecked(false);
                checkBoxKoblenz.setChecked(false);
                checkbox = "checkBoxAachen";
                et_name.setHint("FH-Kennung");*/

                checkBoxTrier.setChecked(true);
                checkBoxAachen.setChecked(false);
                createToastMessage("Es werden noch Tester für Aachen gesucht! Melde dich bei: hochschulcrawler@gmail.com");
            } else if (!checkBoxAachen.isChecked() && !checkBoxTrier.isChecked() && !checkBoxKoblenz.isChecked()) {
                checkBoxTrier.setChecked(true);

            }
        });
    }

    private void getLayoutObjects() {
        setContentView(R.layout.activity_main);
        progressBarLogin = findViewById(R.id.progressBarLogin);
        btn_login = findViewById(R.id.btn_login);
        checkBoxTrier = findViewById(R.id.checkBoxTrier);
        checkBoxAachen = findViewById(R.id.checkBoxAachen);
        checkBoxKoblenz = findViewById(R.id.checkBoxKoblenz);
        linearlayoutcheckboxes = findViewById(R.id.linearlayoutcheckboxes);
        et_name = findViewById(R.id.et_name);
    }

    private void checkFirstLogin() {
        new Thread(() -> {
            try {
                long difftime = System.currentTimeMillis() - timestampTimeout;
                if (difftime < 300000) {
                    throw new TooManyFalseLoginException("gesperrt - timeout");
                }

                storePreferences(MainActivity.username, MainActivity.password, MainActivity.checkbox);
                timestampTimeout = System.currentTimeMillis();
                Crawler_Service.loginQIS();

                //Anmeldung hat 1A funktioniert
                runOnUiThread(() -> {
                    loginsuccess();
                    MainActivity.login = true;
                    Button btn_login = findViewById(R.id.btn_login);
                    btn_login.setText("Logout");
                });

            } catch (TooManyFalseLoginException e) {    //Anmeldung schlug fehl, weil zu oft falsches Passwort/Username
                runOnUiThread(() -> {
                    loginfailed();
                    progressBarLogin.setVisibility(View.GONE);
                    linearlayoutcheckboxes.setVisibility(View.VISIBLE);
                    MainActivity.login = false;
                });
                createToastMessage("Anmeldung fehlgeschlagen! Zu oft falscher Benutzername/Passwort eingegeben! Deine IP-Adresse ist für einige Minuten gesperrt.");
                cancelNotifications();

            } catch (Throwable t) {     //Anmeldung schlug aus anderen Gründen fehl
                runOnUiThread(() -> {
                    loginfailed();
                    progressBarLogin.setVisibility(View.GONE);
                    linearlayoutcheckboxes.setVisibility(View.VISIBLE);
                    MainActivity.login = false;
                });
                createToastMessage("Anmeldung fehlgeschlagen! Benutzerkennung/Passwort falsch oder keine/schlechte Verbidung zum QIS!");
                cancelNotifications();
            }
        }).start();
    }

    private void cancelNotifications() {
        try {
            new NotificationChannel("Hochschul-Crawler", "Hochschul-Crawler", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(54295);
            notificationManager.cancel(54296);
            notificationManager.cancel(54297);
        } catch (Exception ignored) {}
    }

    private void loginsuccess() {
        TextView loggingstatus_text = findViewById(R.id.loggingstatus_text);
        loggingstatus_text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        loggingstatus_text.setTextColor(Color.GREEN);
        loggingstatus_text.setText("Logged in");

        TextView appCloseText = findViewById(R.id.appCloseText);
        appCloseText.setVisibility(View.VISIBLE);

        progressBarLogin.setVisibility(View.GONE);
        linearlayoutcheckboxes.setVisibility(View.GONE);
    }

    private void loginfailed() {
        TextView loggingstatus_text = findViewById(R.id.loggingstatus_text);
        loggingstatus_text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        loggingstatus_text.setTextColor(Color.RED);
        loggingstatus_text.setText("Not logged in");

        TextView appCloseText = findViewById(R.id.appCloseText);
        appCloseText.setVisibility(View.GONE);
    }

    @SuppressLint("BatteryLife")
    private void startAlarm() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        Intent intentFlag = new Intent();
        intentFlag.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
            intentFlag.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        } else {
            intentFlag.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intentFlag.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intentFlag);
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intentCrawlerClass = new Intent(this, Crawler_Service.class);
        intentCrawlerClass.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pi = PendingIntent.getBroadcast(this, 8686, intentCrawlerClass, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), (MainActivity.value * 60) * 1000, pi);
    }

    private boolean checkIntent() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return powerManager.isIgnoringBatteryOptimizations(getPackageName());
    }

    private void stopAlarm() {
        @SuppressLint("BatteryLife") Intent intent = new Intent(this, Crawler_Service.class).setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent sender = PendingIntent.getBroadcast(this, 8686, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        sender.cancel();
    }

    private boolean checkAlarm() {
        @SuppressLint("BatteryLife") Intent intent = new Intent(this, Crawler_Service.class).setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        @SuppressLint("UnspecifiedImmutableFlag") boolean alarmUp = PendingIntent.getBroadcast(this, 8686, intent, PendingIntent.FLAG_NO_CREATE) != null;
        return alarmUp;
    }

    private void storePreferences(String username, String password, String hochschule) {
        SharedPreferences.Editor editor = getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE).edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.putString("hochschule", hochschule);
        editor.putInt("interval", (MainActivity.value * 60) * 1000);
        editor.apply();

        Crawler_Service.setData(username, password, hochschule);
    }

    private void createToastMessage(String text) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show());
    }
}