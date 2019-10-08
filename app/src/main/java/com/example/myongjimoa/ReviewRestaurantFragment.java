package com.example.myongjimoa;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ReviewRestaurantFragment extends Fragment {

    RecyclerView recycler_view;
    public GestureDetector gesture_detector;
    RestaurantAdapter restaurant_adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.review_restaurant, container, false);

        gesture_detector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

        recycler_view = (RecyclerView)view.findViewById(R.id.restaurant_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        recycler_view.setLayoutManager(layoutManager);

        recycler_view.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

                View childView = rv.findChildViewUnder(e.getX(), e.getY());

                if(childView != null && gesture_detector.onTouchEvent((e))) {
                    int currentPos = rv.getChildAdapterPosition(childView);
                    ((ReviewActivity)getActivity()).addReviewListFragment(restaurant_adapter.getRestaurant(currentPos));
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

       // if(restaurant_adapter == null) {
            restaurant_adapter = new RestaurantAdapter();
            downloadRestaurantList();
    //    }

        recycler_view.setAdapter(restaurant_adapter);

        return view;
    }


    public void downloadRestaurantList() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
        Call<List<Restaurant>> call = connectDB.downloadRestaurant(((ReviewActivity)getActivity()).category);
        call.enqueue(new Callback<List<Restaurant>>() {
            @Override
            public void onResponse(Call<List<Restaurant>> call, Response<List<Restaurant>> response) {

                List<Restaurant> result = response.body();
                if(result != null) {
                    if (result.size() != 0) {
                        for (int i = 0; i < result.size(); i++) {
                            restaurant_adapter.add(new Restaurant(result.get(i).getId(), result.get(i).getTitle(), result.get(i).getCategory(), result.get(i).getTelephone(), result.get(i).getHomepage(), result.get(i).getAddress(), result.get(i).getMapx(), result.get(i).getMapy(), result.get(i).getRestaurant_id(), result.get(i).getTime(), result.get(i).getMenu(), result.get(i).getImage(), result.get(i).getReview_num(), result.get(i).getScore()));
                            //Log.d("불러옴", "ㅇㅇ");
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
                // 일단 이미지 제외하고 설정
                title.setText(data.getTitle());
                category.setText(data.getCategory());
                score.setText(data.getScore() + "");
                address.setText(data.getAddress());
                review_num.setText(data.getReview_num()+ "");
                rating_bar.setRating(data.getScore());
                Glide.with(getActivity())
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
        public RestaurantAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant, parent, false);
            return new ViewHolder(view);
        }


        @Override
        public void onBindViewHolder(RestaurantAdapter.ViewHolder holder, int position) {
            holder.setData(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public Restaurant getRestaurant(int pos) {
            return items.get(pos);
        }
    }
}
