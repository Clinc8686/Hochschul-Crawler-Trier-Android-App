package de.clinc8686.hochschul_crawler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.BufferedReader;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

    public class Crawler_Service extends BroadcastReceiver  {
        private static String password;
        private static String username;
        private static String hochschule;

        @Override
        public void onReceive(Context context, Intent intent) {
            getPreferences(context);

            new Thread(() -> {
                if (!Notification.isNotificationVisible(context.getApplicationContext())) {
                    new Notification(context.getApplicationContext(), "Hochschul Crawler", "", "");
                }
            }).start();

            LocalDateTime localdatetime = LocalDateTime.now();
            if (!(localdatetime.getHour() >= 1 && localdatetime.getHour() <= 5)) {
                new Thread(() -> {
                    Notification notification = new Notification(context.getApplicationContext(), "Prüfe neue Noten", "", "");
                    try {
                        Login login = new Login(context.getApplicationContext(), username, password, hochschule);
                        HtmlPage grades = login.loginQIS();
                        checkGrades(grades, context, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        notification.closeNotification();
                    }
                }).start();
            }
        }

        public static void checkGrades(HtmlPage grades, Context context, boolean firstlogin) throws Exception {
            String gradePageString = grades.asText();
            BufferedReader reader = new BufferedReader(new StringReader(gradePageString));

            int year = Integer.parseInt(LocalDate.now().format(DateTimeFormatter.ofPattern("yy")));
            String semester;
            if (LocalDate.now().getMonthValue() >= 10 || LocalDate.now().getMonthValue() <= 4) {
                if (LocalDate.now().getMonthValue() >= 10 && LocalDate.now().getMonthValue() <= 12) {
                    semester = "WiSe " + year + "/" + (year + 1);
                } else {
                    semester = "WiSe " + (year - 1) + "/" + year;
                }
            } else {
                semester = "SoSe " + year;
            }

            String stringLine;
            String mod = "";
            String pass;
            String grade;
            String sem;
            String modul;
            String modNum;

            while ((stringLine = reader.readLine()) != null) {
                if ((((stringLine.contains("BE") || stringLine.contains("NB")) || stringLine.contains("NE")) && (!(mod.contains("PV") || mod.contains("Studienleistung") || stringLine.contains("Modul:"))) && (stringLine.contains("WiSe") || stringLine.contains("SoSe")))) {
                    sem = replaceAndSplitWhitespaces(stringLine,1) + " " + replaceAndSplitWhitespaces(stringLine, 2);
                    grade = replaceAndSplitWhitespaces(stringLine,3);
                    pass = replaceAndSplitWhitespaces(stringLine, 4);
                    modul = mod.replaceAll("\\s+", " ").substring(mod.indexOf(" ") + 1);
                    modNum = replaceAndSplitWhitespaces(mod, 0);

                    Database database = new Database(context);
                    boolean newgrades = database.insertData(sem, modNum, modul, pass, grade, context);
                    if (newgrades && !firstlogin) {
                        new Notification(context,context.getString(R.string.neueNoten), semester, modNum + " " + modul);
                    }
                }
                mod = stringLine;
            }
        }

        private static String replaceAndSplitWhitespaces(String StringToModify, int position) {
            return StringToModify.replaceAll("\\s+", " ").split(" ")[position];
        }

        private void getPreferences(Context context) {
            SharedPreferences prefs = (context.getApplicationContext().getSharedPreferences((context.getApplicationContext().getResources().getString(R.string.app_name)), Context.MODE_PRIVATE));
            username = prefs.getString("username", "None");
            password = prefs.getString("password", "None");
            hochschule = prefs.getString("hochschule", "None");
        }

        public static void setData(String username, String password, String hochschule) {
            Crawler_Service.username = username;
            Crawler_Service.password = password;
            Crawler_Service.hochschule = hochschule;
        }
    }
