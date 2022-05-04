package de.clinc8686.hochschul_crawler;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.RequiresApi;
import java.util.ArrayList;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {
    ViewPager2 viewPager;
    HomeFragment homefragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_home);
        viewPager = findViewById(R.id.pager);
        setViewPagerAdapter();
    }

    public void setViewPagerAdapter() {
        ViewPageAdapter viewPageAdapter = new ViewPageAdapter(this);
        ArrayList<Fragment> fragmentList = new ArrayList<>();
        homefragment = new HomeFragment();
        fragmentList.add(homefragment);
        fragmentList.add(new GradeFragment());

        viewPageAdapter.setData(fragmentList);
        viewPager.setAdapter(viewPageAdapter);
    }

    public void loginClicked(View view) {
        homefragment.loginClicked(view);
    }

    public void GroupClicked(View view) {
        homefragment.GroupClicked(view);
    }
}