package com.chenxi.cebim.entity;

public class QuestionDivideModel {
    String text;

    public QuestionDivideModel () {
    }

    public QuestionDivideModel (String text) {
        this.text = text;
    }

    public String getText () {
        return text;
    }

    public void setText (String text) {
        this.text = text;
    }
}
