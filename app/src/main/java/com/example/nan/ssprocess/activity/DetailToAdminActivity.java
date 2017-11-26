package com.example.nan.ssprocess.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.service.MyMqttService;

public class DetailToAdminActivity extends AppCompatActivity {

    private static final String TAG="nlgDetailToAdmin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_to_admin);

        Button publishButton = findViewById(R.id.publish_button);

        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyMqttService.publishMessage("done");
            }
        });
    }
}
