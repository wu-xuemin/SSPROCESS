package com.example.nan.ssprocess.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.adapter.ScanResultAdapter;
import com.example.nan.ssprocess.adapter.TaskRecordAdapter;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.basic.TaskMachineListData;
import com.example.nan.ssprocess.net.Network;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @author nan
 */
public class ScanResultActivity extends AppCompatActivity {

    private static String TAG = "nlgScanResultActivity";
    private ScanResultAdapter mScanResultAdapter;
    private AlertDialog mInstallDialog=null;
    private AlertDialog mQaDialog=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);
        //返回前页按钮
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //获取传递过来的信息
        Intent intent = getIntent();
        final ArrayList<TaskMachineListData> mScanResultList = (ArrayList<TaskMachineListData>) intent.getSerializableExtra("mTaskMachineList");

        //列表
        RecyclerView mScanResultRv = findViewById(R.id.scan_result_rv);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mScanResultRv.setLayoutManager(manager);
        mScanResultRv.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mScanResultAdapter = new ScanResultAdapter(mScanResultList);
        mScanResultRv.setAdapter(mScanResultAdapter);
        //点击跳转，把所有接收到的数据传递给下一个activity
        mScanResultAdapter.setOnItemClickListener(new ScanResultAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(final int position){
                Log.d(TAG, "onItemClick: gson :"+new Gson().toJson(mScanResultList.get(position)));
                switch (SinSimApp.getApp().getRole()){
                    case SinSimApp.LOGIN_FOR_INSTALL:
                        mInstallDialog = new AlertDialog.Builder(ScanResultActivity.this).create();
                        mInstallDialog.setMessage("是否现在开始安装？");
                        mInstallDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        mInstallDialog.setButton(AlertDialog.BUTTON_POSITIVE, "是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //TODO:先改状态再执行跳转
                                Intent intent = new Intent();
                                intent.setClass(ScanResultActivity.this,DetailToInstallActivity.class);
                                intent.putExtra("mTaskMachineListData", mScanResultList.get(position));
                                startActivity(intent);
                                finish();
                            }
                        });
                        mInstallDialog.show();

                        break;
                    case SinSimApp.LOGIN_FOR_QA:
                        mQaDialog = new AlertDialog.Builder(ScanResultActivity.this).create();
                        mQaDialog.setMessage("是否现在开始质检？");
                        mQaDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        mQaDialog.setButton(AlertDialog.BUTTON_POSITIVE, "是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //TODO:先改状态再执行跳转
                                Intent intent = new Intent();
                                intent.setClass(ScanResultActivity.this,DetailToCheckoutActivity.class);
                                intent.putExtra("mTaskMachineListData", mScanResultList.get(position));
                                startActivity(intent);
                                finish();
                            }
                        });
                        mQaDialog.show();
                        break;
                    default:
                        Toast.makeText(ScanResultActivity.this,"账号错误，请检查登入账号!", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
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
