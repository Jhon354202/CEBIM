package com.chenxi.cebim.activity.inspection;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.adapter.LocationSearchAdapter;
import com.chenxi.cebim.view.ClearEditText;

import java.util.ArrayList;

public class LocationSearchActivity extends BaseActivity implements PoiSearch.OnPoiSearchListener {

    RelativeLayout noResource;
    LinearLayout back;
    ClearEditText searchContent;
    private PoiSearch.Query query;// Poi查询条件类

    String latitudeAndLongitude;
    double latitude;
    double longitude;
    ListView listView;

    ArrayList<PoiItem> result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_search);

        //获取前一个界面传过来的经纬度
        Intent intent = getIntent();
        latitudeAndLongitude = intent.getStringExtra("latitudeAndLongitude");
        //获取经纬度
        String[] latitudeAndLongitudeArr = latitudeAndLongitude.split(",");
        latitude = Double.valueOf(latitudeAndLongitudeArr[0].toString());
        longitude = Double.valueOf(latitudeAndLongitudeArr[1].toString());

        init();
    }

    /**
     * Method Desc:控件初始化
     */
    private void init() {

        noResource = (RelativeLayout) findViewById(R.id.rl_search_no_res);

        back = (LinearLayout) findViewById(R.id.ll_back_locationsearch);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //设置软键盘，把回车设置成搜索
        searchContent = (ClearEditText) findViewById(R.id.et_search_content);
        searchContent.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchContent.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        searchContent.setHintTextColor(Color.WHITE);

        searchContent.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //是否是回车键
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    //隐藏键盘
                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(LocationSearchActivity.this.getCurrentFocus()
                                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                    String content = searchContent.getText().toString().trim();
                    //搜索
                    search(content);
                }
                return false;
            }
        });

        listView = (ListView) findViewById(R.id.location_search_listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String title = result.get(position).getTitle();
                String snippet = result.get(position).getSnippet();

                Intent intent = new Intent();
                intent.putExtra("title", title);
                intent.putExtra("Snippet", snippet);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }

    /**
     * Method Desc:搜索功能
     */
    private void search(String content) {

        if ((content == null) || (content.equals(""))) {
            ToastUtils.showShort("输入为空");

        } else {
            query = new PoiSearch.Query(content, "汽车服务|汽车销售|汽车维修|摩托车服务|餐饮服务|" +
                    "购物服务|生活服务|体育休闲服务|医疗保健服务|住宿服务|风景名胜|商务住宅|政府机构及社会团体" +
                    "|科教文化服务|交通设施服务|金融保险服务|公司企业|道路附属设施|地名地址信息|公共设施", "");
            // keyWord表示搜索字符串，第二个参数表示POI搜索类型，默认为：生活服务、餐饮服务、商务住宅
            // 共分为以下20种：汽车服务|汽车销售|
            // 汽车维修|摩托车服务|餐饮服务|购物服务|生活服务|体育休闲服务|医疗保健服务|
            // 住宿服务|风景名胜|商务住宅|政府机构及社会团体|科教文化服务|交通设施服务|
            // 金融保险服务|公司企业|道路附属设施|地名地址信息|公共设施
            // cityCode表示POI搜索区域，（这里可以传空字符串，空字符串代表全国在全国范围内进行搜索）
//            query.setPageSize(30);// 设置每页最多返回多少条poiitem
//            query.setPageNum(1);// 设置查第一页
            PoiSearch poiSearch = new PoiSearch(this, query);
            //如果不为空值
            if (latitude != 0.0 && longitude != 0.0) {
                poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(latitude,
                        longitude), 500));// 设置周边搜索的中心点以及区域
                poiSearch.searchPOIAsyn();// 开始搜索
                poiSearch.setOnPoiSearchListener(this);// 设置数据返回的监听器
            } else {
                ToastUtils.showShort("定位失败");
            }

        }

    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        int pageCount = poiResult.getPageCount();
        result = poiResult.getPois();

        if (result.size() == 0) {
            noResource.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            LocationSearchAdapter mAdapter = new LocationSearchAdapter(LocationSearchActivity.this, result);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listView.setAdapter(mAdapter);
            noResource.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }


}
