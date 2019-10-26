package com.example.myongjimoa;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BoardActivity extends AppCompatActivity {

    String board_title;
    String user_id;
    String user_nickname;
    String board_title_id;

    RecyclerView recycler_view;
    BoardPostTitleAdapter board_post_title_adapter;
    BoardPostTitleAdapter search_adapter;
    String search_query; // 검색어
    String search_count_board_id;
    boolean search_scroll;

    public GestureDetector gesture_detector;
    SwipeRefreshLayout swipe_refresh_layout;
    Button write;

    String count_board_id;

    boolean scroll;

    MenuItem search;

    private final int WRITE_REQUEST_CODE = 1000;
    private final int POST_REMOVE_REQUEST_CODE = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board);

        Intent it = getIntent();
        board_title_id = it.getStringExtra("board_title_id");
        board_title = it.getStringExtra("board_title");
        user_id = it.getStringExtra("user_id");
        user_nickname = it.getStringExtra("user_nickname");
        // 이전 Activity에서 정보 받아옴
        setTitle(board_title);

        write = (Button) findViewById(R.id.write);
        write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //((BoardActivity)getActivity()).addBoardWriteFragment();
                Intent it = new Intent(BoardActivity.this, BoardWriteActivity.class);
                it.putExtra("user_id", user_id);
                it.putExtra("board_title_id", board_title_id);
                startActivityForResult(it, WRITE_REQUEST_CODE); // 글쓰기 버튼 클릭 시 글쓰기 화면으로 이동
            }
        });

        recycler_view = (RecyclerView) findViewById(R.id.post_recycler_view);

        scroll = true;
        count_board_id = "`board`.`id`+1";
        board_post_title_adapter = new BoardPostTitleAdapter();

        recycler_view.setAdapter(board_post_title_adapter);

        // 위로 당겼을때의 코드
        swipe_refresh_layout = (SwipeRefreshLayout) findViewById(R.id.board_swipe_refresh_layout);
        swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadPost(); // 게시글 목록 새로고침
                swipe_refresh_layout.setRefreshing(false);
            }
        });


        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recycler_view.setLayoutManager(layoutManager); // 수직 방향의 LinearLayout을 RecyclerView에 지정

        gesture_detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

        recycler_view.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View childView = rv.findChildViewUnder(e.getX(), e.getY());
                if (childView != null && gesture_detector.onTouchEvent((e))) {
                    int currentPos = rv.getChildAdapterPosition(childView);
                    Intent it = new Intent(BoardActivity.this, BoardPostActivity.class);
                    Post post;
                    if (search.isActionViewExpanded()) { // 검색창 확장되었을 시 현재 터치한 아이템
                        post = search_adapter.getPost(currentPos);
                    } else {
                        post = board_post_title_adapter.getPost(currentPos); // 검색창 축소 상태일 시 현재 터치한 아이템
                    }
                    it.putExtra("id", post.getId());
                    it.putExtra("title", post.getTitle());
                    it.putExtra("description", post.getDescription());
                    it.putExtra("number", post.getNumber());
                    it.putExtra("major", post.getMajor());
                    it.putExtra("date", post.getDate());
                    it.putExtra("nickname", post.getNickname());
                    it.putStringArrayListExtra("images", post.getImages());
                    it.putExtra("recommend_num", post.getRecommend_num() + "");
                    it.putExtra("user_id", user_id);
                    it.putExtra("user_nickname", user_nickname);
                    startActivityForResult(it, POST_REMOVE_REQUEST_CODE); // 게시글 화면으로 이동
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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 뒤로가기버튼 활성화

        downloadPostList(); // 게시글 목록 다운로드

    }


    public void downloadPostList() {

        ConnectDB connectDB = Request.getRetrofit().create(ConnectDB.class);
        Call<List<Post>> call = connectDB.downloadPost(board_title_id, count_board_id); // List<Post> 형태로 받아옴
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {

                List<Post> result = response.body();
                if (result != null) {
                    if (result.size() != 0) {
                        for (int i = 0; i < result.size(); i++) { // 다운로드된 게시글 어댑터에 등록
                            board_post_title_adapter.add(new Post(result.get(i).getId(), result.get(i).getTitle(), result.get(i).getDescription(), result.get(i).getNumber(), result.get(i).getMajor(), result.get(i).getDate(), result.get(i).getNickname(), result.get(i).getImages(), result.get(i).getRecommend_num()));
                        }
                        if (result.size() < 15) scroll = false; // 데이터 다 가져왔을 경우, 화면 끝에 닿았을 때 더이상 다운로드 안일어나게함
                        count_board_id = result.get(result.size() - 1).getId(); // 현재 다운로드된 마지막 게시글의 id(PK)값
                    }
                }
                recycler_view.clearOnScrollListeners();
                recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView view, int scrollState) {
                        if (scrollState == RecyclerView.SCROLL_STATE_SETTLING) {
                            if (scroll) downloadPostList(); // 스크롤이 끝에 닿았을때 scroll이 true 상태이면(더 다운받을 게시글이 있으면) 이어서 받아옴
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                Log.d("실패", t.getMessage());
            }
        });
    }

    public void reloadPost() {
        // 게시글 새로고침 일어났을 시 어댑터와 다른 상태변수들 초기화, 다시 다운로드
        if(search.isActionViewExpanded()) search.collapseActionView();
        scroll = true;
        count_board_id = "`board`.`id`+1";
        board_post_title_adapter = new BoardPostTitleAdapter();
        recycler_view.setAdapter(board_post_title_adapter);
        downloadPostList();
    }


    class BoardPostTitleAdapter extends RecyclerView.Adapter<BoardPostTitleAdapter.ViewHolder> {
        List<Post> items = new ArrayList<>();

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView text;

            public ViewHolder(View itemView) {
                super(itemView);
                text = (TextView) itemView.findViewById(R.id.post_title);

                // 제목 글자수가 1줄 초과하면 ...으로 처리.
                text.setMaxLines(1);
                text.setEllipsize(TextUtils.TruncateAt.END);

            }

            public void setData(Post data) {
                //값 읽어오기
                text.setText(data.getTitle());
            }
        }

        public void add(Post item) {
            items.add(item);
            notifyDataSetChanged(); // 뭔지 검색
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_title_item, parent, false);
            return new BoardPostTitleAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.setData(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public Post getPost(int pos) {
            return items.get(pos);
        }
    }

    public void downloadSearchPostList() {

        ConnectDB connectDB = Request.getRetrofit().create(ConnectDB.class);
        Call<List<Post>> call = connectDB.downloadSearchPost(board_title_id, search_count_board_id, search_query); // List<Post> 형태로 받아옴
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {

                // downloadPostList와 동일한 처리
                List<Post> result = response.body();
                if (result != null) {
                    if (result.size() != 0) {
                        for (int i = 0; i < result.size(); i++) {
                            search_adapter.add(new Post(result.get(i).getId(), result.get(i).getTitle(), result.get(i).getDescription(), result.get(i).getNumber(), result.get(i).getMajor(), result.get(i).getDate(), result.get(i).getNickname(), result.get(i).getImages(), result.get(i).getRecommend_num()));
                        }
                        if (result.size() < 15) search_scroll = false; // 데이터 다 가져왔을 경우
                        search_count_board_id = result.get(result.size() - 1).getId();
                    }
                }
                recycler_view.clearOnScrollListeners();
                recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView view, int scrollState) {
                        if (scrollState == RecyclerView.SCROLL_STATE_SETTLING) {
                            if (search_scroll) downloadSearchPostList();
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                Log.d("실패", t.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_refresh, menu);
        menu.findItem(R.id.menu_report).setVisible(false);
        menu.findItem(R.id.menu_modify).setVisible(false);
        menu.findItem(R.id.menu_remove).setVisible(false); // 안쓰는 메뉴들 안보이게설정
        search = menu.findItem(R.id.menu_search);

        search.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // 검색 활성화 시 search_adapter로 등록. 글쓰기 버튼 안보이게 설정
                search_adapter = new BoardPostTitleAdapter();
                recycler_view.setAdapter(search_adapter);
                write.setVisibility(View.INVISIBLE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // 검색창 축소 시 원래 상태로 돌림
                recycler_view.setAdapter(board_post_title_adapter);
                search_adapter = null;
                write.setVisibility(View.VISIBLE);
                return true;
            }
        });

        SearchView sv = (SearchView) search.getActionView();

        sv.setSubmitButtonEnabled(true);

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 검색 완료 버튼 눌렀을 시 검색 진행
                if(search_adapter != null) {
                    search_adapter = new BoardPostTitleAdapter();
                    recycler_view.setAdapter(search_adapter);
                }
                search_query = query;
                search_count_board_id = "`board`.`id`+1";
                downloadSearchPostList();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                return true;
            case R.id.menu_refresh:
                reloadPost();
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == WRITE_REQUEST_CODE){
            reloadPost();
        } else if(resultCode == RESULT_OK && requestCode == POST_REMOVE_REQUEST_CODE) { // 수정 또는 삭제일어났을때 reload
            reloadPost();
        }
    }
}
