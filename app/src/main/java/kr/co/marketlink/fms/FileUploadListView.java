package kr.co.marketlink.fms;

import android.graphics.drawable.Drawable;

/**
 * Created by wtkim on 2017. 3. 22..
 */

public class FileUploadListView {
    private Drawable iconDrawable;
    private String titleStr;
    private String descStr;

    public Drawable getIconDrawable() {
        return iconDrawable;
    }

    public void setIconDrawable(Drawable iconDrawable) {
        this.iconDrawable = iconDrawable;
    }

    public String getTitleStr() {
        return titleStr;
    }

    public void setTitleStr(String titleStr) {
        this.titleStr = titleStr;
    }

    public String getDescStr() {
        return descStr;
    }

    public void setDescStr(String descStr) {
        this.descStr = descStr;
    }
}
