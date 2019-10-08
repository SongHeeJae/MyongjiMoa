package com.example.myongjimoa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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

public class ReviewActivity extends AppCompatActivity {

    private final int GET_GALLERY_IMAGE = 0;

    String category;
    String user_id;
    String nickname;

    private FragmentManager fragment_manager;
    private ReviewRestaurantFragment review_restaurant_fragment;
    private ReviewListFragment review_list_fragment;
    private ReviewWriteFragment review_write_fragment;

    Restaurant current_restaurant;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_main);

        fragment_manager = getSupportFragmentManager();
        review_restaurant_fragment = new ReviewRestaurantFragment();

        Intent it = getIntent();
        category = it.getStringExtra("category");
        user_id = it.getStringExtra("user_id");
        nickname = it.getStringExtra("nickname");

        fragment_manager.beginTransaction().add(R.id.review_fragment, review_restaurant_fragment).commit();
    }

    public void addReviewListFragment(Restaurant r) {
        current_restaurant = r;
        review_list_fragment = new ReviewListFragment();
        FragmentTransaction fragment_transaction = fragment_manager.beginTransaction().add(R.id.review_fragment, review_list_fragment);
        fragment_transaction.addToBackStack(null); // 뒤로가기 했을때 액티비티 종료 안되기 위함.
        fragment_transaction.commitAllowingStateLoss();
    }

    public void addReviewWriteFragment() {
        review_write_fragment = new ReviewWriteFragment();
        FragmentTransaction fragment_transaction = fragment_manager.beginTransaction().add(R.id.review_fragment, review_write_fragment);
        fragment_transaction.addToBackStack(null); // 뒤로가기 했을때 액티비티 종료 안되기 위함.
        fragment_transaction.commit();
    //    fragment_manager.beginTransaction().hide(review_list_fragment).commit();
    //    fragment_manager.beginTransaction().show(review_write_fragment).commit();

        Log.d("쓰기실행", "쓰기실행");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            review_write_fragment.setWriteImage(data.getClipData());
        }
    }

    public void getGallery() {
        Intent it = new Intent();
        it.setAction(Intent.ACTION_GET_CONTENT);
        it.setType("image/*");
        it.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(it, "Get Image"), GET_GALLERY_IMAGE);
        //최근사진 읽어오면 절대경로 오류남
        // startActivityForResult(it, GET_GALLERY_IMAGE);
    }

    public void removeWriteFragment() {
      //  review_list_fragment.clearAdapter();
        review_list_fragment.reloadReview();
        fragment_manager.beginTransaction().remove(review_write_fragment).commit();
        fragment_manager.popBackStack();
    }
}
