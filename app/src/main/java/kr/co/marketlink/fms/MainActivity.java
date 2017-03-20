package kr.co.marketlink.fms;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import kr.co.marketlink.common.Common;
import kr.co.marketlink.common.Post;
import kr.co.marketlink.ui.MlButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,Post.PostHandler{

    boolean viewCreated=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        Common.currentActivity=this;
        super.onResume();
        start();
    }

    //시작
    public void start(){
        //로그인 확인
        if(!isLogin()){
            Intent intent=new Intent(this,LoginActivity.class);
            startActivity(intent);
            return;
        }
        //뷰 생성
        if(!viewCreated){
            setContentView(R.layout.activity_main);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            TextView tv_toolbar_title=(TextView)toolbar.findViewById(R.id.tv_toolbar_title);
            tv_toolbar_title.setText(R.string.title_main);
            addEventListener();
            viewCreated=true;
        }
        Object[][] params = {
                {"TOKEN",Common.getPreference(getApplicationContext(),"TOKEN")}
        };
        Post.Post(Post.CALLTYPE_MAIN,getString(R.string.url_main),params,this,this);
    }

    //로그인 확인
    private boolean isLogin(){
        if(Common.getPreference(getApplicationContext(),"TOKEN").equals(""))return false;
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuitem_config) {
            Intent intent=new Intent(this,ConfigActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //event listener
    private void addEventListener(){
        MlButton btn_field=(MlButton)findViewById(R.id.btn_field);
        MlButton btn_message=(MlButton)findViewById(R.id.btn_message);
        if(btn_field!=null)btn_field.setOnClickListener(this);
        if(btn_message!=null)btn_message.setOnClickListener(this);
    }

    //click handler
    public void onClick(View view){
        int id = view.getId();
        Intent intent=null;
        if (id==R.id.btn_field){
            intent=new Intent(this,ProjectListActivity.class);
        } else if (id==R.id.btn_message){
            intent=new Intent(this,MessageActivity.class);
        }
        if(intent!=null)startActivity(intent);
    }

    @Override
    public void onPostResult(int calltype, JSONObject json) {
        if(calltype==Post.CALLTYPE_MAIN)dataloadHandler(json);
    }

    //dataload handler
    private void dataloadHandler(JSONObject json){
        String RESULT="";
        String ERR="";
        String MESSAGE_CNT="";
        String NAME="";
        JSONObject DATA=null;
        try{
            RESULT=json.getString("RESULT");
            ERR=json.getString("ERR");
            MESSAGE_CNT=json.getString("MESSAGE_CNT");
            DATA=json.getJSONObject("DATA");
            NAME=DATA.getString("NAME");
        } catch (Exception e){
            if(ERR.equals(""))ERR=e.toString();
        }
        if(!ERR.equals("")){
            Toast.makeText(this, ERR, Toast.LENGTH_SHORT).show();
            return;
        }
        //Change title
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView tv_toolbar_title=(TextView)toolbar.findViewById(R.id.tv_toolbar_title);
        tv_toolbar_title.setText(NAME);
        //Change message badge
        MlButton btn_message=(MlButton)findViewById(R.id.btn_message);
        btn_message.setText(getString(R.string.btn_message)+"("+MESSAGE_CNT+")");
    }

}
