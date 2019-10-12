package com.example.myongjimoa;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import me.relex.circleindicator.CircleIndicator;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

public class ImageActivity extends AppCompatActivity {


    ArrayList<String> images;
    FragmentStatePagerAdapter adapter;
    Button download;
    ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.image);
        Intent it = getIntent();
        images = new ArrayList<>(it.getStringArrayListExtra("images"));

        download = (Button) findViewById(R.id.download_image);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ImageDownload().execute(images.get(pager.getCurrentItem()));
            }
        });

        pager = (ViewPager) findViewById(R.id.image_view_pager);
        adapter = new ImageAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        pager.setAdapter(adapter);
        pager.setCurrentItem(it.getIntExtra("current", 0));
        CircleIndicator indicator = (CircleIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(pager);

    }

    public class ImageAdapter extends FragmentStatePagerAdapter {

        public ImageAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public Fragment getItem(int position) {
            return ImageFragment.newInstance(images.get(position));
        }

    }

    private class ImageDownload extends AsyncTask<String, Void, Void> {
        private String fileName;
        private final String SAVE_FOLDER = "/myongjimoa";

        @Override
        protected Void doInBackground(String... params) {
            //다운로드 경로를 지정
            String savePath = Environment.getExternalStorageDirectory().toString() + SAVE_FOLDER;
            File dir = new File(savePath); //상위 디렉토리가 존재하지 않을 경우 생성
            if (!dir.exists()) {
                dir.mkdirs();
            } //파일 이름 :날짜_시간

            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            String format_date = sdf.format(date);

            fileName = "MM_" + format_date;
            //웹 서버 쪽 파일이 있는 경로
            String fileUrl = params[0];
            //다운로드 폴더에 동일한 파일명이 존재하는지 확인
            //if (new File(savePath + "/" + fileName).exists() == false) {
            //} else {
            //}
            String localPath = savePath + "/" + fileName + ".jpg";
            try {
                URL imgUrl = new URL(fileUrl);
                //서버와 접속하는 클라이언트 객체 생성
                HttpURLConnection conn = (HttpURLConnection) imgUrl.openConnection();
                int len = conn.getContentLength();
                byte[] tmpByte = new byte[len];
                InputStream is = conn.getInputStream();
                File file = new File(localPath);
                //파일 저장 스트림 생성
                FileOutputStream fos = new FileOutputStream(file);
                int read; //입력 스트림을 파일로 저장
                while (true) {
                    read = is.read(tmpByte);
                    if (read <= 0) break;
                    fos.write(tmpByte, 0, read); //file 생성
                }
                is.close();
                fos.close();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result); //저장한 이미지 열기

            String targetDir = Environment.getExternalStorageDirectory().toString() + SAVE_FOLDER;
            File file = new File(targetDir + "/" + fileName + ".jpg"); //type 지정 (이미지)
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));

            Toast.makeText(getApplicationContext(), "다운로드가 완료되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
