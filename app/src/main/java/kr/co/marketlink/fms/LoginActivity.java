package kr.co.marketlink.fms;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

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
        addEventListener();
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