package com.example.myongjimoa;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
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
    private SettingFragment setting_fragment;
    private BottomNavigationView bottom_navigation_view;
    User my_info;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent it = getIntent();
        my_info = new User(it.getStringArrayExtra("user_info"));
        fragment_manager = getSupportFragmentManager();
        community_fragment = new CommunityFragment();
        review_fragment = new ReviewFragment();
        info_fragment = new InfoFragment();
        setting_fragment = new SettingFragment();


        fragment_manager.beginTransaction().replace(R.id.main_content, community_fragment).commitAllowingStateLoss();

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
                    case R.id.game:
                        fragment_manager.beginTransaction().replace(R.id.main_content, info_fragment).commitAllowingStateLoss();
                        break;
                    case R.id.setting:
                        fragment_manager.beginTransaction().replace(R.id.main_content, setting_fragment).commitAllowingStateLoss();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        permission();
      /*  if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        1);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        }
*/
    }


    // 거부했을때 체크크


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
}
