package de.clinc8686.hochschul_crawler.crawler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;
import android.util.Log;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;

import de.clinc8686.hochschul_crawler.grades.Database;
import de.clinc8686.hochschul_crawler.Login;
import de.clinc8686.hochschul_crawler.Notification;

public class Crawler_Service extends BroadcastReceiver  {
    private static String password;
    private static String username;

    @Override
    public void onReceive(Context context, Intent intent) {
        getPreferences(context);
        Log.e("QISlogin", username + " " + password);

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
                    Log.e("QISInfox", "55 " + username + " " + password);
                    Login login = new Login(context.getApplicationContext(), username, password);
                    Log.e("QISInfo", "66 " + username + " " + password);
                    HtmlPage grades = login.loginQIS();
                    Log.e("QISInfo", "77");
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
        Log.e("QISInfo", "1");
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
        Log.e("QISInfo", "2");
        while ((stringLine = reader.readLine()) != null) {
            if ((((stringLine.contains("BE") || stringLine.contains("NB")) || stringLine.contains("NE")) && (!(mod.contains("PV") || mod.contains("Studienleistung") || stringLine.contains("Modul:"))) && (stringLine.contains("WiSe") || stringLine.contains("SoSe")))) {
                sem = replaceAndSplitWhitespaces(stringLine,1) + " " + replaceAndSplitWhitespaces(stringLine, 2);
                grade = replaceAndSplitWhitespaces(stringLine,3);
                pass = replaceAndSplitWhitespaces(stringLine, 4);
                modul = mod.replaceAll("\\s+", " ").substring(mod.indexOf(" ") + 1);
                modNum = replaceAndSplitWhitespaces(mod, 0);

                Database database = new Database(context);
                boolean newgrades = database.insertData(sem, modNum, modul, pass, grade, context);
                Log.e("QISInfo", modul + " " + sem + " " + newgrades + firstlogin);
                if (newgrades && !firstlogin) {
                    new Notification(context,"Neue Noten", semester, modNum + " " + modul);
                }
                //test
                SQLiteDatabase sqlgrade = (context.getApplicationContext().openOrCreateDatabase("HochschulCrawlerGrades", Context.MODE_PRIVATE, null));
                sqlgrade.execSQL("DELETE FROM Grades WHERE MODUL = 'Software-Entwurf' AND SEMESTER = 'WiSe 22/23'");
            }
            mod = stringLine;
        }
    }

    private static String replaceAndSplitWhitespaces(String StringToModify, int position) {
        return StringToModify.replaceAll("\\s+", " ").split(" ")[position];
    }

    private void getPreferences(Context context) {
        SharedPreferences prefs = (context.getApplicationContext().getSharedPreferences("de.clinc8686.qishochschulcrawler", Context.MODE_PRIVATE));
        String encryptedUsername = prefs.getString("username", "None");
        String encryptedPassword = prefs.getString("password", "None");

        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            String KEY_ALIAS = "Q1S_HS#Trier4L14S";
            Key key = keyStore.getKey(KEY_ALIAS, null);
            username = decrypt(encryptedUsername, key);
            password = decrypt(encryptedPassword, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] decode(String data) {
        return Base64.decode(data, Base64.DEFAULT);
    }

    public String decrypt(String encryptedData, Key key) throws Exception {
        byte[] dataInBytes = decode(encryptedData);
        Cipher decryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
        int DATA_LENGTH = 128;
        GCMParameterSpec spec = new GCMParameterSpec(DATA_LENGTH, decryptionCipher.getIV());
        decryptionCipher.init(Cipher.DECRYPT_MODE, key, spec);
        byte[] decryptedBytes = decryptionCipher.doFinal(dataInBytes);
        return new String(decryptedBytes);
    }

    public static void setData(String username, String password) {
        Crawler_Service.username = username;
        Crawler_Service.password = password;
    }
}
