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
import android.widget.Toast;

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

    Button submit;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.add_user, container, false);
        // 사용할 레이아웃 inflate

        view.setBackgroundColor(Color.rgb(255, 255, 255)); // add로 전환할 시 화면 겹침 방지하기 위해 배경색 지정

        user_email = (EditText) view.findViewById(R.id.user_email);
        user_password = (EditText) view.findViewById(R.id.user_password);
        user_nickname = (EditText) view.findViewById(R.id.user_nickname);
        user_major = (EditText) view.findViewById(R.id.user_major);
        user_number = (EditText) view.findViewById(R.id.user_number);
        user_name = (EditText) view.findViewById(R.id.user_name);
        submit = (Button) view.findViewById(R.id.submit); // 레이아웃에 사용되는 객체 할당

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newUser();
            }
        }); // 리스너 등록. 버튼 클릭 시 회원가입 진행

        return view;
    }

    public void newUser() {

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String format_date = sdf.format(date); // 회원 가입 진행시 서버에 전송할 현재 시간 구함.


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(ScalarsConverterFactory.create()) // 문자열 형태로 받음.
                .build();

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
        Call<String> call = connectDB.addUser(user_email.getText().toString().trim(), user_password.getText().toString(), user_nickname.getText().toString().trim(), user_major.getText().toString().trim(), user_number.getText().toString().trim(), user_name.getText().toString().trim(), format_date);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                    String result = response.body().trim();
                    if(result.equals("success")) { // 회원 가입 정상 수행 시 서버에서 success 문자열 출력
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("메시지");
                        builder.setCancelable(false);
                        Log.d("회원가입 성공", result);
                        builder.setMessage("회원 가입에 성공하였습니다.");
                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((LoginActivity)getActivity()).removeAddUserFragment();
                            }
                        });
                        builder.show();
                    } else if(result.equals("email")){ // 서버에서 이메일 중복 검사에 걸림
                        Toast.makeText(getActivity(), "이미 등록된 이메일입니다.", Toast.LENGTH_SHORT).show();
                    } else if(result.equals("nickname")) { // 서버에서 닉네임 중복 검사에 걸림
                        Toast.makeText(getActivity(), "이미 등록된 닉네임입니다.", Toast.LENGTH_SHORT).show();
                    }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }
}
