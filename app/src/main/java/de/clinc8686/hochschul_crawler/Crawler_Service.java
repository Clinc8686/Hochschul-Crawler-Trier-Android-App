package de.clinc8686.hochschul_crawler;

import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedList;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Crawler_Service extends JobService {
    public static String password;
    public static String username;
    public static LinkedList<String> gradelist = new LinkedList<>();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Crawler_Service.password = MainActivity.password;
        Crawler_Service.username = MainActivity.username;

        LocalDateTime localdatetime = LocalDateTime.now();
        if (!(localdatetime.getHour() >= 1 && localdatetime.getHour() <= 5)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        HtmlPage grades = loginQIS();
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

        HtmlPage qis_login_page;
        HtmlPage hs_Login = webClient.getPage("https://qis.hochschule-trier.de/qisserver/rds?state=user&type=0&category=menu.browse&startpage=portal.vm");
        HtmlForm hs_login_form = hs_Login.getFormByName("login");
        HtmlTextInput hs_login_username = hs_login_form.getInputByName("j_username");
        HtmlPasswordInput hs_login_password = hs_login_form.getInputByName("j_password");
        hs_login_username.setValueAttribute(Crawler_Service.username);
        hs_login_password.setValueAttribute(Crawler_Service.password);
        HtmlButton button = hs_Login.getFirstByXPath("//button[@type='submit']");
        qis_login_page = button.click();

        HtmlPage qis_homepage;
        HtmlForm qis_login_form = qis_login_page.getFormByName("loginform");
        HtmlTextInput qis_login_username = qis_login_form.getInputByName("asdf");
        HtmlPasswordInput qis_login_password = qis_login_form.getInputByName("fdsa");
        qis_login_username.setValueAttribute(Crawler_Service.username);
        qis_login_password.setValueAttribute(Crawler_Service.password);
        HtmlInput qis_login_button = qis_login_form.getInputByName("submit");
        qis_homepage = qis_login_button.click();

        HtmlAnchor a = qis_homepage.getAnchorByText("Prüfungsverwaltung");
        HtmlPage b = a.click();
        HtmlAnchor c = b.getAnchorByText("Notenspiegel");
        HtmlPage d = c.click();
        HtmlAnchor e = d.getAnchorByText("Abschluss Bachelor of Science");
        HtmlPage f = e.click();
        HtmlAnchor g = f.getAnchorByText("Informatik - Digitale Medien und Spiele (PO-Version 2019)");
        grades = g.click();

        webClient.close();
        return grades;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
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

                //if (!gradelist.contains(semester+"|"+mod)) {
                  //  gradelist.add(semester+"|"+mod);
                    sendPushNotification(semester, mod);
                //}

            }
            mod = s;
        }
    }

    public void sendPushNotification(String semester, String mod) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Clinc8686", "Clinc8686", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(Crawler_Service.this, "Clinc8686")
                .setSmallIcon(R.mipmap.hochschulcrawlerlogoicon)
                .setContentTitle("Hochschul-Crawler")
                .setContentText("Es sind neue Noten verfügbar!")
                .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Es sind neue Noten für das " + semester + " in " + mod + " verfügbar!"));

        NotificationManagerCompat maCom = NotificationManagerCompat.from(Crawler_Service.this);
        maCom.notify(1, builder.build());
    }
}