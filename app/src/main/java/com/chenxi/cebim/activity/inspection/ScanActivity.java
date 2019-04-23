package com.chenxi.cebim.activity.inspection;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.utils.NumberUtil;
import com.chenxi.cebim.utils.TimeUtil;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pub.devrel.easypermissions.EasyPermissions;

public class ScanActivity extends BaseActivity implements PoiSearch.OnPoiSearchListener, View.OnClickListener,
        LocationSource, AMapLocationListener, EasyPermissions.PermissionCallbacks {

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

    // 标识，用于判断是否只显示一次定位信息和用户重新定位
    private boolean isFirstLoc = true;

    // 控件
    private ImageView scanButton;
    private ImageView back;
    TextView currentDay, currentLocation, signinTime, adjustlocation;
    //    XListView mapList;
    ImageView search;
    TextView ensure,isScanning;
    com.amap.api.maps2d.MapView map;
    ProgressBar progressBar;
    Button signin, btRefresh;
    EditText visiter;

    String latitudeAndLongitude;
    String location;
    String provenceCityAndDistrict;

    //纬度
    double latitude;
    //经度
    double longitude;

    //扫描结果
    String scanResult;

    int userId;

    //签到时间
    String time;

    //权限申请 RequestCode
    public static final int REQUEST_CODE = 111;

    String title, snippet;
    String searchContnet;

    protected static final int GAT_ITEM_COUNT = 2;

    int onLoadCount = 0;//加载次数统计
    private int selectPosition = -1;//用于记录用户选择的变量

//    LocationTuningAdapter mAdapter;

    String TAG = "GetPositionActivity";

    private boolean noFirstRequestLocation = false;
    private boolean noFirstRequestCarmera = false;

    private RelativeLayout rlRefresh;

//    private Handler handler = new Handler() {
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//
//                case GAT_ITEM_COUNT:
//                    if (myPoiResult.getPois().size() < msg.arg1) {
//                        mapList.setPullLoadEnable(false);
//                    } else {
//                        mapList.setPullLoadEnable(true);
//                    }
//
//                    break;
//
//                default:
//                    break;
//            }
//        }
//
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        initView();

        //获取从MainActivity中传过来的数据
        userId = SPUtils.getInstance().getInt("UserID");

        // 显示地图
        mapView = (MapView) findViewById(R.id.working_upper_map);
        // 必须要写
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

        // 开启定位权限，并开始定位
        if (EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {//检查是否获取该权限
            initLoc();
        } else {
            //第二个参数是被拒绝后再次申请该权限的解释
            //第三个参数是请求码
            //第四个参数是要申请的权限
            EasyPermissions.requestPermissions(this, "定位需要定位权限", 0, Manifest.permission.ACCESS_COARSE_LOCATION);

        }

    }

    private void addPermission(String permission, String whichPermission) {

        if (!EasyPermissions.hasPermissions(this, permission)) {//检查是否获取该权限,没有则进行获取

            //第二个参数是被拒绝后再次申请该权限的解释
            //第三个参数是请求码
            //第四个参数是要申请的权限
            EasyPermissions.requestPermissions(this, whichPermission, 0, permission);
        }
    }

    private void initView() {

        back = findViewById(R.id.toolbar_left_btn);
        back.setOnClickListener(this);

        // 签到按钮点击事件
        scanButton = (ImageView) findViewById(R.id.iv_scanbutton);
        scanButton.setImageResource(R.drawable.sign_button_gray);//刚刚进入时显示为灰色，不可点击状态
        scanButton.setOnClickListener(this);

        currentDay = (TextView) findViewById(R.id.currentday);
        currentDay.setText(TimeUtil.getyear() + "年"
                + NumberUtil.numLengthControl(TimeUtil.getmonth()) + "月"
                + NumberUtil.numLengthControl(TimeUtil.getday()) + "日");

        currentLocation = (TextView) findViewById(R.id.currentlocation);
        signinTime = (TextView) findViewById(R.id.signintime);
        signinTime.setText(new SimpleDateFormat("HH:mm", Locale.CHINA).format(new java.util.Date()));

        time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA).format(new java.util.Date());
        signin = (Button) findViewById(R.id.bt_signin);
        signin.setOnClickListener(this);

        adjustlocation = (TextView) findViewById(R.id.adjustlocation);
        adjustlocation.setOnClickListener(this);

        isScanning=(TextView)findViewById(R.id.tv_is_scanning);

//        rlRefresh=(RelativeLayout) findViewById(R.id.rl_getposition_refresh);
//        btRefresh=(Button) findViewById(R.id.bt_uploadfile_refresh);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_left_btn:
                finish();
                break;

            case R.id.iv_scanbutton:

                if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {//检查是否获取该权限
                    Intent intent = new Intent(ScanActivity.this, CaptureActivity.class);
                    startActivityForResult(intent, REQUEST_CODE);
                } else {
                    //第二个参数是被拒绝后再次申请该权限的解释
                    //第三个参数是请求码
                    //第四个参数是要申请的权限
                    EasyPermissions.requestPermissions(this, "扫二维码需要拍照权限", 0, Manifest.permission.CAMERA);

                    //如果用户勾选了“不在提示”，则直接打开app的管理菜单界面，用户可点击这个界面的权限管理，进行授权或关闭权限
                    if (noFirstRequestCarmera && EasyPermissions.permissionPermanentlyDenied(ScanActivity.this, Manifest.permission.CAMERA)) {
                        getAppDetailSettingIntent(ScanActivity.this);
                        ToastUtils.showShort("扫码需要拍照权限，请在权限管理菜单中开启拍照权限");
                    }
                    noFirstRequestCarmera = true;
                }

                break;

            case R.id.bt_signin:

                if ((scanResult == null) || (scanResult == "")) {
                    ToastUtils.showShort("请扫码成功再提交");
                } else if (provenceCityAndDistrict == "" || provenceCityAndDistrict == null) {
                    ToastUtils.showShort("定位失败，请定位成功后再提交");
                    //添加读写权限
                    addPermission(Manifest.permission.ACCESS_COARSE_LOCATION, "签到需要定位权限");
                    if (noFirstRequestLocation && EasyPermissions.permissionPermanentlyDenied(ScanActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        getAppDetailSettingIntent(ScanActivity.this);
                    }
                    noFirstRequestLocation = true;
                } else {
                    updata();
                }

                break;

            case R.id.adjustlocation:
                if (!EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    ToastUtils.showShort("定位权限未开启");
                    //添加读写权限
                    addPermission(Manifest.permission.ACCESS_COARSE_LOCATION, "地点微调需要定位权限");
                    if (noFirstRequestLocation && EasyPermissions.permissionPermanentlyDenied(ScanActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        getAppDetailSettingIntent(ScanActivity.this);
                    }
                    noFirstRequestLocation = true;
                } else {
                    Intent intent1 = new Intent(ScanActivity.this, LocationTuning.class);
                    startActivityForResult(intent1, 2);
                }
                break;
        }

    }

    //跳转至app应用详情界面
    private void getAppDetailSettingIntent(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
        }
        startActivity(localIntent);
    }

    //上传定位和扫描数据到服务器
    private void updata() {

        if (currentLocation.getText() == null || currentLocation.getText().equals("")) {
            ToastUtils.showShort("位置信息获取失败，请获取位置信息再签到");
        } else if(scanResult.contains("http")||scanResult.contains("https")){

            RequestBody requestBody = new FormBody.Builder()
                    .add("Location", currentLocation.getText().toString())//位置
                    .add("Coordinate",latitudeAndLongitude)//纬度、精度
                    .build();

            Request request = new Request.Builder()
                    .url(scanResult)
                    .post(requestBody)
                    .build();

            MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    ToastUtils.showShort("签到失败");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() == 200) {
                        ToastUtils.showShort("签到成功");
                        finish();
                    } else {
                        ToastUtils.showShort("签到失败");
                    }
                }
            });
        }else{
            ToastUtils.showShort("无效二维码");
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //把申请权限的回调交由EasyPermissions处理
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    //下面两个方法是实现EasyPermissions的EasyPermissions.PermissionCallbacks接口
    //分别返回授权成功和失败的权限
    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (perms.get(0).equals("android.permission.READ_PHONE_STATE")) {
            ToastUtils.showShort("READ_PHONE_STATE权限获取成功");
        } else if (perms.get(0).equals("android.permission.ACCESS_COARSE_LOCATION")) {
            ToastUtils.showShort("定位权限获取成功");
            initLoc();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

        if (perms.get(0).equals("android.permission.ACCESS_COARSE_LOCATION")) {
            ToastUtils.showShort("定位权限获取失败,请在设置中手动开启");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /**
         * 处理二维码扫描结果
         */
        if ((requestCode == REQUEST_CODE) && (resultCode == RESULT_OK)) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    scanResult = result;
                    ToastUtils.showShort("扫码成功");
                    isScanning.setText("你已扫码成功");
                    signin.setBackground(getResources().getDrawable(R.drawable.submit_view_pressed));
                    signin.setClickable(true);
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    ToastUtils.showShort("解析二维码失败");
                }
            }
        } else if ((requestCode == 1) && (resultCode == RESULT_OK)) {
            ArrayList<String> returnInfo = data.getStringArrayListExtra("data_return");
            String str = returnInfo.toString();
            String returnInfoStr = str.substring(1, str.length() - 1);
            visiter.setText(returnInfoStr);
            visiter.setSelection(returnInfoStr.length());//将光标移至文字末尾
        } else if ((requestCode == 2) && (resultCode == RESULT_OK)) {
            title = data.getStringExtra("title");
            snippet = data.getStringExtra("Snippet");

            if ((title != null) || (title != "")) {
                currentLocation.setText(title);
            } else {
                currentLocation.setText(location);
            }

        }
    }

    /**
     * Method Desc:搜索方法
     */
    private void search(String content, int pageNum) {
        searchContnet = content;
        query = new PoiSearch.Query(content, "公司企业", "");
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
            poiSearch.setOnPoiSearchListener(ScanActivity.this);// 设置数据返回的监听器
            poiSearch.searchPOIAsyn();// 开始搜索

        }
//        else {
//            ToastUtil.show(GetPositionActivity.this, "定位失败", 1000);
//        }

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
                amapLocation.getAoiName();


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
                    // 获取定位信息
                    StringBuffer buffer1 = new StringBuffer();
                    buffer1.append(amapLocation.getAddress());

                    // 获取经纬度
                    StringBuffer buffer2 = new StringBuffer();
                    buffer2.append(amapLocation.getLatitude() + ","
                            + amapLocation.getLongitude());

                    latitudeAndLongitude = buffer2.toString();

                    // 获取打开地点
                    StringBuffer buffer3 = new StringBuffer();
                    buffer3.append(amapLocation.getAddress() + ","
                            + amapLocation.getStreet()
                            + amapLocation.getStreetNum());

                    location = buffer3.toString();
                    provenceCityAndDistrict = amapLocation.getProvince() + "、" + amapLocation.getCity() + "、" + amapLocation.getDistrict() +
                            "、" + amapLocation.getAoiName();
                    currentLocation.setText(provenceCityAndDistrict);
                    isFirstLoc = false;
                    scanButton.setImageResource(R.drawable.sign_button_light);//定位成功，把打卡图标设置为可点击状态
                }

            } else {
                // 显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError",
                        "location Error, ErrCode:"
                                + amapLocation.getErrorCode() + ", errInfo:"
                                + amapLocation.getErrorInfo());
            }
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
    }

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

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        myPoiResult = poiResult;

//        if ((searchContnet == "") || (searchContnet == null)) {//没有关键词搜索的情况
//            poiItemList.addAll(poiResult.getPois());
//            if (poiItemList.size() <= 10) {
//                mAdapter = new LocationTuningAdapter(GetPositionActivity.this, poiItemList);
//                mapList.setAdapter(mAdapter);
//
//                //获取选中的参数
//                selectPosition = 1;
//                mAdapter.notifyDataSetChanged();
//
//                title = mAdapter.arrayList.get(0).getTitle();
//                snippet = mAdapter.arrayList.get(0).getSnippet();
//                showOrHideFooterView();
//            } else {
//
//                mAdapter.notifyDataSetChanged();
//                showOrHideFooterView();
//            }
//
//        } else {//有关键词搜索的情况
//            mAdapter = new LocationTuningAdapter(GetPositionActivity.this, poiResult.getPois());
//            mapList.setAdapter(mAdapter);
//
//            //获取选中的参数
//            selectPosition = 1;
//            mAdapter.notifyDataSetChanged();
//
//            title = mAdapter.arrayList.get(0).getTitle();
//            snippet = mAdapter.arrayList.get(0).getSnippet();
//            showOrHideFooterView();
//        }
//
//        progressBar.setVisibility(View.GONE);
//        search.setVisibility(View.VISIBLE);
//        ensure.setVisibility(View.VISIBLE);
//        onLoad();
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    private void onLoad() {

//        mapList.stopRefresh();
//        mapList.stopLoadMore();

    }

    //如果listView中的数据不能填充整个屏幕，隐藏FooterView；
    private void showOrHideFooterView() {

//        //刚进入界面及切换界面中两个选项卡时，调用getLastVisiblePosition()和getFirstVisiblePosition()方法的时候，ListView 并没有加载完成，所以用到异步
//        mapList.post(new Runnable() {
//            public void run() {
//                int itemCount = mapList.getLastVisiblePosition() - mapList.getFirstVisiblePosition() + 1;
//
//                Message message = new Message();
//                message.what = GAT_ITEM_COUNT;
//                message.arg1 = itemCount;
//                handler.sendMessage(message);
//            }
//        });
    }

//    @Override
//    public void onRefresh() {
//
//    }
//
//    @Override
//    public void onLoadMore() {
//        onLoadCount++;
//        if (query.getPageSize() == 0) {
//            ToastUtils.showShort("数据加载完毕！");
//        } else {
//            search("", onLoadCount);
//        }
//    }

//    public class LocationTuningAdapter extends BaseAdapter {
//        private Context context;
//        private ArrayList<PoiItem> arrayList;
//
//        public LocationTuningAdapter(Context context, ArrayList<PoiItem> arrayList) {
//            this.context = context;
//            this.arrayList = arrayList;
//        }
//
//        @Override
//        public int getCount() {
//
//            return arrayList.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//
//            return arrayList.get(position);
//        }
//
//        @Override
//        public long getItemId(int position) {
//
//            return position;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            ViewHolder holder = null;
//            if (convertView == null) {
//                holder = new ViewHolder();
//                convertView = View.inflate(context, R.layout.item_listview, null);
//                holder.item_name = (TextView) convertView
//                        .findViewById(R.id.item_name);
//                holder.item_location = (TextView) convertView
//                        .findViewById(R.id.item_location);
//                holder.item_isSelect = (ImageView) convertView
//                        .findViewById(R.id.is_select);
//                convertView.setTag(holder);
//
//            } else {
//                holder = (ViewHolder) convertView.getTag();
//            }
//
//            holder.item_name.setText(String.valueOf(arrayList.get(position)));
//            holder.item_location.setText(String.valueOf(arrayList.get(position).getSnippet()));
//
//            if (selectPosition == position + 1) {
//                holder.item_isSelect.setVisibility(View.VISIBLE);
//            } else {
//                holder.item_isSelect.setVisibility(View.INVISIBLE);
//            }
//
//            return convertView;
//        }
//
//        public class ViewHolder {
//            TextView item_name;         //名称
//            TextView item_location;     //位置
//            ImageView item_isSelect;     //是否选中
//
//        }
//
//    }
}
