package kr.co.marketlink.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;

import kr.co.marketlink.common.Common;
import kr.co.marketlink.fms.R;

/**
 * Created by yangjaesang on 2017. 1. 15..
 */

public class MlInput extends LinearLayout{

    final int defaultBackgroundColor=Color.parseColor("#ffffffff");
    final int defaultBorderColor=Color.parseColor("#ff666666");
    final int defaultBorderWidth=2;
    final int defaultCornerRadius=0;

    EditText et_center;

    //******** 생성자 ********
    public MlInput(Context context) {
        super(context);
        initView();
    }

    public MlInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
        getAttrs(attrs);
    }

    public MlInput(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        getAttrs(attrs,defStyleAttr);
    }
    //******** 생성자 ********

    //뷰 초기화
    private void initView() {
        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);
        View v = li.inflate(R.layout.ml_input, this, false);
        addView(v);

        et_center=(EditText)findViewById(R.id.et_center);
    }

    //테두리와 배경색 적용
    void setBorderBg(int bgColor, int borderColor, int borderWidth, int cornerRadius){
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(bgColor); // Changes this drawbale to use a single color instead of a gradient
        gd.setCornerRadius(cornerRadius);
        gd.setStroke(borderWidth, borderColor);
        if(Build.VERSION.SDK_INT>=16)this.setBackground(gd);
    }

    //속성 적용
    private void getAttrs(AttributeSet attrs){
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MlInput);
        setTypeArray(attrs, typedArray);
    }
    private void getAttrs(AttributeSet attrs,int defStyleAttr){
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MlInput, defStyleAttr, 0);
        setTypeArray(attrs, typedArray);
    }

    private void setTypeArray(AttributeSet attrs, TypedArray typedArray) {
        String hint = getContext().getString(attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android","hint",R.string.blank));
        if(hint.equals(""))hint=attrs.getAttributeValue("http://schemas.android.com/apk/res/android","hint");
        int inputType = attrs.getAttributeIntValue("http://schemas.android.com/apk/res/android","inputType",1);
        int backgroundColor=typedArray.getColor(R.styleable.MlInput_ml_backgroundColor,defaultBackgroundColor);
        int borderColor=typedArray.getColor(R.styleable.MlInput_ml_borderColor,defaultBorderColor);
        int borderWidth=typedArray.getColor(R.styleable.MlInput_ml_borderWidth,defaultBorderWidth);
        int cornerRadius=typedArray.getColor(R.styleable.MlInput_ml_cornerRadius,defaultCornerRadius);

        et_center.setHint(hint);
        if (inputType != EditorInfo.TYPE_NULL)et_center.setInputType(inputType);
        setBorderBg(backgroundColor,borderColor,borderWidth,cornerRadius);

        typedArray.recycle();
    }

    //값 꺼내기
    public String getText(){
        return et_center.getText().toString();
    }

    //값 적용
    public void setText(String txt){
        et_center.setText(txt);
    }

    //EditText disable
    public void setTextDisable(){
        et_center.setClickable(false);
        et_center.setEnabled(false);
        et_center.setFocusable(false);
        et_center.setFocusableInTouchMode(false);
    }
}