package kr.co.marketlink.fms;

import android.content.Context;
import android.content.Intent;
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
import kr.co.marketlink.common.GPS;
import kr.co.marketlink.common.ListHelper;
import kr.co.marketlink.common.Post;

public class FieldListActivity extends AppCompatActivity implements Post.PostHandler,ListHelper.OnScrollListener.I_bottomScrollHandler, View.OnClickListener{

    final private Context mContext=this;
    String PNUM="";
    String MORE_YN="N";
    String FINAL_ONLY_YN="N";
    String TITLE="";
    int PAGE=1;
    boolean loading=false;
    JSONArray FIELDNAME=new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Common.currentActivity=this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fieldlist);

        Common.log("FieldListActivity > onCreate");

        TITLE=getIntent().getExtras().getString("TITLE");
        PNUM=Integer.toString(getIntent().getExtras().getInt("PNUM"));
        FINAL_ONLY_YN=getIntent().getExtras().getString("GUBUN");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView tv_toolbar_title=(TextView)toolbar.findViewById(R.id.tv_toolbar_title);
        tv_toolbar_title.setText(TITLE);

        ArrayList<FieldListItem> list=new ArrayList<>();
        ListView listView = (ListView) findViewById(R.id.lv_list);
        listView.setOnScrollListener(new ListHelper.OnScrollListener(this));
        FieldListAdapter adapter=new FieldListAdapter(this,R.layout.listitem_fieldlist,list);
        listView.setAdapter(adapter);


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
        ListView listView = (ListView) findViewById(R.id.lv_list);
        FieldListAdapter adapter=(FieldListAdapter)listView.getAdapter();
        adapter.clear();
        PAGE=1;
        start();
    }

    //시작
    private void start(){
        getList();
    }

    //FieldListItem
    private class FieldListItem {
        public View view;
        public String id="";
        public String contents="";
        public boolean finalInserted=false;
        public String fms="";
        public FieldListItem(String id, String contents, boolean finalInserted, String fms) {
            this.id = id;
            this.contents = contents;
            this.finalInserted = finalInserted;
            this.fms = fms;
        }
    }

    private class FieldListAdapter extends ArrayAdapter<FieldListItem> {
        private List<FieldListItem> items;
        private LayoutInflater inflater;

        public FieldListAdapter(Context context, int resource, List<FieldListItem> objects) {
            super(context, resource, objects);
            items=objects;
            inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            boolean isNew=false;
            FieldListItem item = items.get(position);
            if(convertView==null){
                convertView=inflater.inflate(R.layout.listitem_fieldlist,null);
                isNew=true;
            }
            if(item!=null){
                LinearLayout ll_box=(LinearLayout)convertView.findViewById(R.id.ll_box);
                TextView tv_contents=(TextView)convertView.findViewById(R.id.tv_contents);
                TextView tv_fms=(TextView)convertView.findViewById(R.id.tv_fms);

                if(isNew){
                    Common.setBorderBg(ll_box, Color.parseColor("#ffffffff"),Color.parseColor("#ff666666"),1,0);
                    convertView.setOnClickListener((View.OnClickListener)mContext);
                }
                tv_contents.setText(item.contents);
                if(item.fms.equals("")){
                    tv_fms.setVisibility(View.GONE);
                } else {
                    tv_fms.setText(item.fms);
                }
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
                ,{"GUBUN", FINAL_ONLY_YN}
        };
        Post.Post(Post.CALLTYPE_FIELD_LIST,getString(R.string.url_fieldList),params,this,this);
    }

    @Override
    public void onPostResult(int calltype, JSONObject json) {
        if(calltype==Post.CALLTYPE_FIELD_LIST)dataloadHandler(json);
    }

    //dataload handler
    private void dataloadHandler(JSONObject json){
        String RESULT="";
        String ERR="";
        JSONArray LIST=null;
        ListView listView = (ListView) findViewById(R.id.lv_list);
        FieldListAdapter adapter=(FieldListAdapter)listView.getAdapter();
        try{
            RESULT=json.getString("RESULT");
            ERR=json.getString("ERR");
            MORE_YN=json.getString("MORE_YN");
            LIST=json.getJSONArray("LIST");
            //FIELDNAME은 첫 페이지일 때 처리
            if(PAGE==1){
                FIELDNAME=json.getJSONArray("FIELDNAME");
            }
            JSONObject DATA=null;
            for(int i=0;i<LIST.length();i++){
                DATA=(JSONObject)LIST.get(i);
                String _id=DATA.getString("_id");
                String contents="";
                String addBr="";
                String fms_st="";
                String fms_ft="";
                String fms="";
                boolean finalInserted=false;
                for(int f=0;f<FIELDNAME.length();f++){
                    JSONObject field=(JSONObject)FIELDNAME.get(f);
                    try{
                        contents+=addBr+field.getString("_id")+":"+DATA.getString(field.getString("_id"));
                        addBr="\n";
                    } catch(Exception e){}
                }
                if(DATA.has("FMS_FT"))finalInserted=!(DATA.getString("FMS_FT").equals(""));
                if(DATA.has("FMS_ST"))fms_st=DATA.getString("FMS_ST");
                if(DATA.has("FMS_FT"))fms_ft=DATA.getString("FMS_FT");
                if(fms_st.equals(""))fms_st="미컨택";
                if(!fms_ft.equals(""))fms+="확정일자:"+fms_ft;
                adapter.add(new FieldListItem(_id,contents,finalInserted,fms));
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

    @Override
    public void onClick(View v) {
        int id=v.getId();
        switch (id){
            case R.id.rl_fieldlistitem:itemClick((FieldListItem)v.getTag());break;
        }
    }

    //아이템 클릭
    void itemClick(FieldListItem item){
        Intent intent=new Intent(this,FieldDetailActivity.class);
        intent.putExtra("PNUM",PNUM);
        intent.putExtra("TITLE",TITLE);
        intent.putExtra("finalInserted",item.finalInserted);
        intent.putExtra("_ID",item.id);
        intent.putExtra("CONTENTS",item.contents);
        startActivity(intent);
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