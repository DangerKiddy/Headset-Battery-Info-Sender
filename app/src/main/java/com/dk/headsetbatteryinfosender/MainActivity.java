package com.dk.headsetbatteryinfosender;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.illposed.osc.*;
import com.illposed.osc.argument.ArgumentHandler;
import com.illposed.osc.argument.handler.Activator;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private static final String HEADSET_COMPANY = "pico";
    public static MainActivity instance;
    private OSCSerializerAndParserBuilder serializer;
    private OSCController controller;
    private TextView statusText;
    private TextView errorText;
    private Button scanAgain;
    private Handler handler = new Handler();

    private static int lastBatteryLevel = -1;
    private static boolean wasCharging = false;

    private SharedPreferences sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        setupSerializer();

        statusText = (TextView)findViewById(R.id.StatusText);
        errorText = (TextView)findViewById(R.id.ErrorText);
        SetStatusText("Launching");

        scanAgain = (Button)findViewById(R.id.ScanAgain);
        scanAgain.setOnClickListener(view -> {
            Log.i("OSC","Re-scanning network");
            SetStatusText("Re-scanning network");

            SetLastUsedDeviceName("");
            CloseOSC();
            StartNetworkScan();
        });

        StartNetworkScan();
        InitBatteryService();
    }
    public static void SetStatusText(String text)
    {
        instance.handler.post(() -> instance.statusText.setText(text));
    }

    public static void SetErrorText(String text)
    {
        instance.handler.post(() -> instance.errorText.setText(text));
    }

    public static void StartNetworkScan()
    {
        instance.scanAgain.setEnabled(false);

        Context context = instance.getApplicationContext();
        new NetworkScanTask(context).execute();
    }
    public static void FinishNetworkScan()
    {
        lastBatteryLevel = -1;
        wasCharging = false;

        instance.handler.post(() -> instance.scanAgain.setEnabled(true));
    }

    private void InitBatteryService()
    {
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        nm.createNotificationChannel(new NotificationChannel("HBI_NTF_CHNL", "Something", NotificationManager.IMPORTANCE_DEFAULT));

        Intent batteryServiceIntent = new Intent(this, BatteryService.class);

        startService(batteryServiceIntent);
    }

    public static OSCSerializerAndParserBuilder GetSerializer()
    {
        return instance.serializer;
    }
    public static void SetOSCController(String host, String deviceNetworkName)
    {
        SetLastUsedDeviceName(deviceNetworkName);

        instance.controller = new OSCController(host, GetSerializer());
        instance.controller.CreateListener();
        instance.controller.CreateSender();
    }
    public static void SetLastUsedDeviceName(String deviceNetworkName)
    {
        SharedPreferences.Editor editor = instance.sharedPref.edit();
        editor.putString("deviceName", deviceNetworkName);
        editor.apply();
    }
    public static void CloseOSC()
    {
        if (instance.controller != null) {
            instance.controller.Close();
        }
    }

    public static String TryGetLastDeviceIp()
    {
        return instance.sharedPref.getString("deviceName", "");
    }

    public static void NotifyHeadsetCompany()
    {
        instance.controller.Send("/battery/headset/company", HEADSET_COMPANY);
    }
    void setupSerializer()
    {
        serializer = new OSCSerializerAndParserBuilder();
        serializer.setUsingDefaultHandlers(false);

        List<ArgumentHandler> defaultParserTypes = Activator.createSerializerTypes();
        defaultParserTypes.remove(16);

        char typeChar = 'a';
        for (ArgumentHandler argumentHandler:defaultParserTypes) {
            serializer.registerArgumentHandler(argumentHandler, typeChar);
            typeChar++;
        }
    }

    public static void GetBatteryInfoAndSend()
    {
        BatteryManager bm = (BatteryManager) instance.getApplicationContext().getSystemService(BATTERY_SERVICE);
        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        int batStatus = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS);

        boolean isCharging = batStatus == BatteryManager.BATTERY_STATUS_CHARGING;
        SendBatteryLevel(batLevel, isCharging);
    }

    public static void SendBatteryLevel(int batteryLevel, boolean isCharging) {
        instance.sendBatteryLevel(batteryLevel, isCharging);
    }
    void sendBatteryLevel(int batteryLevel, boolean isCharging) {
        if (controller != null) {
            if (lastBatteryLevel != batteryLevel)
            {
                lastBatteryLevel = batteryLevel;

                controller.Send("/battery/headset/level", batteryLevel);
            }

            if ((!wasCharging && isCharging) || (wasCharging && !isCharging))
            {
                wasCharging = isCharging;

                controller.Send("/battery/headset/charging", isCharging);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}