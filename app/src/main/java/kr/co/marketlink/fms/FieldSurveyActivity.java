package kr.co.marketlink.fms;

import android.app.Activity;
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
import kr.co.marketlink.common.File;
import kr.co.marketlink.common.GPS;
import kr.co.marketlink.common.ListHelper;
import kr.co.marketlink.common.Post;
import kr.co.marketlink.ui.MlButton;
import kr.co.marketlink.ui.MlInput;

public class FieldSurveyActivity extends AppCompatActivity implements View.OnClickListener, Post.PostHandler, ListHelper.OnScrollListener.I_bottomScrollHandler {

    String PNUM = "";
    String TITLE = "";
    String LID = "";
    final int PICK_PHOTO_FOR_AVATAR = 0;
    String MORE_YN = "N";
    int PAGE = 1;
    boolean loading = false;
    ArrayList<HistoryItem> list = new ArrayList<>();
    HistoryListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Common.currentActivity = this;
        Common.addActivity(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fieldsurvey);
        Common.log("FieldSurveyActivity > onCreate");

        TITLE = getIntent().getExtras().getString("TITLE");
        PNUM = Integer.toString(getIntent().getExtras().getInt("PNUM"));
        LID = getIntent().getExtras().getString("LID");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView tv_toolbar_title = (TextView) toolbar.findViewById(R.id.tv_toolbar_title);
        tv_toolbar_title.setText(TITLE);

        MlButton ml_type = (MlButton) findViewById(R.id.ml_type);
        Common.setBorderBg(ml_type
                , Common.getColor(getApplicationContext(), android.R.color.background_light)
                , Common.getColor(getApplicationContext(), android.R.color.darker_gray)
                , 1, 0);
        ml_type.setTextColor(Common.getColor(getApplicationContext(), android.R.color.darker_gray));
        ml_type.setText("조사완료 리스트");

        ListView listView = (ListView) findViewById(R.id.lv_list);
        listView.setOnScrollListener(new ListHelper.OnScrollListener(this));
        adapter = new HistoryListAdapter(this, R.layout.listitem_history, list);
        listView.setAdapter(adapter);

        addEventListener();
        start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //event listener
    private void addEventListener() {
        MlButton btn_start = (MlButton) findViewById(R.id.btn_start);
        MlButton btn_pause = (MlButton) findViewById(R.id.btn_pause);
        MlButton btn_end = (MlButton) findViewById(R.id.btn_end);
        if (btn_start != null) btn_start.setOnClickListener(this);
        if (btn_pause != null) btn_pause.setOnClickListener(this);
        if (btn_end != null) btn_end.setOnClickListener(this);
    }

    //click handler
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_image) {
            pickImage();
        } else if (id == R.id.btn_start) {
            save(LID, getString(R.string.FMS_ST_START), "", "");
        } else if (id == R.id.btn_pause) {
            save(LID, getString(R.string.FMS_ST_PAUSE), "", "");
        } else if (id == R.id.btn_end) {
            save(LID, getString(R.string.FMS_ST_END), "", "");
        }
    }

    //저장
    void save(String LID, String fms_st, String fms_img, String fms_etc) {
        GPS.getGps(getApplicationContext());
        Object[][] params = {
                {"TOKEN", Common.getPreference(getApplicationContext(), "TOKEN")}
                , {"PNUM", PNUM}
                , {"LID", LID}
                , {"FMS_ST", fms_st}
                , {"FMS_IMG", fms_img}
                , {"FMS_ETC", fms_etc}
                , {"LAT", GPS.lastLat}
                , {"LNG", GPS.lastLng}
        };
        Post.Post(Post.CALLTYPE_FIELDSURVEY_WRITE, getString(R.string.url_fieldSurveyWrite), params, this, this);
    }

    @Override
    public void onPostResult(int calltype, JSONObject json) {
        if (calltype == Post.CALLTYPE_FIELDSURVEY_WRITE) saveHandler(json);
        else if (calltype == Post.CALLTYPE_FIELD_HISTORY) dataloadHandler(json);
    }

    //저장 후 처리
    private void saveHandler(JSONObject json) {
        String ERR = "";
        String FMS_ST = "";
        try {
            ERR = json.getString("ERR");
            FMS_ST = json.getString("FMS_ST");
            LID = json.getString("LID");
        } catch (Exception e) {
        }
        if (!ERR.equals("")) {
            Toast.makeText(this, ERR, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, getString(R.string.desc_saved), Toast.LENGTH_SHORT).show();
        if (FMS_ST.equals(getString(R.string.FMS_ST_END))) complete();
    }

    //완료
    public void complete() {
        LID = "";
        list.clear();
        adapter.notifyDataSetChanged();
        start();
    }

    //사진 올리기
    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PHOTO_FOR_AVATAR);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_FOR_AVATAR && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(this, getString(R.string.desc_cannot_image), Toast.LENGTH_SHORT).show();
                return;
            }
            String imgBase64 = File.imageToBase64(getApplicationContext(), data.getData(), 20);
            if (imgBase64.equals("")) {
                Toast.makeText(this, getString(R.string.desc_cannot_image), Toast.LENGTH_SHORT).show();
                return;
            }
            save(LID, "", imgBase64, "");
        }
    }

    //시작
    private void start() {
        getList();
    }

    //HistoryItem
    private class HistoryItem {
        public View view;
        public String id = "";
        public String _id = "";
        public String startTime = "";
        public String endTime = "";

        public HistoryItem(String id, String _id, long startTime, long endTime) {
            this.id = id;
            this._id = _id;
            this.startTime = Common.longToDatetime(startTime);
            this.endTime = Common.longToDatetime(endTime);
        }
    }

    private class HistoryListAdapter extends ArrayAdapter<HistoryItem> {
        private List<HistoryItem> items;
        private LayoutInflater inflater;

        public HistoryListAdapter(Context context, int resource, List<HistoryItem> objects) {
            super(context, resource, objects);
            items = objects;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.listitem_history, null);
            }
            final HistoryItem item = items.get(position);
            if (item != null) {
                LinearLayout ll_box = (LinearLayout) convertView.findViewById(R.id.ll_box);
                TextView tv_id = (TextView) convertView.findViewById(R.id.tv_id);
                TextView tv_startTime = (TextView) convertView.findViewById(R.id.tv_startTime);
                TextView tv_endTime = (TextView) convertView.findViewById(R.id.tv_endTime);

                Common.setBorderBg(ll_box, Color.parseColor("#ffffffff"), Color.parseColor("#ff666666"), 1, 0);
                tv_id.setText(getString(R.string.desc_id, item.id));
                tv_startTime.setText(getString(R.string.desc_startTime, item.startTime));
                tv_endTime.setText(getString(R.string.desc_endTime, item.endTime));
                item.view = convertView;
                convertView.setTag(item);

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(FieldSurveyActivity.this, FieldSurveyFileUploadActivity.class);
                        intent.putExtra("PNUM", PNUM);
                        intent.putExtra("ID", item.id);
                        intent.putExtra("_ID", item._id);
                        startActivity(intent);
                    }
                });

            }
            return convertView;
        }
    }

    //리스트 불러오기
    private void getList() {
        if (loading) return;
        loading = true;
        Object[][] params = {
                {"TOKEN", Common.getPreference(getApplicationContext(), "TOKEN")}
                , {"PAGE", Integer.toString(PAGE)}
                , {"PNUM", PNUM}
        };
        Post.Post(Post.CALLTYPE_FIELD_HISTORY, getString(R.string.url_fieldHistory), params, this, this);
    }

    //dataload handler
    private void dataloadHandler(JSONObject json) {
        String RESULT = "";
        String ERR = "";
        JSONArray LIST = null;
        ListView listView = (ListView) findViewById(R.id.lv_list);
        HistoryListAdapter adapter = (HistoryListAdapter) listView.getAdapter();

        try {
            RESULT = json.getString("RESULT");
            ERR = json.getString("ERR");
            MORE_YN = json.getString("MORE_YN");
            LIST = json.getJSONArray("LIST");
            JSONObject DATA = null;
            for (int i = 0; i < LIST.length(); i++) {
                DATA = (JSONObject) LIST.get(i);
                String LID = DATA.getString("LID");
                String _ID = DATA.getString("_id");
                long DT = DATA.getLong("DT");
                long ET = DATA.getLong("ET");
                adapter.add(new HistoryItem(LID,_ID, DT, ET));
            }

        } catch (Exception e) {
            if (ERR.equals("")) ERR = e.toString();
        }

        if (!ERR.equals("")) {
            Toast.makeText(this, ERR, Toast.LENGTH_SHORT).show();
            return;
        }
        loading = false;
    }

    //바닥 스크롤
    @Override
    public void bottomScroll() {
        if (loading) return;
        if (MORE_YN.equals("N")) return;
        PAGE++;
        getList();
    }
}
