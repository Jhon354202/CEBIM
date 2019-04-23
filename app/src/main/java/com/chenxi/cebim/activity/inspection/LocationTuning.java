package com.chenxi.cebim.activity.inspection;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.view.XListView;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class LocationTuning extends BaseActivity implements PoiSearch.OnPoiSearchListener, View.OnClickListener
        , LocationSource, AMapLocationListener, XListView.IXListViewListener {

    // 显示地图需要的变量
    private MapView mapView;// 地图控件
    private AMap aMap;// 地图对象

    // 定位需要的声明
    private AMapLocationClient mLocationClient = null;// 定位发起端
    private AMapLocationClientOption mLocationOption = null;// 定位参数
    private LocationSource.OnLocationChangedListener mListener = null;// 定位监听器

    private PoiSearch.Query query;// Poi查询条件类
    private PoiResult myPoiResult; // poi返回的结果
    ArrayList<PoiItem> poiItemList = new ArrayList<PoiItem>();

    LocationTuningAdapter mAdapter;

    // 标识，用于判断是否只显示一次定位信息和用户重新定位
    private boolean isFirstLoc = true;

    LinearLayout back;
    EditText searchBox;
    Button searchButton;
    XListView mapList;
    ImageView search;
    TextView ensure;
    com.amap.api.maps2d.MapView map;
    ProgressBar progressBar;

    double latitude;
    double longitude;

    String title, snippet;
    String searchContnet;

    protected static final int GAT_ITEM_COUNT = 2;

    int onLoadCount = 0;//加载次数统计
    private int selectPosition = -1;//用于记录用户选择的变量

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case GAT_ITEM_COUNT:
                    if (myPoiResult.getPois().size() < msg.arg1) {
                        mapList.setPullLoadEnable(false);
                    } else {
                        mapList.setPullLoadEnable(true);
                    }

                    break;

                default:
                    break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_tuning);

        back = (LinearLayout) findViewById(R.id.ll_back_locationtuning);
        back.setOnClickListener(this);

        // 地图可点击
        map = (MapView) findViewById(R.id.locationTuning_map);
        mapView = (MapView) findViewById(R.id.locationTuning_map);
        mapView.onCreate(savedInstanceState);

        // 获取地图对象
        aMap = mapView.getMap();

        // 设置显示定位按钮 并且可以点击
        UiSettings settings = aMap.getUiSettings();
        // 设置定位监听
        aMap.setLocationSource(this);
        // 是否显示定位按钮
        settings.setMyLocationButtonEnabled(false);
        // 关闭一切手势操作
        settings.setAllGesturesEnabled(true);
        // 是否显示缩放按钮
        settings.setZoomControlsEnabled(false);
        // 是否可触发定位并显示定位层
        aMap.setMyLocationEnabled(true);

        // 定位的小图标 默认是蓝点 这里自定义，其实就是一张图片
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(R.drawable.maplocation));
        myLocationStyle.radiusFillColor(android.R.color.transparent);
        myLocationStyle.strokeColor(android.R.color.transparent);
        aMap.setMyLocationStyle(myLocationStyle);

        // 开始定位
        initLoc();

        initview(); // 初始化控件

    }

    // 定位
    private void initLoc() {
        // 初始化定位
        mLocationClient = new AMapLocationClient(this);
        // 设置定位回调监听
        mLocationClient.setLocationListener(this);
        // 初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        // 设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption
                .setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        // 设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        // 设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        // 设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        // 设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        // 设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        // 给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        // 启动定位
        mLocationClient.startLocation();
    }

    // 定位回调函数
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {

        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                // 定位成功回调信息，设置相关消息
                amapLocation.getLocationType();// 获取当前定位结果来源，如网络定位结果，详见官方定位类型表
                amapLocation.getLatitude();// 获取纬度
                amapLocation.getLongitude();// 获取经度
                amapLocation.getAccuracy();// 获取精度信息
                SimpleDateFormat df = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss");
                Date date = new Date(amapLocation.getTime());
                df.format(date);// 定位时间
                amapLocation.getAddress();// 地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                amapLocation.getCountry();// 国家信息
                amapLocation.getProvince();// 省信息
                amapLocation.getCity();// 城市信息
                amapLocation.getDistrict();// 城区信息
                amapLocation.getStreet();// 街道信息
                amapLocation.getStreetNum();// 街道门牌号信息
                amapLocation.getCityCode();// 城市编码
                amapLocation.getAdCode();// 地区编码

                // 如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
                if (isFirstLoc) {
                    // 设置缩放级别
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                    // 将地图移动到定位点
                    aMap.moveCamera(CameraUpdateFactory
                            .changeLatLng(new LatLng(
                                    amapLocation.getLatitude(), amapLocation
                                    .getLongitude())));
                    // 点击定位按钮 能够将地图的中心移动到定位点
                    mListener.onLocationChanged(amapLocation);

                    // 获取经纬度
                    latitude = amapLocation.getLatitude();
                    longitude = amapLocation.getLongitude();


                    isFirstLoc = false;
                    search("", 0);//定位结束，回调成功后进行搜索，直接传空串
                }

            } else {
                // 显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError",
                        "location Error, ErrCode:"
                                + amapLocation.getErrorCode() + ", errInfo:"
                                + amapLocation.getErrorInfo());

//                Toast.makeText(LocationTuning.this, "定位失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    // 激活定位
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;

    }

    // 停止定位
    @Override
    public void deactivate() {
        mListener = null;
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    /**
     * 初始化控件
     */
    private void initview() {

        searchBox = (EditText) findViewById(R.id.et_search);
        searchButton = (Button) findViewById(R.id.bt_search);

        // ListView展示
        mapList = (XListView) findViewById(R.id.listView1);
        mapList.setPullRefreshEnable(false);
        mapList.setPullLoadEnable(true);
        mapList.setAutoLoadEnable(false);
        mapList.setXListViewListener(this);

        mapList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //获取选中的参数
                selectPosition = position;
                mAdapter.notifyDataSetChanged();

                title = mAdapter.arrayList.get(position - 1).getTitle();
                snippet = mAdapter.arrayList.get(position - 1).getSnippet();

            }
        });

        search = (ImageView) findViewById(R.id.iv_locationtuning_search);
        ensure = (TextView) findViewById(R.id.tv_ensure);

        //定义progressBar，并在刚进入时显示出来
        progressBar = (ProgressBar) findViewById(R.id.pb_search_resource);
        progressBar.setVisibility(View.VISIBLE);
        search.setVisibility(View.INVISIBLE);
        ensure.setVisibility(View.INVISIBLE);

        searchButton.setOnClickListener(this);
        search.setOnClickListener(this);
        ensure.setOnClickListener(this);

    }

    /**
     *Method Desc:搜索方法
     */
    private void search(String content, int pageNum) {
        searchContnet = content;
        query = new PoiSearch.Query(content, "汽车服务|汽车销售|汽车维修|摩托车服务|餐饮服务|" +
                "购物服务|生活服务|体育休闲服务|医疗保健服务|住宿服务|风景名胜|商务住宅|政府机构及社会团体" +
                "|科教文化服务|交通设施服务|金融保险服务|公司企业|道路附属设施|地名地址信息|公共设施", "");
        // keyWord表示搜索字符串，第二个参数表示POI搜索类型，默认为：生活服务、餐饮服务、商务住宅
        // 共分为以下20种：汽车服务|汽车销售|
        // 汽车维修|摩托车服务|餐饮服务|购物服务|生活服务|体育休闲服务|医疗保健服务|
        // 住宿服务|风景名胜|商务住宅|政府机构及社会团体|科教文化服务|交通设施服务|
        // 金融保险服务|公司企业|道路附属设施|地名地址信息|公共设施
        // cityCode表示POI搜索区域，（这里可以传空字符串，空字符串代表全国在全国范围内进行搜索）

        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        query.setPageNum(pageNum);
        PoiSearch poiSearch = new PoiSearch(this, query);
        //如果不为空值
        if (latitude != 0.0 && longitude != 0.0) {
            poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(latitude,
                    longitude), 500));// 设置周边搜索的中心点以及区域
            poiSearch.setOnPoiSearchListener(this);// 设置数据返回的监听器
            poiSearch.searchPOIAsyn();// 开始搜索

        } else {
            ToastUtils.showShort("定位失败");
        }

    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {

        myPoiResult = poiResult;

        if ((searchContnet == "") || (searchContnet == null)) {//没有关键词搜索的情况
            poiItemList.addAll(poiResult.getPois());
            if (poiItemList.size() <= 10) {
                mAdapter = new LocationTuningAdapter(LocationTuning.this, poiItemList);
                mapList.setAdapter(mAdapter);

                //获取选中的参数
                selectPosition = 1;
                mAdapter.notifyDataSetChanged();

                title = mAdapter.arrayList.get(0).getTitle();
                snippet = mAdapter.arrayList.get(0).getSnippet();
                showOrHideFooterView();
            } else {

                mAdapter.notifyDataSetChanged();
                showOrHideFooterView();
            }

        } else {//有关键词搜索的情况
            mAdapter = new LocationTuningAdapter(LocationTuning.this, poiResult.getPois());
            mapList.setAdapter(mAdapter);

            //获取选中的参数
            selectPosition = 1;
            mAdapter.notifyDataSetChanged();

            title = mAdapter.arrayList.get(0).getTitle();
            snippet = mAdapter.arrayList.get(0).getSnippet();
            showOrHideFooterView();
        }

        progressBar.setVisibility(View.GONE);
        search.setVisibility(View.VISIBLE);
        ensure.setVisibility(View.VISIBLE);
        onLoad();
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back_locationtuning:
                finish();
                break;

            case R.id.iv_locationtuning_search:
                Intent intent1 = new Intent(LocationTuning.this, LocationSearchActivity.class);
                intent1.putExtra("latitudeAndLongitude", latitude + "," + longitude);
                startActivityForResult(intent1, 1);
                break;

            case R.id.tv_ensure:

                Intent intent2 = new Intent();
                intent2.putExtra("title", title);
                intent2.putExtra("Snippet", snippet);
                setResult(RESULT_OK, intent2);

                finish();
                break;

            default:
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    String returnTitle = data.getStringExtra("title");
                    search(returnTitle, 0);
                }
                break;
            default:
        }
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onLoadMore() {
        onLoadCount++;
        if (query.getPageSize() == 0) {
            ToastUtils.showShort("数据加载完毕！");
        } else {
            search("", onLoadCount);
        }
    }

    private void onLoad() {

        mapList.stopRefresh();
        mapList.stopLoadMore();

    }

    //如果listView中的数据不能填充整个屏幕，隐藏FooterView；
    private void showOrHideFooterView() {

        //刚进入界面及切换界面中两个选项卡时，调用getLastVisiblePosition()和getFirstVisiblePosition()方法的时候，ListView 并没有加载完成，所以用到异步
        mapList.post(new Runnable() {
            public void run() {
                int itemCount = mapList.getLastVisiblePosition() - mapList.getFirstVisiblePosition() + 1;

                Message message = new Message();
                message.what = GAT_ITEM_COUNT;
                message.arg1 = itemCount;
                handler.sendMessage(message);
            }
        });
    }

    public class LocationTuningAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<PoiItem> arrayList;

        public LocationTuningAdapter(Context context, ArrayList<PoiItem> arrayList) {
            this.context = context;
            this.arrayList = arrayList;
        }

        @Override
        public int getCount() {

            return arrayList.size();
        }

        @Override
        public Object getItem(int position) {

            return arrayList.get(position);
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(context, R.layout.item_listview, null);
                holder.item_name = (TextView) convertView
                        .findViewById(R.id.item_name);
                holder.item_location = (TextView) convertView
                        .findViewById(R.id.item_location);
                holder.item_isSelect = (ImageView) convertView
                        .findViewById(R.id.is_select);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.item_name.setText(String.valueOf(arrayList.get(position)));
            holder.item_location.setText(String.valueOf(arrayList.get(position).getSnippet()));

            if (selectPosition == position + 1) {
                holder.item_isSelect.setVisibility(View.VISIBLE);
            } else {
                holder.item_isSelect.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }

        public class ViewHolder {
            TextView item_name;         //名称
            TextView item_location;     //位置
            ImageView item_isSelect;     //是否选中

        }

    }
}
