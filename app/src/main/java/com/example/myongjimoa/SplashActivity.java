package com.example.myongjimoa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent it = new Intent(this, LoginActivity.class);
        startActivity(it);
        finish();
    }
}
