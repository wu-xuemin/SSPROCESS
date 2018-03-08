package com.example.nan.ssprocess.ui.activity;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.adapter.ProcessToInstallAdapter;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.service.MyMqttService;
import com.example.nan.ssprocess.ui.fragment.TabInstallPlanFragment;
import com.example.nan.ssprocess.ui.fragment.TabInstallReadyFragment;

import java.util.ArrayList;

/**
 * @author nan  2017/11/16
 */
public class ProcessToInstallActivity extends AppCompatActivity {
    private static String TAG = "nlgProcessToInstall";
    Intent mqttIntent;
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
        mqttIntent = new Intent(this, MyMqttService.class);
        startService(mqttIntent);

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        //ViewPager的适配器
        ProcessToInstallAdapter adapter = new ProcessToInstallAdapter(getSupportFragmentManager(), titleList, fragmentList);
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
                stopService(mqttIntent);
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

}
