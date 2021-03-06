package com.example.myongjimoa;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BoardMainActivity extends AppCompatActivity {

    RecyclerView recycler_view;
    BoardTitleAdapter m_adapter;

    public GestureDetector gesture_detector;
    String user_id;
    String user_nickname;
    String user_major;
    ImageButton my_major;
    private AdView mAdView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board_main);


        mAdView = findViewById(R.id.board_main_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        setTitle("게시판");

        my_major = (ImageButton) findViewById(R.id.my_major);

        if (!isNetworkConnected()) { // false 인 경우 네트워크 연결 안되어있음.
            AlertDialog.Builder builder = new AlertDialog.Builder(BoardMainActivity.this);
            builder.setTitle("메시지")
                    .setMessage("네트워크 연결을 확인해 주세요.")
                    .setCancelable(false)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            builder.show();
            return ;
        }

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

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    void downloadBoardList() {

        ConnectDB connectDB = Request.getRetrofit().create(ConnectDB.class);
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
            }

        });
    }

    class BoardTitleAdapter extends RecyclerView.Adapter<BoardTitleAdapter.ViewHolder> {
        List<Board> items = new ArrayList<>();

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView text;
            LinearLayout linearLayout;

            public ViewHolder(View itemView) {
                super(itemView);
                text = (TextView) itemView.findViewById(R.id.title);
            }

            public void setData(Board data) {
                //값 읽어오기

                linearLayout = (LinearLayout) itemView.findViewById(R.id.recycle_title);
                RecyclerView.LayoutParams p1 = (RecyclerView.LayoutParams) linearLayout.getLayoutParams();

                if(data.getTitle().equals("자유게시판")) {
                    linearLayout.setBackgroundResource(R.color.colorboard1);
                    p1.leftMargin = 13;
                    p1.rightMargin = 13;
                    text.setText(" > " + data.getTitle());
                }
                else if(data.getTitle().equals("명지장터")) {
                    linearLayout.setBackgroundResource(R.color.colorboard1);
                    p1.leftMargin = 13;
                    p1.rightMargin = 13;
                    text.setText(" > " + data.getTitle());
                }
                else if(data.getTitle().equals("동아리게시판")) {
                    linearLayout.setBackgroundResource(R.color.colorboard1);
                    p1.leftMargin = 13;
                    p1.rightMargin = 13;
                    text.setText(" > " + data.getTitle());
                }
                else if(data.getTitle().equals("자취생게시판")) {
                    linearLayout.setBackgroundResource(R.color.colorboard1);
                    p1.leftMargin = 13;
                    p1.rightMargin = 13;
                    text.setText(" > " + data.getTitle());
                }
                else if(data.getTitle().equals("기숙사생게시판")) {
                    linearLayout.setBackgroundResource(R.color.colorboard1);
                    p1.leftMargin = 13;
                    p1.rightMargin = 13;
                    text.setText(" > " + data.getTitle());
                }
                else {
                    p1.leftMargin = 200;
                    p1.rightMargin = 200;
                    linearLayout.setBackgroundResource(R.color.colorboard2);
                    text.setText(data.getTitle());
                }
                linearLayout.setLayoutParams(p1);
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
