package de.clinc8686.hochschul_crawler;

import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import java.io.BufferedReader;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Crawler_Service extends BroadcastReceiver {
    private static String password;
    private static String username;
    private static String hochschule;
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        getPreferences();

        new Thread(() -> {
            try {
                if (!isNotificationVisible()) {
                    createNotificationChannel("Hochschul-Crawler", "", "");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        LocalDateTime localdatetime = LocalDateTime.now();
        if (!(localdatetime.getHour() >= 1 && localdatetime.getHour() <= 5)) {
            new Thread(() -> {
                try {
                    createNotificationChannel("Prüfe neue Noten", "", "");
                    HtmlPage grades = loginQIS();
                    checkGrades(grades);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    closeNotification();
                }
            }).start();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public static HtmlPage loginQIS() throws Exception {
        HtmlPage grades;

        WebClient webClient = new WebClient();
        webClient.getOptions().setTimeout(30000);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setRedirectEnabled(true);

        HtmlPage qis_login_page = null;
        if(hochschule.equals("checkBoxTrier")) {
            HtmlPage hs_Login = webClient.getPage("https://qis.hochschule-trier.de/qisserver/rds?state=user&type=0&category=menu.browse&startpage=portal.vm");
            if (hs_Login.asText().contains("gesperrt")) {
                webClient.close();
                throw new TooManyFalseLoginException("gesperrt");
            }
            HtmlForm hs_login_form = hs_Login.getFormByName("login");
            HtmlTextInput hs_login_username = hs_login_form.getInputByName("j_username");
            HtmlPasswordInput hs_login_password = hs_login_form.getInputByName("j_password");
            hs_login_username.setValueAttribute(Crawler_Service.username);
            hs_login_password.setValueAttribute(Crawler_Service.password);
            //HtmlButton button = null; //= hs_Login.getFirstByXPath("//*[@type='submit']");
            HtmlButton button = hs_Login.getFirstByXPath("//button[@type='submit']");
            qis_login_page = button.click();
        } else if (hochschule.equals("checkBoxAachen")) {
            qis_login_page = webClient.getPage("https://www.qis.fh-aachen.de/");
        } else if (hochschule.equals("checkBoxKoblenz")) {
            qis_login_page = webClient.getPage("https://qisserver.hs-koblenz.de/");
        }

        HtmlPage qis_homepage = null;
        HtmlForm qis_login_form = qis_login_page.getFormByName("loginform");
        HtmlTextInput qis_login_username = qis_login_form.getInputByName("asdf");
        HtmlPasswordInput qis_login_password = qis_login_form.getInputByName("fdsa");
        qis_login_username.setValueAttribute(Crawler_Service.username);
        qis_login_password.setValueAttribute(Crawler_Service.password);

        HtmlButton qis_login_button = null;
        HtmlSubmitInput qis_login_submitbutton = null;
        if ((boolean) qis_login_form.getFirstByXPath(("count(//button[@type='submit']) > 0"))) {
            qis_login_button = qis_login_form.getFirstByXPath("//button[@type='submit']");
            qis_homepage = qis_login_button.click();
        } else if ((boolean) qis_login_form.getFirstByXPath(("count(//button[@type='submit']) > 0"))) {
            qis_login_button = qis_login_form.getFirstByXPath("//button[@type='submit']");
            qis_homepage = qis_login_button.click();
        } else if ((boolean) qis_login_form.getFirstByXPath(("count(//*[@type='submit']) > 0"))) {
            qis_login_submitbutton = qis_login_form.getFirstByXPath("//*[@type='submit']");
            qis_homepage = qis_login_submitbutton.click();
        }
        if (!qis_homepage.asText().contains("Sie sind angemeldet als")) {
            throw new Exception("nicht angemeldet");
        }

        int counter = 0;
        boolean performanceRecord = true, examinationManagement = true, attainment = true, graduation = true;
        while (counter <= 3 && !qis_homepage.asText().contains("Name des Studierenden")) {
            if (performanceRecord && ((boolean) qis_homepage.getFirstByXPath("count(//a[starts-with(@href, 'https://qis.hochschule-trier.de') and text()='Notenspiegel']) > 0"))) {
                //HtmlAnchor x = qis_homepage.getAnchorByText("Notenspiegel");
                HtmlAnchor x = qis_homepage.getFirstByXPath("//a[starts-with(@href, 'https://qis.hochschule-trier.de') and text()='Notenspiegel']");
                qis_homepage = x.click();
                performanceRecord = false;
            } else if ((boolean) qis_homepage.getFirstByXPath("count(//a[starts-with(@href, 'https://qis.hochschule-trier.de') and starts-with (@title, 'Leistungen für')]) > 0")) {
                HtmlAnchor x = qis_homepage.getFirstByXPath("//a[starts-with(@href, 'https://qis.hochschule-trier.de') and starts-with (@title, 'Leistungen für')]");
                qis_homepage = x.click();
                examinationManagement = false;
            } else if (attainment && (examinationManagement && ((boolean) qis_homepage.getFirstByXPath("count(//a[starts-with(@class, 'regular') and contains(text(),'Abschluss')]) > 0")))) {
                HtmlAnchor x = qis_homepage.getFirstByXPath("//a[starts-with(@class, 'regular') and contains(text(),'Abschluss')]");
                qis_homepage = x.click();
                attainment = false;
            } else if (graduation && (qis_homepage.asText().contains("Prüfungsverwaltung"))) {
                HtmlAnchor x = qis_homepage.getAnchorByText("Prüfungsverwaltung");
                qis_homepage = x.click();
                graduation = false;
            }
            counter++;
        } if (qis_homepage.asText().contains("Name des Studierenden")) {
            grades = qis_homepage;
        } else {
            throw new Exception("konnte keinem Link folgen");
        }

        webClient.close();
        return grades;
    }

    private void checkGrades(HtmlPage grades) throws Exception {
        String gradePageString = grades.asText();
        BufferedReader reader = new BufferedReader(new StringReader(gradePageString));

        int year = Integer.parseInt(LocalDate.now().format(DateTimeFormatter.ofPattern("yy")));
        String semester;
        if (LocalDate.now().getMonthValue() >= 10 || LocalDate.now().getMonthValue() <= 4) {
            if(LocalDate.now().getMonthValue() >= 10 && LocalDate.now().getMonthValue() <= 12) {
                semester = "WiSe " + year + "/" + (year+1);
            } else {
                semester = "WiSe " + (year-1) + "/" + year;
            }
        } else {
            semester = "SoSe " + year;
        }

        String stringLine;
        String mod = "";

        while ((stringLine = reader.readLine()) != null) {
            if(((stringLine.contains("BE") || stringLine.contains("NB") || stringLine.contains("NE")) && stringLine.contains(semester)) && (!(mod.contains("PV") || mod.contains("Studienleistung")))) {
                mod = mod.replace("\t", " ");
                mod = mod.replace("  ", " ");

                stringLine = stringLine.replace("\t", " ");
                stringLine = stringLine.replace("  ", " ");
                //String[] s_splitted = s.split("\\s+");

                /*String mod_reg = mod;
                mod_reg = mod_reg.replace("ä", "ae");
                mod_reg = mod_reg.replace("ö", "oe");
                mod_reg = mod_reg.replace("ü", "ue");
                mod_reg = mod_reg.replace("Ä", "AE");
                mod_reg = mod_reg.replace("Ö", "OE");
                mod_reg = mod_reg.replace("Ü", "UE");*/

                if (connectToDatabase(semester+"|"+mod)) {
                    createNotificationChannel("Neue Noten", semester, mod);
                }
            }
            mod = stringLine;
        }
    }

    private void createNotificationChannel(String channel, String semester, String mod) {
        NotificationChannel notificationChannel;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel)
                .setSmallIcon(R.mipmap.hochschulcrawlerlogoicon)
                .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        NotificationManager manager = context.getSystemService(NotificationManager.class);

        switch (channel) {
            case "Hochschul-Crawler":
                notificationChannel = new NotificationChannel(channel, channel, NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(notificationChannel);

                builder.setContentTitle("Hochschul-Crawler-Service")
                        .setContentText("Hochschul-Crawler läuft im Hintergrund.")
                        .setOngoing(true)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Hochschul-Crawler läuft im Hintergrund."));

                manager.notify(54295, builder.build());
                break;
            case "Neue Noten":
                notificationChannel = new NotificationChannel(channel, channel, NotificationManager.IMPORTANCE_HIGH);
                manager.createNotificationChannel(notificationChannel);

                builder.setContentTitle("Hochschul-Crawler")
                        .setContentText("Es sind neue Noten verfügbar!")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Es sind neue Noten für das " + semester + " in " + mod + " verfügbar!"));

                manager.notify(54296, builder.build());
                break;
            case "Prüfe neue Noten":
                notificationChannel = new NotificationChannel(channel, channel, NotificationManager.IMPORTANCE_LOW);
                manager.createNotificationChannel(notificationChannel);

                builder.setContentTitle("Hochschul-Crawler-Sync")
                        .setContentText("Prüfe auf neue Noten")
                        .setProgress(0, 0, true)
                        .setAutoCancel(true);

                manager.notify(54297, builder.build());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + channel);
        }
    }

    private void closeNotification() {
        try {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(54297);
        } catch (Exception ignored) {}
    }

    private boolean isNotificationVisible () {
        boolean isRunning = false;
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        StatusBarNotification[] sbn = notificationManager.getActiveNotifications();
        for (StatusBarNotification notification: sbn) {
            if (notification.getNotification().getChannelId().equals("Hochschul-Crawler")) {
                isRunning = true;
            }
        }
        return isRunning;
    }

    private boolean connectToDatabase(String sem_mod) {
        SQLiteDatabase sqlgrade = context.openOrCreateDatabase("HochschulCrawlerGrades", Context.MODE_PRIVATE,null);
        sqlgrade.execSQL("CREATE TABLE IF NOT EXISTS Grades(ID INTEGER PRIMARY KEY AUTOINCREMENT,SEMMOD TEXT NOT NULL);");
        Cursor resultSet = sqlgrade.rawQuery("Select SEMMOD from Grades WHERE SEMMOD = \'"+sem_mod+"\'",null);

        if (!resultSet.moveToFirst()) {
            sqlgrade.execSQL("INSERT INTO Grades (SEMMOD) VALUES(\""+sem_mod+"\");");
            resultSet.close();
            return true;
        }
        resultSet.close();
        return false;
    }

    private void getPreferences() {
        SharedPreferences prefs = context.getSharedPreferences(context.getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
        username = prefs.getString("username", "None");//"None" is the default value.
        password = prefs.getString("password", "None");//"None" is the default value.
        hochschule = prefs.getString("hochschule", "None");
    }

    public static void setData(String username, String password, String hochschule) {
        Crawler_Service.username = username;
        Crawler_Service.password = password;
        Crawler_Service.hochschule = hochschule;
    }
}