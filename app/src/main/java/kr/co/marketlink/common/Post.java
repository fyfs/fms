package kr.co.marketlink.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import kr.co.marketlink.fms.LoginActivity;

/**
 * Created by yangjaesang on 2017. 1. 19..
 */

public class Post {

    static public final int CALLTYPE_LOGIN=1;
    static public final int CALLTYPE_AGREE=2;
    static public final int CALLTYPE_MAIN=3;
    static public final int CALLTYPE_FIELD=4;
    static public final int CALLTYPE_FIELD_HISTORY =5;
    static public final int CALLTYPE_FIELD_LIST =6;
    static public final int CALLTYPE_FIELD_WRITE =7;
    static public final int CALLTYPE_FIELDSURVEY_WRITE =8;
    static public final int CALLTYPE_MESSAGE =9;
    static public final int CALLTYPE_MESSAGE_READ =10;
    static public final int CALLTYPE_MESSAGE_DELETE =11;
    static public final int CALLTYPE_ACCOUNT =12;
    static public final int CALLTYPE_ACCOUNT_WRITE =13;
    static public final int CALLTYPE_APPINFO =14;
    static public final int CALLTYPE_APPTOKEN_WRITE =15;
    static public final int CALLTYPE_FIELD_DATA =16; //PNUM 으로 프로젝트 정보조회.

    public static void Post(int calltype, String url, Object[][] params,PostHandler handler,Activity activity) {
        HttpAsyncTask hat = new HttpAsyncTask();
        hat.calltype=calltype;
        hat.datas =params;
        hat.postHandler=handler;
        hat.context=activity;
        hat.execute(url);
    }

    private static String GET(String targetURL,String urlParameters){
        URL url;
        HttpURLConnection connection = null;
        try {
            //Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream ());
            wr.writeBytes (urlParameters);
            wr.flush();
            wr.close();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"ERR\":\"Exception1\"}";
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }
    public interface PostHandler {
        void onPostResult(int calltype, JSONObject json);
    }
    static private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        private int calltype;
        private Object[][] datas;
        private PostHandler postHandler;
        private String params = "";
        private Context context=null;
        @Override
        protected String doInBackground(String... urls){
            try {
                params = "";
                //if(activity!=null)params += "UID=" + URLEncoder.encode(Common.getPreference(activity.getApplicationContext(),"UID"), "UTF-8");
                if(datas!=null) {
                    int i;
                    Object[] data;
                    String key, value;
                    for (i = 0; i < datas.length; i++) {
                        data = datas[i];
                        if(data[0]==null)continue;
                        else key = (String) data[0];
                        if(data[1]==null)value="";
                        else if(data[1] instanceof Integer) value = Integer.toString((Integer)data[1]);
                        else if(data[1] instanceof Long) value = Long.toString((Long)data[1]);
                        else if(data[1] instanceof Double) value = Double.toString((Double)data[1]);
                        else value=(String)data[1];
                        params += "&" + key + "=" + URLEncoder.encode(value, "UTF-8");
                    }
                }
            } catch(Exception e){
                params = "";
            }
            return GET(urls[0],params);
        }
        @Override
        protected void onPostExecute(String result){
            Common.log(result);
            JSONObject json=new JSONObject();
            String ERR="";
            String RESULT="NOT OK";
            try{
                json=new JSONObject(result);
                ERR=json.getString("ERR");
                RESULT=json.getString("RESULT");
            } catch(Exception e){
                ERR=e.toString();
                RESULT="NOT OK";
            }
            if(ERR.equals("")&&!RESULT.equals("OK"))ERR=RESULT;
            try {
                json.put("ERR", ERR);
                json.put("RESULT", RESULT);
            } catch(Exception e){
                //여기로 올 일이 없을
            }
            //로그아웃된 경우
            try{
                if(RESULT.equals("LOGOUT")){
                    Intent intent=new Intent(context, LoginActivity.class);
                    intent.putExtra("HAS_TARGET_ACTIVITY",true);
                    if(context!=null)context.startActivity(intent);
                    return;
                }
            } catch(Exception e){Common.log(e.toString());}
            if(postHandler!=null)postHandler.onPostResult(calltype,json);
        }
    }

}
