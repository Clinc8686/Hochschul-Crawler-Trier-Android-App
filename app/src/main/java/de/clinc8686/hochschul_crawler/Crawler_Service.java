package de.clinc8686.hochschul_crawler;

import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;

import android.annotation.SuppressLint;
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
                        createNotificationChannel("Hochschul-Crawler", "", "");
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
                        createNotificationChannel("Prüfe neue Noten", "", "");
                        HtmlPage grades = loginQIS();
                        Log.e("Crawler_Service", "checkgrades aufruf");
                        checkGrades(grades);
                        closeNotification();
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

    @SuppressLint("SetJavaScriptEnabled")
    public static HtmlPage loginQIS() throws Exception {
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
        if (hs_Login.asText().contains("gesperrt")) {
            webClient.close();
            throw new IllegalAccessException("gesperrt");
        }
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
        HtmlAnchor e = d.getFirstByXPath("//a[starts-with(@class, 'regular') and contains(text(),'Abschluss')]");
        HtmlPage f = e.click();
        HtmlAnchor g = f.getFirstByXPath("//a[starts-with(@href, 'https://qis.hochschule-trier.de') and starts-with (@title, 'Leistungen für')]");
        grades = g.click();
        Log.e("Crawler_Service", "login4");

        webClient.close();
        return grades;
    }

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

                Log.e("connectToDatabase", "jopp");

                i.putExtra("username", username);
                i.putExtra("password", password);

                i.getExtras().getString("username");
                i.getExtras().getString("password");

                createNotificationChannel("Neue Noten", semester, mod);
                connectToDatabase(semester+"|"+mod);
            }
            mod = s;
        }
    }

    public void createNotificationChannel(String channel, String semester, String mod) {
        NotificationChannel NChannel;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel)
                .setSmallIcon(R.mipmap.hochschulcrawlerlogoicon)
                .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        NotificationManagerCompat maCom = NotificationManagerCompat.from(this);
        NotificationManager manager = getSystemService(NotificationManager.class);

        switch (channel) {
            case "Hochschul-Crawler":
                NChannel = new NotificationChannel(channel, channel, NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(NChannel);

                builder.setContentTitle("Hochschul-Crawler-Service")
                        .setContentText("Hochschul-Crawler läuft im Hintergrund.")
                        .setOngoing(true)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Hochschul-Crawler läuft im Hintergrund."));

                maCom.notify(54295, builder.build());
                break;
            case "Neue Noten":
                NChannel = new NotificationChannel(channel, channel, NotificationManager.IMPORTANCE_HIGH);
                manager.createNotificationChannel(NChannel);

                builder.setContentTitle("Hochschul-Crawler")
                        .setContentText("Es sind neue Noten verfügbar!")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Es sind neue Noten für das " + semester + " in " + mod + " verfügbar!"));

                maCom.notify(54296, builder.build());
                break;
            case "Prüfe neue Noten":
                NChannel = new NotificationChannel(channel, channel, NotificationManager.IMPORTANCE_LOW);
                manager.createNotificationChannel(NChannel);

                builder.setContentTitle("Hochschul-Crawler-Sync")
                        .setContentText("Prüfe auf neue Noten")
                        .setAutoCancel(true);

                maCom.notify(54297, builder.build());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + channel);
        }
    }

    private void closeNotification() {
        try {
            NotificationManagerCompat maCom = NotificationManagerCompat.from(Crawler_Service.this);
            maCom.cancel(54297);
        } catch (Exception e) {

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean isNotificationVisible () {
        boolean laeuft = false;
        NotificationManager manager = getSystemService(NotificationManager.class);
        StatusBarNotification[] sbn = manager.getActiveNotifications();
        for (StatusBarNotification x: sbn) {
            if (x.getNotification().getChannelId().equals("Hochschul-Crawler")) {
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
            return false;
        }

        resultSet.close();
        return true;
    }
}