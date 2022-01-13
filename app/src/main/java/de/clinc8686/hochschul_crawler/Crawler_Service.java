package de.clinc8686.hochschul_crawler;

import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import java.io.BufferedReader;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Crawler_Service extends JobService {
    public static String password;
    public static String username;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Intent i = new Intent(Crawler_Service.this, Crawler_Service.class);
        try {
            Crawler_Service.password = jobParameters.getExtras().getString("password");
            Crawler_Service.username = jobParameters.getExtras().getString("username");
            Log.e("Crawler_Service", "entnehme Passwort1");
        } catch (Exception e) {
            Crawler_Service.username = i.getExtras().getString("username");
            Crawler_Service.password = i.getExtras().getString("password");
            Log.e("Crawler_Service", "entnehme Passwort2");
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.e("Crawler_Service", "pruefe NotificationVisibility");
                    if (!isNotificationVisible()) {
                        Log.e("Crawler_Service", "createNotificationStatus()");
                        createNotificationStatus();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        Log.e("Crawler_Service", "pruefe Zeit");
        LocalDateTime localdatetime = LocalDateTime.now();
        if (!(localdatetime.getHour() >= 1 && localdatetime.getHour() <= 5)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.e("Crawler_Service", "login aufruf");
                        HtmlPage grades = loginQIS();
                        Log.e("Crawler_Service", "checkgrades aufruf");
                        checkGrades(grades);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            Log.e("HU", "Login between 0 and 5 o'clock");
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.e("CrawlerService", "ichwurdegestoppt!");
        return true;
    }

    public HtmlPage loginQIS() throws Exception {
        HtmlPage grades = null;

        WebClient webClient = new WebClient();
        webClient.getOptions().setTimeout(30000);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setRedirectEnabled(true);
        Log.e("Crawler_Service", "login1");

        HtmlPage qis_login_page;
        HtmlPage hs_Login = webClient.getPage("https://qis.hochschule-trier.de/qisserver/rds?state=user&type=0&category=menu.browse&startpage=portal.vm");
        HtmlForm hs_login_form = hs_Login.getFormByName("login");
        HtmlTextInput hs_login_username = hs_login_form.getInputByName("j_username");
        HtmlPasswordInput hs_login_password = hs_login_form.getInputByName("j_password");
        hs_login_username.setValueAttribute(Crawler_Service.username);
        hs_login_password.setValueAttribute(Crawler_Service.password);
        HtmlButton button = hs_Login.getFirstByXPath("//button[@type='submit']");
        qis_login_page = button.click();
        Log.e("Crawler_Service", "login2");

        HtmlPage qis_homepage;
        HtmlForm qis_login_form = qis_login_page.getFormByName("loginform");
        HtmlTextInput qis_login_username = qis_login_form.getInputByName("asdf");
        HtmlPasswordInput qis_login_password = qis_login_form.getInputByName("fdsa");
        qis_login_username.setValueAttribute(Crawler_Service.username);
        qis_login_password.setValueAttribute(Crawler_Service.password);
        HtmlInput qis_login_button = qis_login_form.getInputByName("submit");
        qis_homepage = qis_login_button.click();
        Log.e("Crawler_Service", "login3");

        HtmlAnchor a = qis_homepage.getAnchorByText("Prüfungsverwaltung");
        HtmlPage b = a.click();
        HtmlAnchor c = b.getAnchorByText("Notenspiegel");
        HtmlPage d = c.click();
        HtmlAnchor e = d.getAnchorByText("Abschluss Bachelor of Science");
        HtmlPage f = e.click();
        HtmlAnchor g = f.getAnchorByText("Informatik - Digitale Medien und Spiele (PO-Version 2019)");
        grades = g.click();
        Log.e("Crawler_Service", "login4");

        webClient.close();
        return grades;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void checkGrades(HtmlPage grades) throws Exception {
        Log.e("Crawler_Service", "checke jetzt die grades");
        String grade_page_s = grades.asText().toString();
        BufferedReader reader = new BufferedReader(new StringReader(grade_page_s));
        Intent i = new Intent(this, Crawler_Service.class);

        Log.e("Crawler_Service", "jetzt parsen");
        int year = Integer.parseInt(LocalDate.now().format(DateTimeFormatter.ofPattern("yy")));
        String semester = "";
        if (LocalDate.now().getMonthValue() >= 10 || LocalDate.now().getMonthValue() <= 4) {
            Log.e("Crawler_Service", "1.if true");
            if(LocalDate.now().getMonthValue() >= 10 && LocalDate.now().getMonthValue() <= 12) {
                Log.e("Crawler_Service", "2.if true");
                semester = "WiSe " + year + "/" + (year+1);
            } else {
                Log.e("Crawler_Service", "2.if false");
                semester = "WiSe " + year + "/" + (year-1);
            }
        } else {
            Log.e("Crawler_Service", "1.if false");
            semester = "SoSe " + year;
        }

        //semester = "SoSe 21"; //temporär
        String s;
        String mod = "";

        Log.e("Crawler_Service", "ich while");
        while ((s = reader.readLine()) != null) {
            Log.e("Crawler_Service", "while schleife");
            if(((s.contains("BE") || s.contains("NB") || s.contains("NE")) && s.contains(semester)) && (!(mod.contains("PV") || mod.contains("Studienleistung")))) {
                Log.e("Crawler_Service", "whileif true");
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

                //sendPushNotification(semester, mod);
                Log.e("connectToDatabase", "jopp");

                i.putExtra("username", username);
                i.putExtra("password", password);

                i.getExtras().getString("username");
                i.getExtras().getString("password");

                if (connectToDatabase(semester+"|"+mod)) {
                    sendPushNotification(semester, mod);
                    Log.e("connectToDatabase", "jopp");
                } else {
                    Log.e("connectToDatabase", "nope");
                    sendPushNotification(semester, mod);
                }

            }/* else {
                Log.e("Crawler_Service", "whileif false");
            }*/
            mod = s;
        }
    }

    public void createNotificationStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Clinc8686", "Clinc8686", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Clinc8686")
                .setSmallIcon(R.mipmap.hochschulcrawlerlogoicon)
                .setContentTitle("Hochschul-Crawler-Service")
                .setContentText("Prüfe auf neue Noten.")
                .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Prüfe auf neue Noten."));

        NotificationManagerCompat maCom = NotificationManagerCompat.from(this);
        maCom.notify(1, builder.build());
    }

    public void sendPushNotification(String semester, String mod) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Clinc8686Action", "Clinc8686Action", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Clinc8686Action")
                .setSmallIcon(R.mipmap.hochschulcrawlerlogoicon)
                .setContentTitle("Hochschul-Crawler")
                .setContentText("Es sind neue Noten verfügbar!")
                .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Es sind neue Noten für das " + semester + " in " + mod + " verfügbar!"));

        NotificationManagerCompat maCom = NotificationManagerCompat.from(this);
        maCom.notify(2, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean isNotificationVisible () {
        boolean laeuft = false;
        NotificationManager manager = getSystemService(NotificationManager.class);
        StatusBarNotification[] sbn = manager.getActiveNotifications();
        for (StatusBarNotification x: sbn) {
            if (x.getNotification().getChannelId().equals("Clinc8686")) {
                laeuft = true;
            }
        }
        return laeuft;
    }

    public boolean connectToDatabase(String sem_mod) {
        SQLiteDatabase sqlgrade = openOrCreateDatabase("HochschulCrawlerGrades",MODE_PRIVATE,null);
        sqlgrade.execSQL("CREATE TABLE IF NOT EXISTS Grades(ID INTEGER PRIMARY KEY AUTOINCREMENT,SEMMOD TEXT NOT NULL);");
        Cursor resultSet = sqlgrade.rawQuery("Select SEMMOD from Grades WHERE SEMMOD = \'"+sem_mod+"\'",null);

        if (!resultSet.moveToFirst()) {
            sqlgrade.execSQL("INSERT INTO Grades (SEMMOD) VALUES(\""+sem_mod+"\");");
            Log.e("SQL-if", "nix");
            resultSet.close();
            //sqlgrade.close();
            return false;
        }

        resultSet.close();
        //sqlgrade.close();
        return true;
    }
}