package com.example.nan.ssprocess.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.nan.ssprocess.R;

/**
 * @author nan  2017/11/16
 */
public class ProcessToAdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_to_admin);
        Button scanQrcodeBotton = (Button) findViewById(R.id.scan_qrcode_button);
        scanQrcodeBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ProcessToAdminActivity.this,ScanQrcodeActivity.class);
                startActivity(intent);
            }
        });
    }
}
