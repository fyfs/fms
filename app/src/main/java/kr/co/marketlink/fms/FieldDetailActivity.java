package kr.co.marketlink.fms;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;

import kr.co.marketlink.common.Common;
import kr.co.marketlink.common.File;
import kr.co.marketlink.common.GPS;
import kr.co.marketlink.common.Post;
import kr.co.marketlink.ui.MlButton;
import kr.co.marketlink.ui.MlInput;

public class FieldDetailActivity extends AppCompatActivity implements View.OnClickListener, Post.PostHandler{

    String PNUM="";
    String TITLE="";
    boolean finalInserted=false;
    String _ID ="";
    String CONTENTS="";
    String FMS="";
    final int PICK_PHOTO_FOR_AVATAR=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Common.currentActivity=this;
        Common.addActivity(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fielddetail);

        Common.log("FieldDetailActivity > onCreate");

        TITLE=getIntent().getExtras().getString("TITLE");
        PNUM=getIntent().getExtras().getString("PNUM");
        finalInserted=getIntent().getExtras().getBoolean("finalInserted");
        _ID =getIntent().getExtras().getString("_ID");
        CONTENTS=getIntent().getExtras().getString("CONTENTS");
        FMS=getIntent().getExtras().getString("FMS");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView tv_toolbar_title=(TextView)toolbar.findViewById(R.id.tv_toolbar_title);
        tv_toolbar_title.setText(TITLE);

        LinearLayout ll_box=(LinearLayout)findViewById(R.id.ll_box);
        LinearLayout ll_final=(LinearLayout)findViewById(R.id.ll_final);
        LinearLayout ll_notfinal=(LinearLayout)findViewById(R.id.ll_notfinal);
        //LinearLayout ll_doing=(LinearLayout)findViewById(R.id.ll_doing);
        TextView tv_contents=(TextView)findViewById(R.id.tv_contents);
        TextView tv_fms=(TextView)findViewById(R.id.tv_fms);
        tv_contents.setText(CONTENTS);
        tv_fms.setText(FMS);
        if(finalInserted){
            ll_notfinal.setVisibility(View.GONE);
        } else {
            ll_final.setVisibility(View.GONE);
        }
        //ll_doing.setVisibility(View.GONE);

        //박스 테두리
        Common.setBorderBg(ll_box, Color.parseColor("#ffffffff"),Color.parseColor("#ff666666"),1,0);

        addEventListener();

    }

    //GPS 권한 확인
    long clickTime;
    boolean checkGPS(){
        HashMap<String,String> gps= GPS.getGps(getApplicationContext());
        if(gps.get("RESULT").equals("PERMISSION")){
            clickTime=System.currentTimeMillis();
            GPS.getPermission(this);
            return false;
        } else if(gps.get("RESULT").equals("GPSOFF")){
            Toast.makeText(this, getString(R.string.desc_gps), Toast.LENGTH_SHORT).show();
            return false;
        };
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        long now=System.currentTimeMillis();
        if(clickTime+300>now){
            Toast.makeText(this, getString(R.string.desc_permission), Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package",getPackageName(),null));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
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
        MlButton btn_denied=(MlButton)findViewById(R.id.btn_denied);
        MlButton btn_off=(MlButton)findViewById(R.id.btn_off);
        MlButton btn_final=(MlButton)findViewById(R.id.btn_tempsave);
        MlButton btn_start=(MlButton)findViewById(R.id.btn_start);
        MlButton btn_pause=(MlButton)findViewById(R.id.btn_pause);
        MlButton btn_image=(MlButton)findViewById(R.id.btn_image);
        MlButton btn_end=(MlButton)findViewById(R.id.btn_end);
        if(btn_denied!=null)btn_denied.setOnClickListener(this);
        if(btn_off!=null)btn_off.setOnClickListener(this);
        if(btn_final!=null)btn_final.setOnClickListener(this);
        if(btn_start!=null)btn_start.setOnClickListener(this);
        if(btn_pause!=null)btn_pause.setOnClickListener(this);
        if(btn_image!=null)btn_image.setOnClickListener(this);
        if(btn_end!=null)btn_end.setOnClickListener(this);
        btn_image.setVisibility(View.GONE);
    }

    //click handler
    public void onClick(View view){
        if(!checkGPS()){
            return;
        }
        int id = view.getId();
        if (id==R.id.btn_denied){
            save(getString(R.string.FMS_ST_DENIED),"","","","");
        } else if (id==R.id.btn_off){
            save(getString(R.string.FMS_ST_OFF),"","","","");
        } else if (id==R.id.btn_start){
            save(getString(R.string.FMS_ST_START),"","","","");
        } else if (id==R.id.btn_pause){
            save(getString(R.string.FMS_ST_PAUSE),"","","","");
        } else if (id==R.id.btn_tempsave){
            MlInput mi_final=(MlInput)findViewById(R.id.mi_final);
            MlInput mi_fms_lc=(MlInput)findViewById(R.id.mi_fms_lc);
            String fms_ft=mi_final.getText();
            String fms_lc=mi_fms_lc.getText();
            if(fms_ft.equals("")){
                Toast.makeText(this, getString(R.string.desc_needFinal), Toast.LENGTH_SHORT).show();
                return;
            }
            save("",fms_ft,"","",fms_lc);
        } else if (id==R.id.btn_end){
            complete();
        } else if (id==R.id.btn_image){
            pickImage();
        }
    }

    //저장
    void save(String fms_st,String fms_ft, String fms_img, String fms_etc,String fms_lc){
        GPS.getGps(getApplicationContext());
        Object[][] params = {
                {"TOKEN",Common.getPreference(getApplicationContext(),"TOKEN")}
                ,{"PNUM", PNUM}
                ,{"_ID", _ID}
                ,{"FMS_ST", fms_st}
                ,{"FMS_FT", fms_ft}
                ,{"FMS_LC", fms_lc}
                ,{"FMS_IMG", fms_img}
                ,{"FMS_ETC", fms_etc}
                ,{"LAT", GPS.lastLat}
                ,{"LNG", GPS.lastLng}
        };
        Post.Post(Post.CALLTYPE_FIELD_WRITE,getString(R.string.url_fieldWrite),params,this,this);
    }

    @Override
    public void onPostResult(int calltype, JSONObject json) {
        if(calltype== Post.CALLTYPE_FIELD_WRITE)saveHandler(json);
    }

    //저장 후 처리
    private void saveHandler(JSONObject json){
        String ERR="";
        String FMS_ST="";
        String FMS_FT="";
        try{
            ERR=json.getString("ERR");
            FMS_ST=json.getString("FMS_ST");
            FMS_FT=json.getString("FMS_FT");
        } catch (Exception e){}
        if(!ERR.equals("")){
            Toast.makeText(this, ERR, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, getString(R.string.desc_saved), Toast.LENGTH_SHORT).show();
//        if(FMS_ST.equals(getString(R.string.FMS_ST_START)))fieldStart();
//        else Toast.makeText(this, getString(R.string.desc_saved), Toast.LENGTH_SHORT).show();
//        if(FMS_ST.equals(getString(R.string.FMS_ST_END))||FMS_ST.equals(getString(R.string.FMS_ST_DENIED))||FMS_ST.equals(getString(R.string.FMS_ST_OFF))||(!FMS_FT.equals(""))) finish();
    }

    //조사시작
    void fieldStart(){
        /*
        LinearLayout ll_final=(LinearLayout)findViewById(R.id.ll_final);
        LinearLayout ll_notfinal=(LinearLayout)findViewById(R.id.ll_notfinal);
        LinearLayout ll_doing=(LinearLayout)findViewById(R.id.ll_doing);
        LinearLayout ll_denied=(LinearLayout)findViewById(R.id.ll_denied);
        ll_final.setVisibility(View.GONE);
        ll_notfinal.setVisibility(View.GONE);
        ll_denied.setVisibility(View.GONE);
        ll_doing.setVisibility(View.VISIBLE);
        */
        Toast.makeText(this, "조사가 시작되었습니다", Toast.LENGTH_SHORT).show();

    }

    //조사완료
    void complete(){
        //MlInput mi_etc=(MlInput)findViewById(R.id.mi_etc);
        //String fms_etc=mi_etc.getText();
        String fms_etc="";
        save(getString(R.string.FMS_ST_END),"","",fms_etc,"");
    }

    //사진 올리기
    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PHOTO_FOR_AVATAR);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_FOR_AVATAR && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(this, getString(R.string.desc_cannot_image), Toast.LENGTH_SHORT).show();
                return;
            }
            String imgBase64= File.imageToBase64(getApplicationContext(),data.getData(),20);
            if(imgBase64.equals("")){
                Toast.makeText(this,getString(R.string.desc_cannot_image), Toast.LENGTH_SHORT).show();
                return;
            }
            save("","",imgBase64,"","");
        }
    }
}
