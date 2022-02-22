package de.clinc8686.hochschul_crawler;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class GradeFragment extends Fragment {
    View view;
    ArrayList<ModulInfo> modulInfos;
    ArrayList<TextView> textviews;
    int width, height;

    public GradeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.view = inflater.inflate(R.layout.fragment_grade, container, false);
        this.textviews = new ArrayList<>();

        WindowManager wm = (WindowManager) Objects.requireNonNull(getContext()).getSystemService(Context.WINDOW_SERVICE);
        Display screensize = wm.getDefaultDisplay();
        Point size = new Point();
        screensize.getSize(size);
        this.width = size.x;
        this.height = size.y;

        ((TextView) view.findViewById(R.id.swipeleft)).setTextSize(width/80.f);
        SearchView searchView = view.findViewById(R.id.searchBar);

        LinearLayout linearLayout1 = (LinearLayout) searchView.getChildAt(0);
        LinearLayout linearLayout2 = (LinearLayout) linearLayout1.getChildAt(2);
        LinearLayout linearLayout3 = (LinearLayout) linearLayout2.getChildAt(1);
        AutoCompleteTextView autoComplete = (AutoCompleteTextView) linearLayout3.getChildAt(0);
        autoComplete.setTextSize(width/61.6f);
        autoComplete.setTextColor(Color.WHITE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchString) {
                for(TextView textview : textviews) {
                    if (textview.getText().toString().toLowerCase(Locale.ROOT).contains(searchString.toLowerCase(Locale.ROOT))) {
                        view.findViewById(R.id.table).scrollTo(0, textview.getBottom()-100);

                        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), Color.BLUE, Color.TRANSPARENT);
                        colorAnimation.setDuration(500);
                        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animator) {
                                textview.setBackgroundColor((int) animator.getAnimatedValue());
                            }
                        });
                        colorAnimation.start();

                        InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getContext()).getSystemService(getContext().INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchString) {
                for(TextView textview : textviews) {
                    if (textview.getText().toString().contains(searchString)) {
                        view.findViewById(R.id.table).scrollTo(0, textview.getBottom()-100);
                        return true;
                    }
                }
                return false;
            }
        });

        if (HomeFragment.loginsucess) {
            Database database = new Database(view.getContext());
            modulInfos = database.selectData(view.getContext());
            if (modulInfos.size() > 0) {
                view.findViewById(R.id.noGrades).setVisibility(View.GONE);
                view.findViewById(R.id.searchBar).setVisibility(View.VISIBLE);
                LinearLayout linearLayout = view.findViewById(R.id.linearLayoutGrades);

                for (ModulInfo modulInfo : modulInfos) {
                    linearLayout.addView(createModulTextView(modulInfo.modulNumber, modulInfo.modul));
                    linearLayout.addView(createGradeTextView(modulInfo.pass, modulInfo.grade));
                    linearLayout.addView(createSemesterTextView(modulInfo.semester));
                }
            }
        }
        return view;
    }

    @SuppressLint("SetTextI18n")
    TextView createModulTextView(String modulNumber, String modul) {
        TextView textView = new TextView(view.getContext());
        textView.setText(modulNumber + " " + modul);
        textView.setTextSize(width/61.6f);
        textView.setTextColor(Color.WHITE);
        textView.setTypeface(ResourcesCompat.getFont(view.getContext(), R.font.inte_medium));
        textView.setPadding(2, 2, 2, 1);
        textView.setShadowLayer(3.0f, -1, -1, Color.LTGRAY);
        return textView;
    }

    TextView createGradeTextView(String pass, String grade) {
        TextView textView = new TextView(view.getContext());
        textView.setText(pass + " mit " + grade);
        textView.setTextSize(width/77.f);
        textView.setTextColor(Color.rgb(228, 228, 228));
        textView.setTypeface(ResourcesCompat.getFont(view.getContext(), R.font.inte_medium));
        textView.setPadding(2, 1, 2, 1);
        textView.setShadowLayer(3.0f, -1, -1, Color.LTGRAY);
        return textView;
    }

    TextView createSemesterTextView(String semester) {
        TextView textView = new TextView(view.getContext());
        textView.setText(semester);
        textView.setTextSize(width/77.f);
        textView.setTextColor(Color.rgb(228, 228, 228));
        textView.setTypeface(ResourcesCompat.getFont(view.getContext(), R.font.inte_medium));
        textView.setPadding(2, 1, 2, 70);
        textView.setShadowLayer(3.0f, -1, -1, Color.LTGRAY);
        return textView;
    }
}