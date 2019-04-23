package com.chenxi.cebim.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.AboutasActivity;
import com.chenxi.cebim.activity.FeedbackActivity;
import com.chenxi.cebim.activity.LoginActivity;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.utils.ActivityCollector;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class SettingFragment extends BaseFragment implements View.OnClickListener {

    View view;
    private TextView loadOut, userName;
    private RelativeLayout rl_about_us, rl_suggestion;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_setting, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        loadOut = (TextView) view.findViewById(R.id.tv_load_out);
        loadOut.setOnClickListener(this);
        userName = (TextView) view.findViewById(R.id.user_name);
        userName.setText(SPUtils.getInstance().getString("UserName"));
        rl_about_us = (RelativeLayout) view.findViewById(R.id.rl_about_us);
        rl_about_us.setOnClickListener(this);
        rl_suggestion = (RelativeLayout) view.findViewById(R.id.rl_suggestion);
        rl_suggestion.setOnClickListener(this);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.tv_load_out:
                loadOut();
                break;
            case R.id.rl_about_us:
                intent = new Intent(getActivity(), AboutasActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_suggestion:
                intent = new Intent(getActivity(), FeedbackActivity.class);
                startActivity(intent);
                break;
        }
    }

    //登出方法
    private void loadOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("您确定要退出登录吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                Request request = new Request.Builder()
                        .url(AppConst.innerIp + "/api/User")
                        .build();

                MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        try {
                            String header = response.headers().toString();
                            String responseData = response.body().string();
                            if (responseData.equals("true")) {
                                ActivityCollector.finishAll();
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                intent.putExtra("from", "SettingFragment");
                                startActivity(intent);
                                getActivity().finish();
                            } else {
                                ToastUtils.showShort("登出失败，请检查网络是否设置成功");
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                            System.out.println(e);
                        }
                    }
                });


            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // 设置取消的监听, 用户点击返回键时会触发
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });

        Dialog dialog = builder.show();
    }
}
