package kr.co.marketlink.fms;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;

import kr.co.marketlink.common.Common;
import kr.co.marketlink.common.ListHelper;

public class ConfigActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    final int LISTITEM_ACCOUNT=1;
    final int LISTITEM_AGREE=2;
    final int LISTITEM_PRIVACY=3;
    final int LISTITEM_INFO=4;
    final int LISTITEM_LOGOUT=5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView tv_toolbar_title=(TextView)toolbar.findViewById(R.id.tv_toolbar_title);
        tv_toolbar_title.setText(R.string.title_config);

        addList();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //add list
    private void addList(){
        ArrayList<ListHelper.SimpleListItem> list=new ArrayList<>();
        list.add(new ListHelper.SimpleListItem(getString(R.string.title_account),LISTITEM_ACCOUNT));
        list.add(new ListHelper.SimpleListItem(getString(R.string.title_agree),LISTITEM_AGREE));
        list.add(new ListHelper.SimpleListItem(getString(R.string.title_privacy),LISTITEM_PRIVACY));
        list.add(new ListHelper.SimpleListItem(getString(R.string.title_info),LISTITEM_INFO));
        list.add(new ListHelper.SimpleListItem(getString(R.string.title_logout),LISTITEM_LOGOUT));
        ListHelper.SimpleListAdapter adapter=new ListHelper.SimpleListAdapter(this,R.layout.ml_simple_listitem,list);
        ListView listView = (ListView) findViewById(R.id.lv_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    //메뉴 클릭
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ListView listView = (ListView) parent;
        ListHelper.SimpleListItem item = (ListHelper.SimpleListItem) listView.getItemAtPosition(position);
        Intent intent=null;
        switch (item.value){
            case LISTITEM_ACCOUNT:intent=new Intent(this,AccountActivity.class);break;
            case LISTITEM_AGREE:intent=new Intent(this,ConfigAgreeActivity.class);break;
            case LISTITEM_PRIVACY:intent=new Intent(this,ConfigPrivacyActivity.class);break;
            case LISTITEM_INFO:intent=new Intent(this,InfoActivity.class);break;
            case LISTITEM_LOGOUT:logout();break;
        }
        if(intent==null)return;
        startActivity(intent);
    }

    //로그아웃
    private void logout(){
        Common.setPreference(getApplicationContext(),"TOKEN","");
        Common.setPreference(getApplicationContext(),"_ID","");
        Common.setPreference(getApplicationContext(),"COMPANY","");
        Toast.makeText(this, R.string.toast_logout, Toast.LENGTH_SHORT).show();
        finish();
    }

}
