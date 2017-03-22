package kr.co.marketlink.fms;

import android.*;
import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import kr.co.marketlink.common.Common;
import kr.co.marketlink.common.GPS;
import kr.co.marketlink.common.Post;

public class ImageSoundUploadActivity extends AppCompatActivity implements View.OnClickListener, Post.PostHandler ,UploadHelper.UploadHandler{
    String UPLOAD_TYPE = "";
    String PNUM = "";
    String _id = "";
    final int PICK_PHOTO_FOR_AVATAR = 0;
    final int PICK_VOICE_FOR_AVATAR = 1;
    Activity activity = this;

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
        _id = getIntent().getExtras().getString("_ID");

        Common.log("_id(2)==>" + _id);

        Button btn_imageupload = (Button) findViewById(R.id.btn_imageupload);
        Button btn_soundupload = (Button) findViewById(R.id.btn_soundupload);

        if (UPLOAD_TYPE.equals("SOUND")) {
            btn_imageupload.setVisibility(View.GONE);
        } else {
            btn_soundupload.setVisibility(View.GONE);
        }

        btn_imageupload.setVisibility(View.VISIBLE);
        btn_soundupload.setVisibility(View.VISIBLE);

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
                //Toast.makeText(this,"작업중입니다.",Toast.LENGTH_LONG).show();
                pickImageVoice("i");
                break;
            case R.id.btn_soundupload:
                //Toast.makeText(this,"작업중입니다.",Toast.LENGTH_LONG).show();
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
                    //startActivity(Intent.createChooser(intent, "Select music"));
                    startActivityForResult(intent.createChooser(intent, "voice"), PICK_VOICE_FOR_AVATAR);
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
            UploadHelper.upload(this,data.getData(),this);
        } else if (requestCode == PICK_VOICE_FOR_AVATAR && resultCode == Activity.RESULT_OK) {
            UploadHelper.upload(this,data.getData(),this);
        }
    }

    @Override
    public void OnUploadComplete(String filename) {
        save("","",filename,"","");
    }

    @Override
    public void OnUploadFail(String error) {
        Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
    }

    //저장
    void save(String fms_st, String fms_ft, String fms_img, String fms_etc, String fms_lc) {
        //Common.log("_id(2)==>"+_id);
        GPS.getGps(getApplicationContext());
        Object[][] params = {
                {"TOKEN", Common.getPreference(getApplicationContext(), "TOKEN")}
                , {"PNUM", PNUM}
                , {"_ID", _id}
                , {"FMS_ST", fms_st}
                , {"FMS_FT", fms_ft}
                , {"FMS_LC", fms_lc}
                , {"FMS_IMG", fms_img}
                , {"FMS_ETC", fms_etc}
                , {"LAT", GPS.lastLat}
                , {"LNG", GPS.lastLng}
        };
        Post.Post(Post.CALLTYPE_FIELD_WRITE, getString(R.string.url_fieldWrite), params, this, this);
    }

    @Override
    public void onPostResult(int calltype, JSONObject json) {
        if (calltype == Post.CALLTYPE_FIELD_WRITE) saveHandler(json);
    }

    //저장 후 처리
    private void saveHandler(JSONObject json) {
        String ERR = "";
        String FMS_ST = "";
        String FMS_FT = "";
        try {
            ERR = json.getString("ERR");
            FMS_ST = json.getString("FMS_ST");
            FMS_FT = json.getString("FMS_FT");
        } catch (Exception e) {
        }
        if (!ERR.equals("")) {
            Toast.makeText(this, ERR, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, getString(R.string.desc_saved), Toast.LENGTH_SHORT).show();
//        if(FMS_ST.equals(getString(R.string.FMS_ST_START)))fieldStart();
//        else Toast.makeText(this, getString(R.string.desc_saved), Toast.LENGTH_SHORT).show();
//        if(FMS_ST.equals(getString(R.string.FMS_ST_END))||FMS_ST.equals(getString(R.string.FMS_ST_DENIED))||FMS_ST.equals(getString(R.string.FMS_ST_OFF))||(!FMS_FT.equals(""))) finish();
    }


}
