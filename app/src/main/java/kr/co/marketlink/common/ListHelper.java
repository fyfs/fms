package kr.co.marketlink.common;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import kr.co.marketlink.fms.R;

/**
 * Created by yangjaesang on 2017. 1. 21..
 */

public class ListHelper {
    static public class SimpleListItem {
        public String label="";
        public int value=-1;
        public SimpleListItem(String label, int value) {
            this.label = label;
            this.value = value;
        }
    }
    static public class SimpleListAdapter extends ArrayAdapter<SimpleListItem>{
        private List<SimpleListItem> items;
        private LayoutInflater inflater;

        public SimpleListAdapter(Context context, int resource, List<SimpleListItem> objects) {
            super(context, resource, objects);
            items=objects;
            inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                convertView=inflater.inflate(R.layout.ml_simple_listitem,null);
            }
            SimpleListItem item = items.get(position);
            if(item!=null){
                TextView tv_simpleListItem=(TextView)convertView.findViewById(R.id.tv_simpleListItem);
                tv_simpleListItem.setText(item.label);
            }
            return convertView;
        }
    }
    //바닥 스크롤 처리
    static public class OnScrollListener implements AbsListView.OnScrollListener{
        private I_bottomScrollHandler bottomScrollHandler;
        public OnScrollListener(I_bottomScrollHandler bottomScrollHandler) {
            this.bottomScrollHandler=bottomScrollHandler;
        }
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {}

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if(firstVisibleItem+visibleItemCount==totalItemCount)bottomScrollHandler.bottomScroll();
        }
        public interface I_bottomScrollHandler{
            public void bottomScroll();
        }
    }
}
