package de.clinc8686.hochschul_crawler;

import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
//import androidx.annotation.RequiresApi;
//import androidx.core.app.NotificationCompat;
//import androidx.core.app.NotificationManagerCompat;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
//import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

//import androidx.appcompat.app.AppCompatActivity;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.Buffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainActivity extends AppCompatActivity {
    public static String password;
    public static String username;
    public static boolean login = false;
    public static int value = 60;
    public static ProgressBar progressBarLogin;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("Liste", String.valueOf(Crawler_Service.gradelist));

        progressBarLogin = findViewById(R.id.progressBarLogin);
        Button btn_login = findViewById(R.id.btn_login);
        boolean service_status = checkService();
        if (service_status) {
            btn_login.setText("Logout");
            TextView loggingstatus_text = findViewById(R.id.loggingstatus_text);
            loggingstatus_text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            loggingstatus_text.setTextColor(Color.GREEN);
            loggingstatus_text.setText("Logged in");
        }

        btn_login.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                EditText et_name = findViewById(R.id.et_name);
                EditText et_password = findViewById(R.id.et_password);
                MainActivity.username = et_name.getText().toString();
                MainActivity.password = et_password.getText().toString();

                if (et_name.getText().toString().equals("") || et_password.getText().toString().equals("")) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this,
                            "Benutzerkennung oder Passwort leer!",
                            Toast.LENGTH_LONG).show());
                } else {
                    if (MainActivity.login == false) {
                        LocalDateTime localdatetime = LocalDateTime.now();
                        if (!(localdatetime.getHour() >= 1 && localdatetime.getHour() <= 5)) {
                            checkFirstLogin(MainActivity.username, MainActivity.password);
                            startService();
                            progressBarLogin.setProgress(100);
                        } else {
                            Log.e("HU", "Login between 0 and 5 o'clock");
                            runOnUiThread(() -> Toast.makeText(MainActivity.this,
                                    "Login failed: QIS zwischen 0 und 5 Uhr nicht erreichbar!",
                                    Toast.LENGTH_LONG).show());
                        }
                    } else {
                        if (isMyServiceRunning(Crawler_Service.class)) {
                            stopService();
                        }

                        btn_login.setText("Login");
                        MainActivity.login = false;
                        MainActivity.username = "";
                        MainActivity.password = "";
                    }
                }
            }
        });

        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //int value;
            TextView text_seekbar_minute = findViewById(R.id.text_seekbar_minute);
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                MainActivity.value = i;
                text_seekbar_minute.setText("Alle " + MainActivity.value + " Minuten wird aktualisiert.\n" +
                        "Geschätzte Datennutzung im Monat: " + dataUsage(MainActivity.value));
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

        /*new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @SuppressLint("SetJavaScriptEnabled")
            @Override
            public void run() {
                HtmlPage grades = null;
                try {
                    grades = loginQIS();
                    checkGrades(grades);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();*/
    }

    private void checkFirstLogin(String username, String password) {
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @SuppressLint("SetJavaScriptEnabled")
            @Override
            public void run() {

                try {
                    HtmlPage grades = loginQIS();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView loggingstatus_text = findViewById(R.id.loggingstatus_text);
                            loggingstatus_text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            loggingstatus_text.setTextColor(Color.GREEN);
                            loggingstatus_text.setText("Logged in");
                            MainActivity.login = true;

                            Button btn_login = findViewById(R.id.btn_login);
                            btn_login.setText("Logout");
                            progressBarLogin.setProgress(70);
                        }
                    });
                } catch (Throwable t) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView loggingstatus_text = findViewById(R.id.loggingstatus_text);
                            loggingstatus_text.setText("Not Logged In");
                            loggingstatus_text.setTextColor(Color.RED);
                            MainActivity.login = false;
                        }
                    });

                    Log.e("HU", "Failed to load login test", t);
                    runOnUiThread(() -> Toast.makeText(MainActivity.this,
                            "Anmeldung fehlgeschlagen! Benutzerkennung/Passwort falsch oder keine/schlechte Verbidung zum QIS!",
                            Toast.LENGTH_LONG).show());
                }
            }
        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startService() {
        progressBarLogin.setProgress(80);
        // get the jobScheduler instance from current context
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);

        // MyJobService provides the implementation for the job
        ComponentName jobService = new ComponentName(getApplicationContext(), Crawler_Service.class);

        int period = (MainActivity.value * 60) * 1000;

        // define that the job will run periodically in intervals of 15 minutes
        JobInfo jobInfo = new JobInfo.Builder(8686, jobService).setPeriodic(period).setPersisted(true).build();

        // schedule/start the job
        int result = jobScheduler.schedule(jobInfo);
        if (result == JobScheduler.RESULT_SUCCESS) {
            Log.d("Service", "Successfully scheduled job: " + result);
        } else {
            Log.e("Service-Crawler", "RESULT_FAILURE: " + result);
        }
        progressBarLogin.setProgress(90);
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
        Log.e("Jobundso", "gestoppt!");
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean checkService() {
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE ) ;

        int Job_id = Integer.parseInt("8686");

        if (Job_id == JobScheduler.RESULT_SUCCESS) {
            Log.d("Service-Crawler", "Service läuft noch!");
            return true;
        } else {
            Log.e("Service-Crawler", "Service läuft nicht mehr!");
            return false;
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public HtmlPage loginQIS() throws Exception {
        progressBarLogin.setProgress(10);
        HtmlPage grades = null;

        WebClient webClient = new WebClient();
        webClient.getOptions().setTimeout(30000);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setRedirectEnabled(true);
        progressBarLogin.setProgress(30);

        HtmlPage qis_login_page;
        HtmlPage hs_Login = webClient.getPage("https://qis.hochschule-trier.de/qisserver/rds?state=user&type=0&category=menu.browse&startpage=portal.vm");
        HtmlForm hs_login_form = hs_Login.getFormByName("login");
        HtmlTextInput hs_login_username = hs_login_form.getInputByName("j_username");
        HtmlPasswordInput hs_login_password = hs_login_form.getInputByName("j_password");
        hs_login_username.setValueAttribute(MainActivity.username);
        hs_login_password.setValueAttribute(MainActivity.password);
        HtmlButton button = hs_Login.getFirstByXPath("//button[@type='submit']");
        qis_login_page = button.click();
        progressBarLogin.setProgress(40);

        HtmlPage qis_homepage;
        HtmlForm qis_login_form = qis_login_page.getFormByName("loginform");
        HtmlTextInput qis_login_username = qis_login_form.getInputByName("asdf");
        HtmlPasswordInput qis_login_password = qis_login_form.getInputByName("fdsa");
        qis_login_username.setValueAttribute(MainActivity.username);
        qis_login_password.setValueAttribute(MainActivity.password);
        HtmlInput qis_login_button = qis_login_form.getInputByName("submit");
        qis_homepage = qis_login_button.click();
        progressBarLogin.setProgress(50);

        HtmlAnchor a = qis_homepage.getAnchorByText("Prüfungsverwaltung");
        HtmlPage b = a.click();
        HtmlAnchor c = b.getAnchorByText("Notenspiegel");
        HtmlPage d = c.click();
        HtmlAnchor e = d.getAnchorByText("Abschluss Bachelor of Science");
        HtmlPage f = e.click();
        HtmlAnchor g = f.getAnchorByText("Informatik - Digitale Medien und Spiele (PO-Version 2019)");
        grades = g.click();
        progressBarLogin.setProgress(60);

        webClient.close();
        return grades;

        //String grade_page_s = grades.asNormalizedText();
        //Log.e("test", grades.asText().toString());
    }

    /*@RequiresApi(api = Build.VERSION_CODES.O)
    public void checkGrades(HtmlPage grades) throws Exception {
        String grade_page_s = grades.asText().toString();
        BufferedReader reader = new BufferedReader(new StringReader(grade_page_s));

        int year = Integer.parseInt(LocalDate.now().format(DateTimeFormatter.ofPattern("yy")));
        String semester = "";
        if (LocalDate.now().getMonthValue() >= 10 || LocalDate.now().getMonthValue() <= 4) {
            if(LocalDate.now().getMonthValue() >= 10 && LocalDate.now().getMonthValue() <= 12) {
                semester = "WiSe " + year + "/" + (year+1);
            } else {
                semester = "WiSe " + year + "/" + (year-1);
            }
        } else {
            semester = "SoSe " + year;
        }

        semester = "SoSe 21"; //temporär
        String s;
        String mod = "";
        //Preferences prefs = Preferences.userRoot().node("Hochschul-Scraper");
            while ((s = reader.readLine()) != null) {

                if(((s.contains("BE") || s.contains("NB") || s.contains("NE")) && s.contains(semester)) && (!(mod.contains("PV") || mod.contains("Studienleistung")))) {
                    mod = mod.replace("\t", " ");
                    mod = mod.replace("  ", " ");

                    s = s.replace("\t", " ");
                    s = s.replace("  ", " ");
                    String[] s_splitted = s.split("\\s+");

                    String mod_reg = mod;
                    mod_reg = mod_reg.replace("ä", "ae");
                    mod_reg = mod_reg.replace("ö", "oe");
                    mod_reg = mod_reg.replace("ü", "ue");
                    mod_reg = mod_reg.replace("Ä", "AE");
                    mod_reg = mod_reg.replace("Ö", "OE");
                    mod_reg = mod_reg.replace("Ü", "UE");

                    Log.e("QIS", mod);

                    sendPushNotification(semester, mod);

                    FileOutputStream fos = null;
                    try {
                        fos = openFileOutput("module.txt", MODE_PRIVATE);
                        fos.write((semester + "|" + mod).getBytes());
                        fos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                            /*if (pref_exists == false) {
                                prefs.put(mod_reg + "|" + s_splitted[1] + " " + s_splitted[2], "1");

                                //try {
                                    String jsonInputString = "{ \"modul\": \"" + mod_reg + "|" + s_splitted[1] + " " + s_splitted[2] + "\" }";
                                    System.out.println("sende jetzt: " + jsonInputString);
                                    StringEntity entity = new StringEntity(jsonInputString, ContentType.APPLICATION_FORM_URLENCODED);
                                    HttpClient httpClient = HttpClientBuilder.create().build();
                                    HttpPost request = new HttpPost("https://mariolampert.de/HS-Bot/API/index.php");
                                    request.setEntity(entity);
                                    HttpResponse response = httpClient.execute(request);
                                //} catch (Exception e) {
                                //    JOptionPane.showMessageDialog(new JFrame(), "Fehler!\nIch konnte keine Nachricht an den Server senden!.", "Hochschul-Crawler",
                                //            JOptionPane.ERROR_MESSAGE);
                                //    System.exit(0);
                                //}
                            }*/
                /*}
                mod = s;
            }
    }*/

    /*public void sendPushNotification(String semester, String mod) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Clinc8686", "Clinc8686", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "Clinc8686")
                .setSmallIcon(R.mipmap.hochschulcrawlerlogoicon)
                .setContentTitle("Hochschul-Crawler")
                .setContentText("Es sind neue Noten verfügbar!")
                .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Es sind neue Noten für das " + semester + " in " + mod + " verfügbar!"));

        NotificationManagerCompat maCom = NotificationManagerCompat.from(MainActivity.this);
        maCom.notify(1, builder.build());
    }*/

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}