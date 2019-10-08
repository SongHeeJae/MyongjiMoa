package com.example.myongjimoa;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class AddUserFragment extends Fragment {

    EditText user_email;
    EditText user_password;
    EditText user_nickname;
    EditText user_major;
    EditText user_number;
    EditText user_name;

    String[] user_data;

    Button submit;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.add_user, container, false);

        view.setBackgroundColor(Color.rgb(255, 255, 255));

        user_email = (EditText) view.findViewById(R.id.user_email);
        user_password = (EditText) view.findViewById(R.id.user_password);
        user_nickname = (EditText) view.findViewById(R.id.user_nickname);
        user_major = (EditText) view.findViewById(R.id.user_major);
        user_number = (EditText) view.findViewById(R.id.user_number);
        user_name = (EditText) view.findViewById(R.id.user_name);

        submit = (Button) view.findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newUser();
            }
        });

        return view;
    }

    public void newUser() {

        Date date = new Date(); // 시스템 시간으로 구함 동기화되는지 확인 필요
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String format_date = sdf.format(date);


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(ScalarsConverterFactory.create()) // 문자열로 받기 위함.
                .build();

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
        Call<String> call = connectDB.addUser(user_email.getText().toString(), user_password.getText().toString(), user_nickname.getText().toString(), user_major.getText().toString(), user_number.getText().toString(), user_name.getText().toString(), format_date);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                    String result = response.body().trim();
                    Log.d("회원가입 성공", result);
                    if(result.equals("success")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("메시지");
                        builder.setMessage("회원 가입에 성공하였습니다.");
                        builder.setCancelable(false);
                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((LoginActivity)getActivity()).removeAddUserFragment();
                            }
                        });
                        builder.show();
                    } else {

                    }

            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("연결 실패", t.getMessage());
                Log.d("연결성공", "가입실패");
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("메시지");
                builder.setMessage("회원 가입에 실패하였습니다.");
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
