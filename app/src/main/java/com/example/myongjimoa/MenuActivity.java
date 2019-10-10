package com.example.myongjimoa;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MenuActivity extends AppCompatActivity {

    TextView text;
    String data;
    String url1 = "https://www.mju.ac.kr/mbs/mjukr/jsp/restaurant/restaurant.jsp?configIdx=36548&id=mjukr_051002020000";
    String url2 = "https://www.mju.ac.kr/mbs/mjukr/jsp/restaurant/restaurant.jsp?configIdx=36337&id=mjukr_051002050000";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        text = (TextView) findViewById(R.id.menu_text);
        new JsoupAsyncTask().execute(url1);
        new JsoupAsyncTask().execute(url2);
    }


    private class JsoupAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                List<String> day = new ArrayList<>();
                day.add("Monday_Data");
                day.add("Tuesday_Data");
                day.add("Wedensday_Data");
                day.add("Thursday_Data");
                day.add("Friday_Data");
                Document doc = Jsoup.connect(params[0]).get();
                Elements e1 = doc.select("table[class=sub]");
                int i = 0;
                for(Element e : e1) {
                    Elements e2 = e.select("div[name=" + day.get(i) + "]");
                    for(Element ee : e2) {
                        data += ee.text();
                    }
                    i++;
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            text.setText(data);
        }
    }
}
