package com.example.myongjimoa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class MenuFragment extends Fragment {

    private String menu1;
    private String menu2;

    public static MenuFragment newInstance(String m1, String m2) {
        MenuFragment fragment = new MenuFragment();
        Bundle args = new Bundle();
        args.putString("menu1", m1);
        args.putString("menu2", m2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        menu1 = getArguments().getString("menu1");
        menu2 = getArguments().getString("menu2");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.menu_item, container, false);
        TextView text1 = (TextView) view.findViewById(R.id.menu1);
        TextView text2 = (TextView) view.findViewById(R.id.menu2);
        TextView text3 = (TextView) view.findViewById(R.id.menu3);
        TextView text4 = (TextView) view.findViewById(R.id.menu4);
        TextView text5 = (TextView) view.findViewById(R.id.menu5);
        TextView text6 = (TextView) view.findViewById(R.id.menu6);

        String[] m1 = menu1.trim().split("\n");
        String[] m2 = menu2.trim().split("\n");
        text1.setText(m1[0].trim().replaceAll(" ", "\n"));
        text2.setText(m1[1].trim().replaceAll(" ", "\n"));
        text3.setText(m1[2].trim().replaceAll(" ", "\n"));
        text4.setText(m2[0].trim().replaceAll(" ", "\n"));
        text5.setText(m2[1].trim().replaceAll(" ", "\n"));
        text6.setText(m2[2].trim().replaceAll(" ", "\n"));
        return view;
    }
}
