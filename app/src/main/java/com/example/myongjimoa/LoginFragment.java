package com.example.myongjimoa;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginFragment extends Fragment {
    Button login;
    Button add_user;
    EditText user_email;
    EditText user_password;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.login_main, container, false);

        login = (Button) view.findViewById(R.id.login);
        add_user = (Button) view.findViewById(R.id.add_user);
        user_email = (EditText) view.findViewById(R.id.login_email);
        user_password = (EditText) view.findViewById(R.id.login_password);

        add_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LoginActivity)getActivity()).addAddUserFragment();
            }
        });

        login.setOnClickListener(new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                mainLogin();
            }
        } );
        return view;
    }

    public void mainLogin() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(GsonConverterFactory.create()) // 문자열로 받기 위함.
                .build();

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
        Call<User> call = connectDB.userLogin(user_email.getText().toString(), user_password.getText().toString());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                    User result = response.body();

                    if(response.body() != null) {
                        Intent it = new Intent(getActivity(), MainActivity.class);
                        it.putExtra("id", result.getId());
                        it.putExtra("email_id", result.getEmail_id());
                        it.putExtra("user_nickname", result.getNickname());
                        it.putExtra("major", result.getMajor());
                        it.putExtra("number", result.getNumber());
                        it.putExtra("name", result.getName());
                        it.putExtra("date", result.getDate());
                        it.putExtra("admin", result.getAdmin());
                        getActivity().startActivity(it);
                        getActivity().finish();

                    } else {
                        Log.d("눌임", "ㅇㅇ");
                    }

            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d("로그인 처리 실패", t.getMessage());
                Log.d("로그인실패", "로그인 실패");
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("메시지");
                builder.setMessage("로그인에 실패하였습니다.");
                builder.setCancelable(false);
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.show();
            }
        });
    }
}
