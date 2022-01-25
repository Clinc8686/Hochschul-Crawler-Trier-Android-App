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
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
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
    public static String password;
    public static String username;
    private static boolean login = false;
    private static int value = 60;
    @SuppressLint("StaticFieldLeak")
    private ProgressBar progressBarLogin;
    private long timestampTimeout = 0;
    @SuppressLint("StaticFieldLeak")
    public static CheckBox checkBoxTrier, checkBoxAachen, checkBoxKoblenz;
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
            builder.setTitle("Datenschutzerklärung")
                    .setMessage("Die Nutzung dieser Applikation ist mit der Verarbeitung personenbezogener Daten verbunden. " +
                            "\n\nDie Applikation verarbeitet personenbezogene Daten unter Beachtung der einschlägigen Datenschutzvorschriften. " +
                            "\nEine Datenverarbeitung findet nur mit Ihrer Erlaubnis statt. " +
                            "\n\nAllgemeine Angaben: Die Informationen werden durch das Kontaktformular erhoben und sind Grundlage nach Art. 6, 1a DSGVO zur Verwendung der Applikation benötigt. " +
                            "\n\nDatenweitergabe: Zur Abfrage der Informationen werden die Daten an die Server der jeweiligen Hochschule übermittelt und dortige Informationen ausgewertet. " +
                            "Hierzu zählt die Rechenzentrumskennung, das Passwort und bei der Hochschule abgespeicherte Daten wie Modulfächer und die persönliche Leistung der jeweiligen Module (Noten, bestandene oder nicht bestandene Module). " +
                            "\n\nWiderruf: Die Einwilligung zur Erhebung, Verarbeitung, Speicherung und Nutzung personenbezogener Daten kann jederzeit mit Wirkung durch Entfernung/Deinstallation der Apllikation von ihrem Gerät widerrufen werden. " +
                            "\n\nWenn Sie Fragen oder Anregungen zu diesen Informationen haben oder wegen der Geltendmachung Ihrer Rechte an uns wenden möchten, richten Sie Ihre Anfrage bitte an: Mario Lampert, hochschulcrawler@gmail.com")
                    .setPositiveButton("Ich akzeptiere", dialogClickListener)
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
                    runOnUiThread(() -> Toast.makeText(MainActivity.this,
                                "Benutzerkennung oder Passwort leer!",
                                Toast.LENGTH_LONG).show());
                    runOnUiThread(() -> {
                        loginfailed();
                        progressBarLogin.setVisibility(View.GONE);
                        linearlayoutcheckboxes.setVisibility(View.VISIBLE);
                        MainActivity.login = false;
                    });
                } else {
                    LocalDateTime localdatetime = LocalDateTime.now();
                    if (localdatetime.getHour() >= 1 && localdatetime.getHour() <= 5) {
                         Log.e("HU", "Login between 1 and 5 o'clock");
                         runOnUiThread(() -> Toast.makeText(MainActivity.this,
                                "Login failed: QIS zwischen 0 und 5 Uhr nicht erreichbar!",
                                Toast.LENGTH_LONG).show());

                        runOnUiThread(() -> {
                            loginfailed();
                            progressBarLogin.setVisibility(View.GONE);
                            linearlayoutcheckboxes.setVisibility(View.VISIBLE);
                            MainActivity.login = false;
                        });
                    } else {
                        //startService();
                        //startAlarm();
                        checkFirstLogin();
                        if (!checkIntent()) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                            builder1.setTitle("Hinweis!");
                            builder1.setMessage("Ohne die folgende Berechtigung kann die App nicht optimal im Hintergrund funktionieren.");
                            builder1.setCancelable(true);
                            builder1.setNeutralButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            startAlarm();
                                            dialog.cancel();
                                            //stopAlarm();
                                            //finishAndRemoveTask();
                                        }
                                    });

                            AlertDialog alert11 = builder1.create();
                            alert11.show();
                        } else {
                            startAlarm();
                        }
                    }
                }
            } else {
                //Beim neustarten der App, prüfen ob Service noch läuft, weil Anmeldung nicht mehr vorhanden ist. Ggf. Service stoppen
                //if (checkService()) {
                if (checkAlarm()) {
                    //stopService();
                    stopAlarm();
                }

                try {
                    new NotificationChannel("Hochschul-Crawler", "Hochschul-Crawler", NotificationManager.IMPORTANCE_HIGH);
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(54295);
                } catch (Exception ignored) {

                }

                loginfailed();
                progressBarLogin.setVisibility(View.GONE);
                linearlayoutcheckboxes.setVisibility(View.VISIBLE);
                MainActivity.login = false;
                btn_login.setText("Login");
                MainActivity.username = "";
                MainActivity.password = "";

                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "Service wurde gestoppt & Logindaten entfernt.",
                         Toast.LENGTH_LONG).show());
            }
        });

        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressWarnings("FieldMayBeFinal")
            TextView text_seekbar_minute = findViewById(R.id.text_seekbar_minute);
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                MainActivity.value = i;
                text_seekbar_minute.setText("Alle " + MainActivity.value + " Minuten wird aktualisiert.\n" +
                        "Geschätzte Datennutzung im Monat: \n" + dataUsage());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                text_seekbar_minute.setText("Alle " + MainActivity.value + " Minuten wird aktualisiert.\n" +
                        "Geschätzte Datennutzung im Monat: " + dataUsage());
            }

            @SuppressLint("DefaultLocale")
            public String dataUsage() {
                double du = ((170.0 * (((60.0/MainActivity.value)*19.0)*30.0))/1000.0);
                if (du >= 1000) {
                    du = du / 1000.0;
                    return String.format("%.2f", du ) + " GByte";
                } else {
                    return (int) du + " MByte";
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
                checkBoxAachen.setChecked(true);
            }
        });

        checkBoxKoblenz.setOnCheckedChangeListener((compoundButton, b) -> {
            if (checkBoxKoblenz.isChecked()) {
                checkBoxTrier.setChecked(false);
                checkBoxAachen.setChecked(false);
                checkbox = "checkBoxKoblenz";
                et_name.setHint("HRZ-Login");
            } else if (!checkBoxAachen.isChecked() && !checkBoxTrier.isChecked() && !checkBoxKoblenz.isChecked()) {
                checkBoxKoblenz.setChecked(true);
            }
        });

        checkBoxAachen.setOnCheckedChangeListener((compoundButton, b) -> {
            if (checkBoxAachen.isChecked()) {
                checkBoxTrier.setChecked(false);
                checkBoxKoblenz.setChecked(false);
                checkbox = "checkBoxAachen";
                et_name.setHint("FH-Kennung");
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
                Log.e("difftime", difftime + "");
                if (difftime < 300000) {
                    throw new TooManyFalseLoginException("gesperrt - timeout");
                }

                Crawler_Service.password = MainActivity.password;
                Crawler_Service.username = MainActivity.username;
                Crawler_Service.hochschule = MainActivity.checkbox;
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
                Log.e("Service-Crawler", "Failed to load login test", e);
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "Anmeldung fehlgeschlagen! Zu oft falscher Benutzername/Passwort eingegeben! Deine IP-Adresse ist für einige Minuten gesperrt.",
                        Toast.LENGTH_LONG).show());
                cancelNotifications();

            } catch (Throwable t) {     //Anmeldung schlug aus anderen Gründen fehl
                runOnUiThread(() -> {
                    loginfailed();
                    progressBarLogin.setVisibility(View.GONE);
                    linearlayoutcheckboxes.setVisibility(View.VISIBLE);
                    MainActivity.login = false;
                });
                Log.e("Service-Crawler", "Failed to load login test", t);
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "Anmeldung fehlgeschlagen! Benutzerkennung/Passwort falsch oder keine/schlechte Verbidung zum QIS!",
                        Toast.LENGTH_LONG).show());
                cancelNotifications();
            }
        }).start();
    }

    private void cancelNotifications() {
        try {
            new NotificationChannel("Hochschul-Crawler", "Hochschul-Crawler", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(54295);
            mNotificationManager.cancel(54296);
            mNotificationManager.cancel(54297);
        } catch (Exception ignored) {

        }
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
    public void startAlarm() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        Intent intent=new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
            intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        } else {
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, Crawler_Service.class);
        i.putExtra("username", MainActivity.username);
        i.putExtra("password", MainActivity.password);
        i.putExtra("hochschule", MainActivity.checkbox);
        i.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pi = PendingIntent.getBroadcast(this, 8686, i, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), (MainActivity.value * 60) * 1000, pi);
    }

    public boolean checkIntent() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return powerManager.isIgnoringBatteryOptimizations(getPackageName());
    }

    public void stopAlarm() {
        @SuppressLint("BatteryLife") Intent intent = new Intent(this, Crawler_Service.class).setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent sender = PendingIntent.getBroadcast(this, 8686, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        sender.cancel();
    }

    public boolean checkAlarm() {
        @SuppressLint("BatteryLife") Intent intent = new Intent(this, Crawler_Service.class).setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        @SuppressLint("UnspecifiedImmutableFlag") boolean alarmUp = PendingIntent.getBroadcast(this, 8686, intent, PendingIntent.FLAG_NO_CREATE) != null;
        if (alarmUp) {
            Log.d("myTag", "Alarm is already active");
            return true;
        } else {
            Log.d("myTag", "Alarm is already active not");
        }
        return false;
    }
}