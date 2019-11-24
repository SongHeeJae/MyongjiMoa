package com.example.myongjimoa;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
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

public class MenuActivity extends AppCompatActivity {

    ProgressBar progressBar;
    String url1 = "https://www.mju.ac.kr/mbs/mjukr/jsp/restaurant/restaurant.jsp?configIdx=36548&id=mjukr_051002020000"; // 학생회관
    String url2 = "https://www.mju.ac.kr/mbs/mjukr/jsp/restaurant/restaurant.jsp?configIdx=36337&id=mjukr_051002050000"; // 명진당
    ArrayList<String> day_menu1; // 학생회관
    ArrayList<String> day_menu2;
    Handler handler = new Handler();
    boolean t_bool = true;
    boolean temp = true;
    int finished;
    int value;

    ViewPager view_pager;
    TabLayout tabs;
    MenuAdapter menu_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        progressBar = (ProgressBar) findViewById(R.id.h_progressbar);
        progressBar.setIndeterminate(false);
        progressBar.setProgress(80);
        day_menu1 = new ArrayList<>();
        day_menu2 = new ArrayList<>();

        tabs = (TabLayout) findViewById(R.id.food_days_tabs);
        tabs.addTab(tabs.newTab().setText("MON"));
        tabs.addTab(tabs.newTab().setText("TUE"));
        tabs.addTab(tabs.newTab().setText("WED"));
        tabs.addTab(tabs.newTab().setText("THU"));
        tabs.addTab(tabs.newTab().setText("FRI")); // 탭 메뉴들 지정해줌
        tabs.setTabGravity(tabs.GRAVITY_FILL);
        view_pager = (ViewPager) findViewById(R.id.food_menu_view_pager);
        if (!isNetworkConnected()) { // false 인 경우 네트워크 연결 안되어있음.
            AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
            builder.setTitle("메시지")
                    .setMessage("네트워크 연결을 확인해 주세요.")
                    .setCancelable(false)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            builder.show();
            progressBar.setProgress(0);
        }
        else {
            new DownloadMenu().execute(url1);
            new DownloadMenu().execute(url2); // 다운로드 진행
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    private class DownloadMenu extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(t_bool) { // true 상태, 즉 로딩중인 경우
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            value += 1;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (t_bool) {
                                        progressBar.setProgress(value);
                                    } else {
                                        progressBar.setProgress(100);
                                        Toast.makeText(getApplicationContext(), "정보 불러오기 완료!", Toast.LENGTH_SHORT).show();
                                        if(!temp)
                                        {
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    progressBar.setProgress(0);
                                                }
                                            }, 500);
                                        }
                                    }
                                }
                            });

                            if(!temp)
                                break;

                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                thread.start();
            }
        }

        @Override
        protected Void doInBackground(String... params) { // 명지대학교 홈페이지 접속하여 메뉴 긁어옴

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
                // 각 url로 명지대학교 홈페이지에서 해당하는 요일 태그 순서대로 메뉴 긁어옴
                if(params[0].equals(url1)) {
                    for (Element e : e1) {
                        if(i>=day.size()) break;
                        Elements e2 = e.select("div[name=" + day.get(i++) + "]");
                        String menu = "";
                        for (Element ee : e2) menu += ee.text() + "\n";
                        day_menu1.add(menu);
                    }
                } else {
                    for (Element e : e1) {
                        Elements e2 = e.select("div[name=" + day.get(i++) + "]");
                        String menu = "";
                        int j=0;
                        for (Element ee : e2) {
                            if(j != 1 && j != 4) menu += ee.text() + "\n";
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
        protected void onPostExecute(Void result) { // doInBackground의 실행이 끝난 뒤의 처리
            finished++;
            if(finished == 2) { // 메뉴 두개 긁어오는 것이 끝나면
                t_bool = false;
                menu_adapter = new MenuAdapter(day_menu1, day_menu2);
                view_pager.setAdapter(menu_adapter); // 어댑터 지정
                tabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(view_pager));
                view_pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs)); // 뷰페이저와 탭 서로 감지하기위하여 리스너 등록
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                int day = calendar.get(Calendar.DAY_OF_WEEK);
                view_pager.setCurrentItem(day-2); // 뷰페이저는 현재 요일을 구하여 그 화면으로 초기 지정
                if(!t_bool)
                    temp = false;
            }
        }
    }

    public class MenuAdapter extends PagerAdapter {

        ArrayList<String> menu1;
        ArrayList<String> menu2;

        public MenuAdapter(ArrayList<String> menu1, ArrayList<String> menu2) {
            this.menu1 = new ArrayList<>();
            this.menu2 = new ArrayList<>();
            this.menu1.addAll(menu1);
            this.menu2.addAll(menu2);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.menu_item, container, false);
            TextView text1 = (TextView) view.findViewById(R.id.menu1);
            TextView text2 = (TextView) view.findViewById(R.id.menu2);
            TextView text3 = (TextView) view.findViewById(R.id.menu3);
            TextView text4 = (TextView) view.findViewById(R.id.menu4);
            TextView text5 = (TextView) view.findViewById(R.id.menu5);
            TextView text6 = (TextView) view.findViewById(R.id.menu6);

            String[] m1 = menu1.get(position).trim().split("\n");
            String[] m2 = menu2.get(position).trim().split("\n");
            text1.setText(m1[0].trim().replaceAll(" ", "\n"));
            text2.setText(m1[1].trim().replaceAll(" ", "\n"));
            text3.setText(m1[2].trim().replaceAll(" ", "\n"));
            text4.setText(m2[0].trim().replaceAll(" ", "\n"));
            text5.setText(m2[1].trim().replaceAll(" ", "\n"));
            text6.setText(m2[2].trim().replaceAll(" ", "\n"));
            container.addView(view) ;

            return view ;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return (view == (View)object);
        }
    }
}