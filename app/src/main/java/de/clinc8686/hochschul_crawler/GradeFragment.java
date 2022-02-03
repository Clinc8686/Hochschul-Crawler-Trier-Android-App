package de.clinc8686.hochschul_crawler;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GradeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GradeFragment extends Fragment {
    View view;
    ArrayList<ModulInfo> modulInfos;
    ArrayList<TextView> textviews;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public GradeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GradeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GradeFragment newInstance(String param1, String param2) {
        GradeFragment fragment = new GradeFragment();
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

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.view = inflater.inflate(R.layout.fragment_grade, container, false);
        this.textviews = new ArrayList<>();

        SearchView searchView = view.findViewById(R.id.searchBar);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchString) {
                for(TextView textview : textviews) {
                    if (textview.getText().toString().toLowerCase(Locale.ROOT).contains(searchString.toLowerCase(Locale.ROOT))) {
                        view.findViewById(R.id.table).scrollTo(0, textview.getBottom()-100);

                        int color2 = Color.BLUE;
                        int color1 = Color.TRANSPARENT;
                        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), color2, color1);
                        colorAnimation.setDuration(500); // milliseconds
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
            view.findViewById(R.id.noGrades).setVisibility(View.GONE);
            view.findViewById(R.id.searchBar).setVisibility(View.VISIBLE);
            LinearLayout linearLayout2 = view.findViewById(R.id.linearLayoutGrades);

            for (ModulInfo modulInfo : modulInfos) {
                TextView textView = new TextView(view.getContext());
                textView.setText(modulInfo.modulNumber + " " + modulInfo.modul);
                textView.setTextSize(18.0f);
                textView.setTextColor(Color.WHITE);
                textView.setTypeface(ResourcesCompat.getFont(view.getContext(), R.font.inte_medium));
                textView.setPadding(2, 2, 2, 1);
                textView.setShadowLayer(3.0f, -1, -1, Color.LTGRAY);
                linearLayout2.addView(textView);
                textviews.add(textView);

                TextView textView2 = new TextView(view.getContext());
                textView2.setText(modulInfo.pass + " mit " + modulInfo.grade);
                textView2.setTextSize(14.0f);
                textView2.setTextColor(Color.rgb(228, 228, 228));
                textView2.setTypeface(ResourcesCompat.getFont(view.getContext(), R.font.inte_medium));
                textView2.setPadding(2, 1, 2, 1);
                textView2.setShadowLayer(3.0f, -1, -1, Color.LTGRAY);
                linearLayout2.addView(textView2);

                TextView textView3 = new TextView(view.getContext());
                textView3.setText(modulInfo.semester);
                textView3.setTextSize(14.0f);
                textView3.setTextColor(Color.rgb(228, 228, 228));
                textView3.setTypeface(ResourcesCompat.getFont(view.getContext(), R.font.inte_medium));
                textView3.setPadding(2, 1, 2, 70);
                textView3.setShadowLayer(3.0f, -1, -1, Color.LTGRAY);
                linearLayout2.addView(textView3);
            }
        }

        Button leftbutton = view.findViewById(R.id.leftFragmentButton);
        leftbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        return view;
    }
}