package kr.co.marketlink.fms;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import kr.co.marketlink.common.Common;
import kr.co.marketlink.common.Post;

/**
 * Created by yangjaesang on 2017. 1. 31..
 */

public class FmsFirebaseInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        //변경된 토큰을 DB에 저장
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        if(!Common.getPreference(getApplicationContext(),"TOKEN").equals("")){
            Object[][] params = {
                    {"TOKEN",Common.getPreference(getApplicationContext(),"TOKEN")}
                    ,{"FCM_TOKEN", refreshedToken}
            };
            Post.Post(Post.CALLTYPE_APPTOKEN_WRITE,getString(R.string.url_fcmTokenWrite),params,null,null);
        }
    }
}
