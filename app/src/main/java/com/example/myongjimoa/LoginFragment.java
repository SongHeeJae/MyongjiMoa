package com.example.myongjimoa;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {
    ImageButton login;
    ImageButton add_user;
    EditText user_email;
    EditText user_password;
    LinearLayout layout;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.login_main, container, false);
        // 사용할 레이아웃 inflate
        login = (ImageButton) view.findViewById(R.id.login);
        add_user = (ImageButton) view.findViewById(R.id.add_user);
        user_email = (EditText) view.findViewById(R.id.login_email);
        user_password = (EditText) view.findViewById(R.id.login_password); // 레이아웃의 id 값으로 버튼, 텍스트 객체 할당

        layout = (LinearLayout) view.findViewById(R.id.layout);
        layout.setBackgroundResource(R.drawable.back);

        user_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        add_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LoginActivity)getActivity()).addAddUserFragment();
            }
        }); // 리스너 등록. 버튼 클릭 시 회원가입화면으로 전환

        login.setOnClickListener(new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                mainLogin();
            }
        } );  // 리스너 등록. 버튼 클릭 시 메인 화면으로 전환
        return view;
    }

    public void mainLogin() {

        SharedPreferences loginInformation = this.getActivity().getSharedPreferences("setting", 0);
        final SharedPreferences.Editor editor = loginInformation.edit();

        ConnectDB connectDB = Request.getRetrofit().create(ConnectDB.class);
        Call<User> call = connectDB.userLogin(user_email.getText().toString(), user_password.getText().toString());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                    User result = response.body(); // User에 결과가 담김

                    if(result.getId() != null) { // 받은 결과의 아이디 값이 null이 아니면 로그인 성공
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
                        getActivity().startActivity(it);
                        getActivity().finish(); // 회원정보 담아서 MainActivity로 이동. LoginActivity는 종료
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()); // 로그인 실패하였을 시 알림 처리
                        builder.setTitle("메시지");
                        builder.setMessage("아이디나 비밀번호가 틀렸습니다.");
                        builder.setCancelable(false);
                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.show();
                    }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d("로그인 처리 실패", t.getMessage());
            }
        });
    }
}
