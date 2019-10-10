package com.example.myongjimoa;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.TextView;

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
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class BoardWriteActivity extends AppCompatActivity {

    EditText write_title;
    EditText write_description;
    Button write_submit;
    Button picture;
    RecyclerView recycler_view;
    BoardWriteImageAdapter board_write_image_adapter;
    public GestureDetector gesture_detector;
    ArrayList<String> path;
    int upload_count;

    String user_id;
    String board_title_id;

    private final int GET_GALLERY_IMAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board_write);

        write_title = (EditText) findViewById(R.id.write_title);
        write_description = (EditText) findViewById(R.id.write_description);
        write_submit = (Button) findViewById(R.id.write_submit);
        picture = (Button) findViewById(R.id.picture);
        recycler_view = (RecyclerView) findViewById(R.id.board_write_image);

        Intent it = getIntent();
        user_id = it.getStringExtra("user_id");
        board_title_id = it.getStringExtra("board_title_id");

        path =  new ArrayList<>();

        board_write_image_adapter = new BoardWriteImageAdapter();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        recycler_view.setLayoutManager(layoutManager);
        recycler_view.setAdapter(board_write_image_adapter);
        recycler_view.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View childView = rv.findChildViewUnder(e.getX(), e.getY());

                if(childView != null && gesture_detector.onTouchEvent((e))) {
                    int currentPos = rv.getChildAdapterPosition(childView);
                    board_write_image_adapter.removeItem(currentPos);
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
                imageUpload();
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
    }


    public void imageUpload() {

        if (board_write_image_adapter.getItemCount() > 0) {

            // 이미지 업로드 부분에서 이미지 업로드가 아직 실행중인데 다른쪽에서 게시글 정보가 업데이트된다면? 파일업로드처리가 끝난후, 게시글 업로드하도록 수정할필요있음
            // 옵저버패턴 https://flowarc.tistory.com/entry/%EB%94%94%EC%9E%90%EC%9D%B8-%ED%8C%A8%ED%84%B4-%EC%98%B5%EC%A0%80%EB%B2%84-%ED%8C%A8%ED%84%B4Observer-Pattern
            // 콜백으로 구현, 브로드캐스팅으로 구현

            Date date = new Date(); // 시스템 시간으로 구함 동기화되는지 확인 필요
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            String format_date = sdf.format(date);

            // Amazon Cognito 인증 공급자 초기화
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    this,
                    "ap-northeast-2:9c5bb2b0-44a8-4a1c-944a-98d817d44e82", // 자격 증명 풀 ID
                    Regions.AP_NORTHEAST_2 // 리전
            );

            TransferObserver observer;
            AmazonS3 s3 = new AmazonS3Client(credentialsProvider, Region.getRegion(Regions.AP_NORTHEAST_2));
            TransferUtility transfer_utility = TransferUtility.builder().s3Client(s3).context(this).build();
            s3.setEndpoint("s3.ap-northeast-2.amazonaws.com");

            for (int i = 0; i < board_write_image_adapter.getItemCount(); i++) {
                String name = board_title_id + "_"  + user_id + "_" + format_date + "_" + i;
                File file = new File(board_write_image_adapter.getItem(i));
                observer = transfer_utility.upload(
                        "myongjimoa/board_images",
                        name, // 이미지 파일이름설정 개별적으로 중복안되게구성해야함. user_id + 현재시간?
                        file
                ); // 파일 여러개 동시 업로드하는거 찾아야됨
                Log.d("파일업로드횟수", i + "");
                path.add(name);
                observer.setTransferListener(new TransferListener() {
                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        Log.d("s3", "onStateChanged ㅇㅇㅇㅇㅇㅇㅇㅇ");
                        if (TransferState.COMPLETED == state) {
                            Log.d("전송완료", "ㅇㅇ"); // 여기다 콜백으로 구현
                            upload_count++;
                            if (upload_count == board_write_image_adapter.getItemCount()) {
                                Log.d("이미지업로드끝", "ㅇㅇ");
                                posting(path); // 콜백으로 구현 근데 속도느림 개선필요 동시파일업로드하도록
                                upload_count = 0; // 삭제 코드
                            }
                        }
                    }
                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                        Log.d("s3", "onProgressChanged ㅇㅇㅇㅇㅇㅇㅇㅇ"); //프로그레스바
                    }

                    @Override
                    public void onError(int id, Exception ex) {
                        Log.d("s3에러", ex.getMessage());
                    }
                });
            }
        } else {
            posting(path);
        }
    }

    public void posting(ArrayList<String> path) {

        Log.d("dd", "다음줄 실행ㅇㅇㅇㅇㅇ");

        Date date = new Date(); // 시스템 시간으로 구함 동기화되는지 확인 필요
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String format_date = sdf.format(date);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(ScalarsConverterFactory.create()) // 문자열로 받기 위함.
                .build();

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
        Call<String> call = connectDB.writePost(board_title_id, user_id, write_title.getText().toString(), write_description.getText().toString(), path, format_date);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                String result = response.body().trim();
                Log.d("result는?", result);
                if(result.equals("success")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(BoardWriteActivity.this);
                    builder.setTitle("메시지");
                    builder.setMessage("글쓰기에 성공하였습니다.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(RESULT_OK);
                            finish();
                        }
                    });
                    builder.show();
                } else {
                    Log.d("글쓰기실패", "반환값없음");
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("글쓰기 연결 실패", t.getMessage());
            }
        });
    }

    public void setWriteImage(ClipData clip_data) {
        if (clip_data != null) {
            for (int i = 0; i < clip_data.getItemCount(); i++) {
                    board_write_image_adapter.add(getRealPathFromURI(clip_data.getItemAt(i).getUri()));
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {

        String[] proj = { MediaStore.Images.Media.DATA };

        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        cursor.moveToNext();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
        Uri uri = Uri.fromFile(new File(path));

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
                //값 읽어오기
               // img.setImageBitmap(data);
                // download 경로 못읽는거 버그 오류
                Glide.with(BoardWriteActivity.this)
                        .load(data)
                        .override(500)
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

    public void getGallery() {
        Intent it = new Intent();
        it.setAction(Intent.ACTION_PICK);
        it.setType("image/*");
        it.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(it, "Get Image"), GET_GALLERY_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            setWriteImage(data.getClipData());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
