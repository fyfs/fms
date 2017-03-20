package kr.co.marketlink.fms;

import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import kr.co.marketlink.common.Common;

/**
 * Created by yangjaesang on 2017. 1. 31..
 */

public class FmsFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if(Common.currentActivity!=null && !(Common.getPreference(getApplicationContext(),"TOKEN").equals(""))){
            final String msg=remoteMessage.getNotification().getBody();
            Common.currentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Snackbar snackbar=Snackbar.make(Common.currentActivity.findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG);
                        View snackBarView = snackbar.getView();
                        snackBarView.setBackgroundColor(Common.getColor(getApplicationContext(),R.color.colorWarning));
                        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(Common.getColor(getApplicationContext(),android.R.color.white));
                        snackbar.show();
                        //메인 페이지인 경우 메시지 개수 변경을 위해 새로고침
                        if(Common.currentActivity instanceof MainActivity){
                            ((MainActivity)Common.currentActivity).start();
                            //메시지 페이지인 경우 새로고침
                        } else if(Common.currentActivity instanceof MessageActivity){
                            ((MessageActivity)Common.currentActivity).getMessage();
                        }
                    } catch(Exception e){
                        Common.log(e.toString());
                    }
                }
            });
        }
    }
}
