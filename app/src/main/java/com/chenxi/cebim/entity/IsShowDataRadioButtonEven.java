package com.chenxi.cebim.entity;

//用于控制资料界面的底部导航栏是否显示的evenBus事件对象
public class IsShowDataRadioButtonEven {
    private String isShowRadioButton;

    public IsShowDataRadioButtonEven(String isShowRadioButton) {
        this.isShowRadioButton=isShowRadioButton;
    }

    public String getIsShowRadioButton() {
        return isShowRadioButton;
    }

    public void setIsShowRadioButton(String isShowRadioButton) {
        this.isShowRadioButton = isShowRadioButton;
    }
}
