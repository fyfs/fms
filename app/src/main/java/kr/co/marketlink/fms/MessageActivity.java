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
import android.widget.AdapterView;
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
import kr.co.marketlink.ui.MlButton;

public class MessageActivity extends AppCompatActivity implements View.OnClickListener, Post.PostHandler{

    final private Context mContext=this;
    boolean notread_only=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Common.currentActivity=this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView tv_toolbar_title=(TextView)toolbar.findViewById(R.id.tv_toolbar_title);
        tv_toolbar_title.setText(R.string.title_message);

        addEventListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getMessage();
    }

    public void getMessage(){
        Object[][] params = {
                {"TOKEN",Common.getPreference(getApplicationContext(),"TOKEN")}
                ,{"NOTREAD_ONLY_YN",notread_only?"Y":"N"}
        };
        Post.Post(Post.CALLTYPE_MESSAGE,getString(R.string.url_message),params,this,this);
    }

    @Override
    public void onPostResult(int calltype, JSONObject json) {
        if(calltype== Post.CALLTYPE_MESSAGE)addList(json);
    }

    private void addList(JSONObject json){
        String ERR="";
        ArrayList<MessageItem>list=new ArrayList<>();
        JSONArray LIST = new JSONArray();
        try{
            ERR=json.getString("ERR");
            LIST=json.getJSONArray("LIST");
            for(int i=0;i<LIST.length();i++){
                JSONObject DATA=(JSONObject)LIST.get(i);
                JSONObject MESSAGE=(JSONObject)DATA.getJSONObject("MESSAGE");
                long DT=MESSAGE.getLong("DT");
                String TITLE=MESSAGE.getString("TITLE");
                boolean read=(MESSAGE.getString("READ_YN").equals("Y"));
                String MSG=MESSAGE.getString("MSG");
                list.add(new MessageItem(DT,Common.longToDatetime(DT),TITLE,MSG,read));
            }
        } catch (Exception e){Common.log(e.toString());}
        if(!ERR.equals("")){
            Toast.makeText(this, ERR, Toast.LENGTH_SHORT).show();
            return;
        }
        MessageListAdapter adapter=new MessageListAdapter(this,R.layout.listitem_message,list);
        ListView listView = (ListView) findViewById(R.id.lv_list);
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

    //MessagenItem
    private class MessageItem {
        public View view;
        public Long DT=0l;
        public String time="";
        public String title="";
        public String message="";
        public boolean read=false;
        public MessageItem(Long DT,String time, String title, String message, boolean read) {
            this.DT=DT;
            this.time = time;
            this.title = title;
            this.message = message;
            this.read = read;
        }
    }

    private class MessageListAdapter extends ArrayAdapter<MessageItem> {
        private List<MessageItem> items;
        private LayoutInflater inflater;

        public MessageListAdapter(Context context, int resource, List<MessageItem> objects) {
            super(context, resource, objects);
            items=objects;
            inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                convertView=inflater.inflate(R.layout.listitem_message,null);
            }
            MessageItem item = items.get(position);
            if(item!=null){
                LinearLayout ll_box=(LinearLayout)convertView.findViewById(R.id.ll_box);
                TextView tv_time=(TextView)convertView.findViewById(R.id.tv_time);
                TextView tv_title=(TextView)convertView.findViewById(R.id.tv_title);
                TextView tv_message=(TextView)convertView.findViewById(R.id.tv_message);
                MlButton btn_confirm=(MlButton)convertView.findViewById(R.id.btn_confirm);
                MlButton btn_delete=(MlButton)convertView.findViewById(R.id.btn_delete);
                Common.setBorderBg(ll_box, Color.parseColor("#ffffffff"),Color.parseColor("#ff666666"),1,0);
                tv_time.setText(item.time);
                tv_title.setText(item.title);
                tv_message.setText(item.message);
                item.view=convertView;
                convertView.setTag(item);
                btn_confirm.setOnClickListener((View.OnClickListener)mContext);
                btn_delete.setOnClickListener((View.OnClickListener)mContext);
                btn_confirm.setVisibility(View.VISIBLE);
                btn_delete.setVisibility(View.GONE);
                tv_message.setTextColor(Color.parseColor("#ffff0000"));
                if(item.read)setRead(convertView);
            }
            return convertView;
        }
    }

    //읽음 표시
    private void setRead(View messageView){
        MlButton btn_confirm=(MlButton)messageView.findViewById(R.id.btn_confirm);
        MlButton btn_delete=(MlButton)messageView.findViewById(R.id.btn_delete);
        btn_confirm.setVisibility(View.GONE);
        btn_delete.setVisibility(View.VISIBLE);
        TextView tv_message=(TextView)messageView.findViewById(R.id.tv_message);
        tv_message.setTextColor(Color.parseColor("#ff3f51b5"));
    }

    //event listener
    private void addEventListener(){
        MlButton btn_totalMessage=(MlButton)findViewById(R.id.btn_totalMessage);
        MlButton btn_notReadMessage=(MlButton)findViewById(R.id.btn_notReadMessage);
        if(btn_totalMessage!=null)btn_totalMessage.setOnClickListener(this);
        if(btn_notReadMessage!=null)btn_notReadMessage.setOnClickListener(this);
    }

    //click handler
    public void onClick(View view){
        int id = view.getId();
        if (id==R.id.btn_totalMessage){
            notread_only=false;
            getMessage();
        } else if (id==R.id.btn_notReadMessage){
            notread_only=true;
            getMessage();
        } else if (id==R.id.btn_confirm){
            confirm((MessageItem)Common.getListitem(view).getTag());
        } else if (id==R.id.btn_delete){
            delete((MessageItem)Common.getListitem(view).getTag());
        }
    }

    //confirm
    private void confirm(MessageItem item){
        View v=item.view;
        setRead(v);
        Object[][] params = {
                {"TOKEN",Common.getPreference(getApplicationContext(),"TOKEN")}
                ,{"DT",Long.toString(item.DT)}
        };
        Post.Post(Post.CALLTYPE_MESSAGE_READ,getString(R.string.url_messageRead),params,this,this);
    }

    //delete
    private void delete(MessageItem item){
        ListView listView = (ListView) findViewById(R.id.lv_list);
        MessageListAdapter adapter=(MessageListAdapter)listView.getAdapter();
        adapter.remove(item);
        Object[][] params = {
                {"TOKEN",Common.getPreference(getApplicationContext(),"TOKEN")}
                ,{"DT",Long.toString(item.DT)}
        };
        Post.Post(Post.CALLTYPE_MESSAGE_DELETE,getString(R.string.url_messageDelete),params,this,this);
    }

}
