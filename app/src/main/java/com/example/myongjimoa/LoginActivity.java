package com.example.myongjimoa;

import android.os.Bundle;

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
