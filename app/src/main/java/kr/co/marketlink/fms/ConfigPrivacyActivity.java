package kr.co.marketlink.fms;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import kr.co.marketlink.common.Common;
import kr.co.marketlink.common.Post;

public class ConfigPrivacyActivity extends AppCompatActivity implements Post.PostHandler{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Common.currentActivity=this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configprivacy);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView tv_toolbar_title=(TextView)toolbar.findViewById(R.id.tv_toolbar_title);
        tv_toolbar_title.setText(R.string.title_privacy);

        //박스 테두리
        LinearLayout ll_box=(LinearLayout)findViewById(R.id.ll_box);
        Common.setBorderBg(ll_box, Color.parseColor("#ffffffff"),Color.parseColor("#ff666666"),1,0);

        getAgreement();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //getAgreement
    private void getAgreement(){
        Object[][] params = {
        };
        Post.Post(Post.CALLTYPE_AGREE,getString(R.string.url_agree),params,this,this);
    }

    @Override
    public void onPostResult(int calltype, JSONObject json) {
        if(calltype== Post.CALLTYPE_AGREE)setAgreement(json);
    }

    //약관 화면에 적용
    private void setAgreement(JSONObject json){
        String ERR="";
        String PRIVACY="";
        try{
            ERR=json.getString("ERR");
            PRIVACY=json.getString("PRIVACY");
        } catch (Exception e){}
        if(!ERR.equals("")){
            Toast.makeText(this, ERR, Toast.LENGTH_SHORT).show();
            return;
        }
        TextView tv_contents=(TextView)findViewById(R.id.tv_contents);
        tv_contents.setText(PRIVACY);
    }

}
