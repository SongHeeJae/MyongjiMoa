package com.example.myongjimoa;

import android.app.AlertDialog;
import android.content.Context;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
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

public class ReviewListActivity extends AppCompatActivity implements OnMapReadyCallback {

    TextView title;
    TextView category;
    TextView telephone;
    TextView homepage;
    TextView address;
    TextView time;
    TextView menu;
    TextView score;
    TextView review_num;
    TextView no_review;
    RatingBar restaurant_rating_bar;
    ImageButton write_review;
    Restaurant restaurant;

    RecyclerView review_recycler_view;
    ReviewAdapter review_adapter;
    public GestureDetector gesture_detector;

    boolean scroll;
    boolean initcheck;
    String count_review_id;

    MapFragment mapFragment;
    SwipeRefreshLayout swipe_refresh_layout;

    String user_id;
    String user_nickname;

    private final int WRITE_REQUEST_CODE = 1000;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_list);

        mAdView = findViewById(R.id.review_list_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        String[] menulist;

        gesture_detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

        Intent it = getIntent();

        restaurant = new Restaurant(it.getStringExtra("id"), it.getStringExtra("title"), it.getStringExtra("category"), it.getStringExtra("telephone"), it.getStringExtra("homepage"), it.getStringExtra("address"), it.getStringExtra("mapx"), it.getStringExtra("mapy"), it.getStringExtra("restaurant_id"), it.getStringExtra("time"), it.getStringExtra("menu"), it.getStringExtra("image"), Integer.parseInt(it.getStringExtra("review_num")), Float.parseFloat(it.getStringExtra("score")));
        // 가져온 데이터로 Restaurant 객체 생성
        user_id = it.getStringExtra("user_id");
        user_nickname = it.getStringExtra("user_nickname");
        title = (TextView) findViewById(R.id.restaurant_title);
        category = (TextView) findViewById(R.id.restaurant_category);
        telephone = (TextView) findViewById(R.id.restaurant_telephone);
        homepage = (TextView) findViewById(R.id.restaurant_homepage);
        address = (TextView) findViewById(R.id.restaurant_address);
        time = (TextView) findViewById(R.id.restaurant_time);
        menu = (TextView) findViewById(R.id.restaurant_menu);
        score = (TextView) findViewById(R.id.restaurant_score);
        review_num = (TextView) findViewById(R.id.restaurant_review_num);
        no_review = (TextView) findViewById(R.id.no_review);
        restaurant_rating_bar = (RatingBar) findViewById(R.id.review_list_rating_bar);
        write_review = (ImageButton) findViewById(R.id.write_review);
        review_recycler_view = (RecyclerView) findViewById(R.id.review_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        review_recycler_view.setLayoutManager(layoutManager); // 레이아웃 지정

        swipe_refresh_layout = (SwipeRefreshLayout) findViewById(R.id.review_swipe_refresh_layout);
        swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipe_refresh_layout.setRefreshing(false);
                reloadReview(); // 위로 당겼을 때 리뷰 목록 새로고침
            }
        });

        write_review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(ReviewListActivity.this, ReviewWriteActivity.class);
                it.putExtra("user_id", user_id);
                it.putExtra("restaurant_id", restaurant.getId());
                InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(write_review.getWindowToken(), 0); // 화면에 올라온 키보드 내려줌
                startActivityForResult(it, WRITE_REQUEST_CODE); // 글쓰기 버튼 클릭 시 글쓰기 화면으로 이동
            }
        });

        menulist = restaurant.getMenu().split("/");

        title.setText(restaurant.getTitle());
        category.setText(restaurant.getCategory());
        telephone.setText("전화번호 : " + restaurant.getTelephone());
        homepage.setText("홈페이지 : " + restaurant.getHomepage());
        address.setText("식당주소 : " + restaurant.getAddress());
        time.setText("영업시간 : " + restaurant.getTime());
        menu.setText(arrayJoin(menulist));
        score.setText(restaurant.getScore() + "" + " 점");
        review_num.setText(restaurant.getReview_num() + "" + " 개");

        scroll = true;
        count_review_id = "`review`.`id`+1";
        review_adapter = new ReviewAdapter();
        downloadReviewList(); // 다운로드 정보 초기값 지정 후 리뷰 다운로드 진행

        review_recycler_view.setAdapter(review_adapter); // RecyclerView 어댑터 등록

        mapFragment = (MapFragment)getSupportFragmentManager().findFragmentById(R.id.review_map);

        if(mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.review_list_map, mapFragment).commit();
            mapFragment.getMapAsync(this);
        } // 네이버 지도 API 사용할 fragment 생성

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @UiThread
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        Tm128 tm128 = new Tm128(Integer.parseInt(restaurant.getMapx()), Integer.parseInt(restaurant.getMapy()));
        LatLng coord = tm128.toLatLng(); // 서버에는 Tm128 좌표계가 저장되어있어서 LatLng좌표계로 바꿔줌
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(coord);
        naverMap.moveCamera(cameraUpdate); // 그 위치로 지도의 카메라 좌표 옮겨줌
        naverMap.setLiteModeEnabled(true); // 라이트모드 속도빨라짐
        Marker marker = new Marker();
        marker.setPosition(coord);
        marker.setMap(naverMap); // 해당 위치에 마커 찍어줌.
    }

    public String arrayJoin(String array[]) {
        String result = "";
        for(int i = 0; i < array.length; i++) {
            result += array[i];
            if(i < array.length - 1)
                result += "\n";
        }
        return result;
    }

    @Override
    public void onStart() {
        super.onStart();
        restaurant_rating_bar.setRating(restaurant.getScore());
    }

    public void downloadReviewList() { // 리뷰 목록 다운로드

        ConnectDB connectDB = Request.getRetrofit().create(ConnectDB.class);
        Call<ReviewResult> call = connectDB.downloadReview(restaurant.getId(), count_review_id); // ReviewResult 형태로 받아옴
        call.enqueue(new Callback<ReviewResult>() {
            @Override
            public void onResponse(Call<ReviewResult> call, Response<ReviewResult> response) {

                ReviewResult result = response.body();

                if (result.getReviewCount() != 0) {
                    for (int i = 0; i < result.getReviewCount(); i++) {
                        review_adapter.add(result.getReview(i)); // 어댑터에 add
                    }
                    if (result.getReviewCount() < 15) scroll = false; // 데이터 다 가져왔을 경우
                    count_review_id = result.getReview(result.getReviewCount() - 1).getId();
                    // 마지막 리뷰의 id 기억해줌
                }
                else if(review_adapter.getItemCount() == 0)
                    no_review.setVisibility(View.VISIBLE);

                restaurant.setReview_num(result.getReview_num());
                restaurant.setScore(result.getScore());
                score.setText("별점 : " + restaurant.getScore() + "점");
                review_num.setText("리뷰개수 : " + restaurant.getReview_num() + "개");
                restaurant_rating_bar.setRating(restaurant.getScore());
                // 다시 받아온 리뷰 갯수와 별점으로 업데이트

                review_recycler_view.clearOnScrollListeners();
                review_recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView view, int scrollState) {
                        if(scrollState == RecyclerView.SCROLL_STATE_SETTLING ) {
                            if(scroll) downloadReviewList(); // 스크롤이 끝에 닿았을 때 더 불러올데이터가있으면 다시 다운로드 진행
                        }
                    }
                });
            }
            @Override
            public void onFailure(Call<ReviewResult> call, Throwable t) {
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
            ImageButton review_setting;
            int pos;
            String temp;

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
                image_recycler_view.setAdapter(review_image_adapter); // 리뷰 내의 이미지 RecyclerView 어댑터와 레이아웃 설정

                image_recycler_view.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                    @Override
                    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                        View childView = rv.findChildViewUnder(e.getX(), e.getY());

                        if(childView != null && gesture_detector.onTouchEvent((e))) {
                            int currentPos = rv.getChildAdapterPosition(childView);
                            Intent it = new Intent(ReviewListActivity.this, ImageActivity.class);
                            it.putStringArrayListExtra("images", new ArrayList<>(review_image_adapter.getItems()));
                            it.putExtra("current", currentPos);
                            startActivity(it); // 이미지 선택 할 시 ImageActivity로 이동
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
                review_setting = (ImageButton) itemView.findViewById(R.id.review_setting);
                number = (TextView) itemView.findViewById(R.id.review_number);
                major = (TextView) itemView.findViewById(R.id.review_major);

                review_setting.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(ReviewListActivity.this);

                        DialogInterface.OnClickListener dialog_listener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0) { // 신고 버튼 클릭

                                    final AlertDialog.Builder builder = new AlertDialog.Builder(ReviewListActivity.this);
                                    builder.setTitle("신고").setMessage("정말 신고하시겠습니까?");
                                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            reportCurrentReview(items.get(pos).getId(), pos);
                                        }
                                    });

                                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Toast.makeText(getApplicationContext(), "취소되었습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    builder.show();
                                } else if (which == 1) { // 삭제 버튼 클릭
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ReviewListActivity.this);
                                    builder.setTitle("삭제").setMessage("정말 삭제하시겠습니까?");
                                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Toast.makeText(getApplicationContext(), "처리되었습니다.", Toast.LENGTH_SHORT).show();
                                            removeCurrentReview(items.get(pos).getId(), pos);

                                        }
                                    });
                                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Toast.makeText(getApplicationContext(), "취소되었습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    builder.show();
                                }
                            }
                        };
                        // 리뷰 세팅 버튼 클릭시 관리자 또는 본인 계정의 글일시, 다른 계정의 글 일시 다이얼로그 보여줌
                        if(items.get(pos).getNickname().equals(user_nickname) || MainActivity.admin) builder.setItems(new String[]{"신고", "삭제"}, dialog_listener);
                        else builder.setItems(new String[]{"신고"}, dialog_listener);
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                });
            }

            public void setData(Review data, int position) { // xml 데이터 세팅 부분

                temp = data.getDate().substring(0, 16);

                nickname.setText("별명 : " + data.getNickname());
                date.setText("시간 : " + temp);
                rating_bar.setRating(data.getScore());
                description.setText(data.getDescription());
                number.setText("학번 : " + data.getNumber());
                major.setText("전공 : " + data.getMajor());
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
                Glide.with(ReviewListActivity.this)
                        .load(data)
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

    public void reloadReview() { // 리뷰 목록 reload
        // 리뷰 다운로드 데이터 초기화
        scroll = true;
        count_review_id = "`review`.`id`+1";
        review_adapter = new ReviewAdapter();
        review_recycler_view.setAdapter(review_adapter);
        downloadReviewList(); // 다시 다운로드 진행
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == WRITE_REQUEST_CODE && resultCode == RESULT_OK) {
            no_review.setVisibility(View.GONE);
           reloadReview(); // 글쓰기하고 돌아왔을 시 reload
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // 메뉴아이템 이벤트 처리
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                reloadReview(); // 리뷰 새로고침
                return true;
            case android.R.id.home:
                finish(); // 현재 Activity 종료
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

        ConnectDB connectDB = Request.getRetrofit().create(ConnectDB.class);
        Call<String> call = connectDB.reportReview(review_id, user_id);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                String result = response.body().trim();

                if(result.equals("removed")) { // 리뷰가 이미 삭제되었을 경우 리로드.
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
                }

                if(!result.equals("failed")) { // 정상 신고 처리되었을 경우
                    Toast.makeText(getApplicationContext(), "신고했습니다.", Toast.LENGTH_SHORT).show();
                    if(Integer.parseInt(result) >= 5) removeCurrentReview(review_id, position); // 신고 누적 5회 이상일 시 선택 리뷰 삭제 수행

                } else {
                    // 이미 신고한 리뷰 일 때
                    AlertDialog.Builder builder = new AlertDialog.Builder(ReviewListActivity.this);
                    builder.setTitle("메시지");
                    builder.setMessage("이미 신고한 리뷰입니다.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
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

        ConnectDB connectDB = Request.getRetrofit().create(ConnectDB.class);
        Call<String> call = connectDB.removeReview(review_id, restaurant.getId());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                String result = response.body().trim();
                if (result.equals("success")) {
                    if(review_adapter.getItem(position).getImages().size() != 0) {
                        final List<String> remove_list = new ArrayList<>(); // 스레드에서 deleteobjects할때 이미 리스트삭제되어 오류 뜨는경우 있어서 새로 만들어줌
                        remove_list.addAll(review_adapter.getItem(position).getImages());
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                List<DeleteObjectsRequest.KeyVersion> key = new ArrayList<>();
                                for (int i = 0; i < remove_list.size(); i++) {
                                    key.add(new DeleteObjectsRequest.KeyVersion("review_images/" + remove_list.get(i)));
                                } // 키 등록해준 후

                                Request.getAmazonS3(ReviewListActivity.this).deleteObjects(new DeleteObjectsRequest("myongjimoa").withKeys(key));
                                // 해당 버킷의 이미지 삭제 진행
                            }
                        }).start();
                    }
                    reloadReview(); // 삭제 마친후 리뷰 리로드
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("실패", t.getMessage());
            }
        });
    }
}
