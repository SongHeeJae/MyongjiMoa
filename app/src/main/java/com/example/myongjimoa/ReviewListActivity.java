package com.example.myongjimoa;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
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
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ReviewListActivity extends AppCompatActivity implements OnMapReadyCallback {

    ImageView image;
    TextView title;
    TextView category;
    TextView telephone;
    TextView homepage;
    TextView address;
    TextView time;
    TextView menu;
    TextView score;
    TextView review_num;
    RatingBar restaurant_rating_bar;
    Button write_review;
    Restaurant restaurant;


   RecyclerView review_recycler_view;
    ReviewAdapter review_adapter;
    public GestureDetector gesture_detector;

    boolean scroll;
    String count_review_id;

    MapFragment mapFragment;
    SwipeRefreshLayout swipe_refresh_layout;

    String user_id;
    String user_nickname;

    private final int WRITE_REQUEST_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_list);

        gesture_detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

        Intent it = getIntent();

        restaurant = new Restaurant(it.getStringExtra("id"), it.getStringExtra("title"), it.getStringExtra("category"), it.getStringExtra("telephone"), it.getStringExtra("homepage"), it.getStringExtra("address"), it.getStringExtra("mapx"), it.getStringExtra("mapy"), it.getStringExtra("restaurant_id"), it.getStringExtra("time"), it.getStringExtra("menu"), it.getStringExtra("image"), Integer.parseInt(it.getStringExtra("review_num")), Float.parseFloat(it.getStringExtra("score")));
        user_id = it.getStringExtra("user_id");
        user_nickname = it.getStringExtra("user_nickname");
        image = (ImageView) findViewById(R.id.restaurant_image);
        title = (TextView) findViewById(R.id.restaurant_title);
        category = (TextView) findViewById(R.id.restaurant_category);
        telephone = (TextView) findViewById(R.id.restaurant_telephone);
        homepage = (TextView) findViewById(R.id.restaurant_homepage);
        address = (TextView) findViewById(R.id.restaurant_address);
        time = (TextView) findViewById(R.id.restaurant_time);
        menu = (TextView) findViewById(R.id.restaurant_menu);
        score = (TextView) findViewById(R.id.restaurant_score);
        review_num = (TextView) findViewById(R.id.restaurant_review_num);
        restaurant_rating_bar = (RatingBar) findViewById(R.id.review_list_rating_bar);
        write_review = (Button) findViewById(R.id.write_review);
        review_recycler_view = (RecyclerView) findViewById(R.id.review_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        review_recycler_view.setLayoutManager(layoutManager);

        swipe_refresh_layout = (SwipeRefreshLayout) findViewById(R.id.review_swipe_refresh_layout);
        swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipe_refresh_layout.setRefreshing(false);
                reloadReview();
            }
        });

        write_review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(ReviewListActivity.this, ReviewWriteActivity.class);
                it.putExtra("user_id", user_id);
                it.putExtra("restaurant_id", restaurant.getId());
                startActivityForResult(it, WRITE_REQUEST_CODE);
            }
        });

        Glide.with(this)
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
        score.setText(restaurant.getScore() + "");
        review_num.setText(restaurant.getReview_num() + "");

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
                    if(scroll) downloadReviewList();
                }
            }
        });

        mapFragment = (MapFragment)getSupportFragmentManager().findFragmentById(R.id.review_map);
        if(mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.review_list_map, mapFragment).commit();
            mapFragment.getMapAsync(this);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        Call<ReviewResult> call = connectDB.downloadReview(restaurant.getId(), count_review_id);
        call.enqueue(new Callback<ReviewResult>() {
            @Override
            public void onResponse(Call<ReviewResult> call, Response<ReviewResult> response) {

                ReviewResult result = response.body();

                if (result.getReviewCount() != 0) {
                    for (int i = 0; i < result.getReviewCount(); i++) {
                        review_adapter.add(result.getReview(i));
                    }
                    if (result.getReviewCount() < 15) scroll = false; // 데이터 다 가져왔을 경우
                    count_review_id = result.getReview(result.getReviewCount() - 1).getId();
                }
                restaurant.setReview_num(result.getReview_num());
                restaurant.setScore(result.getScore());

                score.setText(restaurant.getScore() + "");
                review_num.setText(restaurant.getReview_num() + "");
                restaurant_rating_bar.setRating(restaurant.getScore());

                review_recycler_view.clearOnScrollListeners();
                review_recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView view, int scrollState) {
                        if(scrollState == RecyclerView.SCROLL_STATE_SETTLING ) {
                            if(scroll) downloadReviewList();
                        }
                    }
                });

            }
            @Override
            public void onFailure(Call<ReviewResult> call, Throwable t) {
                Log.d("실패", t.getMessage());
            }

        });
    }

    class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder>{
        List<Review> items = new ArrayList<Review>();

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView number;
            TextView major;
            TextView nickname;
            TextView date;
            RatingBar rating_bar;
            TextView description;
            RecyclerView image_recycler_view;
            ReviewImageAdapter review_image_adapter;
            Button review_setting;
            int pos;


            public ViewHolder(View itemView) {
                super(itemView);
                nickname = (TextView) itemView.findViewById(R.id.review_nickname);
                date = (TextView) itemView.findViewById(R.id.review_date);
                rating_bar = (RatingBar) itemView.findViewById(R.id.review_item_rating_bar);
                description = (TextView) itemView.findViewById(R.id.review_description);
                image_recycler_view = (RecyclerView) itemView.findViewById(R.id.review_image_recycler_view);
                review_image_adapter = new ReviewImageAdapter();
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ReviewListActivity.this, RecyclerView.HORIZONTAL, false);

                image_recycler_view.setLayoutManager(layoutManager);
                image_recycler_view.setAdapter(review_image_adapter);

                image_recycler_view.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                    @Override
                    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                        View childView = rv.findChildViewUnder(e.getX(), e.getY());

                        if(childView != null && gesture_detector.onTouchEvent((e))) {
                            int currentPos = rv.getChildAdapterPosition(childView);
                            Intent it = new Intent(ReviewListActivity.this, ImageActivity.class);
                            it.putStringArrayListExtra("images", new ArrayList<>(review_image_adapter.getItems()));
                            it.putExtra("current", currentPos);
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
                review_setting = (Button) itemView.findViewById(R.id.review_setting);
                number = (TextView) itemView.findViewById(R.id.review_number);
                major = (TextView) itemView.findViewById(R.id.review_major);

                review_setting.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ReviewListActivity.this);

                        DialogInterface.OnClickListener dialog_listener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0) {
                                    reportCurrentReview(items.get(pos).getId(), pos);
                                } else if (which == 1) {
                                    removeCurrentReview(items.get(pos).getId(), pos);
                                }
                            }
                        };
                        if(items.get(pos).getNickname().equals(user_nickname) || MainActivity.admin) builder.setItems(new String[]{"신고", "삭제"}, dialog_listener);
                        else builder.setItems(new String[]{"신고"}, dialog_listener);
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                });
            }

            public void setData(Review data, int position) {
                nickname.setText(data.getNickname());
                date.setText(data.getDate());
                rating_bar.setRating(data.getScore());
                description.setText(data.getDescription());
                number.setText(data.getNumber());
                major.setText(data.getMajor());
                ArrayList<String> img_data = data.getImages();
                for(int i=0; i<img_data.size(); i++) {
                    review_image_adapter.add("https://myongjimoa.s3.ap-northeast-2.amazonaws.com/review_images/" + img_data.get(i));
                }
                pos=position;
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
            holder.setData(items.get(position), position);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public Review getItem(int pos) {
            return items.get(pos);
        }

    }

    class ReviewImageAdapter extends RecyclerView.Adapter<ReviewImageAdapter.ViewHolder>{
        List<String> items = new ArrayList<>();
        class ViewHolder extends RecyclerView.ViewHolder {

            ImageView img;

            public ViewHolder(View itemView) {
                super(itemView);
                img = (ImageView) itemView.findViewById(R.id.image_item);

            }

            public void setData(String data) {
                Log.d("이미지경로는??", data);
                Glide.with(ReviewListActivity.this)
                        .load(data)
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_image, parent, false);
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

        public List<String> getItems() {
            return items;
        }
    }

    public void reloadReview() {
        scroll = true;
        count_review_id = "`review`.`id`+1";
        review_adapter = new ReviewAdapter();
        review_recycler_view.setAdapter(review_adapter);
        downloadReviewList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == WRITE_REQUEST_CODE && resultCode == RESULT_OK){
           reloadReview();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                reloadReview();
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_refresh, menu);
        menu.findItem(R.id.menu_search).setVisible(false);
        menu.findItem(R.id.menu_modify).setVisible(false);
        menu.findItem(R.id.menu_remove).setVisible(false);
        menu.findItem(R.id.menu_report).setVisible(false);
        return true;
    }

    public void reportCurrentReview(final String review_id, final int position) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(ScalarsConverterFactory.create()) // 문자열로 받기 위함.
                .build();

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
        Call<String> call = connectDB.reportReview(review_id, user_id);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                String result = response.body().trim();

                if(result.equals("removed")) { // 리뷰가 삭제되었을 경우 리로드.
                    AlertDialog.Builder builder = new AlertDialog.Builder(ReviewListActivity.this);
                    builder.setTitle("알림");
                    builder.setMessage("존재하지않는 리뷰입니다.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            reloadReview();
                        }
                    });
                    builder.show();
                } else if(!result.equals("failed")) {
                    if(Integer.parseInt(result) >= 5) {
                        removeCurrentReview(review_id, position);
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ReviewListActivity.this);
                    builder.setTitle("메시지");
                    builder.setMessage("이미 신고한 리뷰입니다.");
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
                Log.d("에러", t.getMessage());
            }
        });
    }

    public void removeCurrentReview(String review_id, final int position) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
        Call<String> call = connectDB.removeReview(review_id, restaurant.getId());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                String result = response.body().trim();
                if (result.equals("success")) {
                    if(review_adapter.getItem(position).getImages().size() != 0) {
                        final List<String> remove_list = new ArrayList<>(); // 스레드에서 deleteobjects할때 이미 리스트삭제되어서 새로 만들어줌
                        remove_list.addAll(review_adapter.getItem(position).getImages());
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // Amazon Cognito 인증 공급자 초기화
                                CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                                        ReviewListActivity.this,
                                        "ap-northeast-2:9c5bb2b0-44a8-4a1c-944a-98d817d44e82", // 자격 증명 풀 ID
                                        Regions.AP_NORTHEAST_2 // 리전
                                );

                                AmazonS3 s3 = new AmazonS3Client(credentialsProvider, Region.getRegion(Regions.AP_NORTHEAST_2));
                                s3.setEndpoint("s3.ap-northeast-2.amazonaws.com");

                                List<DeleteObjectsRequest.KeyVersion> key = new ArrayList<>();
                                for (int i = 0; i < remove_list.size(); i++) {
                                    key.add(new DeleteObjectsRequest.KeyVersion("review_images/" + remove_list.get(i)));
                                }

                                s3.deleteObjects(new DeleteObjectsRequest("myongjimoa").withKeys(key));
                            }
                        }).start();
                    }
                    reloadReview();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("실패", t.getMessage());
            }
        });
    }
}
