package com.chenxi.cebim.activity.coordination;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.appConst.AppConst;

import java.util.ArrayList;
import java.util.List;

//滑动预览图片界面
public class SlidePreviewPicActivity extends BaseActivity {

    private ViewPager mPager;
    private ImageView back;
    private TextView picNum;
    List<String> imagePathList = new ArrayList<>();

    int firstPosition;//第一次打开图片

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_preview_pic);

        Intent intent = getIntent();
        String preViewPicStr = intent.getStringExtra("preViewPic");
        firstPosition = intent.getIntExtra("position", -1);
        //解析字符串为List<ProblemDetailPicModel>
        for (int i = 0; i < preViewPicStr.split("@#@#@#").length; i++) {
            String path = preViewPicStr.split("@#@#@#")[i].split("@@@@@@")[0];
            String id = preViewPicStr.split("@#@#@#")[i].split("@@@@@@")[1];
//            ProblemDetailPicModel problemDetailPicModel = new ProblemDetailPicModel();
            if(id.equals("本地图片")){
                imagePathList.add(path);
            }else{
                imagePathList.add(AppConst.innerIp + "/api/AnnexFile/" + id+"?isArt=true");
            }

        }

        //返回按钮
        back = findViewById(R.id.toolbar_left_btn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        picNum=findViewById(R.id.toolbar_right_tv);

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setPageMargin((int) (getResources().getDisplayMetrics().density * 15));
        mPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return imagePathList.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                PhotoView view = new PhotoView(SlidePreviewPicActivity.this);
                view.enable();
                view.setScaleType(ImageView.ScaleType.FIT_CENTER);

                String imageUrl = imagePathList.get(position);

                //显示图片
                RequestOptions options = new RequestOptions()
//                        .centerCrop()
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.load_fail)
                        .diskCacheStrategy(DiskCacheStrategy.ALL);
                Glide.with(SlidePreviewPicActivity.this)
                        .load(imageUrl)
                        .apply(options)
                        .into(view);

                container.addView(view);
                return view;
            }


            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }
        });

        mPager.setCurrentItem(firstPosition);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                picNum.setText(String.valueOf(i+1)+"/"+imagePathList.size());
            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });


    }
}

