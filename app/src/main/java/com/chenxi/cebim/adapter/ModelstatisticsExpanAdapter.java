package com.chenxi.cebim.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.chenxi.cebim.R;
import com.chenxi.cebim.entity.Modelstatistics;
import com.chenxi.cebim.entity.ModelstatisticsFather;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.refactor.library.SmoothCheckBox;

public class ModelstatisticsExpanAdapter extends BaseExpandableListAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private List<ModelstatisticsFather> modelstatisticsList = new ArrayList<>();
    private List<String> checkedChildren = new ArrayList<>();//已选中的子列表项
    private Map<String, Integer> groupCheckedStateMap = new HashMap<>();//父列表的选中状态  value  ----- 1（全选中）  2（部分选中）  3（未选中）

    public ModelstatisticsExpanAdapter(Context context, List<ModelstatisticsFather> modelstatisticsList) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.modelstatisticsList = modelstatisticsList;
    }

    private class HolderViewFather {
        ImageView menu_icon;
        TextView group_title;
        SmoothCheckBox material_group_cb;
        TextView model_num;
    }

    @Override
    public int getGroupCount() {
        return modelstatisticsList.size();
    }


    @Override
    public Object getGroup(int groupPosition) {
        return modelstatisticsList.get(groupPosition);
    }


    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }


    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        HolderViewFather holderViewFather;
        if (convertView == null) {
            holderViewFather = new HolderViewFather();
            convertView = layoutInflater.inflate(R.layout.expand_group_item, parent, false);
            holderViewFather.group_title = convertView.findViewById(R.id.group_title);
            holderViewFather.material_group_cb = convertView.findViewById(R.id.material_group_cb);
            holderViewFather.menu_icon = convertView.findViewById(R.id.menu_icon);
            holderViewFather.model_num = convertView.findViewById(R.id.model_num);
            convertView.setTag(holderViewFather);
        } else {
            holderViewFather = (HolderViewFather) convertView.getTag();
        }
        if (isExpanded) {
            holderViewFather.menu_icon.setImageResource(R.drawable.menu_open);
        } else {
            holderViewFather.menu_icon.setImageResource(R.drawable.menu_close);
        }
        holderViewFather.group_title.setText(modelstatisticsList.get(groupPosition).getTitle());
        holderViewFather.model_num.setText(modelstatisticsList.get(groupPosition).getDownloadNum() + "/" + modelstatisticsList.get(groupPosition).getAllNum());

        if (modelstatisticsList.get(groupPosition).isEdit()) {
            holderViewFather.material_group_cb.setVisibility(View.VISIBLE);

        } else {
            holderViewFather.material_group_cb.setVisibility(View.GONE);
        }

        if (modelstatisticsList.get(groupPosition).isChoose()) {
            holderViewFather.material_group_cb.setChecked(true);
        } else {
            holderViewFather.material_group_cb.setChecked(false);
        }
        holderViewFather.material_group_cb.setOnClickListener(new GroupSmoothCheckBoxClickListener(groupPosition));
        return convertView;
    }

    public void flashData(List<ModelstatisticsFather> datas) {
        this.modelstatisticsList = datas;
        this.notifyDataSetChanged();
    }

    private class HolderViewChild {
        private TextView model_name;
        private SmoothCheckBox material_child_cb;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return modelstatisticsList.get(groupPosition).getModelstatisticsList().size();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return modelstatisticsList.get(groupPosition).getModelstatisticsList().get(childPosition);
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        HolderViewChild childrenView;
        if (convertView == null) {
            childrenView = new HolderViewChild();
            convertView = layoutInflater.inflate(R.layout.expand_child_item, parent, false);
            childrenView.model_name = convertView.findViewById(R.id.model_name);
            childrenView.material_child_cb = convertView.findViewById(R.id.material_child_cb);
            convertView.setTag(childrenView);
        } else {
            childrenView = (HolderViewChild) convertView.getTag();
        }
        childrenView.model_name.setText(modelstatisticsList.
                get(groupPosition).getModelstatisticsList().get(childPosition)
                .getModelName());
        if (modelstatisticsList.get(groupPosition).getModelstatisticsList().get(childPosition).isEdit()) {
            childrenView.material_child_cb.setVisibility(View.VISIBLE);
        } else {
            childrenView.material_child_cb.setVisibility(View.GONE);
        }

        if (modelstatisticsList.get(groupPosition).getModelstatisticsList().get(childPosition).isChoose()) {
            childrenView.material_child_cb.setChecked(true);
        } else {
            childrenView.material_child_cb.setChecked(false);
        }
        childrenView.material_child_cb.setOnClickListener(new ChildSmoothCheckBoxClickListener(groupPosition, childPosition));
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    /*
     * 父节点的SmoothCheckBox点击事件
     * */
    class GroupSmoothCheckBoxClickListener implements View.OnClickListener {
        private int mGroupPosition;

        public GroupSmoothCheckBoxClickListener(int groupPosition) {
            mGroupPosition = groupPosition;
        }

        @Override
        public void onClick(View v) {
            modelstatisticsList.get(mGroupPosition).toggle();
            int childrenCount = modelstatisticsList.get(mGroupPosition).getChildCount();
            boolean groupIsChecked = modelstatisticsList.get(mGroupPosition).isChoose();
            for (int i = 0; i < childrenCount; i++) {
                modelstatisticsList.get(mGroupPosition).getChildItem(i).setChoose(groupIsChecked);
            }
            notifyDataSetChanged();
        }
    }

    /*
     *子列表的SmoothCheckBox点击事件
     * */
    class ChildSmoothCheckBoxClickListener implements View.OnClickListener {
        private int mGroupPosition;
        private int mChildPosition;

        public ChildSmoothCheckBoxClickListener(int groupPosition, int childPosition) {
            this.mChildPosition = childPosition;
            this.mGroupPosition = groupPosition;
        }

        @Override
        public void onClick(View v) {
            List<Modelstatistics> list = new ArrayList<>();
            modelstatisticsList.get(mGroupPosition).getChildItem(mChildPosition).toggle();
            int childrenCount = modelstatisticsList.get(mGroupPosition).getChildCount();
            boolean childrenAllIsChecked = false;
            for (int i = 0; i < childrenCount; i++) {
                if (modelstatisticsList.get(mGroupPosition).getChildItem(i).isChoose()) {
                    list.add(modelstatisticsList.get(mGroupPosition).getChildItem(i));
                }
            }
            if (list.size() == childrenCount) {
                childrenAllIsChecked = true;
            } else {
                childrenAllIsChecked = false;
            }
            modelstatisticsList.get(mGroupPosition).setChoose(childrenAllIsChecked);
            notifyDataSetChanged();
        }
    }
}
