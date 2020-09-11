package com.example.heartrate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    private Button buttonscan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonscan = (Button) findViewById(R.id.hrmscan);
        buttonscan.setText("Start Scanning");
        buttonscan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAsyncScan();
            }
        });
    }
    public void openAsyncScan() {
        Intent intent = new Intent(this, AsyncHeartRateScan.class);
        startActivity(intent);
    }
}
