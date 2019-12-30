package com.example.myongjimoa;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator;

class MyViewPager extends ViewPager {

    public MyViewPager(@NonNull Context context) {
        super(context);
    }

    public MyViewPager(Context context, AttributeSet attr) {
        super(context, attr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}

public class ImageActivity extends AppCompatActivity {

    ArrayList<String> images;
    ImageAdapter adapter;
    Button download;
    MyViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.image);
        Intent it = getIntent();
        images = new ArrayList<>(it.getStringArrayListExtra("images")); // 이미지 목록 가져옴

        pager = (MyViewPager) findViewById(R.id.image_view_pager);
        adapter = new ImageAdapter(images);
        pager.setAdapter(adapter);
        
        download = (Button) findViewById(R.id.download_image);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ImageDownload().execute(images.get(pager.getCurrentItem())); // 다운로드 버튼 클릭 시 이미지 다운로드 수행
            }
        });

        pager.setCurrentItem(it.getIntExtra("current", 0)); // 초기 화면은 이전 화면에서 클릭했던 이미지의 위치로 지정
        CircleIndicator indicator = (CircleIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(pager); // 동그라미 모양의 indicator 지정. 뷰 페이저의 이동에 따라 업데이트가 수행됨
    }

    public class ImageAdapter extends PagerAdapter {

        ArrayList<String> path;

        public ImageAdapter(ArrayList<String> path) {
            this.path = new ArrayList<>();
            this.path.addAll(path);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.images_item, container, false);
            PhotoView img = (PhotoView) view.findViewById(R.id.full_image);

            Glide.with(ImageActivity.this)
                    .load(path.get(position))
                    .into(img);

            container.addView(view) ;
            return view ;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return path.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return (view == (View)object);
        }
    }

    private class ImageDownload extends AsyncTask<String, Void, Void> {
        private String fileName;
        private final String SAVE_FOLDER = "/myongjimoa";

        @Override
        protected Void doInBackground(String... params) { // 이미지 URL로 접속하여 이미지 다운로드 수행
            //다운로드 경로를 지정
            String savePath = Environment.getExternalStorageDirectory().toString() + SAVE_FOLDER;
            File dir = new File(savePath);
            if (!dir.exists()) {
                dir.mkdirs(); //디렉토리가 존재하지 않을 경우 디렉토리 생성
            }

            fileName = "MM_" + Request.getTime("yyyyMMddHHmmss");

            String fileUrl = params[0]; // 웹에 파일이 있는 경로

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
            super.onPostExecute(result);
            String targetDir = Environment.getExternalStorageDirectory().toString() + SAVE_FOLDER;
            File file = new File(targetDir + "/" + fileName + ".jpg"); // 이미지 파일 지정
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file))); // 생성한 파일 업데이트

            Toast.makeText(getApplicationContext(), "다운로드가 완료되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
