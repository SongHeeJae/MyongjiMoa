package com.example.myongjimoa;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.yanzhenjie.album.Action;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumConfig;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.AlbumLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class
ReviewWriteActivity extends AppCompatActivity {

    EditText write_description;
    ImageButton write_submit;
    ImageButton picture;
    RatingBar rating_bar;
    RecyclerView recycler_view;
    ReviewWriteImageAdapter review_write_image_adapter;
    public GestureDetector gesture_detector;
    ArrayList<String> path;

    String user_id;
    String restaurant_id;

    int upload_count;

    int value = 0;
    boolean bool_upload = false;
    ProgressBar progressBar;
    Handler handler = new Handler();

    private final int GET_GALLERY_IMAGE = 0;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_write);

        mAdView = findViewById(R.id.review_write_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        setTitle("리뷰쓰기");

        Intent it = getIntent();
        user_id = it.getStringExtra("user_id");
        restaurant_id = it.getStringExtra("restaurant_id");

        write_description = (EditText) findViewById(R.id.review_write_description);
        write_submit = (ImageButton) findViewById(R.id.review_write_submit);
        picture = (ImageButton) findViewById(R.id.review_picture);
        rating_bar = (RatingBar) findViewById(R.id.review_write_rating_bar);
        recycler_view = (RecyclerView) findViewById(R.id.review_write_image);
        progressBar = (ProgressBar) findViewById(R.id.h_progressbar);

        path = new ArrayList<>();
        review_write_image_adapter = new ReviewWriteImageAdapter();
        gesture_detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        recycler_view.setLayoutManager(layoutManager);
        recycler_view.setAdapter(review_write_image_adapter); // RecyclerView 레이아웃과 어댑터 등록
        recycler_view.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

                View childView = rv.findChildViewUnder(e.getX(), e.getY());

                if(childView != null && gesture_detector.onTouchEvent((e))) {
                    int currentPos = rv.getChildAdapterPosition(childView);
                    review_write_image_adapter.removeItem(currentPos); // 이미지 클릭 시 어댑터에서 삭제
                    return true;
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
    });

        write_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isNetworkConnected()) { // false 인 경우 네트워크 연결 안되어있음.
                    AlertDialog.Builder builder = new AlertDialog.Builder(ReviewWriteActivity.this);
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
                    return ;
                }

                if(write_description.getText().toString().length() == 0)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ReviewWriteActivity.this);
                    builder.setTitle("메시지")
                            .setMessage("리뷰 내용을 입력해 주세요!")
                            .setCancelable(false)
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                    builder.show();
                    return ;
                }

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            value += 5;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setProgress(value);
                                }
                            });

                            if(bool_upload) { // 업로드 끝난 경우
                                progressBar.setProgress(100);
                                break;
                            }

                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                thread.start();
                imageUpload();
            }
        }); // 제출 버튼 클릭 시 서버 업로드 시작

        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGallery();
            }
        }); // 사진 버튼 클릭 시 갤러리 열어줌.

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    public void imageUpload() {
        if (review_write_image_adapter.getItemCount() > 0) {
            TransferObserver observer;
            TransferUtility transfer_utility = TransferUtility.builder().s3Client(Request.getAmazonS3(ReviewWriteActivity.this)).context(this).build();

            for (int i = 0; i < review_write_image_adapter.getItemCount(); i++) {
                String name = restaurant_id + "_"  + user_id + "_" + Request.getTime("yyyyMMddHHmmss") + "_" + i;
                File file = new File(review_write_image_adapter.getItem(i)); // 중복안되는 이름으로 파일 생성
                observer = transfer_utility.upload( // 서버에 업로드
                        "myongjimoa/review_images",
                        name,
                        file
                );
                path.add(name);
                observer.setTransferListener(new TransferListener() {
                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        if (TransferState.COMPLETED == state) {
                            upload_count++;
                            if (upload_count == review_write_image_adapter.getItemCount()) {
                                reviewUpload(path); // 이미지 업로드 끝나면 글 업로드 시작
                                upload_count = 0;
                            }
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    }

                    @Override
                    public void onError(int id, Exception ex) {
                        Log.d("s3에러", ex.getMessage());
                    }
                });
            }
        } else { // 업로드 할 이미지 없을 시
            reviewUpload(path);
        }
    }

    public void reviewUpload(ArrayList<String> path) {
        ConnectDB connectDB = Request.getRetrofit().create(ConnectDB.class);
        Call<String> call = connectDB.writeReview(restaurant_id, Request.filter(write_description.getText().toString()), user_id, rating_bar.getRating(), Request.getTime("yyyy-MM-dd HH:mm:ss"), path);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                String result = response.body().trim();

                if(result.equals("success")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ReviewWriteActivity.this);
                    builder.setTitle("메시지");
                    builder.setMessage("리뷰를 작성했습니다.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(RESULT_OK);
                            finish(); // 글쓰기 완료하면 현재 Activity 종료
                        }
                    });
                    builder.show();
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("글쓰기 연결 실패", t.getMessage());
            }
        });
        bool_upload = true;
    }

    public class ReviewWriteImageAdapter extends RecyclerView.Adapter<ReviewWriteImageAdapter.ViewHolder> {
        List<String> items = new ArrayList<>();

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView img;
            public ViewHolder(View itemView) {
                super(itemView);
                recycler_view.setHasFixedSize(true);
                img = (ImageView) itemView.findViewById(R.id.image_view);
            }

            public void setData(String data) {
                //값 읽어오기
               // img.setImageBitmap(data);
                // download 경로 못읽는거 버그 오류
                Glide.with(ReviewWriteActivity.this)
                        .load(data)
                        .into(img);
            }
        }

        public void removeItem(int position) {
            items.remove(position);
            notifyItemRemoved(position);
            notifyItemChanged(position, items.size());
        }

        public String getItem(int i) {
            return items.get(i);
        }

        public void add(String item) {
            items.add(item);
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.write_image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.setData(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    public void getGallery() { // 갤러리 열기
        Album.initialize(AlbumConfig.newBuilder(this).setAlbumLoader(new AlbumLoader() {
            @Override
            public void load(ImageView imageView, AlbumFile albumFile) {
                load(imageView, albumFile.getPath());
            }

            @Override
            public void load(ImageView imageView, String url) {
                Glide.with(imageView.getContext())
                        .load(url)
                        .into(imageView);
            }
        }).build());

        Album.image(this) // Image selection.
                .multipleChoice()
                .camera(true)
                .columnCount(3)
                .selectCount(20)
                .onResult(new Action<ArrayList<AlbumFile>>() {
                    @Override
                    public void onAction(@NonNull ArrayList<AlbumFile> result) {
                        for (AlbumFile file : result) review_write_image_adapter.add(file.getPath());
                    }
                })
                .onCancel(new Action<String>() {
                    @Override
                    public void onAction(@NonNull String result) {
                    }
                })
                .start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // 메뉴 아이템 이벤트 처리
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
