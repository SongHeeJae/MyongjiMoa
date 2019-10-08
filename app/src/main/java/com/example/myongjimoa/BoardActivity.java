package com.example.myongjimoa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import androidx.appcompat.app.ActionBar;
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
    String board_title_id;

    Post current_post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board);
        fragment_manager = getSupportFragmentManager();
        board_main_fragment = new BoardMainFragment();

        Intent it = getIntent();
        board_title_id = it.getStringExtra("board_title_id");
        board_title = it.getStringExtra("board_title");
        user_id = it.getStringExtra("user_id");
        nickname = it.getStringExtra("nickname");

        setTitle(board_title);

        fragment_manager.beginTransaction().add(R.id.board, board_main_fragment).commit();
    }

    public Post getPost() {
        return current_post;
    }

   public void addBoardWriteFragment() {
       board_write_fragment = new BoardWriteFragment();
       FragmentTransaction fragment_transaction = fragment_manager.beginTransaction().add(R.id.board, board_write_fragment);
       fragment_transaction.addToBackStack(null);
       fragment_transaction.commitAllowingStateLoss();
   }

   public void setCurrentPost(Post p) {
        current_post = p;
   }

    public void addBoardPostFragment(Post p) {
        setCurrentPost(p);
        board_post_fragment = new BoardPostFragment();
        FragmentTransaction fragment_transaction = fragment_manager.beginTransaction().add(R.id.board, board_post_fragment);
        fragment_transaction.addToBackStack(null); // 뒤로가기 했을때 액티비티 종료 안되기 위함.
        fragment_transaction.commitAllowingStateLoss();
    }

    public void removeWriteFragment() {
        board_main_fragment.reloadPost();
        fragment_manager.beginTransaction().remove(board_write_fragment).commit();
        fragment_manager.popBackStack();
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
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("메뉴실행", "ㅇㅇ");
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.search_refresh, menu);
        search = menu.findItem(R.id.menu_search);
        my_menu = menu;
        if(board_post_fragment != null) {
            menu.findItem(R.id.menu_menus).setVisible(true);
            menu.findItem(R.id.menu_search).setVisible(false);
            menu.findItem(R.id.menu_refresh).setVisible(true);
        } else if(board_write_fragment != null) {
            menu.findItem(R.id.menu_menus).setVisible(false);
            menu.findItem(R.id.menu_search).setVisible(false);
            menu.findItem(R.id.menu_refresh).setVisible(false);
        } else {
            menu.findItem(R.id.menu_menus).setVisible(false);
            menu.findItem(R.id.menu_search).setVisible(true);
            menu.findItem(R.id.menu_refresh).setVisible(true);
        }

        search.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                board_main_fragment.setAdapter(true);
                my_menu.findItem(R.id.menu_refresh).setVisible(false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                board_main_fragment.setAdapter(false);
                my_menu.findItem(R.id.menu_refresh).setVisible(true);
                return true;
            }
        });

        SearchView sv=(SearchView)search.getActionView();

        sv.setSubmitButtonEnabled(true);

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String search_query) {
                board_main_fragment.setSearchAdapter(search_query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
        return true;
    }
*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                return true;
            case R.id.menu_refresh:
                if(board_post_fragment != null) {

                } else {

                }
                return true;
            case R.id.menu_menus:
                // 게시글창에서 메뉴눌렀을때 처리
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(board_post_fragment != null) board_post_fragment = null;
        else if (board_main_fragment != null) board_main_fragment = null;
    }


}
