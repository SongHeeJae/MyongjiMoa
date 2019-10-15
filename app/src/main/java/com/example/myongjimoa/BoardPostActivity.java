package com.example.myongjimoa;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.bumptech.glide.Glide;

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

public class BoardPostActivity extends AppCompatActivity {

    TextView post_number;
    TextView post_major;
    TextView post_nickname;
    TextView post_date;
    TextView post_title;
    TextView post_description;
    BoardPostCommentAdapter board_post_comment_adapter;
    BoardPostImageAdapter board_post_image_adapter;
    Button comment_submit;
    EditText write_comment;

    RecyclerView comment_recycler_view;
    RecyclerView image_recycler_view;
    public GestureDetector gesture_detector;

    Button recommend_button;
    TextView recommend_num;

    Post post;
    String user_id;
    String user_nickname;

    private final int MODIFY_REQUEST_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board_post);

        Intent it = getIntent();
        post = new Post(it.getStringExtra("id"), it.getStringExtra("title"), it.getStringExtra("description"), it.getStringExtra("number"), it.getStringExtra("major"), it.getStringExtra("date"), it.getStringExtra("nickname"), it.getStringArrayListExtra("images"), Integer.parseInt(it.getStringExtra("recommend_num")));
        user_id = it.getStringExtra("user_id");
        user_nickname = it.getStringExtra("user_nickname");
        // 받아온 정보 저장

        post_nickname = (TextView) findViewById(R.id.post_nickname);
        post_date = (TextView) findViewById(R.id.post_date);
        post_title = (TextView) findViewById(R.id.post_title);
        post_description = (TextView) findViewById(R.id.post_description);
        comment_submit = (Button) findViewById(R.id.comment_submit);
        write_comment = (EditText) findViewById(R.id.write_comment);
        recommend_button = (Button) findViewById(R.id.recommend_button);
        recommend_num = (TextView) findViewById(R.id.recommend_num);
        post_number = (TextView) findViewById(R.id.post_number);
        post_major = (TextView) findViewById(R.id.post_major);

        board_post_comment_adapter = new BoardPostCommentAdapter();
        board_post_image_adapter = new BoardPostImageAdapter();

        comment_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // 댓글 작성 완료 버튼 클릭 시 댓글 업로드 진행
                commenting();
            }
        });

        post_nickname.setText(post.getNickname());
        post_date.setText(post.getDate());
        post_title.setText(post.getTitle());
        post_description.setText(post.getDescription());
        post_number.setText(post.getNumber());
        post_major.setText(post.getMajor());
        recommend_num.setText(post.getRecommend_num() + "");
        recommend_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recommending();
            }
        });
        for(int i=0; i<post.getImages().size(); i++) {
            board_post_image_adapter.add("https://myongjimoa.s3.ap-northeast-2.amazonaws.com/board_images/" + post.getImages().get(i));
        } // 이미지 경로 등록

        comment_recycler_view = (RecyclerView) findViewById(R.id.post_comment_recycler_view);
        image_recycler_view = (RecyclerView) findViewById(R.id.post_image_recycler_view);

        RecyclerView.LayoutManager vertical_layout_manager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        comment_recycler_view.setLayoutManager(vertical_layout_manager);

        comment_recycler_view.setAdapter(board_post_comment_adapter);

        RecyclerView.LayoutManager horizontal_layout_manager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        image_recycler_view.setLayoutManager(horizontal_layout_manager);

        image_recycler_view.setAdapter(board_post_image_adapter);
        // 각각의 RecyclerView에 layout 지정

        gesture_detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });


        image_recycler_view.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View childView = rv.findChildViewUnder(e.getX(), e.getY());

                if(childView != null && gesture_detector.onTouchEvent((e))) {
                    int currentPos = rv.getChildAdapterPosition(childView);
                    Intent it = new Intent(BoardPostActivity.this, ImageActivity.class);
                    it.putStringArrayListExtra("images", new ArrayList<>(board_post_image_adapter.getItems()));
                    it.putExtra("current", currentPos);
                    startActivity(it); // 이미지 중 하나 클릭시 ImageActivity로 이동
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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        downloadComment(); // 댓글 다운로드 처리

    }

    public void downloadComment() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(GsonConverterFactory.create()) // JSON 형태로 받아옴
                .build(); // 통신 라이브러리 retrofit객체 생성

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
        Call<List<Comment>> call = connectDB.downloadComments(post.getId());
        call.enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                List<Comment> result = response.body();
                if(result != null) {
                    if (result.size() != 0) {
                        for (int i = 0; i < result.size(); i++) {
                            board_post_comment_adapter.add(new Comment(result.get(i).getId(), result.get(i).getNumber(), result.get(i).getMajor(), result.get(i).getComment(), result.get(i).getDate(), result.get(i).getNickname()));
                        } // 어댑터에 담아줌
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                AlertDialog.Builder builder = new AlertDialog.Builder(BoardPostActivity.this);
                builder.setTitle("알림");
                builder.setMessage("존재하지않는 게시글입니다."); // 댓글 작성 실패 시 존재하지 않는 게시글
                builder.setCancelable(false);
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.show();
            }
        });
    }

    public void recommending() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(ScalarsConverterFactory.create()) // 문자열로 받기 위함.
                .build();

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
        Call<String> call = connectDB.recommendPost(post.getId(), user_id);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                String result = response.body().trim();

                if(result.equals("removed")) { // 삭제된 게시글일시
                    AlertDialog.Builder builder = new AlertDialog.Builder(BoardPostActivity.this);
                    builder.setTitle("알림");
                    builder.setMessage("존재하지않는 게시글입니다.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    builder.show();
                } else if(!result.equals("failed")) { // 정상 처리되었을 시
                    post.setRecommend_num(Integer.parseInt(result));
                    recommend_num.setText(post.getRecommend_num() + "");
                } else { // failed 이면 이미 추천 게시글 알림띄어줌
                    AlertDialog.Builder builder = new AlertDialog.Builder(BoardPostActivity.this);
                    builder.setTitle("메시지");
                    builder.setMessage("이미 추천한 게시글입니다.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("포스트 작성 실패", t.getMessage());
            }
        });
    }

    public void commenting() {


        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String format_date = sdf.format(date); // 서버에 전송할 현재 시간

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(ScalarsConverterFactory.create()) // 문자열로 받기 위함.
                .build();

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
        Call<String> call = connectDB.writeComment(post.getId(), user_id, write_comment.getText().toString(), format_date);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                String result = response.body().trim();

                if(result.equals("success")) { // 댓글 작성 성공
                    AlertDialog.Builder builder = new AlertDialog.Builder(BoardPostActivity.this);
                    builder.setTitle("메시지");
                    builder.setMessage("댓글 작성에 성공하였습니다.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            reloadComment();
                        }
                    });
                    InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(write_comment.getWindowToken(), 0); // 화면에 올라온 키보드 내려줌
                    write_comment.setText(""); // 댓글 창 초기화
                    builder.show();
                } else {
                    // 실패 시 존재하지 않는 게시글
                    AlertDialog.Builder builder = new AlertDialog.Builder(BoardPostActivity.this);
                    builder.setTitle("알림");
                    builder.setMessage("존재하지않는 게시글입니다.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    builder.show();
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("포스트 작성 실패", t.getMessage());
            }
        });
    }

    public void reloadComment() {
        board_post_comment_adapter = new BoardPostCommentAdapter();
        comment_recycler_view.setAdapter(board_post_comment_adapter);
        downloadComment();
    }

    // 코멘트 처리 어댑터
    public class BoardPostCommentAdapter extends RecyclerView.Adapter<BoardPostCommentAdapter.ViewHolder> {
        List<Comment> items = new ArrayList<Comment>();

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView comment_nickname;
            TextView comment;
            TextView comment_date;
            TextView comment_number;
            TextView comment_major;
            Button comment_button;
            int pos;
            public ViewHolder(View itemView) {
                super(itemView);
                comment_nickname = (TextView) itemView.findViewById(R.id.comment_nickname);
                comment = (TextView) itemView.findViewById(R.id.comment);
                comment_date = (TextView) itemView.findViewById(R.id.comment_date);
                comment_major = (TextView) itemView.findViewById(R.id.comment_major);
                comment_number = (TextView) itemView.findViewById(R.id.comment_number);
                comment_button = (Button) itemView.findViewById(R.id.comment_button);
                comment_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(BoardPostActivity.this);

                        DialogInterface.OnClickListener dialog_listener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0) {
                                    reportCurrentComment(items.get(pos).getId());
                                } else if (which == 1) {
                                    removeCurrentComment(items.get(pos).getId());
                                }
                            }
                        };
                        if(items.get(pos).getNickname().equals(user_nickname) || MainActivity.admin) builder.setItems(new String[]{"신고", "삭제"}, dialog_listener);
                        else builder.setItems(new String[]{"신고"}, dialog_listener);
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                });
            }

            public void setData(Comment item, int position) {
                //값 읽어오기
                comment_nickname.setText(item.getNickname());
                comment.setText(item.getComment());
                comment_date.setText(item.getDate());
                comment_major.setText(item.getMajor());
                comment_number.setText(item.getNumber());
                pos=position;
            }
        }

        public void add(Comment item) {
            items.add(item);
            notifyDataSetChanged(); // 뭔지 검색
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.board_comment_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.setData(items.get(position), position);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    public class BoardPostImageAdapter extends RecyclerView.Adapter<BoardPostImageAdapter.ViewHolder> {
        List<String> items = new ArrayList<String>();

        class ViewHolder extends RecyclerView.ViewHolder {

            ImageView img;
            public ViewHolder(View itemView) {
                super(itemView);
                img = (ImageView) itemView.findViewById(R.id.image_item);
            }

            public void setData(String item) {
                //값 읽어오기
                Glide.with(BoardPostActivity.this)
                        .load(item)
                        .override(500)
                        .into(img);
            }
        }

        public void add(String item) {
            items.add(item);
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_image, parent, false);
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

        public List<String> getItems() {
            return items;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_refresh, menu);
        menu.findItem(R.id.menu_search).setVisible(false);
        if(!(post.getNickname().equals(user_nickname) || MainActivity.admin)) {
            menu.findItem(R.id.menu_modify).setVisible(false);
            menu.findItem(R.id.menu_remove).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_report:
                reportCurrentPost();
                return true;

            case R.id.menu_refresh:
                reloadComment();
                return true;

            case R.id.menu_remove:
                removeCurrentPost();
                return true;

            case R.id.menu_modify:
                modifyCurrentPost(post.getId());
                return true;

            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void removeCurrentPost() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
        Call<String> call = connectDB.removePost(post.getId());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                String result = response.body().trim();
                if (result.equals("success")) {
                    if(post.getImages().size() != 0) {
                        new Thread(new Runnable() { // 메인스레드에서는 통신작업을 수행할 수 없어서 새로운 스레드 생성. 데이터베이스 서버에서의 삭제가 끝난뒤, 파일 서버에서의 이미지 삭제 이어서 진행
                            @Override
                            public void run() {
                                // Amazon Cognito 인증 공급자 초기화
                                CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                                        BoardPostActivity.this,
                                        "ap-northeast-2:9c5bb2b0-44a8-4a1c-944a-98d817d44e82", // 자격 증명 풀 ID
                                        Regions.AP_NORTHEAST_2 // 리전
                                );

                                AmazonS3 s3 = new AmazonS3Client(credentialsProvider, Region.getRegion(Regions.AP_NORTHEAST_2));
                                s3.setEndpoint("s3.ap-northeast-2.amazonaws.com");

                                List<DeleteObjectsRequest.KeyVersion> key = new ArrayList<>();
                                for (int i = 0; i < post.getImages().size(); i++) {
                                    key.add(new DeleteObjectsRequest.KeyVersion("board_images/" + post.getImages().get(i)));
                                }
                                s3.deleteObjects(new DeleteObjectsRequest("myongjimoa").withKeys(key));
                            }
                        }).start();
                    }
                    setResult(RESULT_OK);
                    finish(); // 삭제 후 현재 Activity 종료
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("실패", t.getMessage());
            }
        });
    }

    public void reportCurrentPost() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(ScalarsConverterFactory.create()) // 문자열로 받기 위함.
                .build();

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
        Call<String> call = connectDB.reportPost(post.getId(), user_id);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                String result = response.body().trim();

                if(result.equals("removed")) { // 이미 삭제된 게시글일시
                    AlertDialog.Builder builder = new AlertDialog.Builder(BoardPostActivity.this);
                    builder.setTitle("알림");
                    builder.setMessage("존재하지않는 게시글입니다.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    builder.show();
                } else if(!result.equals("failed")) { // 신고 정상 수행
                    if(Integer.parseInt(result) >= 5) { // 신고 결과가 5회 이상이면 삭제 진행
                        removeCurrentPost();
                    }
                } else { // 이미 신고한 게시글일시
                    AlertDialog.Builder builder = new AlertDialog.Builder(BoardPostActivity.this);
                    builder.setTitle("메시지");
                    builder.setMessage("이미 신고한 게시글입니다.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("포스트 작성 실패", t.getMessage());
            }
        });
    }

    public void removeCurrentComment(String comment_id) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
        Call<String> call = connectDB.removeComment(comment_id);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                String result = response.body().trim();
                if (result.equals("success")) { // 정상 삭제 수행시 댓글 reload
                    reloadComment();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("실패", t.getMessage());
            }
        });
    }

    public void reportCurrentComment(final String comment_id) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(ScalarsConverterFactory.create()) // 문자열로 받기 위함.
                .build();

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
        Call<String> call = connectDB.reportComment(comment_id, user_id);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                String result = response.body().trim();

                if(result.equals("removed")) { // 게시글 삭제되었을경우, 댓글 삭제되었을경우
                    AlertDialog.Builder builder = new AlertDialog.Builder(BoardPostActivity.this);
                    builder.setTitle("알림");
                    builder.setMessage("존재하지않는 댓글입니다.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    builder.show();
                } else if(!result.equals("failed")) {
                    if(Integer.parseInt(result) >= 5) { // 5회 이상일 시 삭제처리
                        removeCurrentComment(comment_id);
                    }
                } else { // 이미 신고했을 시
                    AlertDialog.Builder builder = new AlertDialog.Builder(BoardPostActivity.this);
                    builder.setTitle("메시지");
                    builder.setMessage("이미 신고한 댓글입니다.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //
                        }
                    });
                    builder.show();
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("포스트 작성 실패", t.getMessage());
            }
        });
    }

    public void modifyCurrentPost(String board_id) {
        Intent it = new Intent(BoardPostActivity.this, BoardWriteActivity.class);
        it.putExtra("modify_mode", true);
        it.putExtra("title", post.getTitle());
        it.putExtra("description", post.getDescription());
        it.putExtra("modify_images", post.getImages());
        it.putExtra("board_id", board_id);
        it.putExtra("user_id", user_id);
        startActivityForResult(it, MODIFY_REQUEST_CODE); // 현재 게시글 정보 가지고 글쓰기 화면으로 이동
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == MODIFY_REQUEST_CODE){ // 수정이 정상적으로 진행되고 돌아왔으면, 그 정보로 화면 업데이트
            post.setTitle(data.getStringExtra("title"));
            post.setDescription(data.getStringExtra("description"));
            post.setRecommend_num(Integer.parseInt(data.getStringExtra("recommend_num")));
            post.setImages(data.getStringArrayListExtra("images"));
            board_post_image_adapter = new BoardPostImageAdapter();
            for(int i=0; i<post.getImages().size(); i++) {
                board_post_image_adapter.add("https://myongjimoa.s3.ap-northeast-2.amazonaws.com/board_images/" + post.getImages().get(i));
            }
            image_recycler_view.setAdapter(board_post_image_adapter);
            post_title.setText(post.getTitle());
            post_description.setText(post.getDescription());
            recommend_num.setText(post.getRecommend_num() + "");
            reloadComment(); // 댓글도 reload
            setResult(RESULT_OK); // 게시판 화면으로 돌아갔을 때 reload 일어날수 있도록 설정
        }
    }
}
