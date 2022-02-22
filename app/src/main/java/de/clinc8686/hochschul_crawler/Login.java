package de.clinc8686.hochschul_crawler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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

public class Login {
    private final Context context;
    private final String username;
    private final String password;
    private final String hochschule;

    Login(Context context, String username, String password, String checkbox) {
        this.context = context;
        this.username = username;
        this.password = password;
        this.hochschule = checkbox;
    }

    public void storePreferences(int value, Activity activity) {
        SharedPreferences.Editor editor = activity.getSharedPreferences(context.getResources().getString(R.string.app_name), Context.MODE_PRIVATE).edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.putString("hochschule", hochschule);
        editor.putInt("interval", value);
        editor.apply();

        Crawler_Service.setData(username, password, hochschule);
    }

    static void loginsuccess(View view, Activity activitiy) {
        activitiy.runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                TextView loggingstatus_text = view.findViewById(R.id.loggingstatus_text);
                loggingstatus_text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                loggingstatus_text.setTextColor(Color.GREEN);
                loggingstatus_text.setText(R.string.logged_in);

                TextView appCloseText = view.findViewById(R.id.appCloseText);
                appCloseText.setVisibility(View.VISIBLE);

                view.findViewById(R.id.progressBarLogin).setVisibility(View.GONE);
                view.findViewById(R.id.radioGroup).setVisibility(View.GONE);
                view.findViewById(R.id.seekBar).setVisibility(View.GONE);
                view.findViewById(R.id.intervall_text).setVisibility(View.GONE);
                view.findViewById(R.id.loginHint).setVisibility(View.INVISIBLE);

                HomeFragment.loginsucess = true;
                Button btn_login = view.findViewById(R.id.btn_login);
                btn_login.setText(R.string.logout);
            }});
    }

    @SuppressLint("SetJavaScriptEnabled")
    public HtmlPage loginQIS() throws Exception {
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);
        HtmlPage grades;

        WebClient webClient = createWebClient();
        HtmlPage qis_login_page = null;
        switch (hochschule) {
            case "checkBoxTrier":
                HtmlPage hs_Login = webClient.getPage("https://qis.hochschule-trier.de/qisserver/rds?state=user&type=0&category=menu.browse&startpage=portal.vm");
                if (hs_Login.asText().contains("gesperrt")) {
                    webClient.close();
                    throw new TooManyFalseLoginException("gesperrt");
                }
                HtmlForm hs_login_form = hs_Login.getFormByName("login");
                HtmlTextInput hs_login_username = hs_login_form.getInputByName("j_username");
                HtmlPasswordInput hs_login_password = hs_login_form.getInputByName("j_password");
                hs_login_username.setValueAttribute(username);
                hs_login_password.setValueAttribute(password);
                //HtmlButton button = null; //= hs_Login.getFirstByXPath("//*[@type='submit']");
                HtmlButton button = hs_Login.getFirstByXPath("//button[@type='submit']");
                qis_login_page = button.click();
                break;
            case "checkBoxAachen":
                qis_login_page = webClient.getPage("https://www.qis.fh-aachen.de/");
                break;
            case "checkBoxKoblenz":
                qis_login_page = webClient.getPage("https://qisserver.hs-koblenz.de/");
                break;
        }

        HtmlPage qis_homepage = null;
        HtmlForm qis_login_form = qis_login_page.getFormByName("loginform");
        HtmlTextInput qis_login_username = qis_login_form.getInputByName("asdf");
        HtmlPasswordInput qis_login_password = qis_login_form.getInputByName("fdsa");
        qis_login_username.setValueAttribute(username);
        qis_login_password.setValueAttribute(password);

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
        }
        if (qis_homepage.asText().contains("Name des Studierenden")) {
            grades = qis_homepage;
        } else {
            throw new Exception("konnte keinem Link folgen");
        }

        webClient.close();
        return grades;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private WebClient createWebClient() {
        WebClient webClient = new WebClient();
        webClient.getOptions().setTimeout(30000);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.waitForBackgroundJavaScript(5000);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        return webClient;
    }
}
