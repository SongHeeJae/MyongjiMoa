package com.example.myongjimoa;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

public class BoardMainFragment extends Fragment {

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

    Menu my_menu;
    MenuItem search;

    @Override
    public void onStart() {
        super.onStart();
        Log.d("스타트", "ㅇㅇ");
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.board_main, container, false);

        write = (Button) view.findViewById(R.id.write);
        write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BoardActivity)getActivity()).addBoardWriteFragment();
            }
        });

        recycler_view = (RecyclerView) view.findViewById(R.id.post_recycler_view);


        scroll = true;
        count_board_id = "`board`.`id`+1";
        board_post_title_adapter = new BoardPostTitleAdapter();

        recycler_view.setAdapter(board_post_title_adapter);

        // 위로 당겼을때의 코드
        swipe_refresh_layout = (SwipeRefreshLayout) view.findViewById(R.id.board_swipe_refresh_layout);
        swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadPost();
                swipe_refresh_layout.setRefreshing(false);
            }
        });


        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        recycler_view.setLayoutManager(layoutManager);

        gesture_detector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

        recycler_view.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View childView = rv.findChildViewUnder(e.getX(), e.getY());
                if(childView != null && gesture_detector.onTouchEvent((e))) {
                    int currentPos = rv.getChildAdapterPosition(childView);
                    ((BoardActivity)getActivity()).addBoardPostFragment(board_post_title_adapter.getPost(currentPos));
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

        downloadPostList();

        return view;
    }

    public void downloadPostList() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
        Call<List<Post>> call = connectDB.downloadPost(((BoardActivity)getActivity()).board_title_id, count_board_id);
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {

                    List<Post> result = response.body();
                    if(result != null) {
                        if (result.size() != 0) {
                            for (int i = 0; i < result.size(); i++) {
                                board_post_title_adapter.add(new Post(result.get(i).getId(), result.get(i).getTitle(), result.get(i).getDescription(), result.get(i).getE_mail(), result.get(i).getDate(), result.get(i).getNickname(), result.get(i).getImages(), result.get(i).getRecommend_num()));
                               // post_list.add();
                            }
                            if (result.size() < 15) scroll = false; // 데이터 다 가져왔을 경우
                            count_board_id = result.get(result.size() - 1).getId();
                        } else {
                            Log.d("글목록없음", "글목록없음");
                        }
                    } else {
                        Log.d("게시판 테이블 생성", "완료");
                    }
                recycler_view.clearOnScrollListeners();
                recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView view, int scrollState) {
                        if(scrollState == RecyclerView.SCROLL_STATE_SETTLING ) {
                            if(scroll) downloadPostList();
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
        scroll = true;
        count_board_id = "`board`.`id`+1";
        board_post_title_adapter = new BoardPostTitleAdapter();
        recycler_view.setAdapter(board_post_title_adapter);
        downloadPostList();
    }


    class BoardPostTitleAdapter extends RecyclerView.Adapter<BoardPostTitleAdapter.ViewHolder>{
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
        public BoardPostTitleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_title_item, parent, false);
            return new BoardPostTitleAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(BoardPostTitleAdapter.ViewHolder holder, int position) {
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

    public void setSearchAdapter() {
        search_adapter = new BoardPostTitleAdapter();
        search_scroll = true;
        search_count_board_id = "`board`.`id`+1";
        recycler_view.setAdapter(search_adapter);
        downloadSearchPostList();
    }

    public void downloadSearchPostList() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
        Call<List<Post>> call = connectDB.downloadSearchPost(((BoardActivity)getActivity()).board_title_id, search_count_board_id, search_query);
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {

                List<Post> result = response.body();
                if(result != null) {
                    if (result.size() != 0) {
                        for (int i = 0; i < result.size(); i++) {
                            search_adapter.add(new Post(result.get(i).getId(), result.get(i).getTitle(), result.get(i).getDescription(), result.get(i).getE_mail(), result.get(i).getDate(), result.get(i).getNickname(), result.get(i).getImages(), result.get(i).getRecommend_num()));
                        }
                        if (result.size() < 15) search_scroll = false; // 데이터 다 가져왔을 경우
                        search_count_board_id = result.get(result.size() - 1).getId();
                    } else {
                        Log.d("글목록없음", "글목록없음");
                    }
                } else {
                    Log.d("게시판 테이블 생성", "완료");
                }
                recycler_view.clearOnScrollListeners();
                recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView view, int scrollState) {
                        if(scrollState == RecyclerView.SCROLL_STATE_SETTLING ) {
                            if(search_scroll) downloadSearchPostList();
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_refresh, menu);
        menu.findItem(R.id.menu_menus).setVisible(false);

        my_menu = menu;
        search = menu.findItem(R.id.menu_search);

        search.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                recycler_view.setAdapter(null);
                write.setVisibility(View.INVISIBLE);
            //    my_menu.findItem(R.id.menu_refresh).setVisible(false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                recycler_view.setAdapter(board_post_title_adapter);
                write.setVisibility(View.VISIBLE);
            //    my_menu.findItem(R.id.menu_refresh).setVisible(true);
                return true;
            }
        });

        SearchView sv=(SearchView)search.getActionView();

        sv.setSubmitButtonEnabled(true);

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search_query = query;
                setSearchAdapter();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
    }
}
