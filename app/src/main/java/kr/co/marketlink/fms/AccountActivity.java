package kr.co.marketlink.fms;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import kr.co.marketlink.common.Common;
import kr.co.marketlink.common.Post;
import kr.co.marketlink.ui.MlButton;
import kr.co.marketlink.ui.MlInput;

public class AccountActivity extends AppCompatActivity implements View.OnClickListener, Post.PostHandler{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        Common.log("AccountActivity > onCreate");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView tv_toolbar_title=(TextView)toolbar.findViewById(R.id.tv_toolbar_title);
        tv_toolbar_title.setText(R.string.title_account);

        addEventListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getInfo();
    }

    //getInfo
    private void getInfo(){
        Object[][] params = {
                {"TOKEN",Common.getPreference(getApplicationContext(),"TOKEN")}
        };
        Post.Post(Post.CALLTYPE_ACCOUNT,getString(R.string.url_account),params,this,this);
    }

    @Override
    public void onPostResult(int calltype, JSONObject json) {
        if(calltype== Post.CALLTYPE_ACCOUNT)setAccount(json);
        else if(calltype== Post.CALLTYPE_ACCOUNT_WRITE)saveHandler(json);
    }

    //회원정보 뿌려주기
    private void setAccount(JSONObject json){
        String ERR="";
        String ID="";
        String NAME="";
        String MOBILE="";
        String EMAIL="";
        JSONObject DATA=new JSONObject();
        try{
            ERR=json.getString("ERR");
            DATA=json.getJSONObject("DATA");
            ID=DATA.getString("ID");
            NAME=DATA.getString("NAME");
            MOBILE=DATA.getString("MOBILE");
            EMAIL=DATA.getString("EMAIL");
        } catch (Exception e){}
        if(!ERR.equals("")){
            Toast.makeText(this, ERR, Toast.LENGTH_SHORT).show();
            return;
        }
        TextView tv_id=(TextView)findViewById(R.id.tv_id);
        MlInput mi_name=(MlInput)findViewById(R.id.mi_name);
        MlInput mi_mobile=(MlInput)findViewById(R.id.mi_mobile);
        MlInput mi_email=(MlInput)findViewById(R.id.mi_email);
        tv_id.setText(ID);
        mi_name.setText(NAME);
        mi_name.setTextDisable();
        mi_mobile.setText(MOBILE);
        mi_mobile.setTextDisable();
        mi_email.setText(EMAIL);
        mi_email.setTextDisable();

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
        MlButton btn_cancel=(MlButton)findViewById(R.id.btn_cancel);
        MlButton btn_save=(MlButton)findViewById(R.id.btn_save);
        if(btn_cancel!=null)btn_cancel.setOnClickListener(this);
        if(btn_save!=null)btn_save.setOnClickListener(this);
    }

    //click handler
    public void onClick(View view){
        int id = view.getId();
        if (id==R.id.btn_cancel){
            finish();
        } else if (id==R.id.btn_save){
            save();
        }
    }

    //저장
    private void save(){
        MlInput mi_name=(MlInput)findViewById(R.id.mi_name);
        MlInput mi_mobile=(MlInput)findViewById(R.id.mi_mobile);
        MlInput mi_email=(MlInput)findViewById(R.id.mi_email);
        MlInput mi_pw=(MlInput)findViewById(R.id.mi_pw);
        String NAME=mi_name.getText();
        String MOBILE=mi_mobile.getText();
        String EMAIL=mi_email.getText();
        String PWD=mi_pw.getText();
        Object[][] params = {
                {"TOKEN",Common.getPreference(getApplicationContext(),"TOKEN")}
                ,{"NAME",NAME}
                ,{"MOBILE",MOBILE}
                ,{"EMAIL",EMAIL}
                ,{"PWD",PWD}
        };
        Post.Post(Post.CALLTYPE_ACCOUNT_WRITE,getString(R.string.url_accountWrite),params,this,this);
    }

    //저장완료
    private void saveHandler(JSONObject json){
        String ERR="";
        try{
            ERR=json.getString("ERR");
        } catch (Exception e){}
        if(!ERR.equals("")){
            Toast.makeText(this, ERR, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, getString(R.string.desc_saved), Toast.LENGTH_SHORT).show();
    }

}
