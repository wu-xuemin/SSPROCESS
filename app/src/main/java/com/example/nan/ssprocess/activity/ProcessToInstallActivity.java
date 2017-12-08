package com.example.nan.ssprocess.activity;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.adapter.ProcessToInstallAdapter;
import com.example.nan.ssprocess.fragment.TabInstallPlanFragment;
import com.example.nan.ssprocess.fragment.TabInstallReadyFragment;

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

    private ArrayList<String> titleList = new ArrayList<String>() {{
        add("安装计划");
        add("非安装计划");
    }};

    private ArrayList<Fragment> fragmentList = new ArrayList<Fragment>() {{
        add(new TabInstallPlanFragment());
        add(new TabInstallReadyFragment());
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_to_install);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        //页面，数据源
        list = new ArrayList<>();
        list.add(new TabInstallPlanFragment());
        list.add(new TabInstallReadyFragment());
        //ViewPager的适配器
        adapter = new ProcessToInstallAdapter(getSupportFragmentManager(),titleList, fragmentList);
        viewPager.setAdapter(adapter);
        //绑定
        tabLayout.setupWithViewPager(viewPager,true);
    }
}
