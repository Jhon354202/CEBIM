package com.chenxi.cebim.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chenxi.cebim.R;
import com.chenxi.cebim.adapter.TaskListTabAdapter;

@SuppressLint("ValidFragment")
public class TabFragment extends Fragment {
    private  int pos;
    public TabFragment(int pos){
        this.pos=pos;
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_tab,container,false);
        TextView tv=view.findViewById(R.id.tv_tab_fragment);
        tv.setText(TaskListTabAdapter.TITLES[pos]);
        return view;
    }
}
