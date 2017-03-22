package kr.co.marketlink.fms;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kr.co.marketlink.common.Common;
import kr.co.marketlink.common.GPS;
import kr.co.marketlink.common.Post;
import kr.co.marketlink.ui.MlButton;

public class ProjectListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, Post.PostHandler {

    final private Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Common.currentActivity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_field);

        Common.log("ProjectListActivity > onCreate");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView tv_toolbar_title = (TextView) toolbar.findViewById(R.id.tv_toolbar_title);
        tv_toolbar_title.setText(R.string.title_field);

        MlButton btn_doingField = (MlButton) findViewById(R.id.btn_doingField);
        MlButton btn_totalField = (MlButton) findViewById(R.id.btn_totalField);
        Common.setBorderBg(btn_doingField
                , Common.getColor(getApplicationContext(), android.R.color.background_light)
                , Common.getColor(getApplicationContext(), android.R.color.darker_gray)
                , 1, 0);
        Common.setBorderBg(btn_totalField
                , Common.getColor(getApplicationContext(), android.R.color.background_light)
                , Common.getColor(getApplicationContext(), android.R.color.darker_gray)
                , 1, 0);
        btn_doingField.setTextColor(Common.getColor(getApplicationContext(), android.R.color.darker_gray));
        btn_totalField.setTextColor(Common.getColor(getApplicationContext(), android.R.color.darker_gray));

        ((ListView)findViewById(R.id.lv_listDoing)).setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        start();
    }

    //시작
    private void start() {
        getList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //set list
    private void setList(ArrayList<FieldItem> list, ListView listView) {
        FieldListAdapter adapter = new FieldListAdapter(this, R.layout.listitem_field, list);
        listView.setAdapter(adapter);
    }

    //FieldListItem
    private class FieldItem {
        public View view;
        public int pnum = 0;
        public String title = "";
        public int completeCnt = 0;
        public boolean img = false;
        public int status = 0;
        public String type = "FF";

        public FieldItem(int pnum, String title, int completeCnt, boolean img, int status, String type) {
            this.pnum = pnum;
            this.title = title;
            this.completeCnt = completeCnt;
            this.img = img;
            this.status = status;
            this.type = type;
        }
    }

    private class FieldListAdapter extends ArrayAdapter<FieldItem> {
        private List<FieldItem> items;
        private LayoutInflater inflater;

        public FieldListAdapter(Context context, int resource, List<FieldItem> objects) {
            super(context, resource, objects);
            items = objects;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FieldItem item = items.get(position);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.listitem_project_list, null);
            }
            if (item != null) {
                LinearLayout ll_wrap = (LinearLayout) convertView.findViewById(R.id.ll_wrap);
                TextView tv_num = (TextView) convertView.findViewById(R.id.tv_num);
                TextView tv_pname = (TextView) convertView.findViewById(R.id.tv_pname);
                TextView tv_type = (TextView) convertView.findViewById(R.id.tv_type);
                if (position % 2 == 1)
                    ll_wrap.setBackgroundColor(Common.getColor(getApplicationContext(), android.R.color.white));

                tv_num.setText(Integer.toString(position + 1));
                tv_pname.setText(item.title);
                tv_type.setText(item.type);
                item.view = convertView;
                convertView.setTag(item);
            }
            return convertView;
        }
    }

    //GPS 권한 확인
    long clickTime;

    boolean checkGPS() {
        HashMap<String, String> gps = GPS.getGps(getApplicationContext());
        if (gps.get("RESULT").equals("PERMISSION")) {
            clickTime = System.currentTimeMillis();
            GPS.getPermission(this);
            return false;
        } else if (gps.get("RESULT").equals("GPSOFF")) {
            Toast.makeText(this, getString(R.string.desc_gps), Toast.LENGTH_SHORT).show();
            return false;
        }
        ;
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        long now = System.currentTimeMillis();
        if (clickTime + 300 > now) {
            Toast.makeText(this, getString(R.string.desc_permission), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    //click handler
    public void onClick(View view) {
        if (!checkGPS()) {
            return;
        }
        int id = view.getId();
        if (id == R.id.btn_start) {
            start((FieldItem) Common.getListitem(view).getTag());
        } else if (id == R.id.btn_history) {
            history((FieldItem) Common.getListitem(view).getTag());
        } else if (id == R.id.btn_contactList) {
            contactList((FieldItem) Common.getListitem(view).getTag());
        } else if (id == R.id.btn_completeList) {
            completeList((FieldItem) Common.getListitem(view).getTag());
        }
    }

    //리스트 불러오기
    private void getList() {
        Object[][] params = {
                {"TOKEN", Common.getPreference(getApplicationContext(), "TOKEN")}
        };
        Post.Post(Post.CALLTYPE_FIELD, getString(R.string.url_field), params, this, this);
    }

    @Override
    public void onPostResult(int calltype, JSONObject json) {
        if (calltype == Post.CALLTYPE_FIELD) dataloadHandler(json);
        else if (calltype == Post.CALLTYPE_FIELDSURVEY_WRITE) saveHandler(json);
    }

    //dataload handler
    private void dataloadHandler(JSONObject json) {
        String RESULT = "";
        String ERR = "";
        JSONArray LIST = null;
        ArrayList<FieldItem> listDoing = new ArrayList<>();
        ArrayList<FieldItem> listFinished = new ArrayList<>();
        try {
            RESULT = json.getString("RESULT");
            ERR = json.getString("ERR");
            LIST = json.getJSONArray("LIST");
            JSONObject DATA = null;
            for (int i = 0; i < LIST.length(); i++) {
                DATA = (JSONObject) LIST.get(i);
                int PNUM = DATA.getInt("PNUM");
                String PNAME = DATA.getString("PNAME");
                boolean IMG = false;
                //boolean IMG=DATA.getString("IMG_YN").equals("Y");
                String TYPE = DATA.getString("TYPE");
                int STATUS = DATA.getInt("STATUS");
                JSONArray IV = DATA.getJSONArray("IV");
                JSONObject IV0 = (JSONObject) IV.get(0);
                int COMPLETE_CNT = 0;
                if (IV0.has("COMPLETE_CNT")) COMPLETE_CNT = IV0.getInt("COMPLETE_CNT");
                if (STATUS == 1) {
                    listDoing.add(new FieldItem(PNUM, PNAME, COMPLETE_CNT, IMG, STATUS, TYPE));
                } else {
                    listFinished.add(new FieldItem(PNUM, PNAME, COMPLETE_CNT, IMG, STATUS, TYPE));
                }
            }
        } catch (Exception e) {
            if (ERR.equals("")) ERR = e.toString();
        }
        setList(listDoing, (ListView) findViewById(R.id.lv_listDoing));
        setList(listFinished, (ListView) findViewById(R.id.lv_listFinished));
        if (!ERR.equals("")) {
            Toast.makeText(this, ERR, Toast.LENGTH_SHORT).show();
            return;
        }
    }

    //start
    private FieldItem startFielditem = null;

    private void start(FieldItem item) {
        GPS.getGps(getApplicationContext());
        Object[][] params = {
                {"TOKEN", Common.getPreference(getApplicationContext(), "TOKEN")}
                , {"PNUM", item.pnum}
                , {"LID", ""}
                , {"FMS_ST", getString(R.string.FMS_ST_START)}
                , {"FMS_IMG", ""}
                , {"FMS_ETC", ""}
                , {"LAT", GPS.lastLat}
                , {"LNG", GPS.lastLng}
        };
        startFielditem = item;
        Post.Post(Post.CALLTYPE_FIELDSURVEY_WRITE, getString(R.string.url_fieldSurveyWrite), params, this, this);
    }

    //저장 후 처리
    private void saveHandler(JSONObject json) {
        String ERR = "";
        String LID = "";
        try {
            ERR = json.getString("ERR");
            LID = json.getString("LID");
        } catch (Exception e) {
        }
        if (startFielditem == null) ERR = getString(R.string.desc_cannot_start);
        if (!ERR.equals("")) {
            Toast.makeText(this, ERR, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, FieldSurveyActivity.class);
        intent.putExtra("PNUM", Integer.toString(startFielditem.pnum));
        intent.putExtra("TITLE", startFielditem.title);
        intent.putExtra("LID", LID);
        startActivity(intent);
    }

    //history
    private void history(FieldItem item) {
        Intent intent = new Intent(this, HistoryActivity.class);
        intent.putExtra("PNUM", item.pnum);
        startActivity(intent);
    }

    //contactList
    private void contactList(FieldItem item) {
        Intent intent = new Intent(this, FieldListActivity.class);
        intent.putExtra("PNUM", item.pnum);
        intent.putExtra("TITLE", item.title);
        intent.putExtra("GUBUN", "N");
        startActivity(intent);
    }

    //completeList
    private void completeList(FieldItem item) {
        Intent intent = new Intent(this, FieldListActivity.class);
        intent.putExtra("PNUM", item.pnum);
        intent.putExtra("TITLE", item.title);
        intent.putExtra("GUBUN", "Y");
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FieldItem fieldItem=(FieldItem)view.getTag();
        Intent intent=null;
        if(fieldItem.type.equals("LIST")) {
            intent = new Intent(this, ListProjectActivity.class);
            intent.putExtra("TITLE", fieldItem.title);
            intent.putExtra("PNUM", fieldItem.pnum);
        } else if(fieldItem.type.equals("FF")) {
            intent = new Intent(this, FieldSurveyActivity.class);
            intent.putExtra("TITLE", fieldItem.title);
            intent.putExtra("PNUM", fieldItem.pnum);
        }
        if(intent!=null)startActivity(intent);
    }
}