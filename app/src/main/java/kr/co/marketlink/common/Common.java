package kr.co.marketlink.common;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.StyleableRes;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by yangjaesang on 2017. 1. 14..
 */

public class Common {

    //testing
    static public boolean testing=true;

    //Log tag
    static private String myTag="FMS_LOG";
    //preference info
    static private String preferenceName="fms";
    static private String secureKey="fmspreffms";

    //Activity stack
    static private ArrayList<Activity> activities=new ArrayList<>();
    static public void addActivity(Activity a){
        activities.add(a);
    }
    static public void flushActivity(){
        for(int i=activities.size()-1;i>=0;i--){
            Activity a=activities.get(i);
            try{
                a.finish();
            } catch(Exception e){
                activities.remove(i);

            }
        }
    }

    //Current Activity
    static public Activity currentActivity=null;

    /**
     * 로그
     * @param msg 출력할 로그
     */
    static public void log(String msg){
        if(msg==null)msg="";
        Log.d(myTag,msg);
    }

    /**
     * 로그
     * @param msg 출력할 로그
     */
    static public void log(boolean msg){
        Log.d(myTag,Boolean.toString(msg));
    }

    /**
     * 로그
     * @param msg 출력할 로그
     */
    static public void log(int msg){
        Log.d(myTag,Integer.toString(msg));
    }

    /**
     * 로그
     * @param msg 출력할 로그
     */
    static public void log(long msg){
        Log.d(myTag,Long.toString(msg));
    }

    /**
     * 로그
     * @param msg 출력할 로그
     */
    static public void log(double msg){
        Log.d(myTag,Double.toString(msg));
    }

    /**
     * Null 대체
     * @param input 입력값
     * @param defaultValue Null일 경우 대체할 값
     * @return 결과값
     */
    static public String isNull(String input,String defaultValue){
        return input==null?defaultValue:input;
    }

    /**
     * 기기 저장값 가져오기
     * @param context context
     * @param prefKey key
     * @return value
     */
    static public String getPreference(Context context, String prefKey){
        String result="";
        try {
            SecurePreferences preferences = new SecurePreferences(context, preferenceName, secureKey, true);
            result = preferences.getString(prefKey);
        } catch (Exception e){
            Common.log("Common getPreference:"+e.toString());
        }
        if(result==null)result="";
        return result;
    }

    /**
     * 기기 저장값 저장
     * @param context context
     * @param prefKey key
     * @param prefValue value
     */
    static public void setPreference(Context context, String prefKey, String prefValue){
        try {
            SecurePreferences preferences = new SecurePreferences(context, preferenceName, secureKey, true);
            preferences.put(prefKey, prefValue);
        } catch (Exception e){
            Common.log("Common setPreference:"+e.toString());
        }
    }

    //테두리와 배경색 적용
    static public void setBorderBg(View view, int bgColor, int borderColor, int borderWidth, int cornerRadius){
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(bgColor); // Changes this drawbale to use a single color instead of a gradient
        gd.setCornerRadius(cornerRadius);
        gd.setStroke(borderWidth, borderColor);
        if(Build.VERSION.SDK_INT>=16)view.setBackground(gd);
    }

    //color
    static public int getColor(Context context,int id){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getColor(id);
        } else {
            return context.getResources().getColor(id);
        }
    }

    //numberFormat
    static public String numberFormat(int num){
        NumberFormat nf = NumberFormat.getInstance();
        return nf.format(num);
    }

    //get Listitem
    static public View getListitem(View child){
        View parent=null;
        while(!(parent instanceof ListView)){
            child=(View)child.getParent();
            parent=(View)child.getParent();
        }
        return child;
    }

    //longToDatetime
    static public String longToDatetime(long value){
        Date date=new Date(value);
        Format format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    public static String uriToPath(Context context, Uri uri) {
        boolean isKitKat = Build.VERSION.SDK_INT >= 19;
        if(isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            String docId;
            String[] split;
            String type;
            if(isExternalStorageDocument(uri)) {
                docId = DocumentsContract.getDocumentId(uri);
                split = docId.split(":");
                type = split[0];
                if("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else {
                if(isDownloadsDocument(uri)) {
                    docId = DocumentsContract.getDocumentId(uri);
                    Uri split1 = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId).longValue());
                    return getDataColumn(context, split1, (String)null, (String[])null);
                }

                if(isMediaDocument(uri)) {
                    docId = DocumentsContract.getDocumentId(uri);
                    split = docId.split(":");
                    type = split[0];
                    Uri contentUri = null;
                    if("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if("video".equals(type)) {
                        contentUri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if("audio".equals(type)) {
                        contentUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    String selection = "_id=?";
                    String[] selectionArgs = new String[]{split[1]};
                    return getDataColumn(context, contentUri, "_id=?", selectionArgs);
                }
            }
        } else {
            if("content".equalsIgnoreCase(uri.getScheme())) {
                return getDataColumn(context, uri, (String)null, (String[])null);
            }

            if("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = "_data";
        String[] projection = new String[]{"_data"};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, (String)null);
            if(cursor != null && cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow("_data");
                String var8 = cursor.getString(column_index);
                return var8;
            }
        } finally {
            if(cursor != null) {
                cursor.close();
            }

        }

        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
