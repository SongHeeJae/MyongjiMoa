package com.example.myongjimoa;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CommunityFragment extends Fragment {

    RecyclerView recycler_view;
    BoardTitleAdapter m_adapter;

    String[] title_list;

    public GestureDetector gesture_detector;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.community, container, false);
        recycler_view = (RecyclerView) view.findViewById(R.id.title_recycler_view);


        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        recycler_view.setLayoutManager(layoutManager);
        m_adapter = new BoardTitleAdapter();
        recycler_view.setAdapter(m_adapter);

        title_list = getResources().getStringArray(R.array.title_list);

        for (int i=0; i<title_list.length ; i++) {
            m_adapter.add(title_list[i]);
        } // 데이터 add


        gesture_detector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
        recycler_view.addOnItemTouchListener(new RecyclerView.OnItemTouchListener()
         {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View childView = rv.findChildViewUnder(e.getX(), e.getY());

                if(childView != null && gesture_detector.onTouchEvent((e))) {
                    int currentPos = rv.getChildAdapterPosition(childView);
                  //  Toast.makeText(getActivity(), title_list[currentPos], Toast.LENGTH_SHORT).show();
                    Intent it = new Intent(getActivity(), BoardActivity.class);
                    it.putExtra("title", title_list[currentPos]);
                    it.putExtra("user_id", ((MainActivity)getActivity()).getUserInfo().getId());
                    it.putExtra("nickname", ((MainActivity)getActivity()).getUserInfo().getNickname());
                    startActivity(it);
                    // title[currentPos]에 담긴 데이터값으로 이동
                    //Toast.makeText(MainActivity.this, kWord[currentPos], Toast.LENGTH_SHORT).show();
                    return true;
                }

                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        return view;
    }
}
