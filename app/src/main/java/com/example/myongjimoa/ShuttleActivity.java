package com.example.myongjimoa;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class ShuttleActivity extends AppCompatActivity {


    String[] citybus = {"08:00", "08:15", "08:30", "08:45", "09:00", "09:15", "09:30", "09:45",
            "10:00", "10:15", "10:30", "10:45", "11:00", "11:30", "12:00", "12:30", "13:00",
            "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00", "17:30",
            "18:00", "18:40", "19:20", "20:00", "20:40"};

    String[] intobus = {"08:10", "08:20", "08:25", "08:40", "08:50", "08:55", "09:10", "09:20",
            "09:25", "09:40", "09:50", "09:55", "10:10", "10:20", "10:25", "10:40", "10:50",
            "10:55", "11:20", "11:40", "11:50", "12:20", "12:40", "12:50", "13:20", "13:40",
            "13:50", "14:20", "14:40", "14:50", "15:20", "15:40", "15:50", "16:20", "16:40",
            "16:50", "17:20", "17:40", "17:50", "18:20", "19:00", "19:40", "20:20", "21:00"};

    String[] intoschool = {"08:15", "08:25","08:30", "08:35", "08:40", "08:45", "08:55",
            "09:00","09:05", "09:10", "09:15","09:25","09:30", "09:35", "09:40",
            "09:45" ,"09:55" ,"10:00" , "10:05", "10:10", "10:15", "10:25", "10:30",
            "10:35", "10:40", "10:45", "10:55", "11:00", "11:05", "11:10", "11:15",
            "11:35", "11:45", "11:55", "12:05", "12:15", "12:35", "12:45", "12:55",
            "13:05", "13:15", "13:35", "13:45", "13:55", "14:05", "14:15", "14:35",
            "14:45", "14:55", "15:05", "15:15", "15:35", "15:45", "15:55", "16:05",
            "16:15", "16:35", "16:45", "16:55", "17:05", "17:15", "17:35", "17:45",
            "17:55", "18:05", "18:20", "18:40", "19:00", "19:20", "19:40", "20:00",
            "20:15", "20:40", "21:00", "21:20"};

    TextView dateNow;
    TextView CityBus;
    TextView IntoBus;
    TextView IntoSchool;

    String formatDateHour;

    ViewPager view_pager;
    TabLayout tabs;
    ShuttleAdapter shuttle_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shuttle);

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        formatDateHour = sdf.format(date);

        dateNow = (TextView) findViewById(R.id.dateNow);
        CityBus = (TextView) findViewById(R.id.CityBus_text);
        IntoBus = (TextView) findViewById(R.id.IntoBus_text);
        IntoSchool = (TextView) findViewById(R.id.IntoSchool_text);

        dateNow.setText("현재 시간은 " + formatDateHour + "분 입니다.");
        setShuttleTime();

        view_pager = (ViewPager) findViewById(R.id.shuttle_view_pager);
        tabs = (TabLayout) findViewById(R.id.shuttle_tabs);
        tabs.addTab(tabs.newTab().setText("학교방향"));
        tabs.addTab(tabs.newTab().setText("시내방향"));
        tabs.addTab(tabs.newTab().setText("진입로방향"));
        tabs.setTabGravity(tabs.GRAVITY_FILL);
        tabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(view_pager));
        view_pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));

        String add_intoschool = "";
        String add_intobus = "";
        String add_citybus = "";

        for (int i = 0; i < intoschool.length; i++)
            add_intoschool += intoschool[i] + "\n";

        for (int i = 0; i < intobus.length; i++)
            add_intobus += intobus[i] + "\n";

        for (int i = 0; i < citybus.length; i++)
            add_citybus += citybus[i] + "\n";

        shuttle_adapter = new ShuttleAdapter(add_intoschool, add_citybus, add_intobus);
        view_pager.setAdapter(shuttle_adapter);

    }

    public void setShuttleTime() {
        CityBus.setText("잠시 후 이용가능한 시내 방향 셔틀이 없습니다.");
        IntoBus.setText("잠시 후 이용가능한 진입로 방향 셔틀이 없습니다.");
        IntoSchool.setText("잠시 후 이용 가능한 학교 방향 셔틀이 없습니다.");
        if(formatDateHour.compareTo("07:00") >= 0) {
            for (int i = 0; i < citybus.length; i++) {
                if (formatDateHour.compareTo(citybus[i]) <= 0) {
                    CityBus.setText("잠시 후 이용 가능한 시내 방향 셔틀은 " + citybus[i] + "분 입니다.");
                    break;
                }
            }

            for (int i = 0; i < intobus.length; i++) {
                if (formatDateHour.compareTo(intobus[i]) <= 0) {
                    IntoBus.setText("잠시 후 이용 가능한 진입로 방향 셔틀은 " + intobus[i] + "분 입니다.");
                    break;
                }
            }

            for (int i = 0; i < intoschool.length; i++) {
                if (formatDateHour.compareTo(intoschool[i]) <= 0) {
                    IntoSchool.setText("잠시 후 이용 가능한 학교 방향 셔틀은 " + intoschool[i] + "분 입니다.");
                    break;
                }
            }
        }
    }

    public class ShuttleAdapter extends PagerAdapter {

        ArrayList<String> times;

        public ShuttleAdapter(String intoschool, String citybus, String intobus) {
            times = new ArrayList<>();
            times.add(intoschool);
            times.add(citybus);
            times.add(intobus);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.shuttle_tab, container, false);
            TextView textView = (TextView) view.findViewById(R.id.shuttle_time) ;
            textView.setText(times.get(position));

            container.addView(view) ;

            return view ;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return (view == (View)object);
        }
    }
}
