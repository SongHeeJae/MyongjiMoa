package com.example.myongjimoa;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class AddUserFragment extends Fragment {

    EditText user_email;
    EditText user_password;
    EditText re_password;
    EditText user_nickname;
    EditText user_number;
    EditText user_name;
    ImageButton submit;
    Spinner majorSpinner;
    CheckBox checkBox;
    CheckBox checkBox2;
    boolean errcode = true;

    String temp_num;
    String temp_pw;


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.add_user, container, false);

        user_email = (EditText) view.findViewById(R.id.user_email);
        user_password = (EditText) view.findViewById(R.id.user_password);
        re_password = (EditText) view.findViewById(R.id.re_password);
        user_nickname = (EditText) view.findViewById(R.id.user_nickname);
        user_number = (EditText) view.findViewById(R.id.user_number);
        user_name = (EditText) view.findViewById(R.id.user_name);
        submit = (ImageButton) view.findViewById(R.id.submit);
        checkBox = (CheckBox) view.findViewById(R.id.etpw);
        checkBox2 = (CheckBox) view.findViewById(R.id.etpw2); // 레이아웃에 사용되는 객체 할당

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked)
                    user_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                else
                    user_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
        });

        checkBox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked)
                    re_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                else
                    re_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
        });

        majorSpinner = (Spinner)view.findViewById(R.id.spinner_major);
        final ArrayAdapter majorAdapter = ArrayAdapter.createFromResource(getContext(), R.array.major_field, android.R.layout.simple_spinner_item);
        majorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        majorSpinner.setAdapter(majorAdapter);
        majorSpinner.setBackgroundColor(Color.rgb(180, 218, 215));
        majorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.rgb(116, 116, 116));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        user_email.setOnKeyListener(new View.OnKeyListener() { // 아이디 입력 edittext 엔터키 차단
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

        re_password.setOnKeyListener(new View.OnKeyListener() { // 비밀번호 재입력 edittext 엔터키 차단
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == event.KEYCODE_ENTER)
                    return true;
                return false;
            }
        });

        user_nickname.setOnKeyListener(new View.OnKeyListener() { // 닉네임 입력 edittext 엔터키 차단
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == event.KEYCODE_ENTER)
                    return true;
                return false;
            }
        });

        user_name.setOnKeyListener(new View.OnKeyListener() { // 이름 입력 edittext 엔터키 차단
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == event.KEYCODE_ENTER)
                    return true;
                return false;
            }
        });

        user_number.setOnKeyListener(new View.OnKeyListener() { // 학번 입력 edittext 엔터키 차단
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == event.KEYCODE_ENTER)
                    return true;
                return false;
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {

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
                    return ;
                }

                if (user_name.getText().toString().equals(""))
                    Toast.makeText(getActivity(), "이름을 입력해 주세요.", Toast.LENGTH_LONG).show();
                else if (user_number.getText().toString().equals(""))
                    Toast.makeText(getActivity(), "학번을 입력해 주세요.", Toast.LENGTH_LONG).show();
                else if (majorSpinner.getSelectedItemPosition() == 0)
                    Toast.makeText(getActivity(), "학과를 선택해 주세요.", Toast.LENGTH_LONG).show();
                else if (user_email.getText().toString().equals("") || !isEmailValid(user_email.getText().toString()))
                    Toast.makeText(getActivity(), "올바른 Email 형식을 입력해 주세요.", Toast.LENGTH_LONG).show();
                else if (user_password.getText().toString().equals(""))
                    Toast.makeText(getActivity(), "비밀번호를 입력해 주세요.", Toast.LENGTH_LONG).show();
                else if (re_password.getText().toString().equals(""))
                    Toast.makeText(getActivity(), "비밀번호 확인을 입력해 주세요.", Toast.LENGTH_LONG).show();
                else if (!user_password.getText().toString().equals(re_password.getText().toString()))
                    Toast.makeText(getActivity(), "비밀번호를 다시 입력해 주세요.", Toast.LENGTH_LONG).show();
                else if (user_nickname.getText().toString().equals(""))
                    Toast.makeText(getActivity(), "닉네임을 입력해 주세요.", Toast.LENGTH_LONG).show();
                else if (user_number.getText().toString().length() < 8)
                    Toast.makeText(getActivity(), "학번은 학교 사이트에 로그인하는 8자리입니다.", Toast.LENGTH_LONG).show();
                else {
                    temp_num = user_number.getText().toString();
                    temp_pw = user_password.getText().toString();

                    ConnectDB connect_DB = Request.getLoginRetrofit().create(ConnectDB.class);
                    Call<String> calling= connect_DB.loginCheck(temp_num, temp_pw);
                    calling.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            String result = response.body();
                            Log.d("", result);
                            if (result != null) {
                                if(result.contains("5회 오류시 비밀번호를 재설정후 사용이 가능합니다") || result.contains("아이디 또는 비밀번호를 잘못 입력하셨습니다.")) {
                                    Toast.makeText(getActivity(), "명지대 계정에 등록된 학번 혹은 비밀번호가 다릅니다.", Toast.LENGTH_LONG).show();
                                    return ;
                                }
                                else
                                    newUser();
                            }
                        }
                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                        }
                    });
                }
            }
        }); // 리스너 등록. 버튼 클릭 시 회원가입 진행

        return view;
    }

    public boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        }
        catch(NumberFormatException e) {
            return false;
        }
    }

    public boolean isEmailValid(String email) {

        String emailvalid = "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                +"((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                +"([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                +"([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";
        // emailvalid에 유효한 형식의 이메일이 가능한 경우의 수를 문자열로 저장한다.

        CharSequence inputStr = email;
        // 매개변수로 받은 email을 inputStr에 CharSequence 타입으로 저장한다.
        // CharSequence 타입 : char 타입형이 sequence(연결) 되어 있다.

        Pattern pattern = Pattern.compile(emailvalid, Pattern.CASE_INSENSITIVE);
        // emailvalid가 패턴의 형식에 맞는지 검사한다.
        Matcher matcher = pattern.matcher(inputStr);

        if(matcher.matches())
            return true;
        else
            return false;
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
            Call<String> call = connectDB.addUser(user_email.getText().toString().trim(), user_password.getText().toString(), user_nickname.getText().toString().trim(), majorSpinner.getSelectedItem().toString(), user_number.getText().toString().trim().substring(2, 4), user_name.getText().toString().trim(), format_date);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    String result = response.body().trim();
                    if (result.equals("success")) { // 회원 가입 정상 수행 시 서버에서 success 문자열 출력
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("메시지");
                        builder.setCancelable(false);
                        builder.setMessage("회원 가입에 성공하였습니다.");
                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((LoginActivity) getActivity()).removeAddUserFragment();
                            }
                        });
                        builder.show();
                    } else if (result.equals("email")) { // 서버에서 이메일 중복 검사에 걸림
                        Toast.makeText(getActivity(), "이미 등록된 이메일입니다.", Toast.LENGTH_SHORT).show();
                    } else if (result.equals("nickname")) { // 서버에서 닉네임 중복 검사에 걸림
                        Toast.makeText(getActivity(), "이미 등록된 닉네임입니다.", Toast.LENGTH_SHORT).show();
                    } else if (result.equals("number")) { // 서버에서 학번 중복 검사에 걸림
                        Toast.makeText(getActivity(), "이미 등록된 학번입니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

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
