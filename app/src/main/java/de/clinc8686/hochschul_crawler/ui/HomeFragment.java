package de.clinc8686.hochschul_crawler.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.time.LocalDateTime;
import java.time.ZoneId;

import de.clinc8686.hochschul_crawler.Login;
import de.clinc8686.hochschul_crawler.LoginSuccess;
import de.clinc8686.hochschul_crawler.Notification;
import de.clinc8686.hochschul_crawler.R;
import de.clinc8686.hochschul_crawler.TooManyFalseLoginException;
import de.clinc8686.hochschul_crawler.crawler.Alarm;
import de.clinc8686.hochschul_crawler.crawler.AlertBuilder;
import de.clinc8686.hochschul_crawler.crawler.BootCompletedReceiver;
import de.clinc8686.hochschul_crawler.crawler.BootLoader;
import de.clinc8686.hochschul_crawler.crawler.Crawler_Service;
import de.clinc8686.hochschul_crawler.grades.Database;

public class HomeFragment extends Fragment {
    private static String password;
    private static String username;
    public static LoginSuccess loginSuccess = new LoginSuccess(false);
    public static int value = 60;
    private ProgressBar progressBarLogin;
    private RadioGroup radioGroup;
    private long timestampTimeout = 0;
    private String checkbox = "checkBoxTrier";
    private EditText et_name;
    private Button btn_login;
    private Login login;
    private TextView text_qis_abschaltung;
    private TextView loginHint;
    private View view;
    private TextView text_seekbar_minute;
    private SeekBar seekBar;
    private TextView intervall_text;
    public static BootCompletedReceiver bootCompletedReceiver;
    private final String BOOT_COMPLETED_RECEIVER = "bootCompletedReceiver";


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.view = inflater.inflate(R.layout.fragment_login, container, false);

        if (savedInstanceState != null) {
            bootCompletedReceiver = (BootCompletedReceiver) savedInstanceState.getSerializable(BOOT_COMPLETED_RECEIVER);
        }

        getLayoutObjects();
        new DisplayHomeMetrics(getContext(), view);
        boolean service_status = Alarm.checkAlarm(getActivity().getApplicationContext());
        if (service_status) {
            HomeFragment.loginSuccess.setVariable(true);
            btn_login.setText("Logout");
            Login.loginsuccess(view, getActivity());
            SharedPreferences prefs = (getContext().getSharedPreferences("de.clinc8686.qishochschulcrawler", Context.MODE_PRIVATE));
            HomeFragment.value = prefs.getInt("interval", 0);
            text_seekbar_minute.setText(getString(R.string.All) + " " + HomeFragment.value + " " + getString(R.string.MinutesToUpdate) +
                    getString(R.string.EstimatedUsage) + dataUsage());
        }

        if (!HomeFragment.loginSuccess.getVariable()) {
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        getActivity().finishAndRemoveTask();
                        break;
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.headerPrivacypolicy)
                    .setMessage(getString(R.string.privacypolicy))
                    .setPositiveButton(R.string.agreement, dialogClickListener)
                    .setNegativeButton(R.string.decline, dialogClickListener);

            AlertDialog dialog = builder.create();
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.rgb(0, 158, 40));
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.rgb(156, 0, 13));
                }
            });
            dialog.show();
        }

        SeekBar seekBar = view.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @SuppressWarnings("FieldMayBeFinal")

            @Override
            public void onProgressChanged(SeekBar seekBar, int progessValue, boolean b) {
                HomeFragment.value = progessValue;
                text_seekbar_minute.setText(getString(R.string.All) + " " + HomeFragment.value + " " +getString(R.string.MinutesToUpdate) +
                        getString(R.string.EstimatedUsage) + " " + dataUsage());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                text_seekbar_minute.setText(getString(R.string.All) + " " + HomeFragment.value + " " + getString(R.string.MinutesToUpdate) +
                        getString(R.string.EstimatedUsage) + " " + dataUsage());
            }
        });

        return view;
    }

    @SuppressLint("DefaultLocale")
    private String dataUsage() {
        double calculatedDataUsage = ((170.0 * (((60.0 / HomeFragment.value) * 19.0) * 30.0)) / 1000.0);
        if (calculatedDataUsage >= 1000) {
            calculatedDataUsage = calculatedDataUsage / 1000.0;
            return String.format("%.2f", calculatedDataUsage) + " GByte";
        } else {
            return (int) calculatedDataUsage + " MByte";
        }
    }

    private void getLayoutObjects() {
        progressBarLogin = view.findViewById(R.id.progressBarLogin);
        btn_login = view.findViewById(R.id.btn_login);
        et_name = view.findViewById(R.id.et_name);
        text_qis_abschaltung = view.findViewById(R.id.text_qis_abschaltung);
        radioGroup = view.findViewById(R.id.radioGroup);
        text_seekbar_minute = view.findViewById(R.id.text_seekbar_minute);
        loginHint = view.findViewById(R.id.loginHint);
        seekBar = view.findViewById(R.id.seekBar);
        intervall_text = view.findViewById(R.id.intervall_text);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void checkFirstLogin(Login login) {
        new Thread(() -> {
            try {
                long difftime = System.currentTimeMillis() - timestampTimeout;
                if (difftime < 300000) {
                    throw new TooManyFalseLoginException("gesperrt - timeout");
                }

                HtmlPage grades = login.loginQIS();
                Crawler_Service.checkGrades(grades, getContext(), true);
                login.storeAndEncrypt(value, getActivity());
                Login.loginsuccess(this.view, requireActivity());
                //Anmeldung hat 1A funktioniert
            } catch (TooManyFalseLoginException e) {    //Anmeldung schlug fehl, weil zu oft falsches Passwort/Username
                timestampTimeout = System.currentTimeMillis();
                getActivity().runOnUiThread(() -> new Message(getActivity(), getString(R.string.LoginFailedTooManyFalse)));
                loginfailed();
                Alarm.stopAlarm(getActivity());
            } catch (Exception e) {     //Anmeldung schlug aus anderen Gründen fehl
                getActivity().runOnUiThread(() -> new Message(getActivity(), getString(R.string.LoginFailedWrong)));
                loginfailed();
                Alarm.stopAlarm(getActivity());
                e.printStackTrace();
            }
        }).start();
    }

    public void loginfailed() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView loggingstatus_text = view.findViewById(R.id.loggingstatus_text);
                loggingstatus_text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                loggingstatus_text.setTextColor(Color.WHITE);
                loggingstatus_text.setText(R.string.not_logged_in);

                TextView appCloseText = view.findViewById(R.id.appCloseText);
                appCloseText.setVisibility(View.GONE);

                progressBarLogin.setVisibility(View.GONE);
                loginHint.setVisibility(View.GONE);
                radioGroup.setVisibility(View.VISIBLE);
                view.findViewById(R.id.intervall_text).setVisibility(View.VISIBLE);
                view.findViewById(R.id.seekBar).setVisibility(View.VISIBLE);

                HomeFragment.loginSuccess.setVariable(false);
                Notification.cancelAllNotifications(getContext());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void loginClicked(View view) {
            progressBarLogin.setVisibility(View.VISIBLE);
            EditText et_password = getActivity().findViewById(R.id.et_password);
            HomeFragment.username = et_name.getText().toString();
            HomeFragment.password = et_password.getText().toString();
            radioGroup.setVisibility(View.GONE);
            seekBar.setVisibility(View.GONE);
            intervall_text.setVisibility(View.GONE);
            loginHint.setVisibility(View.VISIBLE);
            login = new Login(getActivity().getApplicationContext(), username, password);
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            if (!HomeFragment.loginSuccess.getVariable()) {
                if (et_name.getText().toString().equals("") || et_password.getText().toString().equals("")) {
                    new Message(getContext(), getString(R.string.usernameOrPasswdEmpty));
                    loginfailed();
                } else {
                    LocalDateTime localdatetime = LocalDateTime.now(ZoneId.of("Europe/Berlin"));
                    if (localdatetime.getHour() >= 1 && localdatetime.getHour() <= 5) {
                        new Message(getContext(), getString(R.string.LoginFailedInNight));
                        loginfailed();
                    } else {
                        if (!Alarm.checkIntent(getActivity())) {
                            new AlertBuilder(getActivity());
                        } else {
                            new Alarm(getActivity().getApplicationContext(), value);
                            new BootLoader().startBootLoader(getActivity());
                        }

                        checkFirstLogin(login);
                    }
                }
            } else {
                //Beim neustarten der App, prüfen ob Service noch läuft, weil Anmeldung nicht mehr vorhanden ist. Ggf. Service stoppen
                if (Alarm.checkAlarm(getActivity())) {
                    Alarm.stopAlarm(getActivity());
                    new BootLoader().stopBootLoader(getActivity());
                }

                loginfailed();
                btn_login.setText("Login");
                radioGroup.setVisibility(View.VISIBLE);
                HomeFragment.username = "";
                HomeFragment.password = "";
                Alarm.stopAlarm(getActivity());
                new BootLoader().stopBootLoader(getActivity());
                Database database = new Database(getContext());
                database.dropTable();
                SharedPreferences prefs = getActivity().getSharedPreferences("de.clinc8686.qishochschulcrawler", Context.MODE_PRIVATE);
                prefs.edit().clear().apply();
                new Message(getContext(), getString(R.string.StoppedService));
            }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BOOT_COMPLETED_RECEIVER, BootCompletedReceiver.class);
    }
}