package com.example.heartrate;
import android.content.Intent;
import android.os.Bundle;

import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
public class SearchUiSampler extends HeartRateDisplayBase {
    @Override
    protected void OnCreate(Bundle savedInstanceState) {
        showDataDisplay("Connecting...");
        super.onCreate(savedInstanceState);
    }
    @Override
    protected void RequestAccessToPcc() {
        releaseHandle = AntPlusHeartRatePcc.requestAccess(this, this, base_IPluginAccessResultReceiver, base_IDeviceStateChangeReceiver);
    }
}
