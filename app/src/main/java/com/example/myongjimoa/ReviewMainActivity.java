package com.example.myongjimoa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ReviewMainActivity extends AppCompatActivity {

    Button hansik;
    Button chinese;
    Button alchol;
    Button cafe;
    Button boonsik;
    Button japanese;
    Button chicken;
    Button gogi;
    Button yangsik;
    Button all;

    String user_id;
    String user_nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review);



        Button.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view ) {
                Intent it = new Intent(ReviewMainActivity.this, ReviewActivity.class);
                it.putExtra("user_id", user_id);
                switch(view.getId()) { // 각각의 카테고리 눌렀을때의 처리. 뒤에 value로 주는것은 서버 카테고리 테이블 명
                    case R.id.hansik :
                        it.putExtra("category", "hansik_food");
                        break;
                    case R.id.chinese :
                        it.putExtra("category", "chinese_food");
                        break;
                    case R.id.alchol :
                        it.putExtra("category", "alchol_food");
                        break;
                    case R.id.cafe :
                        it.putExtra("category", "cafe_food");
                        break;
                    case R.id.boonsik :
                        it.putExtra("category", "boonsik_food");
                        break;
                    case R.id.japanese :
                        it.putExtra("category", "japanese_food");
                        break;
                    case R.id.chicken :
                        it.putExtra("category", "chicken_food");
                        break;
                    case R.id.gogi :
                        it.putExtra("category", "gogi_food");
                        break;
                    case R.id.yangsik :
                        it.putExtra("category", "yangsik_food");
                        break;
                    case R.id.all :
                        it.putExtra("category", "restaurant");
                        break;
                }
                it.putExtra("user_nickname", user_nickname);
                startActivity(it);
            }
        };

        hansik = (Button)findViewById(R.id.hansik);
        hansik.setOnClickListener(onClickListener);
        chinese = (Button) findViewById(R.id.chinese);
        chinese.setOnClickListener(onClickListener);
        alchol = (Button) findViewById(R.id.alchol);
        alchol.setOnClickListener(onClickListener);
        cafe = (Button) findViewById(R.id.cafe);
        cafe.setOnClickListener(onClickListener);
        boonsik = (Button) findViewById(R.id.boonsik);
        boonsik.setOnClickListener(onClickListener);
        japanese = (Button) findViewById(R.id.japanese);
        japanese.setOnClickListener(onClickListener);
        chicken = (Button) findViewById(R.id.chicken);
        chicken.setOnClickListener(onClickListener);
        gogi = (Button) findViewById(R.id.gogi);
        gogi.setOnClickListener(onClickListener);
        yangsik = (Button) findViewById(R.id.yangsik);
        yangsik.setOnClickListener(onClickListener);
        all = (Button) findViewById(R.id.all);
        all.setOnClickListener(onClickListener);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish(); // 뒤로가기 버튼 종료 처리
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}