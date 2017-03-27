package kr.co.marketlink.fms;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONObject;

import java.util.ArrayList;

import kr.co.marketlink.common.Common;
import kr.co.marketlink.common.Post;
import kr.co.marketlink.ui.MlButton;
import kr.co.marketlink.ui.MlInput;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,Post.PostHandler{

    boolean hasTargetActivity=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(getIntent().getExtras()!=null) hasTargetActivity =getIntent().getExtras().getBoolean("HAS_TARGET_ACTIVITY",false);
        if(!hasTargetActivity)Common.addActivity(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Common.log("LoginActivity > onCreate");

        addEventListener();
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                TelephonyManager mTelephonyMgr;

                mTelephonyMgr = (TelephonyManager)getSystemService(getApplicationContext().TELEPHONY_SERVICE);
                //String imsi = mTelephonyMgr.getSubscriberId();
                String phnNo=mTelephonyMgr.getLine1Number();
                Common.log(phnNo);
                kr.co.marketlink.ui.MlInput editText = (kr.co.marketlink.ui.MlInput)findViewById(R.id.mi_id);
                final kr.co.marketlink.ui.MlInput editPw = (kr.co.marketlink.ui.MlInput)findViewById(R.id.mi_pw);
                //연락처 형식에 맞게 변경
                String mobile = "0"+phnNo.replace("+82","");
                editText.setText(mobile);
                Common.log("mobile : "+mobile);
                //// TODO: 2017. 3. 22. 시작시 아래 주석 해제할것 
                //editText.setTextDisable();

                editPw.requestFocus();
            }
            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {

            }

        };
        new TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setRationaleMessage("사진/파일을 전송하기 위해서는 액세스 권한이 필요합니다")
                .setDeniedMessage("엑세스 권한을 거부할 경우, 해당 서비스를 이용하실 수 없습니다. [설정] > [권한]에서 허용으로 변경해 주시기 바랍니다.")
                .setGotoSettingButtonText("변경하기")
                .setPermissions(Manifest.permission.READ_PHONE_STATE)
                .check();





    }

    //event listener
    private void addEventListener(){
        MlButton btn_login=(MlButton)findViewById(R.id.btn_login);
        if(btn_login!=null)btn_login.setOnClickListener(this);
    }

    //click handler
    public void onClick(View view){
        int id = view.getId();
        if (id==R.id.btn_login)login();
    }

    //login
    void login(){
        MlInput mi_id=(MlInput)findViewById(R.id.mi_id);
        MlInput mi_pw=(MlInput)findViewById(R.id.mi_pw);
        String FCM_TOKEN= FirebaseInstanceId.getInstance().getToken();
        Object[][] params = {
                {"_ID",mi_id.getText()}
                ,{"PWD",mi_pw.getText()}
                ,{"FCM_TOKEN",FCM_TOKEN}
        };
        Post.Post(Post.CALLTYPE_LOGIN,getString(R.string.url_login),params,this,this);
    }

    //postHandler
    @Override
    public void onPostResult(int calltype, JSONObject json) {
        if(calltype==Post.CALLTYPE_LOGIN)loginHandler(json);
    }

    //로그인 처리
    void loginHandler(JSONObject json){
        String RESULT="";
        String ERR="";
        String TOKEN="";
        String USERTYPE="";
        TextView tv_warning=(TextView)findViewById(R.id.tv_warning);
        try{
            RESULT=json.getString("RESULT");
            ERR=json.getString("ERR");
            TOKEN=json.getString("TOKEN");
            USERTYPE=json.getString("USERTYPE");
            if(ERR.equals("")&&(!USERTYPE.equals("iv")))ERR=getString(R.string.desc_cannotLogin);
        } catch (Exception e){}
        if(!ERR.equals("")){
            tv_warning.setText(ERR);
            tv_warning.setVisibility(View.VISIBLE);
            return;
        }
        MlInput mi_pw=(MlInput)findViewById(R.id.mi_pw);
        mi_pw.setText("");
        Common.setPreference(getApplicationContext(),"TOKEN",TOKEN);
        if(hasTargetActivity){
            finish();
            return;
        }
        Intent intent=new Intent(LoginActivity.this,MainActivity.class);
//        Intent intent=new Intent(LoginActivity.this,AgreementActivity.class);
        startActivity(intent);
    }
}