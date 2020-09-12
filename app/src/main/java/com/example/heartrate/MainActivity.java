package com.example.heartrate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    private Button buttonscan;
    private Button searchuisampler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonscan = (Button) findViewById(R.id.hrmscan);
        buttonscan.setText("Async Heart Rate Scan");
        buttonscan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAsyncScan();
            }
        });
        searchuisampler = (Button) findViewById(R.id.uiscan);
        searchuisampler.setText("Ui scan");
        searchuisampler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                opensearchScan();
            }
        });
    }
    public void openAsyncScan() {
        Intent intent = new Intent(this, AsyncHeartRateScan.class);
        startActivity(intent);
    }
    public void opensearchScan() {
        Intent i = new Intent(this, SearchUiSampler.class);
        startActivity(i);
    }
}
