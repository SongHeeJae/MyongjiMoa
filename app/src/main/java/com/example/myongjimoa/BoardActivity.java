package com.example.myongjimoa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class BoardActivity extends AppCompatActivity {

    private final int GET_GALLERY_IMAGE = 0;

    private FragmentManager fragment_manager;
    private BoardPostFragment board_post_fragment;
    private BoardMainFragment board_main_fragment;
    private BoardWriteFragment board_write_fragment;

    String board_title;
    String user_id;
    String nickname;

    Post current_post;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board);

        fragment_manager = getSupportFragmentManager();
        board_main_fragment = new BoardMainFragment();
        board_post_fragment = new BoardPostFragment();
        board_write_fragment = new BoardWriteFragment();

        Intent it = getIntent();
        board_title = it.getStringExtra("title");
        user_id = it.getStringExtra("user_id");
        nickname = it.getStringExtra("nickname");

        setTitle(board_title);
        if(board_title.equals("공지사항")) {
            board_title="gongji_board";
        } else if(board_title.equals("자유게시판")) {
            board_title="free_board";
        } else if(board_title.equals("명지장터")){
            board_title="market_board";
        } else if(board_title.equals("동아리게시판")){
            board_title="dongari_board";
        } else if(board_title.equals("자취생게시판")){
            board_title="myhome_board";
        } else if(board_title.equals("기숙사생게시판")){
            board_title="dormitory_board";
        } else if(board_title.equals("컴퓨터공학과")){
            board_title="computer_board";
        } else if(board_title.equals("정보통신공학과")){
            board_title="jungtong_board";
        } else if(board_title.equals("수학과")){
            board_title="math_board";
        } else if(board_title.equals("물리학과")){
            board_title="pyshic_board";
        } else if(board_title.equals("화학과")){
            board_title="chemical_board";
        } else if(board_title.equals("식품영양학과")){
            board_title="food_board";
        } else if(board_title.equals("생명과학정보학과")){
            board_title="life_board";
        } else if(board_title.equals("전기공학과")){
            board_title="electric_board";
        } else if(board_title.equals("전자공학과")){
            board_title="electronic_board";
        } else if(board_title.equals("화학공학과")){
            board_title="chemicalgong_board";
        } else if(board_title.equals("신소재공학과")){
            board_title="sinsojae_board";
        } else if(board_title.equals("환경에너지공학과")){
            board_title="energy_board";
        } else if(board_title.equals("토목환경공학과")){
            board_title="tomok_board";
        } else if(board_title.equals("교통공학과")){
            board_title="gyotong_board";
        } else if(board_title.equals("기계공학과")){
            board_title="machine_board";
        } else if(board_title.equals("산업경영공학과")){
            board_title="sangyeong_board";
        } else if(board_title.equals("디자인학부")){
            board_title="design_board";
        } else if(board_title.equals("체육학부")){
            board_title="cheyuk_board";
        } else if(board_title.equals("음악학부")){
            board_title="music_board";
        } else if(board_title.equals("바둑학과")){
            board_title="baduk_board";
        } else if(board_title.equals("영화뮤지컬학부")){
            board_title="musical_board";
        } else if(board_title.equals("건축학부")){
            board_title="build_board";
        } else if(board_title.equals("공간디자인전공")){
            board_title="gonggan_board";
        } else if(board_title.equals("전공자유학부")){
            board_title="majorfree_board";
        } else {
            Log.d("유효하지않은 게시판", "ㅇㅇㅇ");
            finish();
        }

        fragment_manager.beginTransaction().add(R.id.board, board_main_fragment).commit();
    }

    public Post getPost() {
        return current_post;
    }

   public void replaceBoardWriteFragment() {
       FragmentTransaction fragment_transaction = fragment_manager.beginTransaction().replace(R.id.board, board_write_fragment);
       fragment_transaction.addToBackStack(null);
       fragment_transaction.commitAllowingStateLoss();
   }

   public void setCurrentPost(Post p) {
        current_post = p;
   }

    public void replaceBoardPostFragment(Post p) {
        setCurrentPost(p);
        FragmentTransaction fragment_transaction = fragment_manager.beginTransaction().replace(R.id.board, board_post_fragment);
        fragment_transaction.addToBackStack(null); // 뒤로가기 했을때 액티비티 종료 안되기 위함.
        fragment_transaction.commitAllowingStateLoss();
    }

    public void removeWriteFragment() {
        board_main_fragment.clearAdapter();
        fragment_manager.beginTransaction().remove(board_write_fragment).commit();
        fragment_manager.popBackStack();
    }

    public String getBoard_title() {
       return board_title;
    }

    public void getGallery() {
        Intent it = new Intent();
        it.setAction(Intent.ACTION_GET_CONTENT);
        it.setType("image/*");
        it.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(it, "Get Image"), GET_GALLERY_IMAGE);
        //최근사진 읽어오면 절대경로 오류남
       // startActivityForResult(it, GET_GALLERY_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            board_write_fragment.setWriteImage(data.getClipData());
        }
    }

    public String getUser_id() {
        return user_id;
    }
}
