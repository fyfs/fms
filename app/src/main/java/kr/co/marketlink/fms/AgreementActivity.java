package kr.co.marketlink.fms;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.w3c.dom.Text;

import kr.co.marketlink.common.Common;
import kr.co.marketlink.common.Post;
import kr.co.marketlink.ui.MlButton;

public class AgreementActivity extends AppCompatActivity implements View.OnClickListener, Post.PostHandler{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);

        //박스 테두리
        LinearLayout ll_box=(LinearLayout)findViewById(R.id.ll_box);
        Common.setBorderBg(ll_box, Color.parseColor("#ffffffff"),Color.parseColor("#ff666666"),1,0);

        addEventListener();
        getAgreement();

    }

    //event listener
    private void addEventListener(){
        MlButton btn_agree=(MlButton)findViewById(R.id.btn_agree);
        MlButton btn_privacy=(MlButton)findViewById(R.id.btn_privacy);
        MlButton btn_agreement=(MlButton)findViewById(R.id.btn_agreement);
        MlButton btn_cancel=(MlButton)findViewById(R.id.btn_cancel);
        if(btn_agree!=null)btn_agree.setOnClickListener(this);
        if(btn_privacy!=null)btn_privacy.setOnClickListener(this);
        if(btn_agreement!=null)btn_agreement.setOnClickListener(this);
        if(btn_cancel!=null)btn_cancel.setOnClickListener(this);
    }

    //getAgreement
    private void getAgreement(){
        Object[][] params = {
        };
        Post.Post(Post.CALLTYPE_AGREE,getString(R.string.url_agree),params,this,this);
    }

    //click handler
    public void onClick(View view){
        TextView tv_agree=(TextView)findViewById(R.id.tv_agree);
        TextView tv_privacy=(TextView)findViewById(R.id.tv_privacy);
        int id = view.getId();
        if (id==R.id.btn_cancel){
            finish();
        } else if(id==R.id.btn_agree){
            tv_agree.setVisibility(View.VISIBLE);
            tv_privacy.setVisibility(View.GONE);
        } else if(id==R.id.btn_privacy){
            tv_agree.setVisibility(View.GONE);
            tv_privacy.setVisibility(View.VISIBLE);
        } else if(id==R.id.btn_agreement){
            Common.flushActivity();
            this.finish();
        }
    }


    @Override
    public void onPostResult(int calltype, JSONObject json) {
        if(calltype== Post.CALLTYPE_AGREE)setAgreement(json);
    }

    //약관 화면에 적용
    private void setAgreement(JSONObject json){
        String RESULT="";
        String ERR="";
        String AGREEMENT="";
        String PRIVACY="";
        try{
            RESULT=json.getString("RESULT");
            ERR=json.getString("ERR");
            AGREEMENT=json.getString("AGREEMENT");
            PRIVACY=json.getString("PRIVACY");
        } catch (Exception e){}
        if(!ERR.equals("")){
            Toast.makeText(this, ERR, Toast.LENGTH_SHORT).show();
            return;
        }
        TextView tv_agree=(TextView)findViewById(R.id.tv_agree);
        TextView tv_privacy=(TextView)findViewById(R.id.tv_privacy);
        tv_agree.setText(AGREEMENT);
        tv_privacy.setText(PRIVACY);
    }

}
