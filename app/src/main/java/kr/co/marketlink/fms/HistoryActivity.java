package kr.co.marketlink.fms;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import kr.co.marketlink.common.Common;
import kr.co.marketlink.common.ListHelper;
import kr.co.marketlink.common.Post;

public class HistoryActivity extends AppCompatActivity implements Post.PostHandler,ListHelper.OnScrollListener.I_bottomScrollHandler{

    String PNUM="";
    String MORE_YN="N";
    int PAGE=1;
    boolean loading=false;
    ArrayList<HistoryItem> list=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Common.currentActivity=this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView tv_toolbar_title=(TextView)toolbar.findViewById(R.id.tv_toolbar_title);
        tv_toolbar_title.setText(R.string.title_history);

        ListView listView = (ListView) findViewById(R.id.lv_list);
        listView.setOnScrollListener(new ListHelper.OnScrollListener(this));
        HistoryListAdapter adapter=new HistoryListAdapter(this,R.layout.listitem_history,list);
        listView.setAdapter(adapter);

        PNUM=Integer.toString(getIntent().getExtras().getInt("PNUM"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        start();
    }

    //시작
    private void start(){
        getList();
    }

    //HistoryItem
    private class HistoryItem {
        public View view;
        public String id="";
        public String startTime="";
        public String endTime="";
        public HistoryItem(String id, long startTime, long endTime) {
            this.id = id;
            this.startTime = Common.longToDatetime(startTime);
            this.endTime = Common.longToDatetime(endTime);
        }
    }

    private class HistoryListAdapter extends ArrayAdapter<HistoryItem> {
        private List<HistoryItem> items;
        private LayoutInflater inflater;

        public HistoryListAdapter(Context context, int resource, List<HistoryItem> objects) {
            super(context, resource, objects);
            items=objects;
            inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                convertView=inflater.inflate(R.layout.listitem_history,null);
            }
            HistoryItem item = items.get(position);
            if(item!=null){
                LinearLayout ll_box=(LinearLayout)convertView.findViewById(R.id.ll_box);
                TextView tv_id=(TextView)convertView.findViewById(R.id.tv_id);
                TextView tv_startTime=(TextView)convertView.findViewById(R.id.tv_startTime);
                TextView tv_endTime=(TextView)convertView.findViewById(R.id.tv_endTime);

                Common.setBorderBg(ll_box, Color.parseColor("#ffffffff"),Color.parseColor("#ff666666"),1,0);
                tv_id.setText(getString(R.string.desc_id,item.id));
                tv_startTime.setText(getString(R.string.desc_startTime,item.startTime));
                tv_endTime.setText(getString(R.string.desc_endTime,item.endTime));
                item.view=convertView;
                convertView.setTag(item);
            }
            return convertView;
        }
    }

    //리스트 불러오기
    private void getList(){
        if(loading)return;
        loading=true;
        Object[][] params = {
                {"TOKEN",Common.getPreference(getApplicationContext(),"TOKEN")}
                ,{"PAGE", Integer.toString(PAGE)}
                ,{"PNUM", PNUM}
        };
        Post.Post(Post.CALLTYPE_FIELD_HISTORY,getString(R.string.url_fieldHistory),params,this,this);
    }

    @Override
    public void onPostResult(int calltype, JSONObject json) {
        if(calltype==Post.CALLTYPE_FIELD_HISTORY)dataloadHandler(json);
    }

    //dataload handler
    private void dataloadHandler(JSONObject json){
        String RESULT="";
        String ERR="";
        JSONArray LIST=null;
        ListView listView = (ListView) findViewById(R.id.lv_list);
        HistoryListAdapter adapter=(HistoryListAdapter)listView.getAdapter();
        try{
            RESULT=json.getString("RESULT");
            ERR=json.getString("ERR");
            MORE_YN=json.getString("MORE_YN");
            LIST=json.getJSONArray("LIST");
            JSONObject DATA=null;
            for(int i=0;i<LIST.length();i++){
                DATA=(JSONObject)LIST.get(i);
                String LID=DATA.getString("LID");
                long DT=DATA.getLong("DT");
                long ET=DATA.getLong("ET");
                adapter.add(new HistoryItem(LID,DT,ET));
            }
        } catch (Exception e){
            if(ERR.equals(""))ERR=e.toString();
        }
        if(!ERR.equals("")){
            Toast.makeText(this, ERR, Toast.LENGTH_SHORT).show();
            return;
        }
        loading=false;
    }

    //바닥 스크롤
    @Override
    public void bottomScroll() {
        if(loading)return;
        if(MORE_YN.equals("N"))return;
        PAGE++;
        getList();
    }
}