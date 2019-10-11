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
            public void onClick(View v) {
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
            Log.d("실행됨", post.getImages().get(i));
            board_post_image_adapter.add("https://myongjimoa.s3.ap-northeast-2.amazonaws.com/board_images/" + post.getImages().get(i));
        }

        comment_recycler_view = (RecyclerView) findViewById(R.id.post_comment_recycler_view);
        image_recycler_view = (RecyclerView) findViewById(R.id.post_image_recycler_view);

        RecyclerView.LayoutManager vertical_layout_manager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        comment_recycler_view.setLayoutManager(vertical_layout_manager);

        comment_recycler_view.setAdapter(board_post_comment_adapter);

        RecyclerView.LayoutManager horizontal_layout_manager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        image_recycler_view.setLayoutManager(horizontal_layout_manager);

        image_recycler_view.setAdapter(board_post_image_adapter);


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

        downloadComment();

    }

    public void downloadComment() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

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
                        }
                    } else {
                        Log.d("댓글없음", "ㅇㅇ");
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
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
                Log.d("추천 성공", result);

                if(result.equals("removed")) {
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
                } else if(!result.equals("failed")) {
                    post.setRecommend_num(Integer.parseInt(result));
                    recommend_num.setText(post.getRecommend_num() + "");
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(BoardPostActivity.this);
                    builder.setTitle("메시지");
                    builder.setMessage("이미 추천한 게시글입니다.");
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

    public void commenting() {


        Date date = new Date(); // 시스템 시간으로 구함 동기화되는지 확인 필요
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String format_date = sdf.format(date);

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
                Log.d("댓글 쓰기 성공", result);

                if(result.equals("success")) {
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
                    imm.hideSoftInputFromWindow(write_comment.getWindowToken(), 0);
                    write_comment.setText("");
                    builder.show();
                } else {
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
            String item = items.get(position);


            Log.d("ㅇㅇ", "실행됨");
        }

        @Override
        public int getItemCount() {
            return items.size();
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
                // 게시글창에서 신고눌렀을떄 처리
                reportCurrentPost();
                return true;

            case R.id.menu_modify:
                reloadComment();
                return true;

            case R.id.menu_remove:
                removeCurrentPost();
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
                        new Thread(new Runnable() {
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
                    } else {
                        Log.d("이미지없음", "이미지없음");
                    }
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Log.d("삭제오류", "삭제오류");
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

                if(result.equals("removed")) {
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
                } else if(!result.equals("failed")) {
                    if(Integer.parseInt(result) >= 5) {
                        removeCurrentPost();
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(BoardPostActivity.this);
                    builder.setTitle("메시지");
                    builder.setMessage("이미 신고한 게시글입니다.");
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
                if (result.equals("success")) {
                    reloadComment();
                } else {
                    Log.d("삭제오류", "삭제오류");
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

                if(result.equals("removed")) { // 게시글 삭제되었을경우, 댓글이 삭제되었을경우. 댓글삭제되었으면 다시 게시글들어와서 댓글받아야함.
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
                    if(Integer.parseInt(result) >= 5) {
                        removeCurrentComment(comment_id);
                    }
                } else {
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
}
