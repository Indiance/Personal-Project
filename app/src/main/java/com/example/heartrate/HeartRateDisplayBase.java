package com.example.heartrate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.DataState;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.ICalculatedRrIntervalReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.IHeartRateDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.IPage4AddtDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.RrFlag;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IDeviceStateChangeReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IPluginAccessResultReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusCommonPcc.IRssiReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusLegacyCommonPcc.ICumulativeOperatingTimeReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusLegacyCommonPcc.IManufacturerAndSerialReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusLegacyCommonPcc.IVersionAndModelReceiver;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public abstract class HeartRateDisplayBase extends Activity {
    protected abstract void OnCreate(Bundle savedInstanceState);

    protected abstract void RequestAccessToPcc();

    AntPlusHeartRatePcc hrPcc = null;
    protected PccReleaseHandle<AntPlusHeartRatePcc> releaseHandle = null;
    TextView tv_status;

    TextView tv_estTimestamp;


    TextView tv_computedHeartRate;
    TextView tv_heartBeatCounter;
    TextView tv_heartBeatEventTime;

    TextView tv_calculatedRrInterval;

    TextView tv_manufacturerID;
    TextView tv_serialNumber;

    TextView tv_hardwareVersion;
    TextView tv_softwareVersion;
    TextView tv_modelNumber;

    TextView tv_dataStatus;
    ArrayList<Double> numArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        numArray = new ArrayList<>();

        handleReset();
    }

    protected void handleReset() {
        if (releaseHandle != null) {
            releaseHandle.close();
        }
        RequestAccessToPcc();
    }

    protected void showDataDisplay(String status) {
        setContentView(R.layout.activity_heart_rate);

        tv_status = findViewById(R.id.textView_Status);

        tv_estTimestamp = findViewById(R.id.textView_EstTimestamp);

        tv_computedHeartRate = findViewById(R.id.textView_ComputedHeartRate);
        tv_heartBeatCounter = findViewById(R.id.textView_HeartBeatCounter);
        tv_heartBeatEventTime = findViewById(R.id.textView_HeartBeatEventTime);

        tv_calculatedRrInterval = findViewById(R.id.textView_CalculatedRrInterval);


        tv_manufacturerID = findViewById(R.id.textView_ManufacturerID);
        tv_serialNumber = findViewById(R.id.textView_SerialNumber);

        tv_hardwareVersion = findViewById(R.id.textView_HardwareVersion);
        tv_softwareVersion = findViewById(R.id.textView_SoftwareVersion);
        tv_modelNumber = findViewById(R.id.textView_ModelNumber);

        tv_dataStatus = findViewById(R.id.textView_DataStatus);

        //Reset the text display
        tv_status.setText(status);

        tv_estTimestamp.setText("---");

        tv_computedHeartRate.setText("---");
        tv_heartBeatCounter.setText("---");
        tv_heartBeatEventTime.setText("---");

        tv_calculatedRrInterval.setText("---");


        tv_manufacturerID.setText("---");
        tv_serialNumber.setText("---");

        tv_hardwareVersion.setText("---");
        tv_softwareVersion.setText("---");
        tv_modelNumber.setText("---");
        tv_dataStatus.setText("---");
    }
    public void SubscribetoHrEvents() {

        hrPcc.subscribeHeartRateDataEvent(new IHeartRateDataReceiver() {
            @Override
            public void onNewHeartRateData(final long estTimestamp, EnumSet<EventFlag> eventFlags,final int computedHeartRate,final long heartBeatCount,final BigDecimal heartBeatEventTime,final DataState dataState)
            {
                // Mark Heart Rate with an Asterisk if not detected
                final String textHeartRate = computedHeartRate
                        + ((DataState.ZERO_DETECTED.equals(dataState))? "*":"");

                //Mark heart beat count and heart beat event time with asterisk if initial value
                final String textHeartBeatCount = heartBeatCount
                        + ((DataState.ZERO_DETECTED.equals(dataState))? "*":"");
                final String textHeartBeatEventTime = heartBeatEventTime
                        + ((DataState.ZERO_DETECTED.equals(dataState))? "*":"");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_estTimestamp.setText(String.valueOf(estTimestamp));

                        tv_computedHeartRate.setText(textHeartRate);
                        tv_heartBeatCounter.setText(textHeartBeatCount);
                        tv_heartBeatEventTime.setText(textHeartBeatEventTime);

                        tv_dataStatus.setText(dataState.toString());
                    }
                });
            }
        });
        hrPcc.subscribePage4AddtDataEvent(new IPage4AddtDataReceiver() {
            @Override
            public void onNewPage4AddtData(final long estTimeStamp, EnumSet<EventFlag> eventFlags,final int manufacturerSpecificByte,final BigDecimal previousHeartBeatEventTime)
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_estTimestamp.setText(String.valueOf(estTimeStamp));
                    }
                });
            }
        });
        hrPcc.subscribeCumulativeOperatingTimeEvent(new ICumulativeOperatingTimeReceiver() {
            @Override
            public void onNewCumulativeOperatingTime(final long estTimestamp, EnumSet<EventFlag> eventFlags,final long cumulativeOperatingTime)
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_estTimestamp.setText(String.valueOf(estTimestamp));
                    }
                });
            }
        });
        hrPcc.subscribeManufacturerAndSerialEvent(new IManufacturerAndSerialReceiver() {
            @Override
            public void onNewManufacturerAndSerial(final long estTimestamp, EnumSet<EventFlag> eventFlags, final int manufacturerID, final int serialNumber)
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_estTimestamp.setText(String.valueOf(estTimestamp));

                        tv_serialNumber.setText(String.valueOf(serialNumber));
                    }
                });
            }
        });
        hrPcc.subscribeVersionAndModelEvent(new IVersionAndModelReceiver() {
            @Override
            public void onNewVersionAndModel(final long estTimestamp, EnumSet<EventFlag> eventFlags,final int hardwareVersion,final int softwareVersion,final int modelNumber)
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_estTimestamp.setText(String.valueOf(estTimestamp));

                        tv_hardwareVersion.setText(String.valueOf(hardwareVersion));
                        tv_softwareVersion.setText(String.valueOf(softwareVersion));
                        tv_modelNumber.setText(String.valueOf(modelNumber));
                    }
                });
            }
        });
        hrPcc.subscribeCalculatedRrIntervalEvent(new ICalculatedRrIntervalReceiver() {
            @Override
            public void onNewCalculatedRrInterval(final long estTimestamp, EnumSet<EventFlag> eventFlags, final BigDecimal rrInterval, final RrFlag flag) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Mark RR with asterisk if source is not cached or page 4
                        if (flag.equals(RrFlag.DATA_SOURCE_CACHED)
                                || flag.equals(RrFlag.DATA_SOURCE_PAGE_4))
                            tv_calculatedRrInterval.setText(String.valueOf(rrInterval));
                        else
                            tv_calculatedRrInterval.setText(rrInterval + "*");
                        numArray.add(rrInterval.doubleValue());
                        double sdev = CalculateSD(numArray);
                        tv_manufacturerID.setText(String.valueOf(sdev));
                    }
                });
            }
        });
        hrPcc.subscribeRssiEvent(new IRssiReceiver() {
            @Override
            public void onRssiData(final long estTimestamp, EnumSet<EventFlag> eventFlags,final int rssi)
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_estTimestamp.setText(String.valueOf(estTimestamp));
                    }
                });
            }
        });
    }
    protected IPluginAccessResultReceiver<AntPlusHeartRatePcc> base_IPluginAccessResultReceiver = new IPluginAccessResultReceiver<AntPlusHeartRatePcc>() {
        @Override
        public void onResultReceived(AntPlusHeartRatePcc result, RequestAccessResult resultCode, DeviceState initialDeviceState)
        {
            showDataDisplay("Connecting");
            switch (resultCode) {
                case SUCCESS:
                    hrPcc = result;
                    tv_status.setText(result.getDeviceName() + ": " + initialDeviceState);
                    SubscribetoHrEvents();
                    if (!result.supportsRssi()) ;
                    break;
                case USER_CANCELLED:
                    break;
                case CHANNEL_NOT_AVAILABLE:
                    Toast.makeText(HeartRateDisplayBase.this,"Channel not available",Toast.LENGTH_SHORT).show();
                    tv_status.setText("Error. Do Menu->Reset");
                    break;
                case OTHER_FAILURE:
                    Toast.makeText(HeartRateDisplayBase.this,"RequestAccessFailed. Check logcat for more details",Toast.LENGTH_SHORT).show();
                    tv_status.setText("Error. Do Menu->Reset ");
                    break;
                case DEPENDENCY_NOT_INSTALLED:
                    tv_status.setText("Error. Do Menu->Reset");
                    AlertDialog.Builder adlgBldr = new AlertDialog.Builder(HeartRateDisplayBase.this);
                    adlgBldr.setTitle("Missing Dependancy");
                    adlgBldr.setMessage("The required service\n\"" + AntPlusHeartRatePcc.getMissingDependencyName() + "\"\n was not found. You need to install the ANT+ Plugins service or you may need to update your existing version if you already have it. Do you want to launch the Play Store to get it?");
                    adlgBldr.setCancelable(true);
                    adlgBldr.setPositiveButton("Go to store", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent startStore = null;
                            startStore = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + AntPlusHeartRatePcc.getMissingDependencyPackageName()));
                            startStore.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            HeartRateDisplayBase.this.startActivity(startStore);
                        }
                    });
                    adlgBldr.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    final AlertDialog waitDialog = adlgBldr.create();
                    waitDialog.show();
                    break;
                case DEVICE_ALREADY_IN_USE:
                    break;
                case SEARCH_TIMEOUT:
                    break;
                case ALREADY_SUBSCRIBED:
                    break;
                case BAD_PARAMS:
                    Toast.makeText(HeartRateDisplayBase.this, "Bad request parameters.", Toast.LENGTH_SHORT).show();
                    tv_status.setText("Error. Do Menu->Reset");
                    break;
                case ADAPTER_NOT_DETECTED:
                    Toast.makeText(HeartRateDisplayBase.this, "ANT Adapter Not Available. Built-in ANT hardware or external adapter required.", Toast.LENGTH_SHORT).show();
                    tv_status.setText("Error. Do Menu->Reset");
                    break;
                case UNRECOGNIZED:
                    Toast.makeText(HeartRateDisplayBase.this,"Failed: UNRECOGNIZED. PluginLib Upgrade Required?",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(HeartRateDisplayBase.this, "Unrecognized result: " + resultCode, Toast.LENGTH_SHORT).show();
                    tv_status.setText("Error. Do Menu->Reset.");
                    break;
            }
        }
    };
    protected IDeviceStateChangeReceiver base_IDeviceStateChangeReceiver = new IDeviceStateChangeReceiver() {
        @Override
        public void onDeviceStateChange(final DeviceState newdeviceState) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_status.setText(hrPcc.getDeviceName() + ":" + newdeviceState);
                }
            });
        }
    };
    @Override
    protected void onDestroy()
    {
        if(releaseHandle != null)
        {
            releaseHandle.close();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_heart_rate, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_reset:
                handleReset();
                tv_status.setText("Resetting.....");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public double CalculateSD(List<Double> numArray) {
        double sum = 0.0, standardDeviation = 0.0;
        int len = numArray.size();
        for (double num : numArray) {
            sum += num;
        }
        double mean = sum / len;
        for (double num : numArray) {
            standardDeviation += Math.pow(num - mean, 2);
        }
        return Math.sqrt(standardDeviation / len);
    }
}