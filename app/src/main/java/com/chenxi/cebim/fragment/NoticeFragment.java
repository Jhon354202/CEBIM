package com.chenxi.cebim.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chenxi.cebim.R;
import com.chenxi.cebim.adapter.NoticeAdapter;
import com.chenxi.cebim.entity.Notice;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class NoticeFragment extends BaseFragment {

    View view;
    private List<Notice> noticeList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefresh;
    private NoticeAdapter adapter;
    private RecyclerView recyclerView;
    private int[] picId={R.drawable.question,R.drawable.form,R.drawable.task,R.drawable.trends,R.drawable.notice,R.drawable.announce} ;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_notice, container,false);


        //RecyclerView逻辑
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_notice);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        //SwipeRefreshLayout下拉刷新的逻辑
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.notice_swip_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshNotice();
            }
        });

        initDatas(0);
        return view;
    }

    private void initDatas(int isrefresh) {

        noticeList.clear();
        InputStream is = null;
        String result = null;
        try {
            is = getActivity().getAssets().open("noticeItem.txt");
            int lenght = is.available();
            byte[] buffer = new byte[lenght];
            is.read(buffer);
            result = new String(buffer, "utf8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //读取assets中的文件信息
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(result);
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String itemName = jsonObject.getString("itemName");
                Notice notice = new Notice(picId[i],itemName);

                noticeList.add(notice);
            }

            if(isrefresh==0){
                adapter = new NoticeAdapter(getActivity(),noticeList);
                recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
                recyclerView.setAdapter(adapter);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void refreshNotice() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initDatas(1);
                        adapter.notifyDataSetChanged();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

}
