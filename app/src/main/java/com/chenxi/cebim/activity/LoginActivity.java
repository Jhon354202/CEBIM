package com.chenxi.cebim.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.utils.StringUtil;
import com.chenxi.cebim.view.ClearEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 登录界面
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {


    private RelativeLayout setting, settingBox;

    private ClearEditText userName, passWord, ipInputBox;

    private TextView register, forgetPassword;

    private Button login;

    private ProgressDialog progressDialog;

    private ImageView settingPic;

    private int isInputIp = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }

//        Intent intent=getIntent();
//        String from=intent.getStringExtra("from");

        setContentView(R.layout.activity_login);

        //控件初始化
        initView();

//        if(from==null){
//            //自动登录
//            autoLogin();
//        }

        //退出登录后，返回该界面，并回传用户名和密码到该界面，显示在界面的相应位置
        showLoginParms();

    }

    private void initView() {
        register = (TextView) findViewById(R.id.tv_register);
        userName = (ClearEditText) findViewById(R.id.et_accountnum);
        passWord = (ClearEditText) findViewById(R.id.et_password);
        forgetPassword = (TextView) findViewById(R.id.tv_forget_password);
        login = (Button) findViewById(R.id.btn_login);
        settingPic = (ImageView) findViewById(R.id.ip_setting_pic);
        setting = (RelativeLayout) findViewById(R.id.rl_setting);
        settingBox = (RelativeLayout) findViewById(R.id.rl_setting);
        ipInputBox = (ClearEditText) findViewById(R.id.et_ipsetting);
        ipInputBox.setText(AppConst.innerIp.substring(7));

        //IP输入框记住IP的功能
//        if (!SPUtils.getInstance().getString("cloudIP", "").toString().isEmpty()) {
//            ipInputBox.setText(SPUtils.getInstance().getString("cloudIP", "").toString());
//            ipInputBox.setText(AppConst.innerIp.substring(6));
//        }

        register.setOnClickListener(this);
        forgetPassword.setOnClickListener(this);
        login.setOnClickListener(this);
        settingPic.setOnClickListener(this);
    }


    @Override

    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_login:

                if (getUserName().length() == 0 || getUserName() == null) {
                    ToastUtils.showShort("请填写用户名");
                    break;
                } else if (getPassWord().length() == 0 || getPassWord() == null) {
                    ToastUtils.showShort("密码不能为空");
                    break;
                }
//                else if (!NetworkUtils.isAvailableByPing()) {
//                    ToastUtils.showShort("网络不可用,请检查网络设置");
//                }
                else {
                    login(false);
                    break;
                }

            case R.id.ip_setting_pic:
                if (isInputIp == 0) {
                    settingBox.setVisibility(View.GONE);
                    isInputIp = 1;
                } else {
                    settingBox.setVisibility(View.VISIBLE);
                    isInputIp = 0;
                }

            default:
                break;
        }
    }


    //判断是否自动登录
    private void autoLogin() {
        String userNameString = (String) SPUtils.getInstance().getString("UserName", "");
        String psw = (String) SPUtils.getInstance().getString("Password", "");
        if ((userNameString.length() != 0) && (psw.length() != 0)) {
//            startActivity(new Intent(LoginActivity.this, NavigationActivity.class));
//            finish();
            userName.setText(userNameString);
            passWord.setText(psw);
            userName.setSelection(userNameString.length());//将光标移至文字末尾
            passWord.setSelection(psw.length());//将光标移至文字末尾
            login(true);
        }
    }

    private void showLoginParms() {

        //接收参数
        String spUserName = (String) SPUtils.getInstance().getString("UserName", "");
        String spPsw = (String) SPUtils.getInstance().getString("Password", "");

        if ((spUserName != null) && (spPsw != null)) {
            userName.setText(spUserName);
            passWord.setText(spPsw);
            userName.setSelection(spUserName.length());//将光标移至文字末尾
            passWord.setSelection(spPsw.length());//将光标移至文字末尾
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    userName.setText(StringUtil.replaceBlank(data.getStringExtra("userName").toString()));
                    passWord.setText(StringUtil.replaceBlank(data.getStringExtra("passWord").toString()));
                }
                break;
            default:
        }
    }

    //----------------------------------------登录请求----------------------------------------//
    private void login(boolean isAutoLoadin) {

        if(!isAutoLoadin){
            //加载加进度条，加班类别请求成功后消失。
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("登录中...");
            progressDialog.setCancelable(true);
            progressDialog.show();  //将进度条显示出来
        }

        //存储这两个参数，用于自动登录

        SPUtils.getInstance().put("UserName", getUserName());
        SPUtils.getInstance().put("Password", getPassWord());

        Request request;
        if (ipInputBox.getText() == null || ipInputBox.getText().toString().equals("")) {
            request = new Request.Builder()
                    .url(AppConst.innerIp + "/api/User?name=" + getUserName() + "&password=" + getPassWord())
                    .build();
        } else {
            request = new Request.Builder()
                    .url("http://" + ipInputBox.getText().toString() + "/api/User?name=" + getUserName() + "&password=" + getPassWord())
                    .build();
        }

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("登陆失败");
                progressDialog.dismiss();
            }

            @Override
            public void onResponse(Call call, Response response) {

                if (response.code() == 200) {
                    try {
                        String responseData = null;
                        responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);

                        String userName = jsonObject.getString("UserName");
                        int useID = jsonObject.getInt("UserID");

                        SPUtils.getInstance().put("UserName", userName);
                        SPUtils.getInstance().put("UserID", useID);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        ToastUtils.showShort("数据解析出错");
                    } catch (IOException e) {
                        e.printStackTrace();
                        ToastUtils.showShort("数据请求出错");
                    }

                    startActivity(new Intent(LoginActivity.this, NavigationActivity.class));
                    finish();
                } else {
                    ToastUtils.showShort("登陆失败，请检查用户名和密码");
                    progressDialog.dismiss();
                }
            }
        });

    }

    //获取密码
    private String getPassWord() {

        String myPpassWord = StringUtil.replaceBlank(passWord.getText().toString());

        return myPpassWord;
    }


    //获取用户名（手机号）
    private String getUserName() {

        String myUserName = StringUtil.replaceBlank(userName.getText().toString());

        return myUserName;

    }
    //----------------------------------------登录请求----------------------------------------//


    //在onDestroy()关闭弹窗，防止窗体溢出异常
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        //在登陆界面退出程序时，如果用户名或者密码有一个为没数据，就清空它们的SharePreference
        if ((getUserName().length() == 0) || (getPassWord().length() == 0)) {
            SPUtils.getInstance().clear();
        }
    }

}
