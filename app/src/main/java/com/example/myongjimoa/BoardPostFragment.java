package com.example.myongjimoa;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class BoardPostFragment extends Fragment {

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


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.board_post, container, false);

        view.setBackgroundColor(Color.rgb(255, 255, 255));

        setHasOptionsMenu(true);


        post_nickname = (TextView) view.findViewById(R.id.post_nickname);
        post_date = (TextView) view.findViewById(R.id.post_date);
        post_title = (TextView) view.findViewById(R.id.post_title);
        post_description = (TextView) view.findViewById(R.id.post_description);
        comment_submit = (Button) view.findViewById(R.id.comment_submit);
        write_comment = (EditText) view.findViewById(R.id.write_comment);
        recommend_button = (Button) view.findViewById(R.id.recommend_button);
        recommend_num = (TextView) view.findViewById(R.id.recommend_num);
        board_post_comment_adapter = new BoardPostCommentAdapter();
        board_post_image_adapter = new BoardPostImageAdapter();

        comment_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commenting();
            }
        });

        Post post = ((BoardActivity)getActivity()).getPost();

        post_nickname.setText(post.getNickname());
        post_date.setText(post.getDate());
        post_title.setText(post.getTitle());
        post_description.setText(post.getDescription());
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

        comment_recycler_view = (RecyclerView) view.findViewById(R.id.post_comment_recycler_view);
        image_recycler_view = (RecyclerView) view.findViewById(R.id.post_image_recycler_view);

        RecyclerView.LayoutManager vertical_layout_manager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        comment_recycler_view.setLayoutManager(vertical_layout_manager);

        comment_recycler_view.setAdapter(board_post_comment_adapter);

        RecyclerView.LayoutManager horizontal_layout_manager = new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false);
        image_recycler_view.setLayoutManager(horizontal_layout_manager);

        image_recycler_view.setAdapter(board_post_image_adapter);


        gesture_detector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

        comment_recycler_view.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
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

        setHasOptionsMenu(true);

        downloadComment();

        return view;
    }

    public void downloadComment() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
        Call<List<Comment>> call = connectDB.downloadComments(((BoardActivity)getActivity()).getPost().getId());
        call.enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                Log.d("실행됨", "ㅋㅋ");
                List<Comment> result = response.body();
                if(result.size() != 0) {
                    for(int i=0; i<result.size(); i++) {
                        board_post_comment_adapter.add(new Comment(result.get(i).getId(), result.get(i).getE_mail(), result.get(i).getComment(), result.get(i).getDate(), result.get(i).getNickname()));
                    }
                } else {
                    Log.d("댓글없음", "댓글없음");
                }
            }
            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                Log.d("코멘트 실패", t.getMessage());
            }
        });
    }

    public void recommending() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(ScalarsConverterFactory.create()) // 문자열로 받기 위함.
                .build();

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
        Call<String> call = connectDB.recommendPost(((BoardActivity)getActivity()).getPost().getId(), ((BoardActivity)getActivity()).user_id);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                String result = response.body().trim();
                Log.d("추천 성공", result);

                if(!result.equals("failed")) {
                    ((BoardActivity)getActivity()).getPost().setRecommend_num(Integer.parseInt(result));
                    recommend_num.setText(((BoardActivity)getActivity()).getPost().getRecommend_num() + "");
               /*     AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("메시지");
                    builder.setMessage("추천하였습니다.");
                    builder.setCancelable(false);

                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 추천 수 업데이트 현재 포스트업데이트, 뷰에 다시그리기

                        }
                    });
                    builder.show();*/
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        Call<String> call = connectDB.writeComment(((BoardActivity)getActivity()).getPost().getId(), ((BoardActivity)getActivity()).user_id, write_comment.getText().toString(), format_date);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                    String result = response.body().trim();
                    Log.d("댓글 쓰기 성공", result);

                    if(result.equals("success")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("메시지");
                        builder.setMessage("댓글 작성에 성공하였습니다.");
                        builder.setCancelable(false);
                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                reloadComment();
                            }
                        });
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(write_comment.getWindowToken(), 0);
                        write_comment.setText("");
                        builder.show();
                    } else {
                        Log.d("댓글쓰기성공", "반환값없음");
                    }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("포스트 작성 실패", t.getMessage());
            }
        });
    }

    public void reloadComment() {
        //board_post_comment_adapter = null;
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
            public ViewHolder(View itemView) {
                super(itemView);
                comment_nickname = (TextView) itemView.findViewById(R.id.comment_nickname);
                comment = (TextView) itemView.findViewById(R.id.comment);
                comment_date = (TextView) itemView.findViewById(R.id.comment_date);
            }

            public void setData(Comment item) {
                //값 읽어오기
                comment_nickname.setText(item.getNickname());
                comment.setText(item.getComment());
                comment_date.setText(item.getDate());
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
            holder.setData(items.get(position));
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
                Glide.with(BoardPostFragment.this.getActivity())
                        .load(item)
                        .override(500)
                        .into(img);

            }
        }

        public void add(String item) {
            items.add(item);
            notifyDataSetChanged(); // 뭔지 검색
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.menu_search).setVisible(false);
        menu.findItem(R.id.menu_menus).setVisible(true);
    }
}
