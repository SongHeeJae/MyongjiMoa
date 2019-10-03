package com.example.myongjimoa;

import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
    public GestureDetector gesture_detector;
    SwipeRefreshLayout swipe_refresh_layout;
    Button write;

   // Button test_button; // 삭제할 버튼 recyclerview add하면 자동으로 refresh 되는지 확인;

    List<Post> post_list;

    String count_board_id;

    boolean scroll;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.board_main, container, false);

        write = (Button) view.findViewById(R.id.write);
        write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BoardActivity)getActivity()).replaceBoardWriteFragment();
            }
        });

        recycler_view = (RecyclerView) view.findViewById(R.id.post_recycler_view);

        if (board_post_title_adapter == null) {
            clearPost();
            downloadPostList();
        }

        recycler_view.setAdapter(board_post_title_adapter);

        // 위로 당겼을때의 코드
        swipe_refresh_layout = (SwipeRefreshLayout) view.findViewById(R.id.board_swipe_refresh_layout);
        swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("감지", "감지");
                reloadPost();
                swipe_refresh_layout.setRefreshing(false);
            }
        });

        // 아래 끝 감지 코드
        recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                int itemTotalCount = recyclerView.getAdapter().getItemCount() - 1;
                if (lastVisibleItemPosition == itemTotalCount) {
                    Log.d("ㅇㅇ", "last Position...");
                    if(scroll) downloadPostList();
                }
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
                    ((BoardActivity)getActivity()).replaceBoardPostFragment(post_list.get(currentPos));
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

        return view;
    }

    public void downloadPostList() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
        Call<List<Post>> call = connectDB.downloadPost(((BoardActivity)getActivity()).getBoard_title(), count_board_id);
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {

                    List<Post> result = response.body();
                    if(result != null) {
                        if (result.size() != 0) {
                            for (int i = 0; i < result.size(); i++) {
                                board_post_title_adapter.add(result.get(i).getTitle());
                                post_list.add(new Post(result.get(i).getId(), result.get(i).getTitle(), result.get(i).getDescription(), result.get(i).getE_mail(), result.get(i).getDate(), result.get(i).getNickname(), result.get(i).getImages()));
                            }
                            if (result.size() < 15) scroll = false; // 데이터 다 가져왔을 경우
                            count_board_id = result.get(result.size() - 1).getId();
                        } else {
                            Log.d("글목록없음", "글목록없음");
                        }
                    } else {
                        Log.d("게시판 테이블 생성", "완료");
                    }

            }
            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                Log.d("실패", t.getMessage());
            }

        });
    }

    public void reloadPost() {
        board_post_title_adapter = null;
        clearPost();
        recycler_view.setAdapter(board_post_title_adapter);
        downloadPostList();
    }

    public void clearPost() {
        scroll = true;
        count_board_id = "`" + ((BoardActivity)getActivity()).getBoard_title() + "`.`id`+1";
        board_post_title_adapter = new BoardPostTitleAdapter();
        if(post_list != null) {
            post_list.clear();
            post_list = null;
        }
        post_list = new ArrayList<Post>();
    }

    public void clearAdapter() {
        board_post_title_adapter = null;
    }

}
