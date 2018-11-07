package com.kerker.practice_downloadmanager;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {
    final String SP_Name = "AutomaticallyOpenApk_SP";
    public final String DownloadID = "LoginID";
    public SharedPreferences settings;
    public SharedPreferences.Editor PE;
    public SharedPreferencesHelper(Context context) {
        settings = context.getSharedPreferences(SP_Name, 0);
        PE = settings.edit();
    }
    public void setDownloadID(long id) {//儲存DownloadID
        PE.putLong(DownloadID,id);
        PE.commit();
    }
    public long getDownloadID() {
        return settings.getLong(DownloadID, -1);
    }//取得DownloadID
}
