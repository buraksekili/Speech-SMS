package com.example.texttosms;

public class ExampleItem {
    private String mText1;
    private String mText2;

    public ExampleItem(String text1, String text2) {
        mText1 = text1;
        mText2 = text2;
    }

    public String getText1(){
        return mText1;
    }

    public String getText2(){
        return mText2;
    }

    public void setText1(String text1) {mText1 = text1;}

    public void setText2(String text2) {mText2 = text2;}
}
