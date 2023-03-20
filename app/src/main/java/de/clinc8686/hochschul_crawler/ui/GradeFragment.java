package de.clinc8686.hochschul_crawler.ui;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;

import de.clinc8686.hochschul_crawler.LoginSuccess;
import de.clinc8686.hochschul_crawler.R;
import de.clinc8686.hochschul_crawler.grades.Database;
import de.clinc8686.hochschul_crawler.grades.GradeText;
import de.clinc8686.hochschul_crawler.grades.ModulInfo;
import de.clinc8686.hochschul_crawler.grades.ModulText;
import de.clinc8686.hochschul_crawler.grades.SemesterText;

public class GradeFragment extends Fragment {
    View view;
    ArrayList<ModulInfo> modulInfos;
    ArrayList<TextView> textviews;
    int width, height;
    ViewGroup viewGroup;

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
        viewGroup = container;

        WindowManager wm = (WindowManager) requireContext().getSystemService(Context.WINDOW_SERVICE);
        Display screensize = wm.getDefaultDisplay();
        Point size = new Point();
        screensize.getSize(size);
        this.width = size.x;
        this.height = size.y;

        return view;
    }

    @SuppressLint("SetTextI18n")
    TextView createModulTextView(String modulNumber, String modul) {
        TextView textView = new ModulText(view, modulNumber, modul);
        textviews.add(textView);
        return textView;
    }

    TextView createGradeTextView(String pass, String grade) {
        TextView textView = new GradeText(view, pass, grade);
        textviews.add(textView);
        return textView;
    }

    TextView createSemesterTextView(String semester) {
        TextView textView = new SemesterText(view, semester);
        textviews.add(textView);
        return textView;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        //((TextView) view.findViewById(R.id.swipeleft)).setTextSize(width/80.f);
        //((TextView) view.findViewById(R.id.noGrades)).setAutoSizeTextTypeUniformWithConfiguration(12, 24, 4, TypedValue.COMPLEX_UNIT_SP);
        SearchView searchView = view.findViewById(R.id.searchBar);

        int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = (TextView) searchView.findViewById(id);
        textView.setTextColor(Color.WHITE);
        textView.setAutoSizeTextTypeUniformWithConfiguration(12, 24, 4, TypedValue.COMPLEX_UNIT_SP);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchString) {
                for(TextView textview : textviews) {
                    if (textview.getText().toString().toLowerCase(Locale.ROOT).contains(searchString.toLowerCase(Locale.ROOT))) {
                        //view.findViewById(R.id.table).scrollTo(0, textview.getBottom()-100);
                        ((ScrollView) view.findViewById(R.id.table)).smoothScrollTo(0, textview.getBottom()-100);
                        Log.e("QIStextview", textview.getBottom() + "");

                        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), Color.BLUE, Color.TRANSPARENT);
                        colorAnimation.setDuration(500);
                        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animator) {
                                textview.setBackgroundColor((int) animator.getAnimatedValue());
                            }
                        });
                        colorAnimation.start();

                        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
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

        changeGradeFragment();

        HomeFragment.loginSuccess.setValueChangeListener(new LoginSuccess.onValueChangeListener() {
            @Override
            public void onChange() {
                changeGradeFragment();
            }
        });
    }

    private void changeGradeFragment() {
        LinearLayout linearLayout = view.findViewById(R.id.linearLayoutGrades);
        if (linearLayout != null) {
            if (HomeFragment.loginSuccess.getVariable()) {
                if(textviews.size() > 0) {
                    for (TextView tv : textviews) {
                        linearLayout.removeView(tv);
                    }
                }

                Database database = new Database(view.getContext());
                modulInfos = database.selectData(view.getContext());
                if (modulInfos.size() > 0) {
                    view.findViewById(R.id.noGrades).setVisibility(View.GONE);
                    view.findViewById(R.id.searchBar).setVisibility(View.VISIBLE);

                    for (ModulInfo modulInfo : modulInfos) {
                        linearLayout.addView(createModulTextView(modulInfo.modulNumber, modulInfo.modul));
                        linearLayout.addView(createGradeTextView(modulInfo.pass, modulInfo.grade));
                        linearLayout.addView(createSemesterTextView(modulInfo.semester));
                    }
                }
            } else {
                view.findViewById(R.id.noGrades).setVisibility(View.VISIBLE);
                view.findViewById(R.id.searchBar).setVisibility(View.GONE);
                for (TextView tv : textviews) {
                    linearLayout.removeView(tv);
                }
            }
        }
    }
}