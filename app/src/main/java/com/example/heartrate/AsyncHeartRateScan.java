package com.example.heartrate;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IPluginAccessResultReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AsyncScanController;
import com.dsi.ant.plugins.antplus.pccbase.AsyncScanController.AsyncScanResultDeviceInfo;
import com.dsi.ant.plugins.antplus.pccbase.AsyncScanController.IAsyncScanResultReceiver;

import java.util.ArrayList;


public class AsyncHeartRateScan extends HeartRateDisplayBase {
    TextView mTextView_Status;
    ArrayList<AsyncScanController.AsyncScanResultDeviceInfo> mAlreadyConnectedDeviceInfos;
    ArrayList<AsyncScanController.AsyncScanResultDeviceInfo> mScannedDeviceInfos;
    ArrayAdapter<String> adapter_devNameList;
    ArrayAdapter<String> adapter_connDevNameList;

    AsyncScanController<AntPlusHeartRatePcc> hrScanCtrl;
    @Override
    protected void OnCreate(Bundle savedInstanceState) {
        initScanDisplay();
    }

    private void initScanDisplay()
    {
        setContentView(R.layout.activity_async_scan);
        mTextView_Status = findViewById(R.id.textView_Status);
        mTextView_Status.setText("Scanning for heart rate devices asynchronously...");

        mAlreadyConnectedDeviceInfos = new ArrayList<AsyncScanResultDeviceInfo>();
        mScannedDeviceInfos = new ArrayList<AsyncScanController.AsyncScanResultDeviceInfo>();

        adapter_connDevNameList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        ListView listView_alreadyConnectedDevs = findViewById(R.id.listView_AlreadyConnectedDevices);
        listView_alreadyConnectedDevs.setAdapter(adapter_connDevNameList);
        listView_alreadyConnectedDevs.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
            {
                requestConnectToResult(mScannedDeviceInfos.get(pos));
            }
        });
        adapter_devNameList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        ListView listView_Devices = findViewById(R.id.listView_FoundDevices);
        listView_Devices.setAdapter(adapter_devNameList);
        listView_Devices.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                requestConnectToResult(mScannedDeviceInfos.get(pos));
            }
        });
    }
    protected void requestConnectToResult(final AsyncScanResultDeviceInfo asyncScanResultDeviceInfo)
    {
        //Inform the user we are connecting
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                mTextView_Status.setText("Connecting to " + asyncScanResultDeviceInfo.getDeviceDisplayName());
                releaseHandle = hrScanCtrl.requestDeviceAccess(asyncScanResultDeviceInfo,
                        new IPluginAccessResultReceiver<AntPlusHeartRatePcc>()
                        {
                            @Override
                            public void onResultReceived(AntPlusHeartRatePcc result,
                                                         RequestAccessResult resultCode, DeviceState initialDeviceState)
                            {
                                if(resultCode == RequestAccessResult.SEARCH_TIMEOUT)
                                {
                                    //On a connection timeout the scan automatically resumes, so we inform the user, and go back to scanning
                                    runOnUiThread(new Runnable()
                                    {
                                        public void run()
                                        {
                                            Toast.makeText(AsyncHeartRateScan.this, "Timed out attempting to connect, try again", Toast.LENGTH_LONG).show();
                                            mTextView_Status.setText("Scanning for heart rate devices asynchronously...");
                                        }
                                    });
                                }
                                else
                                {
                                    //Otherwise the results, including SUCCESS, behave the same as
                                    base_IPluginAccessResultReceiver.onResultReceived(result, resultCode, initialDeviceState);
                                    hrScanCtrl = null;
                                }
                            }
                        }, base_IDeviceStateChangeReceiver);
            }
        });
    }

    @Override
    protected void RequestAccessToPcc() {
        initScanDisplay();
        hrScanCtrl = AntPlusHeartRatePcc.requestAsyncScanController(this, 0, new IAsyncScanResultReceiver() {
            @Override
            public void onSearchStopped(RequestAccessResult reasonStopped) {
                base_IPluginAccessResultReceiver.onResultReceived(null, reasonStopped, DeviceState.DEAD);
            }

            @Override
            public void onSearchResult(final AsyncScanResultDeviceInfo deviceFound) {
                for(AsyncScanResultDeviceInfo i: mScannedDeviceInfos)
                {
                    if(i.getAntDeviceNumber() == deviceFound.getAntDeviceNumber())
                    {
                        return;
                    }
                }
                if(deviceFound.isAlreadyConnected())
                {
                    mAlreadyConnectedDeviceInfos.add(deviceFound);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(adapter_devNameList.isEmpty())
                            {
                                findViewById(R.id.listView_AlreadyConnectedDevices).setVisibility(View.VISIBLE);
                                findViewById(R.id.textView_AlreadyConnectedTitle).setVisibility(View.VISIBLE);
                            }
                            adapter_connDevNameList.add(deviceFound.getDeviceDisplayName());
                            adapter_connDevNameList.notifyDataSetChanged();
                        }
                    });
                }
                else {
                    mScannedDeviceInfos.add(deviceFound);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter_devNameList.add(deviceFound.getDeviceDisplayName());
                            adapter_devNameList.notifyDataSetChanged();
                        }
                    });
                }
            }
        });

    }
    @Override
    public void handleReset()
    {
        if(hrScanCtrl != null)
        {
            hrScanCtrl.closeScanController();
            hrScanCtrl = null;
        }
        super.handleReset();
    }
    @Override
    protected void onDestroy()
    {
        if(hrScanCtrl != null)
        {
            hrScanCtrl.closeScanController();
            hrScanCtrl = null;
        }
        super.onDestroy();
    }
}