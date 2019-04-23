package com.chenxi.cebim.activity.material;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.adapter.ShowStructuralPicGridAdapter;
import com.chenxi.cebim.utils.DiskCacheDirUtil;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MaterialFollowBatchSetupActivity extends BaseActivity {

    @BindView(R.id.img_num)
    TextView imgNum;
    @BindView(R.id.picture_recycler)
    RecyclerView pictureRecycler;
    @BindView(R.id.gv_showfile)
    GridView mGridView;
    @BindView(R.id.nextstate)
    TextView nextstate;
    @BindView(R.id.rnextstate)
    RelativeLayout rnextstate;
    private ShowStructuralPicGridAdapter adapter;
    private List<String> tempPathList = new ArrayList<>();//用于装载临时上传文件地址
    private List<LocalMedia> selectList = new ArrayList<>(); //当前选择的所有图片
    private List<String> pathList = new ArrayList<>();//用于装载上传文件地址
    private List<String> vedioPathList = new ArrayList<>();//用于装载视频地址
    Dialog picDialog;
    String savePath;
    private ImageView back;
    private TextView toolbar_right_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_follow_batch_setup);
        ButterKnife.bind(this);
        savePath = DiskCacheDirUtil.getDiskCacheDir(MaterialFollowBatchSetupActivity.this);//图片、视频、录音存储路径
        adapter = new ShowStructuralPicGridAdapter(MaterialFollowBatchSetupActivity.this, tempPathList, pListener);
        mGridView.setAdapter(adapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < tempPathList.size()) {
                } else {
                    showPicDialog();
                }
            }
        });
        back = findViewById(R.id.toolbar_left_btn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar_right_tv = findViewById(R.id.toolbar_right_tv);
        toolbar_right_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("newstate", nextstate.getText().toString());
                intent.putExtras(bundle);
                setResult(111, intent);
                finish();
            }
        });
    }

    private void showPicDialog() {
        picDialog = new Dialog(MaterialFollowBatchSetupActivity.this, R.style.ActionSheetDialogStyle);
        View inflate = LayoutInflater.from(MaterialFollowBatchSetupActivity.this).inflate(R.layout.pictureselect_dialog, null);
        Button getPhoto = (Button) inflate.findViewById(R.id.bt_take_photo);
        Button album = (Button) inflate.findViewById(R.id.bt_album);
        Button cancel = (Button) inflate.findViewById(R.id.btn_pic_or_vedio_cancel);
        getPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PictureSelector.create(MaterialFollowBatchSetupActivity.this)
                        .openCamera(PictureMimeType.ofAll())
                        .enableCrop(true)
                        .compress(true).showCropFrame(true)
                        .cropCompressQuality(60)
                        .selectionMedia(selectList)
                        .forResult(PictureConfig.CHOOSE_REQUEST);
                picDialog.dismiss();
            }
        });
        album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 进入相册 以下是例子：用不到的api可以不写
                PictureSelector.create(MaterialFollowBatchSetupActivity.this)
                        .openGallery(PictureMimeType.ofImage())//全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                        .maxSelectNum(9 - vedioPathList.size())// 最大图片选择数量 int
                        .minSelectNum(1)// 最小选择数量 int
                        .imageSpanCount(3)// 每行显示个数 int
                        .selectionMode(PictureConfig.MULTIPLE)// 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                        .previewImage(true)// 是否可预览图片 true or false
                        .previewVideo(true)// 是否可预览视频 true or false
                        .enablePreviewAudio(false) // 是否可播放音频 true or false
                        .isCamera(false)// 是否显示拍照按钮 true or false
                        .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                        .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                        .sizeMultiplier(1f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                        .setOutputCameraPath(savePath + "VEDIO")// 自定义拍照保存路径,可不填
                        .enableCrop(false)// 是否裁剪 true or false
                        .compress(true)// 是否压缩 true or false
                        .cropCompressQuality(60)//压缩质量
                        .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中) true or false
                        .minimumCompressSize(100)// 小于100kb的图片不压缩
                        .synOrAsy(true)//同步true或异步false 压缩 默认同步
                        .videoQuality(1)// 视频录制质量 0 or 1 int
                        .videoMaxSecond(60)// 显示多少秒以内的视频or音频也可适用 int
                        .videoMinSecond(1)// 显示多少秒以内的视频or音频也可适用 int
                        .selectionMedia(selectList)// 是否传入已选图片
//                        .recordVideoSecond(600)//视频秒数录制 默认60s int
                        .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code

                picDialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picDialog.dismiss();
            }
        });
        picDialog.setContentView(inflate);
        Window dialogWindow = picDialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.y = 20;
        dialogWindow.setAttributes(lp);
        picDialog.show();
    }

    //GrideView中图片删除按钮点击事件
    private ShowStructuralPicGridAdapter.MyClickListener pListener = new ShowStructuralPicGridAdapter.MyClickListener() {
        @Override
        public void myOnClick(final int position, View v) {
            //获得组件
            //在GridView和ListView中，getChildAt ( int position ) 方法中position指的是当前可见区域的第几个元素。
            //如果你要获得GridView或ListView的第n个View，那么position就是n减去第一个可见View的位置
            View view = mGridView.getChildAt(position - mGridView.getFirstVisiblePosition());
            //获得item中的对应的Imageview，用来拍照返回的时候显示照片略缩图
            delFile(position);
        }
    };

    //删除文件
    public void delFile(final int fileOrder) {

        //删除图片列表中被选中的需要删除的元素
        List<String> tempList = new ArrayList<>();
        tempList.addAll(tempPathList);
        tempPathList.clear();
        tempList.remove(fileOrder);
        tempPathList.addAll(tempList);
        adapter.notifyDataSetChanged();

        pathList.remove(fileOrder);//删除待上传中被选中要删除的元素
        imgNum.setText(tempList.size() + "/9");
        //删除相册元素
        LocalMedia needToDelLocalMedia = null;
        if (fileOrder < selectList.size()) {
            needToDelLocalMedia = selectList.get(fileOrder);

            for (int i = selectList.size() - 1; i >= 0; i--) {
                LocalMedia needToDel = selectList.get(i);
                if (needToDel.equals(needToDelLocalMedia)) {
                    selectList.remove(needToDelLocalMedia);
                }
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    if (tempPathList != null) {
                        tempPathList.clear();
                    }

                    if (pathList != null) {
                        pathList.clear();
                    }

                    selectList = PictureSelector.obtainMultipleResult(data);

                    // 例如 LocalMedia 里面返回三种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                    // 如果裁剪并压缩了，已取压缩路径为准，因为是先裁剪后压缩的
                    for (LocalMedia media : selectList) {
                        if (media.isCompressed()) {
                            tempPathList.add(media.getCompressPath());//获取图片地址，用于上传。
                        } else {
                            //这边判断是否是本地视频，如果是，则进行压缩
                            tempPathList.add(media.getPath());//获取图片地址，用于上传。
                        }
                    }

                    if (vedioPathList != null) {
                        tempPathList.addAll(vedioPathList);
                    }

                    pathList.addAll(tempPathList);
                    adapter.notifyDataSetChanged();
                    imgNum.setText(tempPathList.size() + "/9");
                    break;
                case 111:
                    String state = data.getStringExtra("state");
                    nextstate.setText(state);
                    break;
            }
        }
    }

    @OnClick(R.id.rnextstate)
    public void onViewClicked() {
        Intent intent = new Intent(this, MaterialtrackingSettingsActivity.class);
        intent.putExtra("State", nextstate.getText());
        startActivityForResult(intent, 111);
    }
}
