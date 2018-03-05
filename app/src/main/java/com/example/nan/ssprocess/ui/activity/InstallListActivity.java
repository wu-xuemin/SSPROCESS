package com.example.nan.ssprocess.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.basic.TaskMachineListData;
import com.example.nan.ssprocess.net.Network;

import java.io.File;
import java.util.ArrayList;

public class InstallListActivity extends AppCompatActivity {
    private static String TAG = "nlgInstallListActivity";
    private ProgressDialog mDownloadingDialog=null;

    private static final String[][] MIME_MAP_TABLE = {
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls", "application/vnd.ms-excel"},
            {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".exe", "application/octet-stream"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop", "text/plain"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".txt", "text/plain"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wps", "application/vnd.ms-works"},
            {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/x-zip-compressed"},
            {"", "*/*"}
    };
    /**
     * 根据文件后缀名匹配MIMEType
     *
     * @param file
     * @return
     */
    public static String getMIMEType(File file) {
        String type = "*/*";
        String name = file.getName();
        int index = name.lastIndexOf('.');
        if (index < 0) {
            return type;
        }
        String end = name.substring(index, name.length()).toLowerCase();
        if (TextUtils.isEmpty(end)) {
            return type;
        }

        for (int i = 0; i < MIME_MAP_TABLE.length; i++) {
            if (end.equals(MIME_MAP_TABLE[i][0])) {
                type = MIME_MAP_TABLE[i][1];
            }
        }
        return type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_list);
        //返回前页按钮
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        //获取传递过来的信息
        Intent intent = getIntent();
        final ArrayList<String> mInstallFileList = (ArrayList<String>) intent.getSerializableExtra("mInstallFileList");
        ListView fileListView = findViewById(R.id.file_lv);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                InstallListActivity.this, android.R.layout.simple_list_item_1, mInstallFileList);
        fileListView.setAdapter(adapter);
        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String downloadFileUrl = URL.HTTP_HEAD + SinSimApp.getApp().getServerIP() + URL.DOWNLOAD_DIR + "/" + mInstallFileList.get(position);
                Network.Instance(SinSimApp.getApp()).downloadFile(downloadFileUrl, new DownloadFileHandler());
                //第一次进入刷新页面， 加载loading页面
                if( mDownloadingDialog == null) {
                    mDownloadingDialog = new ProgressDialog(InstallListActivity.this);
                    mDownloadingDialog.setCancelable(false);
                    mDownloadingDialog.setCanceledOnTouchOutside(false);
                    mDownloadingDialog.setMessage("下载中...");
                }
                mDownloadingDialog.show();
            }
        });
    }

    @SuppressLint("HandlerLeak")
    private class DownloadFileHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if(mDownloadingDialog != null && mDownloadingDialog.isShowing()) {
                mDownloadingDialog.dismiss();
            }
            if (msg.what == Network.OK) {
                String downloadFile = (String) msg.obj;
                Toast.makeText(InstallListActivity.this, downloadFile, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                File file = new File(downloadFile);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Intent.ACTION_VIEW);
                String type = getMIMEType(file);
                //设置intent的data和Type属性。
                intent.setDataAndType(Uri.fromFile(file), type);
                try {
                    InstallListActivity.this.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(InstallListActivity.this, "您没有安装Office文件", Toast.LENGTH_SHORT).show();
                }
            } else {
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "DownloadFileHandler handleMessage: "+errorMsg);
                Toast.makeText(InstallListActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDownloadingDialog!=null) {
            mDownloadingDialog.dismiss();
        }
    }
}
