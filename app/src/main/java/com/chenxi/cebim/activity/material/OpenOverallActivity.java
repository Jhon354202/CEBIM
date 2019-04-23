package com.chenxi.cebim.activity.material;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.model.WebModelActivity;
import com.chenxi.cebim.entity.StateNum;
import com.chenxi.cebim.utils.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OpenOverallActivity extends AppCompatActivity {
    List<StateNum> chooseItems = new ArrayList<>();
    JSONArray jsonArray = new JSONArray();
    JSONObject object;
    @BindView(R.id.webview)
    WebView webView;
    @BindView(R.id.progressbar)
    ProgressBar progressBar;
    private String modelIds = "", colors = "", componentIdSbs = "";
    private int projectID;
    private String url;

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_overall);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        chooseItems = (List<StateNum>) intent.getSerializableExtra("chooseItem");
        componentIdSbs = intent.getStringExtra("componentIdSbs");
        modelIds = intent.getStringExtra("modelIds");
        colors = intent.getStringExtra("colors");
        String[] componentIdSbsArray = componentIdSbs.split(",");
        String[] modelIdsArray = modelIds.split(",");
        try {
            for (int i = 0; i < componentIdSbsArray.length; i++) {
                object = new JSONObject();
                object.put("ModelID", Integer.valueOf(modelIdsArray[i]));
                object.put("ID", componentIdSbsArray[i]);
                jsonArray.put(object);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println("jsonArray=====" + jsonArray);
        System.out.println("modelIdString=======" + modelIds);
        System.out.println("colors================" + colors);
        System.out.println("componentIdSbs=========" + componentIdSbs);
        projectID = SPUtils.getInstance().getInt("projectID", -1);
        url = "http://114.115.160.197:8002/Systems/Model/CEBIMModel?" + "projectid=" + projectID + "&ids=" + modelIds + "&from=pc";
        webView.loadUrl(url);//加载url
        webView.addJavascriptInterface(this, "android");//添加js监听 这样html就能调用客户端
        webView.setWebChromeClient(webChromeClient);
        webView.setWebViewClient(webViewClient);
        //支持获取手势焦点
        webView.requestFocusFromTouch();
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);//允许使用js
        webSettings.setDomStorageEnabled(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.supportMultipleWindows();
        webSettings.setSupportMultipleWindows(true);
        /**
         * LOAD_CACHE_ONLY: 不使用网络，只读取本地缓存数据
         * LOAD_DEFAULT: （默认）根据cache-control决定是否从网络上取数据。
         * LOAD_NO_CACHE: 不使用缓存，只从网络获取数据.
         * LOAD_CACHE_ELSE_NETWORK，只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据。
         */
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);//不使用缓存，只从网络获取数据.

        //支持屏幕缩放
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
    }

    WebChromeClient webChromeClient = new WebChromeClient() {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            AlertDialog.Builder localBuilder = new AlertDialog.Builder(webView.getContext());
            localBuilder.setMessage(message).setPositiveButton("确定", null);
            localBuilder.setCancelable(false);
            localBuilder.create().show();

            //注意:
            //必须要这一句代码:result.confirm()表示:
            //处理结果为确定状态同时唤醒WebCore线程
            //否则不能继续点击按钮
            result.confirm();

            return true;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            progressBar.setProgress(newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
        }
    };
    WebViewClient webViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (url.equals("http://www.google.com/")) {
                ToastUtils.showShort("国内不能访问google,拦截该url");
                return true;//表示我已经处理过了
            }
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
            String urls = "javascript:OnShowPosition('" + jsonArray + "'," + colors + ")";
            webView.evaluateJavascript(urls, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {

                }
            });
        }
    };
}
