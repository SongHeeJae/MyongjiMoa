package com.example.myongjimoa;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.*;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;


public class BoardWriteFragment extends Fragment {

    EditText write_title;
    EditText write_description;
    Button write_submit;
    Button picture;
    List<Bitmap> images;
    RecyclerView recycler_view;
    BoardWriteImageAdapter board_write_image_adapter;
    public GestureDetector gesture_detector;
    ArrayList<String> path;


    int upload_count;

    @Override
    public void onStart() {
        super.onStart();
        write_title.setText("");
        write_description.setText("");
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.board_write, container, false);

        write_title = (EditText) view.findViewById(R.id.write_title);
        write_description = (EditText) view.findViewById(R.id.write_description);
        write_submit = (Button) view.findViewById(R.id.write_submit);
        picture = (Button) view.findViewById(R.id.picture);
        recycler_view = (RecyclerView) view.findViewById(R.id.board_write_image);
        Log.d("또실행됨", "ㅇㅇ");

        path =  new ArrayList<String>();
        images = new ArrayList<Bitmap>();

        board_write_image_adapter = new BoardWriteImageAdapter();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false);
        recycler_view.setLayoutManager(layoutManager);
        recycler_view.setAdapter(board_write_image_adapter);

        write_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageUpload();
            }
        });
        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BoardActivity)getActivity()).getGallery();
            }
        });


        gesture_detector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

        return view;
    }

    public static String saveBitmapToJpeg(Context context, Bitmap bitmap, String name){

        File storage = context.getCacheDir(); // 이 부분이 임시파일 저장 경로
        String fileName = name + ".jpg";  // 파일이름은 마음대로!

        File tempFile = new File(storage,fileName);

        try{
            tempFile.createNewFile();  // 파일을 생성해주고

            FileOutputStream out = new FileOutputStream(tempFile);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 90 , out);  // 넘거 받은 bitmap을 jpeg(손실압축)으로 저장해줌

            out.close(); // 마무리로 닫아줍니다.

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tempFile.getAbsolutePath();   // 임시파일 저장경로를 리턴해주면 끝!
    }

    public void imageUpload() {


        if (board_write_image_adapter.getItemCount() > 0) {

            // 이미지 업로드 부분에서 이미지 업로드가 아직 실행중인데 다른쪽에서 게시글 정보가 업데이트된다면? 파일업로드처리가 끝난후, 게시글 업로드하도록 수정할필요있음
            // 옵저버패턴 https://flowarc.tistory.com/entry/%EB%94%94%EC%9E%90%EC%9D%B8-%ED%8C%A8%ED%84%B4-%EC%98%B5%EC%A0%80%EB%B2%84-%ED%8C%A8%ED%84%B4Observer-Pattern
            // 콜백으로 구현, 브로드캐스팅으로 구현

            Date date = new Date(); // 시스템 시간으로 구함 동기화되는지 확인 필요
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            String format_date = sdf.format(date);

            ArrayList<File> files = new ArrayList<File>();
            for (int i = 0; i < board_write_image_adapter.getItemCount(); i++) {
                files.add(new File(saveBitmapToJpeg(getActivity(), board_write_image_adapter.getItem(i), "temp_images" + i)));
            }

            // Amazon Cognito 인증 공급자 초기화
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getActivity(),
                    "ap-northeast-2:9c5bb2b0-44a8-4a1c-944a-98d817d44e82", // 자격 증명 풀 ID
                    Regions.AP_NORTHEAST_2 // 리전
            );

            TransferObserver observer;
            AmazonS3 s3 = new AmazonS3Client(credentialsProvider, Region.getRegion(Regions.AP_NORTHEAST_2));
            //TransferUtility transfer_utility = new TransferUtility(s3, getActivity());
            TransferUtility transfer_utility = TransferUtility.builder().s3Client(s3).context(getActivity()).build();
            s3.setEndpoint("s3.ap-northeast-2.amazonaws.com");

            for (int i = 0; i < board_write_image_adapter.getItemCount(); i++) {
                File file = new File(saveBitmapToJpeg(getActivity(), board_write_image_adapter.getItem(i), "temp_images" + i));
                String name = ((BoardActivity)getActivity()).board_title + "_"  + ((BoardActivity)getActivity()).user_id + "_" + format_date + "_" + i;
                observer = transfer_utility.upload(
                        "myongjimoa/board_images",
                        name, // 이미지 파일이름설정 개별적으로 중복안되게구성해야함. user_id + 현재시간?
                        file
                ); // 파일 여러개 동시 업로드하는거 찾아야됨
                Log.d("파일업로드횟수", i + "");
                path.add(name);
                observer.setTransferListener(new TransferListener() {
                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        Log.d("s3", "onStateChanged ㅇㅇㅇㅇㅇㅇㅇㅇ");
                        if (TransferState.COMPLETED == state) {
                            Log.d("전송완료", "ㅇㅇ"); // 여기다 콜백으로 구현
                            upload_count++;
                            if (upload_count == board_write_image_adapter.getItemCount()) {
                                Log.d("이미지업로드끝", "ㅇㅇ");
                                posting(path); // 콜백으로 구현 근데 속도느림 개선필요 동시파일업로드하도록
                                upload_count = 0;
                            }
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                        Log.d("s3", "onProgressChanged ㅇㅇㅇㅇㅇㅇㅇㅇ");
                    }

                    @Override
                    public void onError(int id, Exception ex) {
                        Log.d("s3에러", ex.getMessage());
                    }
                });
            }
        } else {
            posting(path);
        }
    }

    public void posting(ArrayList<String> path) {

        Log.d("dd", "다음줄 실행ㅇㅇㅇㅇㅇ");

        Date date = new Date(); // 시스템 시간으로 구함 동기화되는지 확인 필요
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String format_date = sdf.format(date);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnectDB.Base_URL)
                .addConverterFactory(ScalarsConverterFactory.create()) // 문자열로 받기 위함.
                .build();

        ConnectDB connectDB = retrofit.create(ConnectDB.class);
            Call<String> call = connectDB.writePost(((BoardActivity) getActivity()).board_title, ((BoardActivity) getActivity()).user_id, write_title.getText().toString(), write_description.getText().toString(), path, format_date);

            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    String result = response.body().trim();
                    Log.d("result는?", result);
                    if(result.equals("success")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("메시지");
                        builder.setMessage("글쓰기에 성공하였습니다.");
                        builder.setCancelable(false);
                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 키보드 내리는 코드
                                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(write_title.getWindowToken(), 0);
                                imm.hideSoftInputFromWindow(write_description.getWindowToken(), 0);
                                ((BoardActivity)getActivity()).removeWriteFragment();
                            }
                        });
                        builder.show();
                    } else {
                        Log.d("글쓰기실패", "반환값없음");
                    }
                }
                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.d("글쓰기 연결 실패", t.getMessage());
                }
            });
    }

    public void setWriteImage(ClipData clip_data) {
        if (clip_data != null) {
            for (int i = 0; i < clip_data.getItemCount(); i++) {
                try {
                    board_write_image_adapter.add(MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), clip_data.getItemAt(i).getUri()));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Log.d("파일없음", e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("입출력불가능", e.getMessage());
                }
            }
        }
    }

    public class BoardWriteImageAdapter extends RecyclerView.Adapter<BoardWriteImageAdapter.ViewHolder> {
        List<Bitmap> items = new ArrayList<Bitmap>();

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView img;
            TextView test;

            public ViewHolder(View itemView) {
                super(itemView);
                img = (ImageView) itemView.findViewById(R.id.image_view);
                test = (TextView) itemView.findViewById(R.id.test_text);
            }

            public void setData(Bitmap data) {
                //값 읽어오기
                img.setImageBitmap(data);
                // download 경로 못읽는거 버그 오류
                test.setText("zz");
            }
        }

        public Bitmap getItem(int i) {
            return items.get(i);
        }

        public void add(Bitmap item) {
            items.add(item);
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.setData(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        RecyclerView.OnItemTouchListener onItemTouchListener = new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        };

    }


    public String getPath(Uri uri) {

        boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (isKitKat && DocumentsContract.isDocumentUri(getActivity(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(contentUri, selection, selectionArgs);
            }
        }
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(uri, null, null);
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public String getDataColumn(Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = "_data";
        String[] projection = {column};
        try {
            cursor = getActivity().getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                Log.d("ㅇㅇ출력", cursor.getString(column_index));
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}