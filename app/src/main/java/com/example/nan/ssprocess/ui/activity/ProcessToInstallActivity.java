package com.example.nan.ssprocess.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.adapter.ProcessToInstallAdapter;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.basic.TaskMachineListData;
import com.example.nan.ssprocess.net.Network;
import com.example.nan.ssprocess.service.MyMqttService;
import com.example.nan.ssprocess.ui.fragment.TabInstallPlanFragment;
import com.example.nan.ssprocess.ui.fragment.TabInstallReadyFragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author nan  2017/11/16
 */
public class ProcessToInstallActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private List<Fragment> list;
    private ProcessToInstallAdapter adapter;
    private static final int SCAN_QRCODE_START = 1;
    private static String TAG = "nlgProcessToInstall";

    /**
     * tab数据源
     */
    private ArrayList<String> titleList = new ArrayList<String>() {{
        add("计划安装");
        add("非计划安装");
    }};

    /**
     * 页面数据源
     */
    private ArrayList<Fragment> fragmentList = new ArrayList<Fragment>() {{
        add(new TabInstallPlanFragment());
        add(new TabInstallReadyFragment());
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_to_install);

        //启动MQTT服务
        Intent startIntent = new Intent(this, MyMqttService.class);
        startService(startIntent);

        Button scanQrcodeBotton = (Button) findViewById(R.id.install_scan_qrcode_button);
        scanQrcodeBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ProcessToInstallActivity.this,ScanQrcodeActivity.class);
                startActivityForResult(intent,SCAN_QRCODE_START);
            }
        });

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        //ViewPager的适配器
        adapter = new ProcessToInstallAdapter(getSupportFragmentManager(),titleList, fragmentList);
        viewPager.setAdapter(adapter);
        //绑定
        tabLayout.setupWithViewPager(viewPager,true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_logout, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                SinSimApp.getApp().setLogOut();
                Intent it = new Intent();
                it.setClass(ProcessToInstallActivity.this, LoginActivity.class);
                startActivity(it);
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case SCAN_QRCODE_START:
                // 当requestCode、resultCode同时为0时，也就是处理特定的结果
                if (resultCode == RESULT_OK)
                {
                    // 取出Intent里的Extras数据传递给跳转的activity
//                    TaskMachineListData mTaskMachineListData = new TaskMachineListData();
//                    mTaskMachineListData=(TaskMachineListData)data.getSerializableExtra("mTaskMachineListData");
//                    Intent intent=new Intent(this,DetailToInstallActivity.class);
//                    intent.putExtra("mTaskMachineListData", mTaskMachineListData);
//                    startActivity(intent);
                    String mMachineStrId = data.getStringExtra("mMachineStrId");

                    final String ip = SinSimApp.getApp().getServerIP();
                    LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
                    String fetchProcessRecordUrl = URL.HTTP_HEAD + ip + URL.FETCH_TASK_RECORD_BY_SCAN_QRCORD_TO_INSTALL;
                    mPostValue.put("page", "0");
                    mPostValue.put("machineStrId", ""+mMachineStrId);
                    Network.Instance(SinSimApp.getApp()).fetchProcessTaskRecordData(fetchProcessRecordUrl, mPostValue, new FetchProcessListDataHandler());
                }
                break;
            default:
                break;
        }
    }
    @SuppressLint("HandlerLeak")
    private class FetchProcessListDataHandler extends Handler {

        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == Network.OK) {
                ArrayList<TaskMachineListData> mScanResultList=(ArrayList<TaskMachineListData>)msg.obj;
                Log.d(TAG, "handleMessage: size: "+mScanResultList.size());
                if (mScanResultList.size()==0){
                    Toast.makeText(ProcessToInstallActivity.this, "没有内容!", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent=new Intent(ProcessToInstallActivity.this,ScanResultActivity.class);
                    intent.putExtra("mTaskMachineList", (Serializable)mScanResultList);
                    startActivity(intent);
                }
            } else {
                String errorMsg = (String)msg.obj;
                Toast.makeText(ProcessToInstallActivity.this, "连接网络失败！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
