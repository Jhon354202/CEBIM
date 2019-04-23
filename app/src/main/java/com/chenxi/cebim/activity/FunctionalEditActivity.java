package com.chenxi.cebim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.adapter.FunctionAllAdapter;
import com.chenxi.cebim.adapter.FunctionSelectedAdapter;
import com.chenxi.cebim.entity.ProjectInfoItem;
import com.chenxi.cebim.view.MyGridView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 功能编辑界面
 */
public class FunctionalEditActivity extends BaseActivity implements AdapterView.OnItemClickListener,
        FunctionSelectedAdapter.SelectedCallback, FunctionAllAdapter.AllCallback {

    private List<ProjectInfoItem> mDatas;//从ProjectActivity获取的原数据
    private List<ProjectInfoItem> mTemData;//中转数据
    private List<ProjectInfoItem> mShowData;//显示在列表中的数据源

    private RelativeLayout back, sure;
    private ScrollView scrollView;
    //    private DragGridView mDragGridView;
    private LinearLayout llFunctionalEdit;
    FunctionSelectedAdapter functionSelectedAdapter;
    FunctionAllAdapter functionAllAdapter;
    MyGridView mGridView;
    MyGridView mDragGridView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_functional_edit);

        initData();
        initView();
    }

    /**
     * 数据初始化
     */
    private void initData() {
        mDatas = new ArrayList<ProjectInfoItem>();
        mTemData = new ArrayList<ProjectInfoItem>();
        mShowData = new ArrayList<ProjectInfoItem>();

        //获取ProjectActivity界面传过来的数据
        mDatas = (List<ProjectInfoItem>) getIntent().getSerializableExtra("checkedList");

        for (int i = 0; i < mDatas.size(); i++) {
            if (mDatas.get(i).getSelected()) {
                mTemData.add(mDatas.get(i));
            }
        }
        mShowData.addAll(mTemData);

    }

    /**
     * 界面初始化
     */
    private void initView() {
        back = (RelativeLayout) findViewById(R.id.rl_edit_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        sure = (RelativeLayout) findViewById(R.id.rl_edit_sure);
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //返回数据给ProjectActivity
                Intent intent = new Intent();
                intent.putExtra("function_selected", (Serializable) mDatas);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        scrollView = (ScrollView) findViewById(R.id.sv_functional_edit);
        llFunctionalEdit = (LinearLayout) findViewById(R.id.ll_functional_edit);

        //全部GridView
        mGridView = (MyGridView) findViewById(R.id.grid_all);
        functionAllAdapter = new FunctionAllAdapter(this, mDatas, this);
        mGridView.setAdapter(functionAllAdapter);

        //已选GridView
        mDragGridView = (MyGridView) findViewById(R.id.dragGridView);
        functionSelectedAdapter = new FunctionSelectedAdapter(this, mShowData, this);
        mDragGridView.setAdapter(functionSelectedAdapter);
    }

    /**
     * 已选列表和全部列表中Item的点击事件
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    /**
     * 全部列表中的Item的删除点击事件
     * @param v
     */
    @Override
    public void allClick(View v) {

        if (!mDatas.get((Integer) v.getTag()).getSelected()) {
            ToastUtils.showShort("点击了全部" + mDatas.get((Integer) v.getTag()).getItemName() + "的删除按钮");
            mDatas.get((Integer) v.getTag()).setSelected(true);

            mTemData.clear();
            mShowData.clear();
            for (int i = 0; i < mDatas.size(); i++) {
                if (mDatas.get(i).getSelected()) {
                    mTemData.add(mDatas.get(i));
                }
            }
            mShowData.addAll(mTemData);

            functionAllAdapter.notifyDataSetChanged();
            functionSelectedAdapter.notifyDataSetChanged();
        }

    }

    /**
     * 响应已选GrideView的del按钮点击事件
     * @param v
     */
    @Override
    public void functionSelectedClick(View v) {
        ToastUtils.showShort("点击了已选" + mShowData.get((Integer) v.getTag()).getItemName() + "的删除按钮");

        for (int i = 0; i < mDatas.size(); i++) {
            if (mShowData.get((Integer) v.getTag()).getItemName().equals(mDatas.get(i).getItemName())) {
                mDatas.get(i).setSelected(false);
            }
        }

        mTemData.clear();
        mShowData.clear();
        for (int i = 0; i < mDatas.size(); i++) {
            if (mDatas.get(i).getSelected()) {
                mTemData.add(mDatas.get(i));
            }
        }
        mShowData.addAll(mTemData);

        functionSelectedAdapter.notifyDataSetChanged();
        functionAllAdapter.notifyDataSetChanged();
    }
}
