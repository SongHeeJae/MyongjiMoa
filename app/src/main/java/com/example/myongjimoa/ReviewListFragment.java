package com.example.myongjimoa;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.Tm128;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ReviewListFragment extends Fragment implements OnMapReadyCallback {

    ImageView image;
    TextView title;
    TextView category;
    TextView telephone;
    TextView homepage;
    TextView address;
    TextView time;
    TextView menu;
    //TextView review_num;
    TextView score;
    RatingBar restaurant_rating_bar;
    Button write_review;
    Restaurant restaurant;

    RecyclerView review_recycler_view;
    ReviewAdapter review_adapter;
    public GestureDetector gesture_detector;

    boolean scroll;
    String count_review_id;

  //  ViewGroup view;

   // private MapView mapView;
    MapFragment mapFragment;
    SwipeRefreshLayout swipe_refresh_layout;


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.review_list, container, false);
        view.setBackgroundColor(Color.rgb(255, 255, 255));

        gesture_detector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

        restaurant = ((ReviewActivity)getActivity()).current_restaurant;
        image = (ImageView) view.findViewById(R.id.restaurant_image);
        title = (TextView) view.findViewById(R.id.restaurant_title);
        category = (TextView) view.findViewById(R.id.restaurant_category);
        telephone = (TextView) view.findViewById(R.id.restaurant_telephone);
        homepage = (TextView) view.findViewById(R.id.restaurant_homepage);
        address = (TextView) view.findViewById(R.id.restaurant_address);
        time = (TextView) view.findViewById(R.id.restaurant_time);
        menu = (TextView) view.findViewById(R.id.restaurant_menu);
      //  review_num = (TextView) view.findViewById(R.id.restaurant_review_num);
        score = (TextView) view.findViewById(R.id.restaurant_score);
        restaurant_rating_bar = (RatingBar) view.findViewById(R.id.review_list_rating_bar);
        write_review = (Button) view.findViewById(R.id.write_review);
        review_recycler_view = (RecyclerView) view.findViewById(R.id.review_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        review_recycler_view.setLayoutManager(layoutManager);

        swipe_refresh_layout = (SwipeRefreshLayout) view.findViewById(R.id.review_swipe_refresh_layout);
        swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("감지", "감지");
                swipe_refresh_layout.setRefreshing(false);
                reloadReview();
            }
        });

        write_review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ReviewActivity)getActivity()).addReviewWriteFragment();
            }
        });

        Glide.with(getActivity())
                .load(restaurant.getImage())
                .override(500)
                .into(image);

        title.setText(restaurant.getTitle());
        category.setText(restaurant.getCategory());
        telephone.setText(restaurant.getTelephone());
        homepage.setText(restaurant.getHomepage());
        address.setText(restaurant.getAddress());
        time.setText(restaurant.getTime());
        menu.setText(restaurant.getMenu());
        //review_num.setText(restaurant.getReview_num() + ""); 업데이트할방법 까다로워서 표시안하기로함
        score.setText(restaurant.getScore() + "");


        scroll = true;
        count_review_id = "`review`.`id`+1";
        review_adapter = new ReviewAdapter();
        downloadReviewList();


        review_recycler_view.setAdapter(review_adapter);

        review_recycler_view.clearOnScrollListeners();
        review_recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
          @Override
            public void onScrollStateChanged(RecyclerView view, int scrollState) {
              if(scrollState == RecyclerView.SCROLL_STATE_SETTLING ) {
                  Log.d("ㅋㅋ", "ㅇㅇ");
                  if(scroll) downloadReviewList();
              }
          }
        });

        FragmentManager fm = getChildFragmentManager();
        mapFragment = (MapFragment)fm.findFragmentById(R.id.review_map);
        if(mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.review_list_map, mapFragment).commit();
            mapFragment.getMapAsync(this);
        }

        return view;
    }


    @UiThread
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        Tm128 tm128 = new Tm128(Integer.parseInt(restaurant.getMapx()), Integer.parseInt(restaurant.getMapy()));
        LatLng coord = tm128.toLatLng();
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(coord);
        naverMap.moveCamera(cameraUpdate);
        naverMap.setLiteModeEnabled(true); // 라이트모드 속도빨라짐
        Marker marker = new Marker();
        marker.setPosition(coord);
        marker.setMap(naverMap);
    }

    @Override
    public void onStart() {
        super.onStart();
        restaurant_rating_bar.setRating(restaurant.getScore());
    }


    public void downloadReviewList() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
        Call<List<Review>> call = connectDB.downloadReview(restaurant.getId(), count_review_id);
        call.enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {

                List<Review> result = response.body();

                if(result != null) {
                    if (result.size() != 0) {
                        for (int i = 0; i < result.size(); i++) {
                            review_adapter.add(new Review(result.get(i).getId(), result.get(i).getDescription(), result.get(i).getNickname(), result.get(i).getUser_id(), result.get(i).getScore(), result.get(i).getDate(), result.get(i).getImages()));
                        }
                        if (result.size() < 15) scroll = false; // 데이터 다 가져왔을 경우
                        count_review_id = result.get(result.size() - 1).getId();
                    } else {
                        Log.d("리뷰목록없음", "리뷰목록없음");
                    }
                } else {
                    Log.d("값없음", "ㅇㅇ");
                }

                review_recycler_view.clearOnScrollListeners();
                review_recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView view, int scrollState) {
                        if(scrollState == RecyclerView.SCROLL_STATE_SETTLING ) {
                            Log.d("ㅋㅋ", "ㅇㅇ");
                            if(scroll) downloadReviewList();
                        }
                    }
                });

            }
            @Override
            public void onFailure(Call<List<Review>> call, Throwable t) {
                Log.d("실패", t.getMessage());
            }

        });
    }

    class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder>{
        List<Review> items = new ArrayList<Review>();

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView nickname;
            TextView date;
            RatingBar rating_bar;
            TextView description;
            RecyclerView image_recycler_view;
            ReviewImageAdapter review_image_adapter;


            public ViewHolder(View itemView) {
                super(itemView);
                nickname = (TextView) itemView.findViewById(R.id.review_nickname);
                date = (TextView) itemView.findViewById(R.id.review_date);
                rating_bar = (RatingBar) itemView.findViewById(R.id.review_item_rating_bar);
                description = (TextView) itemView.findViewById(R.id.review_description);
                image_recycler_view = (RecyclerView) itemView.findViewById(R.id.review_image_recycler_view);
                review_image_adapter = new ReviewImageAdapter();
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ReviewListFragment.this.getActivity(), RecyclerView.HORIZONTAL, false);

                image_recycler_view.setLayoutManager(layoutManager);
                image_recycler_view.setAdapter(review_image_adapter);

            }

            public void setData(Review data) {
                nickname.setText(data.getNickname());
                date.setText(data.getDate());
                rating_bar.setRating(data.getScore());
                description.setText(data.getDescription());
                ArrayList<String> img_data = data.getImages();
                for(int i=0; i<img_data.size(); i++) {
                    review_image_adapter.add(img_data.get(i));
                }
            }
        }

        public void add(Review item) {
            items.add(item);
            notifyDataSetChanged();
        }

        @Override
        public ReviewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_item, parent, false);
            return new ViewHolder(view);
        }


        @Override
        public void onBindViewHolder(ReviewAdapter.ViewHolder holder, int position) {
            holder.setData(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

    }

    class ReviewImageAdapter extends RecyclerView.Adapter<ReviewImageAdapter.ViewHolder>{
        List<String> items = new ArrayList<String>();
        class ViewHolder extends RecyclerView.ViewHolder {

            ImageView img;

            public ViewHolder(View itemView) {
                super(itemView);
                img = (ImageView) itemView.findViewById(R.id.image_item);

            }

            public void setData(String data) {
                Log.d("이미지경로는??", data);
                Glide.with(ReviewListFragment.this.getActivity())
                        .load("https://myongjimoa.s3.ap-northeast-2.amazonaws.com/review_images/" + data)
                        .override(500)
                        .into(img);
            }
        }
        public void add(String item) {
            items.add(item);
            notifyDataSetChanged();
        }

        @Override
        public ReviewImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ReviewImageAdapter.ViewHolder holder, int position) {
            holder.setData(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    public void reloadReview() {
    //    review_recycler_view.clearOnScrollListeners();
        scroll = true;
        count_review_id = "`review`.`id`+1";
        review_adapter = new ReviewAdapter();
        review_recycler_view.setAdapter(review_adapter);
        score.setText(((ReviewActivity)getActivity()).current_restaurant.getScore() + "");
    //    review_num.setText(((ReviewActivity)getActivity()).current_restaurant.getReview_num() + "");
        restaurant_rating_bar.setRating(((ReviewActivity)getActivity()).current_restaurant.getScore());
        downloadReviewList();
    }

}
