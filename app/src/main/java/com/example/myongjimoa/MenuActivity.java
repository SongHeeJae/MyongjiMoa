package com.example.myongjimoa;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

public class MenuActivity extends AppCompatActivity {

    String url1 = "https://www.mju.ac.kr/mbs/mjukr/jsp/restaurant/restaurant.jsp?configIdx=36548&id=mjukr_051002020000"; // 학생회관
    String url2 = "https://www.mju.ac.kr/mbs/mjukr/jsp/restaurant/restaurant.jsp?configIdx=36337&id=mjukr_051002050000"; // 명진당
    List<String> day_menu1; // 학생회관
    List<String> day_menu2;
    int finished;

    ViewPager view_pager;
    TabLayout tabs;
    MenuAdapter menu_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        day_menu1 = new ArrayList<>();
        day_menu2 = new ArrayList<>();

        tabs = (TabLayout) findViewById(R.id.food_days_tabs);
        tabs.addTab(tabs.newTab().setText("월"));
        tabs.addTab(tabs.newTab().setText("화"));
        tabs.addTab(tabs.newTab().setText("수"));
        tabs.addTab(tabs.newTab().setText("목"));
        tabs.addTab(tabs.newTab().setText("금"));
        tabs.setTabGravity(tabs.GRAVITY_FILL);
        view_pager = (ViewPager) findViewById(R.id.food_menu_view_pager);
        new DownloadMenu().execute(url1);
        new DownloadMenu().execute(url2);

    }


    private class DownloadMenu extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                List<String> day = new ArrayList<>();
                day.add("Monday_Data");
                day.add("Tuesday_Data");
                day.add("Wedensday_Data");
                day.add("Thursday_Data");
                day.add("Friday_Data");
                Document doc = Jsoup.connect(params[0]).get();
                Elements e1 = doc.select("table[class=sub]");
                int i = 0;
                if(params[0].equals(url1)) {
                    for (Element e : e1) {
                        Elements e2 = e.select("div[name=" + day.get(i++) + "]");
                        String menu = "";
                        for (Element ee : e2) {
                            menu += ee.text() + "\n";
                        }
                        day_menu1.add(menu);
                    }
                } else {
                    for (Element e : e1) {
                        Elements e2 = e.select("div[name=" + day.get(i++) + "]");
                        String menu = "";
                        int j=0;
                        for (Element ee : e2) {
                            if(j != 1 && j != 4) {
                                menu += ee.text() + "\n";
                            }
                            j++;
                        }
                        day_menu2.add(menu);
                    }
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            finished++;
            if(finished == 2) {
                menu_adapter = new MenuAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
                view_pager.setAdapter(menu_adapter);
                tabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(view_pager));
                view_pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                int day = calendar.get(Calendar.DAY_OF_WEEK);
                view_pager.setCurrentItem(day-2);
            }
        }
    }

    public class MenuAdapter extends FragmentStatePagerAdapter {


        public MenuAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public Fragment getItem(int position) {
            return MenuFragment.newInstance(day_menu1.get(position), day_menu2.get(position));
        }

    }
}
