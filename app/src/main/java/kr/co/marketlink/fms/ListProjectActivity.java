package kr.co.marketlink.fms;

import android.content.Intent;
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

public class ListProjectActivity extends AppCompatActivity implements View.OnClickListener{

    String title="";
    int pnum=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_project);

        Common.log("ListProjectActivity > onCreate");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView tv_toolbar_title = (TextView) toolbar.findViewById(R.id.tv_toolbar_title);
        tv_toolbar_title.setText(R.string.title_field);

        title=getIntent().getExtras().getString("TITLE");
        pnum=getIntent().getExtras().getInt("PNUM");
        TextView tv_title=(TextView)findViewById(R.id.tv_title);
        tv_title.setText(title);

        addEventListener();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        Common.currentActivity=this;
        super.onResume();
    }

    //event listener
    private void addEventListener(){
        findViewById(R.id.btn_notconfirm_list).setOnClickListener(this);
        findViewById(R.id.btn_confirm_list).setOnClickListener(this);
        findViewById(R.id.btn_complete_list).setOnClickListener(this);
    }

    //click handler
    public void onClick(View view){
        int id = view.getId();
        Intent intent=new Intent(this,ProjectDetailListActivity.class);
        if (id==R.id.btn_notconfirm_list){
            intent.putExtra("TYPE","NOTCONFIRM");
        } else if (id==R.id.btn_confirm_list){
            intent.putExtra("TYPE","CONFIRM");
        } else if (id==R.id.btn_complete_list){
            intent.putExtra("TYPE","COMPLETE");
        }
        intent.putExtra("TITLE",title);
        intent.putExtra("PNUM",pnum);
        if(intent!=null)startActivity(intent);
    }

}
