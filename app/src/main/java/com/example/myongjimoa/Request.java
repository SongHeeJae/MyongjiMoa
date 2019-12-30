package com.example.myongjimoa;


import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.esafirm.imagepicker.features.ImagePicker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class Request {

    private static HashMap<Character, HashMap<String, String>> myMap;

    static void initFilterMap() { // 욕설 데이터 저장해줌. 이 메소드는 MainActivity에 진입하였을 때 수행하여 myMap 초기화
        myMap = new HashMap<>();

        HashMap<String, String> 개 = new HashMap<>();

        HashMap<String, String> 니 = new HashMap<>();
        HashMap<String, String> 느 = new HashMap<>();

        HashMap<String, String> 닥 = new HashMap<>();

        HashMap<String, String> 병 = new HashMap<>();

        HashMap<String, String> 씨 = new HashMap<>();
        HashMap<String, String> 씹 = new HashMap<>();
        HashMap<String, String> 새 = new HashMap<>();
        HashMap<String, String> 시 = new HashMap<>();

        HashMap<String, String> 애 = new HashMap<>();
        HashMap<String, String> 엠 = new HashMap<>();

        HashMap<String, String> 좆 = new HashMap<>();
        HashMap<String, String> 존 = new HashMap<>();
        HashMap<String, String> 지 = new HashMap<>();

        HashMap<String, String> 창 = new HashMap<>();

        HashMap<String, String> 한 = new HashMap<>();

        개.put("개년", "나쁜여자");
        개.put("개놈", "나쁜남자");
        니.put("니미", "네 어머니");
        느.put("느금마", "네 어머니");
        느.put("느개비", "네 아버지");
        닥.put("닥쳐", "입다물어");
        병.put("병신", "아픈");
        병.put("병싄", "아픈");
        시.put("시발", "짜증나");
        시.put("시벌", "짜증나");
        시.put("시바", "짜증나");
        씨.put("씨발", "나쁜");
        씨.put("씨바", "못된");
        씹.put("씹", "나쁜");
        새.put("새끼", "아가");
        새.put("새기", "아가");
        새.put("새꺄", "아가");
        애.put("애미", "어머니");
        엠.put("엠창", "어머니");
        좆.put("좆", "엄청");
        존.put("존나", "엄청");
        지.put("지랄", "말도 안되는 소리");
        창.put("창녀", "나쁜여자");
        창.put("창남", "나쁜남자");
        한.put("한남", "한국남자");

        myMap.put('씹', 씹);
        myMap.put('창', 창);
        myMap.put('시', 시);
        myMap.put('니', 니);
        myMap.put('애', 애);
        myMap.put('엠', 엠);
        myMap.put('존', 존);
        myMap.put('지', 지);
        myMap.put('병', 병);
        myMap.put('씨', 씨);
        myMap.put('새', 새);
        myMap.put('좆', 좆);
        myMap.put('닥', 닥);
        myMap.put('느', 느);
        myMap.put('한', 한);
        myMap.put('개', 개);
    }

    static String filter(String text) { // 비속어 필터와 순화어로 변경하는 과정 수행
        HashMap<String, String> temp = null;
        StringBuilder str = new StringBuilder();
        int count = 0;
        for(int i=0; i<text.length(); i++) {
            if((44032 <= (int)text.charAt(i) && (int)text.charAt(i) <= 55203)) { // 한글 유니코드 범위
                if(str.length() == 0) {
                    count = 0;
                    temp = myMap.get(text.charAt(i)); // 첫글자가 욕설 중 하나에 해당할때만 수행
                }
                if(temp != null) {
                    str.append(text.charAt(i));
                    if(temp.get(str.toString()) != null) { // 욕설이면 욕설 처리. 순화어 길이차이만큼 계산, 필요없는 인덱스 추가
                        text = text.substring(0, i - count - str.length() + 1) + temp.get(str.toString()) + text.substring(i + 1);
                        //Log.d("텍스트값 알아보기2222", "text : " + text);
                        i = i - count + temp.get(str.toString()).length() - str.length(); // key와 value의 길이차이 넘어감
                        str.setLength(0);
                    } else if (str.length() > 1 && myMap.get(text.charAt(i)) != null) { // 단어가 새로운 욕의 시작이면 temp값 수정, str 초기화 - 첫번째 if문에서 욕설처리가되었다면 그냥 넘어감.
                        count = 0;
                        temp = myMap.get(text.charAt(i));
                        str.setLength(0);
                        str.append(text.charAt(i));
                    }
                }
            }  else count++;
        }
        return text;
    }

    static Retrofit getLoginRetrofit() {
        return new Retrofit.Builder()
                .baseUrl("https://sso.mju.ac.kr/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
    }

    static AmazonS3 getAmazonS3(Context context) {
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                "ap-northeast-2:9c5bb2b0-44a8-4a1c-944a-98d817d44e82", // 자격 증명 풀 ID
                Regions.AP_NORTHEAST_2 // 리전
        );

        return new AmazonS3Client(credentialsProvider, Region.getRegion(Regions.AP_NORTHEAST_2));
        //s3.setEndpoint("s3.ap-northeast-2.amazonaws.com");
    }



    static Retrofit getRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

  /*  static Retrofit getRetrofitGson() {
        return new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }*/

    static String getTime(String pattern) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        return sdf.format(date);
    }

    // 다이얼로그 만드는 메소드


    // 갤러리 여는 메소드


    // aws 빌드 메소드
}
