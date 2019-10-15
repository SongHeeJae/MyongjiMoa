package com.example.myongjimoa;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    final int MODIFY_REQUEST_CODE=3000;
    User my_info;

    Button info;
    Button board;
    Button review;
    Button shuttle;
    Button menu;

    static boolean admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent it = getIntent();
        my_info = new User(it.getStringExtra("id"), it.getStringExtra("email_id"), it.getStringExtra("user_nickname"), it.getStringExtra("major"), it.getStringExtra("number"), it.getStringExtra("name"), it.getStringExtra("date"), it.getBooleanExtra("admin", false));
        admin = my_info.getAdmin();
        View.OnClickListener listener = new View.OnClickListener() { // 리스너 객체 생성
            @Override
            public void onClick(View v) {
                switch(v.getId()) {
                    // 필요한 정보들 Intent에 저장하여 startActivity
                    case R.id.main_info: // 정보 버튼 클릭 시
                        Intent info_it = new Intent(MainActivity.this, ModifyUserActivity.class);
                        info_it.putExtra("email_id", my_info.getEmail_id());
                        info_it.putExtra("user_nickname", my_info.getNickname());
                        info_it.putExtra("major", my_info.getMajor());
                        info_it.putExtra("number", my_info.getNumber());
                        info_it.putExtra("name", my_info.getName());
                        info_it.putExtra("date", my_info.getDate());
                        info_it.putExtra("user_id", my_info.getId());
                        startActivityForResult(info_it, MODIFY_REQUEST_CODE);
                        break;
                    case R.id.main_board: // 게시판 버튼 클릭 시
                        Intent board_it = new Intent(MainActivity.this, BoardMainActivity.class);
                        board_it.putExtra("user_id", my_info.getId());
                        board_it.putExtra("user_nickname", my_info.getNickname());
                        board_it.putExtra("user_major", my_info.getMajor());
                        startActivity(board_it);
                        break;
                    case R.id.main_review: // 리뷰 버튼 클릭 시
                        Intent review_it = new Intent(MainActivity.this, ReviewMainActivity.class);
                        review_it.putExtra("user_id", my_info.getId());
                        review_it.putExtra("user_nickname", my_info.getNickname());
                        startActivity(review_it);
                        break;
                    case R.id.main_shuttle: // 셔틀 버튼 클릭 시
                        startActivity(new Intent(MainActivity.this, ShuttleActivity.class));
                        break;
                    case R.id.main_menu: // 학식 메뉴 버튼 클릭 시
                        startActivity(new Intent(MainActivity.this, MenuActivity.class));
                        break;
                }
            }
        };

        info = (Button) findViewById(R.id.main_info);
        board = (Button) findViewById(R.id.main_board);
        review = (Button) findViewById(R.id.main_review);
        shuttle = (Button) findViewById(R.id.main_shuttle);
        menu = (Button) findViewById(R.id.main_menu);
        info.setOnClickListener(listener);
        board.setOnClickListener(listener);
        review.setOnClickListener(listener);
        shuttle.setOnClickListener(listener);
        menu.setOnClickListener(listener);

        Request.initFilterMap(); // Request 클래스의 initFilterMap 메소드 수행(비속어 필터 변수 초기화)

        permission(); // 권한 체크 메소드
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
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check(); // 권한 체크를 위해 TedPermission 라이브러리 이용. 읽기, 쓰기, 카메라 권한 허용
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MODIFY_REQUEST_CODE && resultCode == RESULT_OK){
            // 내 정보 수정화면에서 돌아왔을 시 수정된 정보로 업데이트해둠
            my_info.setNickname(data.getStringExtra("user_nickname"));
            my_info.setMajor(data.getStringExtra("user_major"));
        }
    }
}
