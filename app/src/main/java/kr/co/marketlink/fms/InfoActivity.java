package kr.co.marketlink.fms;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import kr.co.marketlink.common.Common;
import kr.co.marketlink.common.Post;
import kr.co.marketlink.ui.MlButton;

public class InfoActivity extends AppCompatActivity implements View.OnClickListener,Post.PostHandler{

    String UPDATE_URL="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Common.currentActivity=this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        Common.log("InfoActivity > onCreate");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView tv_toolbar_title=(TextView)toolbar.findViewById(R.id.tv_toolbar_title);
        tv_toolbar_title.setText(R.string.title_info);

        //박스 테두리
        LinearLayout ll_box=(LinearLayout)findViewById(R.id.ll_box);
        Common.setBorderBg(ll_box, Color.parseColor("#ffffffff"),Color.parseColor("#ff666666"),1,0);

        //버전
        TextView tv_version=(TextView)findViewById(R.id.tv_version);
        tv_version.setText(getString(R.string.desc_version,BuildConfig.VERSION_NAME));

        addEventListener();
        getInfo();

    }

    //getInfo
    private void getInfo(){
        Object[][] params = {
        };
        Post.Post(Post.CALLTYPE_APPINFO,getString(R.string.url_info),params,this,this);
    }

    @Override
    public void onPostResult(int calltype, JSONObject json) {
        if(calltype== Post.CALLTYPE_APPINFO)setUpdate(json);
    }

    //업데이트 버튼 적용
    private void setUpdate(JSONObject json){
        String RESULT="";
        String ERR="";
        UPDATE_URL="";
        int LATEST_VERSION=0;
        int MIN_VERSION=0;
        try{
            RESULT=json.getString("RESULT");
            ERR=json.getString("ERR");
            UPDATE_URL=json.getString("UPDATE_URL");
            LATEST_VERSION=json.getInt("LATEST_VERSION");
            MIN_VERSION=json.getInt("MIN_VERSION");
        } catch (Exception e){}
        if(!ERR.equals("")){
            Toast.makeText(this, ERR, Toast.LENGTH_SHORT).show();
            return;
        }
        MlButton btn_update=(MlButton)findViewById(R.id.btn_update);
        if(LATEST_VERSION==BuildConfig.VERSION_CODE){
            Toast.makeText(this, getString(R.string.desc_latestVersion), Toast.LENGTH_SHORT).show();
        } else {
            btn_update.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //event listener
    private void addEventListener(){
        MlButton btn_update=(MlButton)findViewById(R.id.btn_update);
        if(btn_update!=null)btn_update.setOnClickListener(this);
    }

    //click handler
    public void onClick(View view){
        int id = view.getId();
        if (id==R.id.btn_update){
            Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(UPDATE_URL));
            startActivity(intent);
        }
    }

}
