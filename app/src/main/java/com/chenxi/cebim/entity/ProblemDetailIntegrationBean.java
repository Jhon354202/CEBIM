package com.chenxi.cebim.entity;

//问题详情，最外层bean
public class ProblemDetailIntegrationBean<T> {

    //用来装载不同类型的item数据bean
    T t;
    //item数据bean的类型
    int dataType;

    public ProblemDetailIntegrationBean () {
    }

    public ProblemDetailIntegrationBean (T t, int dataType) {
        this.t = t;
        this.dataType = dataType;
    }

    public T getT () {
        return t;
    }

    public void setT (T t) {
        this.t = t;
    }

    public int getDataType () {
        return dataType;
    }

    public void setDataType (int dataType) {
        this.dataType = dataType;
    }

}
