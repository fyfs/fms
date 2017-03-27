package kr.co.marketlink.fms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import kr.co.marketlink.common.Post;

public class ImageSoundUploadActivity extends AppCompatActivity implements View.OnClickListener, Post.PostHandler, UploadHelper.UploadHandler {
    String UPLOAD_TYPE = "";
    String PNUM = "";
    String _ID = "";
    final int PICK_PHOTO_FOR_AVATAR = 0;
    final int PICK_VOICE_FOR_AVATAR = 1;
    Activity activity = this;
    String filename = "";
    //임시 테스트
    ListView listview;
    FileUploadAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_sound_upload);
        //final Activity activity = this;
        Common.log("ImageSoundUploadActivity > onCreate");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView tv_toolbar_title = (TextView) toolbar.findViewById(R.id.tv_toolbar_title);
        tv_toolbar_title.setText("업로드");

        UPLOAD_TYPE = getIntent().getExtras().getString("UPLOAD_TYPE");
        PNUM = getIntent().getExtras().getString("PNUM");
        _ID = getIntent().getExtras().getString("_ID");

        Common.log("UPLOAD_TYPE==>"+UPLOAD_TYPE);
        Common.log("PNUM==>"+PNUM);
        Common.log("_ID==>"+_ID);

        Button btn_imageupload = (Button) findViewById(R.id.btn_imageupload);
        Button btn_soundupload = (Button) findViewById(R.id.btn_soundupload);

        if (UPLOAD_TYPE.equals("SOUND")) {
            btn_imageupload.setVisibility(View.GONE);
        } else {
            btn_soundupload.setVisibility(View.GONE);
        }
        /*btn_imageupload.setVisibility(View.VISIBLE);
        btn_soundupload.setVisibility(View.VISIBLE);*/


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
            case R.id.btn_imageupload:
                pickImageVoice("i");
                break;
            case R.id.btn_soundupload:
                pickImageVoice("v");
                break;
        }
    }

    /* 사진, 음성 올리기 */
    public void pickImageVoice(final String type) {
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
        save(filename);
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
                , {"FILE", filename}
                , {"UPLOAD_TYPE", UPLOAD_TYPE}
        };
        Post.Post(Post.CALLTYPE_INSERT_IMAGE_INFO, getString(R.string.url_fieldWriteImage), params, this, this);
    }

    //저장
    void save(String filename) {
        this.filename = filename;
        Object[][] params = {
                {"TOKEN", Common.getPreference(getApplicationContext(), "TOKEN")}
                , {"PNUM", PNUM}
                , {"_ID", _ID}
                , {"FILE", filename}
                , {"UPLOAD_TYPE", UPLOAD_TYPE}
        };
        Post.Post(Post.CALLTYPE_FIELD_WRITE, getString(R.string.url_fieldWrite), params, this, this);
    }

    @Override
    public void onPostResult(int calltype, JSONObject json) {
        if (calltype == Post.CALLTYPE_FIELD_WRITE) saveHandler(json);
        else if (calltype == Post.CALLTYPE_INSERT_IMAGE_INFO) insertImageInfoHandler(json);
        else if (calltype == Post.CALLTYPE_SELECT_UPLOAD_LIST) listHandler(json);
    }

    //이미지 저장
    private void saveHandler(JSONObject json) {
        String ERR = "";
        try {
            ERR = json.getString("ERR");
        } catch (Exception e) {
        }
        if (!ERR.equals("")) {
            Toast.makeText(this, ERR, Toast.LENGTH_SHORT).show();
            return;
        }
        insert(filename);
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
    }

    /*
        업로드 리스트 정보 로드
    */
    private void listLoad() {
        Object[][] params = {
                 {"TOKEN", Common.getPreference(getApplicationContext(), "TOKEN")}
                ,{"PNUM", PNUM}
                ,{"_ID", _ID}
        };
        Post.Post(Post.CALLTYPE_SELECT_UPLOAD_LIST, getString(R.string.url_fileuploadList), params, this, this);
    }

    //저장 후 처리
    private void listHandler(JSONObject json) {
        Common.log(json.toString());
        String ERR = "";
        String RESULT = "";
        // Adapter 생성
        adapter = new FileUploadAdapter();

        // 리스트뷰 참조 및 Adapter달기
        listview = (ListView) findViewById(R.id.lv_fileupload);
        listview.setAdapter(adapter);

        try {
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
                String _id = listItem.getString("_id");


                if(listItem.has("FMS_IMG")){
                    JSONArray fms_img = (JSONArray) listItem.getJSONArray("FMS_IMG");
                    for (i = 0; i < 3; i++) { //fms_snd.length() 윤정환 대리 요청으로 우선 3개까지만 나오도록 함.
                        fmsItem = fms_img.getJSONObject(i);
                        dt = fmsItem.getString("DT");
                        url = fmsItem.getString("URL").replaceAll("https://s3.ap-northeast-2.amazonaws.com/marketlinkfms/jpg/", "").replaceAll("https:///s3.ap-northeast-2.amazonaws.com/marketlinkfms/m4a/", "").replaceAll("https:\\/\\/s3.ap-northeast-2.amazonaws.com\\/marketlinkfms\\/m4a\\/", "");
                        adapter.addItem("", dt, url);
                        adapter.notifyDataSetChanged();
                    }
                }else if(listItem.has("FMS_SND")) {
                    JSONArray fms_snd = (JSONArray) listItem.getJSONArray("FMS_SND");
                    for (i = 0; i < 3; i++) { //fms_img.length() 윤정환 대리 요청으로 우선 3개까지만 나오도록 함.
                        fmsItem = fms_snd.getJSONObject(i);
                        dt = fmsItem.getString("DT");
                        url = fmsItem.getString("URL").replaceAll("https://s3.ap-northeast-2.amazonaws.com/marketlinkfms/jpg/", "").replaceAll("https:///s3.ap-northeast-2.amazonaws.com/marketlinkfms/m4a/", "").replaceAll("https:\\/\\/s3.ap-northeast-2.amazonaws.com\\/marketlinkfms\\/m4a\\/", "");
                        adapter.addItem("", dt, url);
                        adapter.notifyDataSetChanged();
                    }
                }
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
