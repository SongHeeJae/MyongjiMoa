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
//    String[] user_data;
    String[] info;
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.login_main, container, false);

        login = (Button) view.findViewById(R.id.login);
        add_user = (Button) view.findViewById(R.id.add_user);
        user_email = (EditText) view.findViewById(R.id.login_email);
        user_password = (EditText) view.findViewById(R.id.login_password);

       // user_data = new String[2];
        info = new String[7];

        add_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LoginActivity)getActivity()).replaceAddUserFragment();
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
                        info[0] = result.getId();
                        info[1] = result.getEmail_id();
                        info[2] = result.getName();
                        info[3] = result.getMajor();
                        info[4] = result.getNumber();
                        info[5] = result.getName();
                        info[6] = result.getDate();

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("메시지");
                        builder.setMessage("로그인에 성공하였습니다.");
                        builder.setCancelable(false);
                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Intent it = new Intent(getActivity(), MainActivity.class);
                                it.putExtra("user_info", info);
                                getActivity().startActivity(it);
                                ((LoginActivity)getActivity()).startActivity(it);
                            }
                        });
                        builder.show();
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


/*
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                Log.d("성공~", "로그인성공");

                        info[0] = jsonObject.getString("email_id");
                        info[1] = jsonObject.getString("nickname");
                        info[2] = jsonObject.getString("school");
                        info[3] = jsonObject.getString("major");
                        info[4] = jsonObject.getString("number");
                        info[5] = jsonObject.getString("name");

                        builder.setTitle("메시지");
                        builder.setMessage("로그인에 성공하였습니다.");
                        builder.setCancelable(false);
                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
@Override
public void onClick(DialogInterface dialog, int which) {
        Intent it = new Intent(m_context, MainActivity.class);

        it.putExtra("user_info", info);
        getActivity().startActivity(it);
        } );
        // 분류
        Log.d("실패", "로그인실패");
        builder.setTitle("메시지");
        builder.setMessage("로그인에 실패하였습니다. 입력정보를 확인해주세요.");
        builder.setCancelable(false);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
@Override
public void onClick(DialogInterface dialog, int which) {
        }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        }

        add_user.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {

        }
        });*/