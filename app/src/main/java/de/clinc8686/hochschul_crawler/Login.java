package de.clinc8686.hochschul_crawler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;


import java.io.IOException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

import de.clinc8686.hochschul_crawler.crawler.Crawler_Service;
import de.clinc8686.hochschul_crawler.ui.HomeFragment;

public class Login {
    private final Context context;
    private final String username;
    private final String password;

    public Login(Context context, String username, String password) {
        this.context = context;
        this.username = username;
        this.password = password;
        Log.e("QISLogin", username + " " + password);
    }

    public void storeAndEncrypt(int value, Activity activity) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        int KEY_SIZE = 128;
        keyGenerator.init(KEY_SIZE);
        Key key = keyGenerator.generateKey();
        String encryptUsername = encrypt(username, key);
        String encryptPassword = encrypt(password, key);
        storePreferences(value, activity, encryptUsername, encryptPassword);
        Crawler_Service.setData(username, password);
    }

    private void storePreferences(int value, Activity activity, String encryptUsername, String encryptPassword) {
        SharedPreferences.Editor editor = activity.getSharedPreferences("de.clinc8686.qishochschulcrawler", Context.MODE_PRIVATE).edit();
        editor.putString("username", encryptUsername);
        editor.putString("password", encryptPassword);
        editor.putInt("interval", value);
        editor.apply();
    }

    public String encrypt(String data, Key key) throws Exception {
        byte[] dataInBytes = data.getBytes();
        Cipher encryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
        encryptionCipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = encryptionCipher.doFinal(dataInBytes);
        return encode(encryptedBytes);
    }

    private String encode(byte[] data) {
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    public static void loginsuccess(View view, Activity activitiy) {
        activitiy.runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                TextView loggingstatus_text = view.findViewById(R.id.loggingstatus_text);
                loggingstatus_text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                loggingstatus_text.setTextColor(Color.rgb(10, 82, 0));
                loggingstatus_text.setText(R.string.logged_in);

                TextView appCloseText = view.findViewById(R.id.appCloseText);
                appCloseText.setVisibility(View.VISIBLE);

                view.findViewById(R.id.progressBarLogin).setVisibility(View.GONE);
                view.findViewById(R.id.radioGroup).setVisibility(View.GONE);
                view.findViewById(R.id.seekBar).setVisibility(View.GONE);
                view.findViewById(R.id.intervall_text).setVisibility(View.GONE);
                view.findViewById(R.id.loginHint).setVisibility(View.INVISIBLE);

                HomeFragment.loginSuccess.setVariable(true);
                Button btn_login = view.findViewById(R.id.btn_login);
                btn_login.setText(R.string.logout);
            }});
    }

    @SuppressLint("SetJavaScriptEnabled")
    public HtmlPage loginQIS() throws Exception {
        HtmlPage grades;
        WebClient webClient = createWebClient();
        HtmlPage hs_Login = webClient.getPage("https://qis.hochschule-trier.de/qisserver/rds?state=user&type=0&category=menu.browse&startpage=portal.vm");
        if (hs_Login.asText().contains("gesperrt")) {
            webClient.closeAllWindows();
            throw new TooManyFalseLoginException("gesperrt");
        }
        Log.e("QISLogin3", username + " " + password);
        int counter = 0;
        while (counter <= 3) {
            hs_Login = searchLoginForm(hs_Login);
            if (hs_Login.asText().contains("Sie sind angemeldet als")) {
                break;
            }
            counter++;
        }

        if (!hs_Login.asText().contains("Sie sind angemeldet als")) {
            throw new Exception("nicht angemeldet");
        }

        HtmlPage qis_homepage = hs_Login;

        counter = 0;
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
        }
        if (qis_homepage.asText().contains("Name des Studierenden")) {
            grades = qis_homepage;
        } else {
            throw new Exception("konnte keinem Link folgen");
        }

        webClient.closeAllWindows();
        return grades;
    }

    private HtmlPage searchLoginForm(HtmlPage hs_Login) throws Exception {
        Log.e("QISLogin2", username + " " + password);
        if (hs_Login.getFirstByXPath("//form[contains(@name, 'login')]") != null) {
            HtmlForm hs_login_form = hs_Login.getFirstByXPath("//form[contains(@name, 'login')]");
            fillForm(hs_login_form);
            return submitForm(hs_login_form);
        } else {
            throw new NoSuchFieldException("Form not found!");
        }
    }

    private HtmlPage submitForm(HtmlForm hs_login_form) throws NoSuchFieldException, IOException {
        HtmlPage qis_homepage = null;
        HtmlButton qis_login_button = null;
        HtmlSubmitInput qis_login_submitbutton = null;
        if ((boolean) hs_login_form.getFirstByXPath(("count(//button[@type='submit']) > 0"))) {
            qis_login_button = hs_login_form.getFirstByXPath("//button[@type='submit']");
            qis_homepage = qis_login_button.click();
        } else if ((boolean) hs_login_form.getFirstByXPath(("count(//*[@type='submit']) > 0"))) {
            qis_login_submitbutton = hs_login_form.getFirstByXPath("//*[@type='submit']");
            qis_homepage = qis_login_submitbutton.click();
        } else {
            throw new NoSuchFieldException("Submit button not found!");
        }
        return qis_homepage;
    }

    private void fillForm(HtmlForm hs_login_form) throws NoSuchFieldException {
        HtmlTextInput hs_login_username;
        HtmlPasswordInput hs_login_password;
        if (hs_login_form.getFirstByXPath("//input[contains(@name, 'username')]") != null || hs_login_form.getFirstByXPath("//input[contains(@name, 'password')]") != null) {
            hs_login_username = hs_login_form.getFirstByXPath("//input[contains(@name, 'username')]");
            hs_login_password = hs_login_form.getFirstByXPath("//input[contains(@name, 'password')]");
        } else if (hs_login_form.getFirstByXPath("//input[contains(@name, 'asdf')]") != null || hs_login_form.getFirstByXPath("//input[contains(@name, 'fdsa')]") != null) {
            hs_login_username = hs_login_form.getFirstByXPath("//input[contains(@name, 'asdf')]");
            hs_login_password = hs_login_form.getFirstByXPath("//input[contains(@name, 'fdsa')]");
        } else {
            throw new NoSuchFieldException("username or password not found!");
        }
        Log.e("QIS", username + " " + password);
        hs_login_username.setValueAttribute(username);
        hs_login_password.setValueAttribute(password);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private WebClient createWebClient() {
        WebClient webClient = new WebClient();
        webClient.getOptions().setTimeout(50000);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.waitForBackgroundJavaScript(50000);
        return webClient;
    }
}
