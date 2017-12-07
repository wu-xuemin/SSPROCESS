package com.example.nan.ssprocess.activity;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.nan.ssprocess.R;
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
    private MyAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_to_install);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        //页面，数据源
        list = new ArrayList<>();
        list.add(new TabInstallPlanFragment());
        list.add(new TabInstallReadyFragment());
        //ViewPager的适配器
        adapter = new MyAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        //绑定
        tabLayout.setupWithViewPager(viewPager);
    }
    class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            return list.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return super.getPageTitle(position);
        }
    }
}
