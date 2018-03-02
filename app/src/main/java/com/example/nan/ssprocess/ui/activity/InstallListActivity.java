package com.example.nan.ssprocess.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.util.ArrayList;

public class InstallListActivity extends AppCompatActivity {
    private static String TAG = "nlgInstallListActivity";
    private Network mNetwork;

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
                String downloadFileUrl = URL.HTTP_HEAD + SinSimApp.getApp().getServerIP() + "/" + mInstallFileList.get(position);
                mNetwork.downloadFile(downloadFileUrl, new DownloadFileHandler());
            }
        });
    }

    @SuppressLint("HandlerLeak")
    private class DownloadFileHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {

            if (msg.what == Network.OK) {
                String downloadFileMsg = (String) msg.obj;
                Toast.makeText(InstallListActivity.this, downloadFileMsg, Toast.LENGTH_SHORT).show();
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
}
