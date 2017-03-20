package kr.co.marketlink.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import kr.co.marketlink.common.Common;
import kr.co.marketlink.fms.R;

/**
 * Created by yangjaesang on 2017. 1. 15..
 */

public class MlButton extends LinearLayout implements View.OnTouchListener{

    final int defaultTextColor=Color.parseColor("#ffffffff");
    final int defaultBackgroundColor=Color.parseColor("#ff3f51b5");
    final int defaultBorderColor=Color.parseColor("#ff666666");
    final int defaultBorderWidth=1;
    final int defaultCornerRadius=0;

    TextView tv_center;

    //******** 생성자 ********
    public MlButton(Context context) {
        super(context);
        initView();
        addEventListener();
    }

    public MlButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
        getAttrs(attrs);
        addEventListener();
    }

    public MlButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        getAttrs(attrs,defStyleAttr);
        addEventListener();
    }
    //******** 생성자 ********

    //뷰 초기화
    private void initView() {

        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);
        View v = li.inflate(R.layout.ml_button, this, false);
        addView(v);

        tv_center=(TextView)findViewById(R.id.tv_center);

    }

    //이벤트 등록
    private void addEventListener(){
        this.setClickable(true);
        this.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float alpha=this.getAlpha();
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:alpha=0.5f;break;
            case MotionEvent.ACTION_UP:alpha=1;break;
            case MotionEvent.ACTION_CANCEL:alpha=1;break;
        }
        this.setAlpha(alpha);
        return false;
    }

    //속성 적용
    private void getAttrs(AttributeSet attrs){
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MlButton);
        setTypeArray(typedArray);
    }
    private void getAttrs(AttributeSet attrs,int defStyleAttr){
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MlButton, defStyleAttr, 0);
        setTypeArray(typedArray);
    }
    private void setTypeArray(TypedArray typedArray) {

        String text = typedArray.getString(R.styleable.MlButton_ml_text);
        int backgroundColor=typedArray.getColor(R.styleable.MlButton_ml_backgroundColor,defaultBackgroundColor);
        int textColor=typedArray.getColor(R.styleable.MlButton_ml_textColor,defaultTextColor);
        int borderColor=typedArray.getColor(R.styleable.MlButton_ml_borderColor,defaultBorderColor);
        int borderWidth=typedArray.getColor(R.styleable.MlButton_ml_borderWidth,defaultBorderWidth);
        int cornerRadius=typedArray.getColor(R.styleable.MlButton_ml_cornerRadius,defaultCornerRadius);

        tv_center.setText(text);
        tv_center.setTextColor(textColor);

        Common.setBorderBg((View)this,backgroundColor,borderColor,borderWidth,cornerRadius);

        typedArray.recycle();
    }

    //버튼 이름 변경
    public void setText(String text){
        tv_center.setText(text);
    }
    public void setTextColor(int color){
        tv_center.setTextColor(color);
    }

}
