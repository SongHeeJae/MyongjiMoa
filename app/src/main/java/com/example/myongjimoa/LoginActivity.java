package com.example.myongjimoa;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class LoginActivity extends AppCompatActivity {
    private FragmentManager fragment_manager;
    private LoginFragment login_fragment;
    private AddUserFragment add_user_fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login); // 레이아웃 설정

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        String id = sp.getString("id", "null");
        if(!id.equals("null")) {
            Toast.makeText(this, "자동로그인 되었습니다.", Toast.LENGTH_LONG).show();
            Intent it = new Intent(this, MainActivity.class);
            it.putExtra("id", id);
            it.putExtra("email_id",sp.getString("email_id", ""));
            it.putExtra("user_nickname", sp.getString("user_nickname", ""));
            it.putExtra("major", sp.getString("major", ""));
            it.putExtra("number", sp.getString("number", ""));
            it.putExtra("name", sp.getString("name", ""));
            it.putExtra("date", sp.getString("date", ""));
            it.putExtra("admin", sp.getBoolean("admin", false));
            startActivity(it);
            finish();
        }

        fragment_manager = getSupportFragmentManager(); // 프래그먼트 매니저 생성
        login_fragment = new LoginFragment(); // 로그인 프래그먼트 생성
        fragment_manager.beginTransaction().add(R.id.login_main, login_fragment).commit(); // 초기화면 login_fragment로 전환
    }

    public void removeAddUserFragment() {
        fragment_manager.beginTransaction().remove(add_user_fragment).commit(); // 회원가입 프래그먼트 삭제
        fragment_manager.popBackStack(); // 백스택에서 제거
    }

    public void addAddUserFragment() {
        add_user_fragment = new AddUserFragment();
        FragmentTransaction fragment_transaction = fragment_manager.beginTransaction().add(R.id.login_main, add_user_fragment);
        fragment_transaction.addToBackStack(null); // 백스택 사용하도록 지정. 화면 전환 전 현재 화면이 백스택에 저장됨
        fragment_transaction.commitAllowingStateLoss(); // 화면 전환
    }
}
