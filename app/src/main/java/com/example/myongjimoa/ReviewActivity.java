package com.example.myongjimoa;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ReviewActivity extends AppCompatActivity {

    String category;
    String user_id;
    String user_nickname;

    RecyclerView recycler_view;
    public GestureDetector gesture_detector;
    RestaurantAdapter restaurant_adapter;
    RestaurantAdapter search_adapter;
    MenuItem search;
    SwipeRefreshLayout swipe_refresh_layout;

    String order; // 초기값 score순

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_main);


        Intent it = getIntent();
        category = it.getStringExtra("category");
        user_id = it.getStringExtra("user_id");
        user_nickname = it.getStringExtra("user_nickname");

        order = "score"; // 정렬 방식 default로 점수 순으로 지정

        gesture_detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

        swipe_refresh_layout = (SwipeRefreshLayout) findViewById(R.id.restaurant_swipe_refresh_layout);
        swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadRestaurant();
                swipe_refresh_layout.setRefreshing(false);
            }
        });
        recycler_view = (RecyclerView) findViewById(R.id.restaurant_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recycler_view.setLayoutManager(layoutManager);

        recycler_view.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

                View childView = rv.findChildViewUnder(e.getX(), e.getY());

                if(childView != null && gesture_detector.onTouchEvent((e))) {
                    int currentPos = rv.getChildAdapterPosition(childView);
                    Restaurant restaurant;
                    if (search.isActionViewExpanded()) restaurant = search_adapter.getItem(currentPos); // 검색모드일때의 아이템
                    else restaurant = restaurant_adapter.getItem(currentPos);
                    Intent it = new Intent(ReviewActivity.this, ReviewListActivity.class);
                    it.putExtra("user_id", user_id);
                    it.putExtra("id", restaurant.getId());
                    it.putExtra("title", restaurant.getTitle());
                    it.putExtra("category", restaurant.getCategory());
                    it.putExtra("telephone", restaurant.getTelephone());
                    it.putExtra("homepage", restaurant.getHomepage());
                    it.putExtra("address", restaurant.getAddress());
                    it.putExtra("mapx", restaurant.getMapx());
                    it.putExtra("mapy", restaurant.getMapy());
                    it.putExtra("restaurant_id", restaurant.getRestaurant_id());
                    it.putExtra("time", restaurant.getTime());
                    it.putExtra("menu", restaurant.getMenu());
                    it.putExtra("image", restaurant.getImage());
                    it.putExtra("review_num", restaurant.getReview_num() + "");
                    it.putExtra("score", restaurant.getScore() + "");
                    it.putExtra("user_nickname", user_nickname);
                    startActivity(it); // ReviewListActivity로 화면 전환
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
        restaurant_adapter = new RestaurantAdapter();
        recycler_view.setAdapter(restaurant_adapter);
        downloadRestaurantList();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void downloadRestaurantList() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(GsonConverterFactory.create()) // JSON 형태로 받아옴
                .build(); // 통신 라이브러리 retrofit 객체 생성

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
        Call<List<Restaurant>> call = connectDB.downloadRestaurant(category, order); // List<Restaurant> 형태로 받아옴
        call.enqueue(new Callback<List<Restaurant>>() {
            @Override
            public void onResponse(Call<List<Restaurant>> call, Response<List<Restaurant>> response) {

                List<Restaurant> result = response.body();
                if(result != null) {
                    if (result.size() != 0) {
                        for (int i = 0; i < result.size(); i++) {
                            restaurant_adapter.add(new Restaurant(result.get(i).getId(), result.get(i).getTitle(), result.get(i).getCategory(), result.get(i).getTelephone(), result.get(i).getHomepage(), result.get(i).getAddress(), result.get(i).getMapx(), result.get(i).getMapy(), result.get(i).getRestaurant_id(), result.get(i).getTime(), result.get(i).getMenu(), result.get(i).getImage(), result.get(i).getReview_num(), result.get(i).getScore()));
                        } // 가져온 데이터 adapter에 등록
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Restaurant>> call, Throwable t) {
                Log.d("실패", t.getMessage());
            }
        });
    }


    class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder>{
        List<Restaurant> items = new ArrayList<>();

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView image;
            TextView title;
            TextView category;
            TextView score;
            TextView address;
            TextView review_num;
            RatingBar rating_bar;


            public ViewHolder(View itemView) {
                super(itemView);
                image = (ImageView) itemView.findViewById(R.id.list_restaurant_image);
                title = (TextView) itemView.findViewById(R.id.list_restaurant_title);
                category = (TextView) itemView.findViewById(R.id.list_restaurant_category);
                score = (TextView) itemView.findViewById(R.id.list_restaurant_score);
                address = (TextView) itemView.findViewById(R.id.list_restaurant_address);
                review_num = (TextView) itemView.findViewById(R.id.list_restaurant_review_num);
                rating_bar = (RatingBar) itemView.findViewById(R.id.list_restaurant_rating_bar);
            }

            public void setData(Restaurant data) {
                title.setText(data.getTitle());
                category.setText(data.getCategory());
                score.setText(data.getScore() + "");
                address.setText(data.getAddress());
                review_num.setText(data.getReview_num()+ "");
                rating_bar.setRating(data.getScore());
                Glide.with(ReviewActivity.this)
                        .load(data.getImage())
                        .override(500)
                        .into(image);
            }
        }

        public void add(Restaurant item) {
            items.add(item);
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant, parent, false);
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

        public Restaurant getItem(int pos) {
            return items.get(pos);
        }

        public List<Restaurant> getItems() {
            return items;
        }

        public void clear() {
            items.clear();
            notifyDataSetChanged();
        }

        public void addAll(List<Restaurant> items) {
            this.items.addAll(items);
            notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.review_refresh, menu);
        search = menu.findItem(R.id.review_search);

        search.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // 검색창 확장되었을때의 어댑터 처리
                search_adapter = new RestaurantAdapter();
                recycler_view.setAdapter(search_adapter);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // 검색창 축소되면 원래의 어댑터로 돌림
                recycler_view.setAdapter(restaurant_adapter);
                search_adapter = null;
                return true;
            }
        });

        SearchView sv = (SearchView) search.getActionView();

        sv.setSubmitButtonEnabled(true);

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // 텍스트가 입력될때 마다 어댑터에서 검색 진행
                search_adapter.clear(); // 초기화하고
                if(newText.equals("")) { // 입력값 없을땐 모든 값
                    search_adapter.addAll(restaurant_adapter.getItems());
                } else {
                    // 입력값 있을때
                    for(int i=0; i<restaurant_adapter.getItems().size(); i++) {
                        if(restaurant_adapter.getItem(i).getTitle().toLowerCase().contains(newText)) {
                            search_adapter.add(restaurant_adapter.getItem(i)); // 어댑터의 해당하는 아이템의 제목이 있을경우 search_adapter에 담아줌
                        }
                    }
                }
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.review_search:
                return true;

            case R.id.review_refresh:
                reloadRestaurant();
                return true;

            case R.id.review_order_num:
                order = "review_num";
                reloadRestaurant();
                return true;

            case R.id.review_order_score:
                order = "score";
                reloadRestaurant();
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void reloadRestaurant() {
        if(search.isActionViewExpanded()) search.collapseActionView();
        restaurant_adapter = new RestaurantAdapter();
        recycler_view.setAdapter(restaurant_adapter);
        downloadRestaurantList();
    }
}
