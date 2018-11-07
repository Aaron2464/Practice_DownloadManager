package com.kerker.practice_downloadmanager;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button btn;
    DownloadManager DM;
    DownloadManager.Request request;
    private long LatestDownloadID;
    String URL;
    DialogFragmentHelper newFragment;
    //"content://downloads/my_downloads"必须这样写不可更改
    public static final Uri CONTENT_URI = Uri.parse("content://downloads/my_downloads");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DownloadNewVersion();
            }
        });
    }


    private void DownloadNewVersion() {
        newFragment = new DialogFragmentHelper();
        newFragment.show(getFragmentManager(), "download apk");
        DM = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        URL = "";
        Uri uri = Uri.parse(URL);
        request = new DownloadManager.Request(uri);
        request.setMimeType("application/vnd.android.package-archive");//設置MIME為Android APK檔
            DownloadManagerEnqueue();
    }
    private void DownloadManagerEnqueue() {
        //創建目錄
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdir();
        //設定APK儲存位置
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "DG_App.apk");
        DownloadCompleteReceiver receiver = new DownloadCompleteReceiver(getApplicationContext());
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));//註冊DOWNLOAD_COMPLETE-BroadcastReceiver
        DownloadObserver downloadObserver = new DownloadObserver(null);
        getContentResolver().registerContentObserver(CONTENT_URI, true, downloadObserver);//註冊ContentObserver
        LatestDownloadID = DM.enqueue(request);
        SharedPreferencesHelper sp = new SharedPreferencesHelper(getApplicationContext());
        sp.setDownloadID(LatestDownloadID);//儲存DownloadID
    }
    class DownloadObserver extends ContentObserver {
        public DownloadObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(LatestDownloadID);
            DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            final Cursor cursor = dm.query(query);
            if (cursor != null && cursor.moveToFirst()) {
                final int totalColumn = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                final int currentColumn = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                int totalSize = cursor.getInt(totalColumn);
                int currentSize = cursor.getInt(currentColumn);
                float percent = (float) currentSize / (float) totalSize;
                final int progress = Math.round(percent * 100);
                runOnUiThread(new Runnable() {//確保在UI Thread執行
                    @Override
                    public void run() {
                        newFragment.setProgress(progress);
                    }
                });
            }
        }
    }
}
