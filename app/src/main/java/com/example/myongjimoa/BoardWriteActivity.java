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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class BoardWriteActivity extends AppCompatActivity {

    EditText write_title;
    EditText write_description;
    ImageButton write_submit;
    ImageButton picture;
    RecyclerView recycler_view;
    BoardWriteImageAdapter board_write_image_adapter;
    public GestureDetector gesture_detector;
    ArrayList<String> path;
    int upload_count;

    String user_id;
    String board_title_id;

    private final int GET_GALLERY_IMAGE = 0;

    boolean modify_mode;
    int modify_image_num;
    ArrayList<String> delete_images;
    String board_id;

    int value = 0;
    boolean bool_upload = false;
    ProgressBar progressBar;
    Handler handler = new Handler();

    AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board_write);

        mAdView = findViewById(R.id.board_write_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        setTitle("글쓰기");

        modify_image_num = 0; // 수정이미지 없으면 0으로초기화 있으면 이 num부터 이미지 올려주면됨.
        modify_mode = false; // 수정 모드 초기값 false

        write_title = (EditText) findViewById(R.id.write_title);
        write_description = (EditText) findViewById(R.id.write_description);
        write_submit = (ImageButton) findViewById(R.id.write_submit);
        picture = (ImageButton) findViewById(R.id.picture);
        recycler_view = (RecyclerView) findViewById(R.id.board_write_image);
        progressBar = (ProgressBar) findViewById(R.id.h_progressbar);

        path =  new ArrayList<>();

        board_write_image_adapter = new BoardWriteImageAdapter();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        recycler_view.setLayoutManager(layoutManager); // 레이아웃 지정
        recycler_view.setAdapter(board_write_image_adapter); // 어댑터 지정
        recycler_view.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View childView = rv.findChildViewUnder(e.getX(), e.getY());

                if(childView != null && gesture_detector.onTouchEvent((e))) {
                    int currentPos = rv.getChildAdapterPosition(childView);
                    board_write_image_adapter.removeItem(currentPos); // 현재 화면에 올라온 이미지 삭제 진행
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(BoardWriteActivity.this);
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

                if(write_description.getText().toString().length() == 0 || write_title.getText().toString().length() == 0)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(BoardWriteActivity.this);
                    builder.setTitle("메시지")
                            .setMessage("제목 혹은 내용을 입력하세요.")
                            .setCancelable(false)
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                    builder.show();
                    return ;
                }

                else {
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

                                if (bool_upload) { // 업로드 끝난 경우
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
            }
        });

        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGallery();
            }
        });


        gesture_detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent it = getIntent();
        user_id = it.getStringExtra("user_id");
        board_title_id = it.getStringExtra("board_title_id");
        modify_mode = it.getBooleanExtra("modify_mode", false);
        if(modify_mode) { // 수정 상태로 화면 열었을 시 수정하려고 가져온 데이터에 화면 보여줌
            board_id = it.getStringExtra("board_id");
            delete_images = new ArrayList<>();
            write_title.setText(it.getStringExtra("title"));
            write_description.setText(it.getStringExtra("description"));
            modify_image_num = it.getStringArrayListExtra("modify_images").size();
            for (String img : it.getStringArrayListExtra("modify_images"))
                board_write_image_adapter.add("https://myongjimoa.s3.ap-northeast-2.amazonaws.com/board_images/" + img);
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(BoardWriteActivity.this);
        builder.setTitle("작성 화면에서 나가시겠습니까?")
                .setMessage("작성 내용은 저장되지 않습니다.")
                .setPositiveButton(" 예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
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

        if (board_write_image_adapter.getItemCount() - modify_image_num > 0) {
            // adapter에 이미지 개수 - 수정할때 원래 가져온개수 만큼 업로드

            TransferObserver observer;
            
            TransferUtility transfer_utility = TransferUtility.builder().s3Client(Request.getAmazonS3(BoardWriteActivity.this)).context(this).build();

            for (int i = modify_image_num; i < board_write_image_adapter.getItemCount(); i++) {
                String name = user_id + "_" + Request.getTime("yyyyMMddHHmmss") + "_" + i; // 관리자가 수정했을시 감지가능
                File file = new File(board_write_image_adapter.getItem(i));
                observer = transfer_utility.upload(
                        "myongjimoa/board_images",
                        name, // 이미지 파일이름설정 개별적으로 중복안되게구성해야함. user_id + 현재시간?
                        file
                );
                path.add(name);
                observer.setTransferListener(new TransferListener() {
                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        if (TransferState.COMPLETED == state) {
                            upload_count++;
                            if (upload_count == board_write_image_adapter.getItemCount() - modify_image_num) { // 이미지 업로드끝나면 진행
                                if(modify_mode) modify(path);
                                else posting(path);
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
        } else { // 이미지 업로드할 것 없을시
            if(modify_mode) modify(path);
            else posting(path);
        }
    }

    public void posting(ArrayList<String> path) {

        ConnectDB connectDB = Request.getRetrofit().create(ConnectDB.class);
        Call<String> call = connectDB.writePost(board_title_id, user_id, Request.filter(write_title.getText().toString()), Request.filter(write_description.getText().toString()), path, Request.getTime("yyyy-MM-dd HH:mm:ss"));

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                String result = response.body().trim();
                if(result.equals("success")) { //글쓰기 성공했을시
                    AlertDialog.Builder builder = new AlertDialog.Builder(BoardWriteActivity.this);
                    builder.setTitle("메시지");
                    builder.setMessage("성공적으로 게시되었습니다.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(RESULT_OK);
                            finish();
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

    public void setWriteImage(ClipData clip_data) { // 갤러리에서 받아온 clip_data의 실제경로로 adapter에 이미지등록
        if (clip_data != null) {
            for (int i = 0; i < clip_data.getItemCount(); i++) {
                    board_write_image_adapter.add(getRealPathFromURI(clip_data.getItemAt(i).getUri()));
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) { // 불러온 이미지의 uri로 실제 경로 구하는 메소드

        String[] proj = { MediaStore.Images.Media.DATA };

        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        cursor.moveToNext();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));

        cursor.close();
        return path;
    }

    class BoardWriteImageAdapter extends RecyclerView.Adapter<BoardWriteImageAdapter.ViewHolder> {
       List<String> items = new ArrayList<>();

       class ViewHolder extends RecyclerView.ViewHolder {
            ImageView img;
            public ViewHolder(View itemView) {
                super(itemView);
                img = (ImageView) itemView.findViewById(R.id.image_view);
            }

            public void setData(String data) {
                Glide.with(BoardWriteActivity.this)
                        .load(data)
                        .into(img);
            }

        }

        public void removeItem(int position) {
           if(modify_mode && position < modify_image_num) {
               modify_image_num--;
               delete_images.add(items.get(position).substring(items.get(position).lastIndexOf("/")+1));
           }
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
        Intent it = new Intent();
        it.setAction(Intent.ACTION_PICK);
        it.setType("image/*");
        it.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // 다중 선택 가능
        startActivityForResult(Intent.createChooser(it, "Get Image"), GET_GALLERY_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            setWriteImage(data.getClipData()); // 갤러리에서 가져온 데이터로 setWriteImage 진행
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish(); // 메뉴의 뒤로가기 클릭시 종료
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void modify(ArrayList<String> path) {

        ConnectDB connectDB = Request.getRetrofit().create(ConnectDB.class);
        Call<Post> call = connectDB.modifyPost(board_id, Request.filter(write_title.getText().toString()), Request.filter(write_description.getText().toString()), path, delete_images);
        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {

                Post result = response.body();
                if(delete_images.size() != 0) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            
                            List<DeleteObjectsRequest.KeyVersion> key = new ArrayList<>();
                            for (int i = 0; i < delete_images.size(); i++) {
                                key.add(new DeleteObjectsRequest.KeyVersion("board_images/" + delete_images.get(i)));
                            }
                            Request.getAmazonS3(BoardWriteActivity.this).deleteObjects(new DeleteObjectsRequest("myongjimoa").withKeys(key));
                        }
                    }).start();
                }
                Intent it = new Intent();
                it.putExtra("title", result.getTitle());
                it.putExtra("description", result.getDescription());
                it.putExtra("recommend_num", result.getRecommend_num() + "");
                it.putExtra("images", result.getImages());
                setResult(RESULT_OK, it); // 수정이 끝나면 가져온 데이터 intent에 담아서 종료
                finish();
            }
            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                Log.d("글쓰기 연결 실패", t.getMessage());
            }
        });
    }
}
