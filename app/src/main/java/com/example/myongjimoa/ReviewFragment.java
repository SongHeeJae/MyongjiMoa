package com.example.myongjimoa;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ReviewFragment extends Fragment {

    Button hansik;
    Button chinese;
    Button alchol;
    Button cafe;
    Button boonsik;
    Button japanese;
    Button chicken;
    Button gogi;
    Button yangsik;
    Button find_restaurant;
    Button all;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup)inflater.inflate(R.layout.review, container, false);

        Button.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view ) {
                Intent it = new Intent(getActivity(), ReviewActivity.class);
                it.putExtra("user_id", ((MainActivity)getActivity()).getUserInfo().getId());
                it.putExtra("nickname", ((MainActivity)getActivity()).getUserInfo().getNickname());
                switch(view.getId()) {
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
                startActivity(it);
            }
        };

        hansik = (Button)view.findViewById(R.id.hansik);
        hansik.setOnClickListener(onClickListener);
        chinese = (Button) view.findViewById(R.id.chinese);
        chinese.setOnClickListener(onClickListener);
        alchol = (Button) view.findViewById(R.id.alchol);
        alchol.setOnClickListener(onClickListener);
        cafe = (Button) view.findViewById(R.id.cafe);
        cafe.setOnClickListener(onClickListener);
        boonsik = (Button) view.findViewById(R.id.boonsik);
        boonsik.setOnClickListener(onClickListener);
        japanese = (Button) view.findViewById(R.id.japanese);
        japanese.setOnClickListener(onClickListener);
        chicken = (Button) view.findViewById(R.id.chicken);
        chicken.setOnClickListener(onClickListener);
        gogi = (Button) view.findViewById(R.id.gogi);
        gogi.setOnClickListener(onClickListener);
        yangsik = (Button) view.findViewById(R.id.yangsik);
        yangsik.setOnClickListener(onClickListener);
        all = (Button) view.findViewById(R.id.all);
        all.setOnClickListener(onClickListener);
        find_restaurant = (Button) view.findViewById(R.id.find_restaurant);
        find_restaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });




        return view;
    }


}
