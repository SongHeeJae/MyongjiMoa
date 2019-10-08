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
        setContentView(R.layout.login);

        fragment_manager = getSupportFragmentManager();
        login_fragment = new LoginFragment();
        add_user_fragment = new AddUserFragment();
        fragment_manager.beginTransaction().add(R.id.login_main, login_fragment).commit();
    }

    public void removeAddUserFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().remove(add_user_fragment).commit();
        fragmentManager.popBackStack();
    }

    public void addAddUserFragment() {
        FragmentTransaction fragment_transaction = fragment_manager.beginTransaction().add(R.id.login_main, add_user_fragment);
        fragment_transaction.addToBackStack(null);
        fragment_transaction.commitAllowingStateLoss();
    }
}
