package com.example.myongjimoa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BoardMainActivity extends AppCompatActivity {

    RecyclerView recycler_view;
    BoardTitleAdapter m_adapter;

    public GestureDetector gesture_detector;
    String user_id;
    String user_nickname;
    String user_major;

    Button my_major;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board_main);

        my_major = (Button) findViewById(R.id.my_major);
        my_major.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(BoardMainActivity.this, BoardActivity.class);

                Board b = m_adapter.getBoard(user_major);
                if(b != null) { // 내 학과 게시판 버튼 클릭시 해당하는 학과 게시판으로 이동
                    it.putExtra("board_title_id", b.getId());
                    it.putExtra("board_title", b.getTitle());
                    it.putExtra("user_id", user_id);
                    it.putExtra("user_nickname", user_nickname);
                    startActivity(it);
                } else
                    Toast.makeText(BoardMainActivity.this, "올바른 학과를 기입해주세요", Toast.LENGTH_SHORT).show();
            }
        });

        Intent it = getIntent();
        user_id = it.getStringExtra("user_id");
        user_nickname = it.getStringExtra("user_nickname");
        user_major = it.getStringExtra("user_major"); // 회원정보 저장해둠

        recycler_view = (RecyclerView) findViewById(R.id.title_recycler_view);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recycler_view.setLayoutManager(layoutManager); // RecyclerView에 수직방향 레이아웃지정함
        m_adapter = new BoardTitleAdapter();
        recycler_view.setAdapter(m_adapter); // 어댑터 지정

        gesture_detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        }); // RecyclerView 아이템 터치 리스너에 사용

        recycler_view.addOnItemTouchListener(new RecyclerView.OnItemTouchListener()
        {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View childView = rv.findChildViewUnder(e.getX(), e.getY());
                // RecyclerView 아이템 터치 리스너. 아이템 터치 시 회원정보, 게시판 정보 가지고 해당 게시판으로 이동
                if(childView != null && gesture_detector.onTouchEvent((e))) {
                    int currentPos = rv.getChildAdapterPosition(childView);
                    Intent it = new Intent(BoardMainActivity.this, BoardActivity.class);
                    it.putExtra("board_title_id", m_adapter.getBoard(currentPos).getId());
                    it.putExtra("board_title", m_adapter.getBoard(currentPos).getTitle());
                    it.putExtra("user_id", user_id);
                    it.putExtra("user_nickname", user_nickname);
                    startActivity(it);
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

        downloadBoardList(); // 게시판 목록 다운로드

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 상단 메뉴의 뒤로가기 버튼 열어줌
    }

    void downloadBoardList() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(GsonConverterFactory.create()) // JSON 형태로 받아옴
                .build(); // retrofit 통신 라이브러리 객체 생성

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
        Call<List<Board>> call = connectDB.downloadBoard(); // List<Board> 형태로 받아옴
        call.enqueue(new Callback<List<Board>>() {
            @Override
            public void onResponse(Call<List<Board>> call, Response<List<Board>> response) {

                List<Board> result = response.body();
                if(result != null) {
                    if (result.size() != 0) {
                        for (int i = 0; i < result.size(); i++) { // 게시판 목록 어댑터에 담아줌
                            m_adapter.add(new Board(result.get(i).getId(), result.get(i).getTitle()));
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Board>> call, Throwable t) {
                Log.d("실패", t.getMessage());
            }

        });
    }

    class BoardTitleAdapter extends RecyclerView.Adapter<BoardTitleAdapter.ViewHolder> {
        List<Board> items = new ArrayList<>();

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView text;

            public ViewHolder(View itemView) {
                super(itemView);
                text = (TextView) itemView.findViewById(R.id.title);

            }

            public void setData(Board data) {
                //값 읽어오기
                text.setText(data.getTitle());
            }
        }

        public void add(Board item) {
            items.add(item);
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.board_title_item, parent, false);
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

        public Board getBoard(int pos) {
            return items.get(pos);
        }

        public Board getBoard(String major) {
            for(int i=0; i<items.size(); i++) {
                if (items.get(i).getTitle().equals(major)) return items.get(i);
            }
            return null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: // 뒤로가기 버튼
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
