package com.example.myongjimoa;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

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
    EditText major_text;
    Button submit;
    String user_id;
    boolean modify_check;

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
        major_text = (EditText) findViewById(R.id.edit_user_major);
        submit = (Button) findViewById(R.id.modify_submit);

        Intent it = getIntent();
        email_id.setText(it.getStringExtra("email_id"));
        nickname.setText(it.getStringExtra("user_nickname"));
        major.setText(it.getStringExtra("major"));
        number.setText(it.getStringExtra("number"));
        name.setText(it.getStringExtra("name"));
        date.setText(it.getStringExtra("date"));
        user_id = it.getStringExtra("user_id");

        modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(modify_check) {
                    nickname_text.setText("");
                    major_text.setText("");
                    modify.setText("취소");
                    submit.setVisibility(View.VISIBLE);
                    textSwitch();
                    modify_check = false;
                } else {
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

    void textSwitch() {
        ViewSwitcher major_switcher = (ViewSwitcher) findViewById(R.id.major_switcher);
        ViewSwitcher nickname_switcher = (ViewSwitcher) findViewById(R.id.nickname_switcher);
        major_switcher.showNext();
        nickname_switcher.showNext();
    }

    void modifyUserInfo() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
        Call<String> call = connectDB.modifyUser(user_id, nickname_text.getText().toString().trim(), major_text.getText().toString().trim());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                String result = response.body().trim();

                if(result.equals("success")) {
                    modify.setText("수정");
                    submit.setVisibility(View.INVISIBLE);
                    nickname.setText(nickname_text.getText().toString());
                    major.setText(major_text.getText().toString());
                    textSwitch();
                    modify_check=true;
                } else {
                    Toast.makeText(getApplicationContext(), "이미 등록된 닉네임입니다.", Toast.LENGTH_SHORT).show();
                }

            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("실패", t.getMessage());
            }

        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("user_nickname", nickname.getText().toString());
        intent.putExtra("user_major", major.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }
}
