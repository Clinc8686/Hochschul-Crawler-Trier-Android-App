package de.clinc8686.hochschul_crawler;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.time.LocalDateTime;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    private static String password;
    private static String username;
    public static boolean loginsucess = false;
    private static int value = 60;
    @SuppressLint("StaticFieldLeak")
    private ProgressBar progressBarLogin;
    private RadioGroup radioGroup;
    private long timestampTimeout = 0;
    private static String checkbox = "checkBoxTrier";
    private EditText et_name;
    private Button btn_login;
    private Login login;
    private TextView text_qis_abschaltung;
    View view;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameterss
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.view = inflater.inflate(R.layout.login_page, container, false);

        getLayoutObjects();
        //boolean service_status = checkAlarm(); //checkService();
        boolean service_status = Alarm.checkAlarm(getActivity().getApplicationContext());
        if (service_status) {
            HomeFragment.loginsucess = true;
            btn_login.setText("Logout");
            Login.loginsuccess(view, getActivity());
        }

        if (!loginsucess) {
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
            builder.setTitle("Datenschutzerklärung & Einwilligung zur Verarbeitung personenbezogener Daten")
                    .setMessage(getString(R.string.privacypolicy))
                    .setPositiveButton("Ich willige diesem ein", dialogClickListener)
                    .setNegativeButton("Ich lehne ab", dialogClickListener).show();
        }
        btn_login.setOnClickListener(view -> {
            progressBarLogin.setVisibility(View.VISIBLE);
            EditText et_password = getActivity().findViewById(R.id.et_password);
            HomeFragment.username = et_name.getText().toString();
            HomeFragment.password = et_password.getText().toString();
            radioGroup.setVisibility(View.GONE);
            login = new Login(getActivity().getApplicationContext(), username, password, checkbox);
            if (!HomeFragment.loginsucess) {
                if (et_name.getText().toString().equals("") || et_password.getText().toString().equals("")) {
                    new Message(getContext(), "Benutzerkennung oder Passwort leer!");
                    loginfailed();
                } else {
                    LocalDateTime localdatetime = LocalDateTime.now();
                    if (localdatetime.getHour() >= 1 && localdatetime.getHour() <= 5) {
                        new Message(getContext(), "Login failed: QIS zwischen 0 und 6 Uhr nicht erreichbar!");
                        loginfailed();
                    } else {
                        checkFirstLogin(login);
                        login.storePreferences(value, getActivity());

                        new Thread(() -> {
                            try {
                                HtmlPage grades = login.loginQIS();
                                Crawler_Service.checkGrades(grades, getContext(), true);
                            } catch (Exception e){
                                    Log.e("Exceptionlogin", e.toString());
                            }
                        }).start();


                        if (!Alarm.checkIntent(getActivity())) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Hinweis!");
                            builder.setMessage("Ohne die folgende Berechtigung kann die App nicht optimal im Hintergrund funktionieren.");
                            builder.setCancelable(true);
                            builder.setNeutralButton(android.R.string.ok,
                                    (dialog, id) -> {
                                        new Alarm(getActivity(), value);
                                        dialog.cancel();
                                    });

                            AlertDialog alert = builder.create();
                            alert.show();
                        } else {
                            new Alarm(getActivity().getApplicationContext(), value);
                        }
                    }
                }
            } else {
                //Beim neustarten der App, prüfen ob Service noch läuft, weil Anmeldung nicht mehr vorhanden ist. Ggf. Service stoppen
                if (Alarm.checkAlarm(getActivity())) {
                    Alarm.stopAlarm(getActivity());
                }

               /*try {
                    new NotificationChannel("Hochschul-Crawler", "Hochschul-Crawler", NotificationManager.IMPORTANCE_HIGH);
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(54295);
                } catch (Exception ignored) {}*/

                loginfailed();
                btn_login.setText("Login");
                radioGroup.setVisibility(View.VISIBLE);
                HomeFragment.username = "";
                HomeFragment.password = "";
                Alarm.stopAlarm(getActivity());
                Database database = new Database(getContext());
                database.dropTable();
                SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getApplicationContext().getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
                prefs.edit().clear().apply();
                new Message(getContext(), "Service wurde gestoppt & Logindaten entfernt.");
                //createToastMessage("Service wurde gestoppt & Logindaten entfernt.");
            }
        });

        SeekBar seekBar = view.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressWarnings("FieldMayBeFinal")
            TextView text_seekbar_minute = view.findViewById(R.id.text_seekbar_minute);

            @Override
            public void onProgressChanged(SeekBar seekBar, int progessValue, boolean b) {
                HomeFragment.value = progessValue;
                text_seekbar_minute.setText("Alle " + HomeFragment.value + " Minuten wird aktualisiert.\n" +
                        "Geschätzte Datennutzung im Monat: \n" + dataUsage());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                text_seekbar_minute.setText("Alle " + HomeFragment.value + " Minuten wird aktualisiert.\n" +
                        "Geschätzte Datennutzung im Monat: " + dataUsage());
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
        });

        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButtonTrier:
                        text_qis_abschaltung.setVisibility(View.VISIBLE);
                        et_name.setHint("Benutzerkennung");
                        checkbox = "checkBoxTrier";
                        break;
                    case R.id.radioButtonKoblenz:
                        text_qis_abschaltung.setVisibility(View.INVISIBLE);
                        et_name.setHint("HRZ-Login");
                        checkbox = "checkBoxKoblenz";

                        //tmp
                        createToastMessage("Es werden noch Tester für Koblenz gesucht! Melde dich bei: hochschulcrawler@gmail.com");
                        RadioButton rb = getActivity().findViewById(R.id.radioButtonTrier);
                        rb.setChecked(true);
                        break;
                    case R.id.radioButtonAachen:
                        text_qis_abschaltung.setVisibility(View.INVISIBLE);
                        et_name.setHint("FH-Kennung");
                        checkbox = "checkBoxAachen";

                        //tmp
                        createToastMessage("Es werden noch Tester für Aachen gesucht! Melde dich bei: hochschulcrawler@gmail.com");
                        RadioButton rb2 = getActivity().findViewById(R.id.radioButtonTrier);
                        rb2.setChecked(true);
                        break;
                }
            }

        });

        Button rightbutton = view.findViewById(R.id.rightFragmentButton);
        rightbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        return view;
    }

    private void getLayoutObjects() {
        progressBarLogin = view.findViewById(R.id.progressBarLogin);
        btn_login = view.findViewById(R.id.btn_login);
        et_name = view.findViewById(R.id.et_name);
        text_qis_abschaltung = view.findViewById(R.id.text_qis_abschaltung);
        radioGroup = view.findViewById(R.id.radioGroup);
    }

    private void createToastMessage(String text) {
        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show());
    }

    public void checkFirstLogin(Login login) {
        new Thread(() -> {
            try {
                long difftime = System.currentTimeMillis() - timestampTimeout;
                if (difftime < 300000) {
                    throw new TooManyFalseLoginException("gesperrt - timeout");
                }

                login.loginQIS();
                //Anmeldung hat 1A funktioniert
                Login.loginsuccess(view, getActivity());

            } catch (TooManyFalseLoginException e) {    //Anmeldung schlug fehl, weil zu oft falsches Passwort/Username
                timestampTimeout = System.currentTimeMillis();
                getActivity().runOnUiThread(() -> new Message(getActivity(), "Anmeldung fehlgeschlagen! Zu oft falscher Benutzername/Passwort eingegeben! Deine IP-Adresse ist für einige Minuten gesperrt."));
                loginfailed();
                Alarm.stopAlarm(getActivity());
            } catch (Exception e) {     //Anmeldung schlug aus anderen Gründen fehl
                getActivity().runOnUiThread(() -> new Message(getActivity(), "Anmeldung fehlgeschlagen! Benutzerkennung/Passwort falsch oder keine/schlechte Verbidung zum QIS!"));
                loginfailed();
                Alarm.stopAlarm(getActivity());
            }
        }).start();
    }

    void loginfailed() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView loggingstatus_text = view.findViewById(R.id.loggingstatus_text);
                loggingstatus_text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                loggingstatus_text.setTextColor(Color.RED);
                loggingstatus_text.setText("Not logged in");

                TextView appCloseText = view.findViewById(R.id.appCloseText);
                appCloseText.setVisibility(View.GONE);

                view.findViewById(R.id.progressBarLogin).setVisibility(View.GONE);
                view.findViewById(R.id.radioGroup).setVisibility(View.VISIBLE);

                HomeFragment.loginsucess = false;
                Notification.cancelAllNotifications(getContext());
            }
        });
    }

}