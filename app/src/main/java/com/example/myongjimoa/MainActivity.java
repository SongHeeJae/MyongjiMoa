package com.example.myongjimoa;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private FragmentManager fragment_manager;
    private CommunityFragment community_fragment;
    private ReviewFragment review_fragment;
    private InfoFragment info_fragment;
    private HomeFragment home_fragment;
    private BottomNavigationView bottom_navigation_view;
    final int MODIFY_REQUEST_CODE=3000;
    User my_info;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent it = getIntent();
        my_info = new User(it.getStringExtra("id"), it.getStringExtra("email_id"), it.getStringExtra("nickname"), it.getStringExtra("major"), it.getStringExtra("number"), it.getStringExtra("name"), it.getStringExtra("date"));
        fragment_manager = getSupportFragmentManager();
        community_fragment = new CommunityFragment();
        review_fragment = new ReviewFragment();
        info_fragment = new InfoFragment();
        home_fragment = new HomeFragment();


        fragment_manager.beginTransaction().replace(R.id.main_content, home_fragment).commitAllowingStateLoss();

        bottom_navigation_view = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottom_navigation_view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.community:
                        fragment_manager.beginTransaction().replace(R.id.main_content, community_fragment).commitAllowingStateLoss();
                    // beginTransaction은 FragmentTrasaction 만드는거니깐 뭔지 검색 필요 트랜잭션을 onCreate에서 할지 결정해야됨 (성능땜에)
                        break;
                    case R.id.review:
                        fragment_manager.beginTransaction().replace(R.id.main_content, review_fragment).commitAllowingStateLoss();
                        break;
                    case R.id.info:
                        fragment_manager.beginTransaction().replace(R.id.main_content, info_fragment).commitAllowingStateLoss();
                        break;
                    case R.id.home:
                        fragment_manager.beginTransaction().replace(R.id.main_content, home_fragment).commitAllowingStateLoss();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        permission();

    }




    public  void permission() {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 권한 요청 성공

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // 권한 요청 실패
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("사진 및 파일을 저장하기 위하여 접근 권한이 필요합니다.")
                .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .check();
    }

    public User getUserInfo() {
        return my_info;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_title_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.main_title_menu_info:

                Intent it = new Intent(this, ModifyUserActivity.class);
                it.putExtra("email_id", my_info.getEmail_id());
                it.putExtra("nickname", my_info.getNickname());
                it.putExtra("major", my_info.getMajor());
                it.putExtra("number", my_info.getNumber());
                it.putExtra("name", my_info.getName());
                it.putExtra("date", my_info.getDate());
                it.putExtra("user_id", my_info.getId());
                startActivityForResult(it, MODIFY_REQUEST_CODE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("리퀘스트코드는?", MODIFY_REQUEST_CODE + "");
        Log.d("ㅇㅇ코드", requestCode + "");
        Log.d("result코드", resultCode + "");
        Log.d("RESULT_OK", RESULT_OK + "");
        if(requestCode == MODIFY_REQUEST_CODE && resultCode == RESULT_OK){
            my_info.setNickname(data.getStringExtra("user_nickname"));
            my_info.setMajor(data.getStringExtra("user_major"));
            //Log.d("리퀘스트코드는?", REQUEST_CODE + "");
        }
    }
}
