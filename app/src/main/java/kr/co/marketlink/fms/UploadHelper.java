package kr.co.marketlink.fms;

import android.content.Context;
import android.net.Uri;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import kr.co.marketlink.common.Common;

/**
 * Created by yangjaesang on 2017. 3. 22..
 */

public class UploadHelper {

    static public void upload(Context context, Uri uri, final UploadHandler uploadHandler) {
        File file=new File(Common.uriToPath(context, uri));
        if(file.length()==0){
            uploadHandler.OnUploadFail("파일이 정상적이지 않습니다");
            return;
        }
        String ext="";
        if(file.getPath().split("\\.").length>0)ext = file.getPath().split("\\.")[file.getPath().split("\\.").length - 1];
        if(ext.equals(""))ext="etc";
        Date now=new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        final String filename= format.format(now)+Double.toString(Math.random()).substring(2,6)+"."+ext;
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                "ap-northeast-2:b8d67f00-3792-445d-8d62-5d73b2c467c9",
                Regions.AP_NORTHEAST_2
        );
        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
        s3.setRegion(Region.getRegion(Regions.AP_NORTHEAST_2));
        TransferUtility transferUtility = new TransferUtility(s3, context);
        TransferObserver transferObserver = transferUtility.upload(
                "marketlinkfms",     /* The bucket to upload to */
                ext+"/"+filename,    /* The key for the uploaded object */
                file        /* The file where the data to upload exists */
        );
        transferObserver.setTransferListener(new TransferListener(){
            @Override
            public void onStateChanged(int id, TransferState state) {
                //IN_PROGRESS
                //FAILED
                //COMPLETED
                Common.log("STATE:"+state.toString());
                if(state.toString().equals("COMPLETED"))uploadHandler.OnUploadComplete(filename);
                else if(state.toString().equals("FAILED"))uploadHandler.OnUploadFail("업로드를 실패했습니다");
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                Common.log(bytesCurrent);
            }

            @Override
            public void onError(int id, Exception ex) {
                Common.log("ERR:"+ex.toString());
            }
        });
    }

    interface UploadHandler{
        void OnUploadComplete(String filename);
        void OnUploadFail(String error);
    }

}
