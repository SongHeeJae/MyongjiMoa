package com.example.myongjimoa;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BoardPostTitleAdapter extends RecyclerView.Adapter<BoardPostTitleAdapter.ViewHolder>{
    List<String> items = new ArrayList<String>();

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text;

        public ViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.post_title);

            // 제목 글자수가 1줄 초과하면 ...으로 처리.
            text.setMaxLines(1);
            text.setEllipsize(TextUtils.TruncateAt.END);

        }

        public void setData(String data) {
            //값 읽어오기
            text.setText(data);
        }
    }

    public void add(String item) {
        items.add(item);
        notifyDataSetChanged(); // 뭔지 검색
    }

    @Override
    public BoardPostTitleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_title_item, parent, false);
        return new BoardPostTitleAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BoardPostTitleAdapter.ViewHolder holder, int position) {
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

   /* public Post getItem(int i) {
        return items.get(i);
    }*/

    /*public void itemsClear() {
        items.clear();
    }*/

}
