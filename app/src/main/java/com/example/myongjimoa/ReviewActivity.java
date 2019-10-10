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

    Button order_review;
    Button order_score;

    String order; // 초기값 score순

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_main);


        Intent it = getIntent();
        category = it.getStringExtra("category");
        user_id = it.getStringExtra("user_id");
        user_nickname = it.getStringExtra("user_nickname");

        order_review = (Button) findViewById(R.id.order_review);
        order_score = (Button) findViewById(R.id.order_score);

        View.OnClickListener order_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()) {
                    case R.id.order_review:
                        order = "review_num";
                        reloadRestaurant();
                        break;
                    case R.id.order_score:
                        order = "score";
                        reloadRestaurant();
                        break;
                }
            }
        };
        order_review.setOnClickListener(order_listener);

        order_score.setOnClickListener(order_listener);

        order = "score";

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
                    if (search.isActionViewExpanded()) restaurant = search_adapter.getItem(currentPos);
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
        restaurant_adapter = new RestaurantAdapter();
        recycler_view.setAdapter(restaurant_adapter);
        downloadRestaurantList();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void downloadRestaurantList() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
        Call<List<Restaurant>> call = connectDB.downloadRestaurant(category, order);
        call.enqueue(new Callback<List<Restaurant>>() {
            @Override
            public void onResponse(Call<List<Restaurant>> call, Response<List<Restaurant>> response) {

                List<Restaurant> result = response.body();
                if(result != null) {
                    if (result.size() != 0) {

                        for (int i = 0; i < result.size(); i++) {
                            restaurant_adapter.add(new Restaurant(result.get(i).getId(), result.get(i).getTitle(), result.get(i).getCategory(), result.get(i).getTelephone(), result.get(i).getHomepage(), result.get(i).getAddress(), result.get(i).getMapx(), result.get(i).getMapy(), result.get(i).getRestaurant_id(), result.get(i).getTime(), result.get(i).getMenu(), result.get(i).getImage(), result.get(i).getReview_num(), result.get(i).getScore()));
                        }
                    } else {
                        Log.d("음식점목록없음", "음식점목록없음");
                    }
                } else {
                    Log.d("error", "error");
                }

            }
            @Override
            public void onFailure(Call<List<Restaurant>> call, Throwable t) {
                Log.d("실패", t.getMessage());
            }

        });
    }


    class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder>{
        List<Restaurant> items = new ArrayList<Restaurant>();

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
        getMenuInflater().inflate(R.menu.search_refresh, menu);
        menu.findItem(R.id.menu_report).setVisible(false);
        menu.findItem(R.id.menu_modify).setVisible(false);
        menu.findItem(R.id.menu_remove).setVisible(false);

        search = menu.findItem(R.id.menu_search);

        search.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                search_adapter = new RestaurantAdapter();
                recycler_view.setAdapter(search_adapter);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
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
                search_adapter.clear();
                if(newText.equals("")) {
                    search_adapter.addAll(restaurant_adapter.getItems());
                } else {
                    for(int i=0; i<restaurant_adapter.getItems().size(); i++) {
                        if(restaurant_adapter.getItem(i).getTitle().toLowerCase().contains(newText)) {
                            search_adapter.add(restaurant_adapter.getItem(i));
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
            case R.id.menu_search:
                return true;

            case R.id.menu_refresh:
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