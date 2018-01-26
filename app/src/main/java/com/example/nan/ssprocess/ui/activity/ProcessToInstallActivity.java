package com.example.nan.ssprocess.ui.activity;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.adapter.ProcessToInstallAdapter;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.bean.basic.TaskMachineListData;
import com.example.nan.ssprocess.ui.fragment.TabInstallPlanFragment;
import com.example.nan.ssprocess.ui.fragment.TabInstallReadyFragment;

import java.util.ArrayList;
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

    //tab数据源
    private ArrayList<String> titleList = new ArrayList<String>() {{
        add("计划安装");
        add("非计划安装");
    }};

    //页面数据源
    private ArrayList<Fragment> fragmentList = new ArrayList<Fragment>() {{
        add(new TabInstallPlanFragment());
        add(new TabInstallReadyFragment());
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_to_install);

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
                    TaskMachineListData mTaskMachineListData = new TaskMachineListData();
                    mTaskMachineListData=(TaskMachineListData)data.getSerializableExtra("mTaskMachineListData");
                    Intent intent=new Intent(this,DetailToAdminActivity.class);
                    intent.putExtra("mTaskMachineListData", mTaskMachineListData);
                    startActivity(intent);
                }
                break;
            default:
                break;
        }
    }
}
