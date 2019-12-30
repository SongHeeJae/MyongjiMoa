package com.example.myongjimoa;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {
    boolean bool_login = false;
    boolean fail_login = false;
    ImageButton login;
    ImageButton add_user;
    EditText user_mail;
    EditText user_password;
    LinearLayout layout;
    ProgressBar progressBar;
    CheckBox auto;
    int value = 0;
    Handler handler = new Handler();

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.login_main, container, false);
        // 사용할 레이아웃 inflate
        login = (ImageButton) view.findViewById(R.id.login);
        add_user = (ImageButton) view.findViewById(R.id.add_user);
        user_mail = (EditText) view.findViewById(R.id.login_mail);
        user_password = (EditText) view.findViewById(R.id.login_password); // 레이아웃의 id 값으로 버튼, 텍스트 객체 할당
        progressBar = (ProgressBar) view.findViewById(R.id.h_progressbar);
        auto = (CheckBox) view.findViewById(R.id.auto);

        layout = (LinearLayout) view.findViewById(R.id.layout);
        layout.setBackgroundResource(R.drawable.back);

        user_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        user_mail.setOnKeyListener(new View.OnKeyListener() { // 아이디 입력 edittext 엔터키 차단
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == event.KEYCODE_ENTER)
                    return true;
                return false;
            }
        });

        user_password.setOnKeyListener(new View.OnKeyListener() { // 비밀번호 입력 edittext 엔터키 차단
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == event.KEYCODE_ENTER)
                    return true;
                return false;
            }
        });

        add_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LoginActivity) getActivity()).addAddUserFragment();
            }
        }); // 리스너 등록. 버튼 클릭 시 회원가입화면으로 전환

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isNetworkConnected()) { // false 인 경우 네트워크 연결 안되어있음.
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()); // 로그인 실패하였을 시 알림 처리
                    builder.setTitle("메시지");
                    builder.setMessage("네트워크 연결을 확인해 주세요.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                    progressBar.setProgress(0);
                    return ;
                }

                if (!bool_login) {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (true) {
                                value += 10;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setProgress(value);
                                    }
                                });

                                if (fail_login) {
                                    progressBar.setProgress(0);
                                    break;
                                }

                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    thread.start();
                }
                else if (bool_login) // 로그인 성공시 progressBar
                    progressBar.setProgress(100);
                mainLogin();
            }
        });  // 리스너 등록. 버튼 클릭 시 메인 화면으로 전환
        return view;
    }

    public void mainLogin() {

        ConnectDB connectDB = Request.getRetrofit().create(ConnectDB.class);

        Call<User> call = connectDB.userLogin(user_mail.getText().toString(), user_password.getText().toString());

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {

                User result = response.body(); // User에 결과가 담김
                if (result.getId() != null) { // 받은 결과의 아이디 값이 null이 아니면 로그인 성공
                    bool_login = true;
                    Toast.makeText(getActivity(), "띵지모아에 오신 것을 환영합니다!", Toast.LENGTH_LONG).show();
                    Intent it = new Intent(getActivity(), MainActivity.class);
                    it.putExtra("id", result.getId());
                    it.putExtra("email_id", result.getEmail_id());
                    it.putExtra("user_nickname", result.getNickname());
                    it.putExtra("major", result.getMajor());
                    it.putExtra("number", result.getNumber());
                    it.putExtra("name", result.getName());
                    it.putExtra("date", result.getDate());
                    it.putExtra("admin", result.getAdmin());
                    if(auto.isChecked()) {
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("id", result.getId());
                        editor.putString("email_id", result.getEmail_id());
                        editor.putString("user_nickname", result.getNickname());
                        editor.putString("major", result.getMajor());
                        editor.putString("number", result.getNumber());
                        editor.putString("name", result.getName());
                        editor.putString("date", result.getDate());
                        editor.putBoolean("admin", result.getAdmin());
                        editor.apply();
                    }
                    getActivity().startActivity(it);
                    getActivity().finish(); // 회원정보 담아서 MainActivity로 이동. LoginActivity는 종료
                } else{
                    fail_login = true;
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()); // 로그인 실패하였을 시 알림 처리
                    builder.setTitle("로그인 실패");
                    builder.setMessage("아이디나 비밀번호가 틀렸습니다.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                    fail_login = false;
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d("로그인 실패", "");
            }
        });
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }
}