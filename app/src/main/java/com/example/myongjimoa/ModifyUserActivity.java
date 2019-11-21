package com.example.myongjimoa;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ModifyUserActivity extends AppCompatActivity {

    Button modify;
    TextView email_id;
    TextView nickname;
    TextView major;
    TextView number;
    TextView name;
    TextView date;
    EditText nickname_text;
    Button submit;
    String user_id;
    Spinner mod_majorSpinner;
    boolean modify_check;

    String tempnick;
    String tempmajor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify_user);

        setTitle("내정보 확인");

        modify_check = true;

        modify = (Button) findViewById(R.id.user_modify);
        email_id = (TextView) findViewById(R.id.modify_user_email);
        nickname = (TextView) findViewById(R.id.modify_user_nickname);
        major = (TextView) findViewById(R.id.modify_user_major);
        number = (TextView) findViewById(R.id.modify_user_number);
        name = (TextView) findViewById(R.id.modify_user_name);
        date = (TextView) findViewById(R.id.modify_user_date);
        nickname_text = (EditText) findViewById(R.id.edit_user_nickname);
        submit = (Button) findViewById(R.id.modify_submit);

        Intent it = getIntent();
        email_id.setText(it.getStringExtra("email_id"));
        nickname.setText(it.getStringExtra("user_nickname"));
        major.setText(it.getStringExtra("major"));
        number.setText(it.getStringExtra("number"));
        name.setText(it.getStringExtra("name"));
        date.setText(it.getStringExtra("date"));
        user_id = it.getStringExtra("user_id");

        tempnick = nickname.getText().toString().trim();
        tempmajor = major.getText().toString();

        nickname.setOnKeyListener(new View.OnKeyListener() { // 닉네임 재입력 edittext 엔터키 차단
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == event.KEYCODE_ENTER)
                    return true;
                return false;
            }
        });

        modify.setOnClickListener(new View.OnClickListener() { // 수정버튼 이벤트 처리

            @Override
            public void onClick(View v) { // 수정 버튼 눌렀을 때

                mod_majorSpinner = (Spinner)findViewById(R.id.spinner_major);
                ArrayAdapter majorAdapter = ArrayAdapter.createFromResource(getBaseContext(), R.array.major_field, android.R.layout.simple_spinner_item);
                majorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mod_majorSpinner.setAdapter(majorAdapter);
                mod_majorSpinner.setBackgroundColor(Color.WHITE);

                if(modify_check) { // 수정 상태일때 원상태로돌려줌
                    nickname_text.setText("");
                    modify.setText("취소");
                    submit.setVisibility(View.VISIBLE);
                    textSwitch();
                    modify_check = false;
                } else {
                    // 수정상태로 들어감
                    modify.setText("수정");
                    submit.setVisibility(View.INVISIBLE);
                    textSwitch();
                    modify_check=true;
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ModifyUserActivity.this);
                builder.setTitle("알림")
                        .setMessage("회원 정보를 수정하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!isNetworkConnected()) { // false 인 경우 네트워크 연결 안되어있음.
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ModifyUserActivity.this);
                                    builder.setTitle("메시지")
                                            .setMessage("네트워크 연결을 확인해 주세요.")
                                            .setCancelable(false)
                                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                }
                                            });
                                    builder.show();
                                    return ;
                                }
                                modifyUserInfo();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        });
    }

    void textSwitch() { // ViewSwitcher를 이용하여 TextField를 EditText로 바꿔줌
        ViewSwitcher major_switcher = (ViewSwitcher) findViewById(R.id.major_switcher);
        ViewSwitcher nickname_switcher = (ViewSwitcher) findViewById(R.id.nickname_switcher);
        major_switcher.showNext();
        nickname_switcher.showNext();
    }


    void modifyUserInfo() { // 회원정보 수정 진행 // 서버에서
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
        Call<String> call = connectDB.modifyUser(user_id, nickname_text.getText().toString().trim(), mod_majorSpinner.getSelectedItem().toString());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                String result = response.body().trim();

                if(nickname_text.getText().toString().trim().equals("") || mod_majorSpinner.getSelectedItem().toString().equals("학과를 선택하세요.")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ModifyUserActivity.this);
                    builder.setTitle("메시지");
                    builder.setMessage("모든 정보를 기입하세요!");
                    builder.setCancelable(false);
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                }

                else {
                    if(nickname_text.getText().toString().trim().equals(tempnick) || result.equals("success")) { // 결과 정상 처리
                        modify.setText("수정");
                        submit.setVisibility(View.INVISIBLE);
                        nickname.setText(nickname_text.getText().toString());
                        major.setText(mod_majorSpinner.getSelectedItem().toString());
                        textSwitch();
                        modify_check=true;
                        Toast.makeText(getApplicationContext(), "수정되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        // 이미 등록된 닉네임일때
                        Toast.makeText(getApplicationContext(), "이미 등록된 닉네임입니다.", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
            }

        });
    }

    @Override
    public void onBackPressed() { // 뒤로가기로 Activity 종료할 때 수정된 사용자 닉네임과 전공 담아줌.

        /*
        에러 내용
        수정하다가 앱을 종료하면 서버에 정보가 저장되지 않아서 다음 실행할때 내정보 확인화면에 들어가면
        정보가 default 설정값으로 설정되어 있음.
        정보가 이전 정보로 돌아갈 수 있도록 서버를 만져야 함.
         */

        Intent intent = new Intent();

        if(nickname_text.getText().toString().trim().equals("") || mod_majorSpinner.getSelectedItem().toString().equals("학과를 선택하세요.")) {
            // 닉네임과 전공을 수정 중에 나간 경우, 수정 전 정보로 기입해줌.
            intent.putExtra("user_nickname", tempnick);
            intent.putExtra("user_major", tempmajor);
            setResult(RESULT_OK, intent);
            finish();
            super.onBackPressed();
        }

        else {
            intent.putExtra("user_nickname", nickname.getText().toString());
            intent.putExtra("user_major", major.getText().toString());
            setResult(RESULT_OK, intent);
            finish();
            super.onBackPressed();
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }
}
