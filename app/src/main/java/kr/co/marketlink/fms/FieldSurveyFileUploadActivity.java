package kr.co.marketlink.fms;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import kr.co.marketlink.common.Common;
import kr.co.marketlink.common.GPS;
import kr.co.marketlink.common.Post;

public class FieldSurveyFileUploadActivity extends AppCompatActivity implements View.OnClickListener, Post.PostHandler, UploadHelper.UploadHandler {
    String UPLOAD_TYPE = "";
    String PNUM = "";
    String ID = "";
    String _ID = "";
    final int PICK_PHOTO_FOR_AVATAR = 0;
    final int PICK_VOICE_FOR_AVATAR = 1;
    Activity activity = this;
    String filename = "";
    String IDHEAD = "";
    //임시 테스트
    ListView listview;
    FileFFUploadAdapter adapter;
    Button btn_imageupload;
    Button btn_soundupload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_field_survey_file_upload);
        Common.log("FieldSurveyFileUploadActivity > onCreate");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView tv_toolbar_title = (TextView) toolbar.findViewById(R.id.tv_toolbar_title);
        tv_toolbar_title.setText("업로드");

        PNUM = getIntent().getExtras().getString("PNUM");
        ID = getIntent().getExtras().getString("ID");
        _ID = getIntent().getExtras().getString("_ID");

        //해당 프로젝트의 확인 타입 체크
        selectIdhead();

        btn_imageupload = (Button) findViewById(R.id.btn_imageupload2);
        btn_soundupload = (Button) findViewById(R.id.btn_soundupload2);



        /*btn_imageupload.setVisibility(View.VISIBLE);
        btn_soundupload.setVisibility(View.VISIBLE);*/
    }

    //이미지타입 조회
    public void typeLoad() {
        Object[][] params = {
                {"TOKEN", Common.getPreference(getApplicationContext(), "TOKEN")}
                , {"PNUM", PNUM}
        };
        Post.Post(Post.CALLTYPE_SELECT_PROJECT_TYPE, getString(R.string.url_selectProjectUploadType), params, this, this);
    }

    //user 테이블에서 THEAD 조회
    public void selectIdhead() {
        Object[][] params = {
                {"TOKEN", Common.getPreference(getApplicationContext(), "TOKEN")}
                , {"_ID", _ID}
        };
        Post.Post(Post.CALLTYPE_SELECT_THEAD, getString(R.string.url_selectThead), params, this, this);
    }

    @Override
    public void onPostResult(int calltype, JSONObject json) {
        if (calltype == Post.CALLTYPE_SELECT_PROJECT_TYPE) typeLoadHandler(json);
        else if (calltype == Post.CALLTYPE_INSERT_IMAGE_INFO) insertImageInfoHandler(json);
        else if (calltype == Post.CALLTYPE_SELECT_UPLOAD_LIST) listHandler(json);
        else if (calltype == Post.CALLTYPE_SELECT_THEAD) selectIdheadHandler(json);
    }

    private void writeHandler(JSONObject json) {
        String ERR = "";
        try {
            Toast.makeText(this, "저장 되었습니다.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, ERR, Toast.LENGTH_SHORT).show();
        }
    }

    private void typeLoadHandler(JSONObject json) {
        String ERR = "";
        try {
            ERR = json.getString("ERR");
            UPLOAD_TYPE = json.getString("UPLOAD_TYPE");

            if (UPLOAD_TYPE.equals("SOUND")) {
                btn_imageupload.setVisibility(View.GONE);
                btn_soundupload.setVisibility(View.VISIBLE);
            } else {
                btn_imageupload.setVisibility(View.VISIBLE);
                btn_soundupload.setVisibility(View.GONE);
            }

            if (UPLOAD_TYPE.equals("")) {
                Toast.makeText(this, "프로젝트 > 이미지/음성 타입을 지정하여 주세요.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Toast.makeText(this, ERR, Toast.LENGTH_SHORT).show();
        }
    }

    private void selectIdheadHandler(JSONObject json) {
        String ERR = "";
        try {
            ERR = json.getString("ERR");
            IDHEAD = json.getString("IDHEAD");
            typeLoad();
        } catch (Exception e) {
            Toast.makeText(this, ERR, Toast.LENGTH_SHORT).show();
        }
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_imageupload2:
                pickImageVoice("i");
                break;
            case R.id.btn_soundupload2:
                pickImageVoice("v");
                break;
        }
    }

    /* 사진, 음성 올리기 */
    public void pickImageVoice(final String type) {
        //Common.log("type==>"+type);
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                if (type.equals("i")) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, PICK_PHOTO_FOR_AVATAR);
                } else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("audio/*");
                    startActivityForResult(intent, PICK_VOICE_FOR_AVATAR);
                }
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            }
        };
        new TedPermission(activity)
                .setPermissionListener(permissionlistener)
                .setRationaleMessage("사진/파일을 전송하기 위해서는 액세스 권한이 필요합니다")
                .setDeniedMessage("엑세스 권한을 거부할 경우, 해당 서비스를 이용하실 수 없습니다. [설정] > [권한]에서 허용으로 변경해 주시기 바랍니다.")
                .setGotoSettingButtonText("변경하기")
                .setPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_FOR_AVATAR && resultCode == Activity.RESULT_OK) {
            UploadHelper.upload(this, data.getData(), this);
        } else if (requestCode == PICK_VOICE_FOR_AVATAR && resultCode == Activity.RESULT_OK) {
            UploadHelper.upload(this, data.getData(), this);
        }
    }

    @Override
    public void OnUploadComplete(String filename) {
        insert(filename);
        //https://s3.ap-northeast-2.amazonaws.com/marketlinkfms/jpg/201703221707484708.jpg
        //https://s3.ap-northeast-2.amazonaws.com/marketlinkfms/m4a/201703221707484708.m4a
    }

    @Override
    public void OnUploadFail(String error) {
        Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
    }

    //DB 정보저장
    void insert(String filename) {
        Object[][] params = {
                {"TOKEN", Common.getPreference(getApplicationContext(), "TOKEN")}
                , {"PNUM", PNUM}
                , {"_ID", _ID}
                , {"LID", ID}
                , {"FILE", filename}
                , {"IDHEAD", IDHEAD}
                , {"UPLOAD_TYPE", UPLOAD_TYPE}
        };
        Post.Post(Post.CALLTYPE_INSERT_IMAGE_INFO, getString(R.string.url_fieldWriteffImage), params, this, this);
    }

    //이미지 업로드시, DB 정보 추가.
    private void insertImageInfoHandler(JSONObject json) {
        String ERR = "";
        try {
            ERR = json.getString("ERR");
            //어댑터 갱신
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
        }
        if (!ERR.equals("")) {
            Toast.makeText(this, ERR, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, getString(R.string.desc_saved), Toast.LENGTH_SHORT).show();
        adapter.notifyDataSetChanged();
    }

    /*
        업로드 리스트 정보 로드
    */
    private void listLoad() {
        Common.log("listLoad() call!!");
        Object[][] params = {
                {"TOKEN", Common.getPreference(getApplicationContext(), "TOKEN")}
                , {"ID", ID}
                , {"_ID", _ID}
                , {"PNUM", PNUM}
                , {"IDHEAD", IDHEAD}
        };
        Post.Post(Post.CALLTYPE_SELECT_UPLOAD_LIST, getString(R.string.url_fileuploadFFList), params, this, this);
    }

    //저장 후 처리
    private void listHandler(JSONObject json) {

        String ERR = "";
        String RESULT = "";
        // Adapter 생성
        adapter = new FileFFUploadAdapter();

        // 리스트뷰 참조 및 Adapter달기
        listview = (ListView) findViewById(R.id.lv_ff_fileupload);
        listview.setAdapter(adapter);
        try {
            Common.log("ffupload==>"+json.toString());
            ERR = json.getString("ERR");
            RESULT = json.getString("RESULT");
            JSONObject listItem;
            JSONObject fmsItem;
            int i;

            if (!ERR.equals("")) {
                Toast.makeText(this, ERR, Toast.LENGTH_SHORT).show();
                return;
            } else {
                String dt = "";
                String url = "";
                JSONArray list = (JSONArray) json.getJSONArray("LIST");
                listItem = list.getJSONObject(0);
                Common.log("listItem==>"+list.toString());

                if(listItem.has("FMS_IMG")){
                    JSONObject fms_img = listItem.getJSONObject("FMS_IMG");
                    url = fms_img.getString("URL");
                    url = url.replaceAll("https://s3.ap-northeast-2.amazonaws.com/marketlinkfms/jpg/","");

                }else if(listItem.has("FMS_SND")){
                    JSONObject fms_snd = listItem.getJSONObject("FMS_SND");
                    url = fms_snd.getString("URL");
                    url = url.replaceAll("https://s3.ap-northeast-2.amazonaws.com/marketlinkfms/m4a/","").replaceAll("https://s3.ap-northeast-2.amazonaws.com/marketlinkfms/mp3/","");
                }

                adapter.addItem("", "", url);



                /*if (UPLOAD_TYPE.equals("SOUND")) {
                    if (fms_snd.length() > 0) {
                        for (i = 0; i < fms_img.length(); i++) { //fms_img.length() 윤정환 대리 요청으로 우선 3개까지만 나오도록 함.
                            fmsItem = fms_snd.getJSONObject(i);
                            dt = fmsItem.getString("DT");
                            url = fmsItem.getString("URL").replaceAll("https://s3.ap-northeast-2.amazonaws.com/marketlinkfms/jpg/", "").replaceAll("https:///s3.ap-northeast-2.amazonaws.com/marketlinkfms/m4a/", "").replaceAll("https:\\/\\/s3.ap-northeast-2.amazonaws.com\\/marketlinkfms\\/m4a\\/", "");
                            adapter.addItem("", dt, url);
                        }
                    }
                } else {
                    if (fms_img.length() > 0) {
                        for (i = 0; i < fms_img.length(); i++) { //fms_img.length() 윤정환 대리 요청으로 우선 3개까지만 나오도록 함.
                            fmsItem = fms_img.getJSONObject(i);
                            dt = fmsItem.getString("DT");
                            url = fmsItem.getString("URL").replaceAll("https://s3.ap-northeast-2.amazonaws.com/marketlinkfms/jpg/", "").replaceAll("https:///s3.ap-northeast-2.amazonaws.com/marketlinkfms/m4a/", "").replaceAll("https:\\/\\/s3.ap-northeast-2.amazonaws.com\\/marketlinkfms\\/m4a\\/", "");
                            adapter.addItem("", dt, url);
                        }
                    }
                }*/

            }
        } catch (Exception e) {
            Common.log(e.toString());
        }
    }

    @Override
    protected void onResume() {
        //업로드 리스트 정보 호출
        listLoad();
        super.onResume();
    }
}
