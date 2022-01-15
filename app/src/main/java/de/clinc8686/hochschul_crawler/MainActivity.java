package de.clinc8686.hochschul_crawler;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDateTime;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {
    public static String password;
    public static String username;
    public static boolean login = false;
    public static int value = 60;
    public static ProgressBar progressBarLogin;
    private long timestampTimeout = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBarLogin = findViewById(R.id.progressBarLogin);
        Button btn_login = findViewById(R.id.btn_login);

        boolean service_status = checkService();
        if (service_status) {
            MainActivity.login = true;
            btn_login.setText("Logout");
            loginsuccess();
        }

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBarLogin.setVisibility(View.VISIBLE);
                EditText et_name = findViewById(R.id.et_name);
                EditText et_password = findViewById(R.id.et_password);
                MainActivity.username = et_name.getText().toString();
                MainActivity.password = et_password.getText().toString();

                if (!MainActivity.login) {
                    if (et_name.getText().toString().equals("") || et_password.getText().toString().equals("")) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this,
                                    "Benutzerkennung oder Passwort leer!",
                                    Toast.LENGTH_LONG).show());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loginfailed();
                                progressBarLogin.setVisibility(View.INVISIBLE);
                                MainActivity.login = false;
                            }
                        });
                    } else {
                        LocalDateTime localdatetime = LocalDateTime.now();
                        if (!(localdatetime.getHour() >= 1 && localdatetime.getHour() <= 5)) {
                            checkFirstLogin(MainActivity.username, MainActivity.password);
                            startService();
                        } else {
                             Log.e("HU", "Login between 1 and 5 o'clock");
                             runOnUiThread(() -> Toast.makeText(MainActivity.this,
                                    "Login failed: QIS zwischen 0 und 5 Uhr nicht erreichbar!",
                                    Toast.LENGTH_LONG).show());

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loginfailed();
                                    progressBarLogin.setVisibility(View.INVISIBLE);
                                    MainActivity.login = false;
                                }
                            });
                        }
                    }
                } else {
                    //Beim neustarten der App, prüfen ob Service noch läuft, weil Anmeldung nicht mehr vorhanden ist. Ggf. Service stoppen
                    if (checkService()) {
                        stopService();
                    }

                    try {
                        NotificationChannel channel = new NotificationChannel("Hochschul-Crawler", "Hochschul-Crawler", NotificationManager.IMPORTANCE_HIGH);
                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.cancel(54295);
                    } catch (Exception e) {

                    }

                    loginfailed();
                    progressBarLogin.setVisibility(View.INVISIBLE);
                    MainActivity.login = false;
                    btn_login.setText("Login");
                    MainActivity.username = "";
                    MainActivity.password = "";

                    runOnUiThread(() -> Toast.makeText(MainActivity.this,
                            "Service wurde gestoppt & Logindaten entfernt.",
                             Toast.LENGTH_LONG).show());
                }
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
                        "Geschätzte Datennutzung im Monat: \n" + dataUsage(MainActivity.value));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                text_seekbar_minute.setText("Alle " + MainActivity.value + " Minuten wird aktualisiert.\n" +
                        "Geschätzte Datennutzung im Monat: " + dataUsage(MainActivity.value));
            }

            public String dataUsage(int i) {
                double du = ((170.0 * (((60.0/MainActivity.value)*19.0)*30.0))/1000.0);
                if (du >= 1000) {
                    du = du / 1000.0;
                    return String.format("%.2f", du ) + " GByte";
                } else {
                    return (int) du + " MByte";
                }
            }
        });
    }

    private void checkFirstLogin(String username, String password) {
        new Thread(new Runnable() {
            @SuppressLint("SetJavaScriptEnabled")
            @Override
            public void run() {
                try {
                    long difftime = System.currentTimeMillis() - timestampTimeout;
                    Log.e("difftime", difftime + "");
                    if (difftime < 300000) {
                        throw new IllegalAccessException("gesperrt - timeout");
                    }

                    Crawler_Service.password = MainActivity.password;
                    Crawler_Service.username = MainActivity.username;
                    timestampTimeout = System.currentTimeMillis();
                    Crawler_Service.loginQIS();

                    runOnUiThread(new Runnable() {  //Anmeldung hat 1A funktioniert
                        @Override
                        public void run() {
                            loginsuccess();
                            //progressBarLogin.setVisibility(View.GONE);
                            MainActivity.login = true;
                            Button btn_login = findViewById(R.id.btn_login);
                            btn_login.setText("Logout");
                        }
                    });

                } catch (IllegalAccessException e) {    //Anmeldung schlug fehl, weil zu oft falsches Passwort/Username
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loginfailed();
                            progressBarLogin.setVisibility(View.INVISIBLE);
                            MainActivity.login = false;
                        }
                    });
                    Log.e("Service-Crawler", "Failed to load login test", e);
                    runOnUiThread(() -> Toast.makeText(MainActivity.this,
                            "Anmeldung fehlgeschlagen! Zu oft falscher Benutzername/Passwort eingegeben! Deine IP-Adresse ist für einige Minuten gesperrt.",
                            Toast.LENGTH_LONG).show());
                    cancelNotifications();

                } catch (Throwable t) {     //Anmeldung schlug aus anderen Gründen fehl
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loginfailed();
                            progressBarLogin.setVisibility(View.INVISIBLE);
                            MainActivity.login = false;
                        }
                    });
                    Log.e("Service-Crawler", "Failed to load login test", t);
                    runOnUiThread(() -> Toast.makeText(MainActivity.this,
                            "Anmeldung fehlgeschlagen! Benutzerkennung/Passwort falsch oder keine/schlechte Verbidung zum QIS!",
                            Toast.LENGTH_LONG).show());
                    cancelNotifications();
                }
            }
        }).start();
    }

    private void cancelNotifications() {
        try {
            NotificationChannel channel = new NotificationChannel("Hochschul-Crawler", "Hochschul-Crawler", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(54295);
            mNotificationManager.cancel(54296);
            mNotificationManager.cancel(54297);
        } catch (Exception e) {

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
    }

    private void loginfailed() {
        TextView loggingstatus_text = findViewById(R.id.loggingstatus_text);
        loggingstatus_text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        loggingstatus_text.setTextColor(Color.RED);
        loggingstatus_text.setText("Not Logged in");

        TextView appCloseText = findViewById(R.id.appCloseText);
        appCloseText.setVisibility(View.GONE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startService() {

        // get the jobScheduler instance from current context
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);

        // MyJobService provides the implementation for the job
        ComponentName jobService = new ComponentName(getApplicationContext(), Crawler_Service.class);

        int period = (MainActivity.value * 60) * 1000;

        PersistableBundle bundle = new PersistableBundle();
        bundle.putString("username", MainActivity.username);
        bundle.putString("password", MainActivity.password);

        // define that the job will run periodically in intervals of 15 minutes
        JobInfo jobInfo = new JobInfo.Builder(8686, jobService)
                .setPeriodic(period)
                .setPersisted(true)
                .setExtras(bundle)
                .build();

        // schedule/start the job
        int result = jobScheduler.schedule(jobInfo);
        if (result == JobScheduler.RESULT_SUCCESS) {
            Log.e("Service-Crawler", "Successfully scheduled job: " + result);
        } else {
            Log.e("Service-Crawler", "RESULT_FAILURE: " + result);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void stopService() {
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE ) ;

        int Job_id = Integer.parseInt("8686");
        try {
            scheduler.cancel(Job_id);
        } catch (Exception e) {
            return;
        }
        Log.e("Service-Crawler", "gestoppt!");
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean checkService() {
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE ) ;

        for ( JobInfo jobInfo : scheduler.getAllPendingJobs() ) {
            if ( jobInfo.getId() == 8686 ) {
                Log.e("Crawler_Service", "jobläuft");
                return true;
            }
        }
        return false;
    }
}