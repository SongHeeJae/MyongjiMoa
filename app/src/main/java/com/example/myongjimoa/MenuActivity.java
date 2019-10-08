package com.example.myongjimoa;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
    //    downloadMenuList();
    }

 /*   public void downloadMenuList() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://www.naver.com/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
        Call<String> call = connectDB.downloadMenu();
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                String result = response.body();
                if (result != null) {
                    Log.d("결과", result);
                } else {
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("실패", t.getMessage());
            }

        });
    }*/
}
