package com.dk.headsetbatteryinfosender;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.BatteryManager;
import android.os.Bundle;

import com.illposed.osc.*;
import com.illposed.osc.argument.ArgumentHandler;
import com.illposed.osc.argument.handler.Activator;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static MainActivity instance;
    private OSCSerializerAndParserBuilder serializer;
    private OSCController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupSerializer();

        Context context = getApplicationContext();
        new NetworkScanTask(context).execute();

        BatteryReceiver batteryReceiver = new BatteryReceiver(this);
    }
    public static OSCSerializerAndParserBuilder GetSerializer()
    {
        return instance.serializer;
    }
    public static void SetOSCController(String host)
    {
        instance.controller = new OSCController(host, GetSerializer());
        instance.controller.CreateSender();
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

    private static int lastBatteryLevel = -1;
    private static boolean wasCharging = false;
    void sendBatteryLevel(int batteryLevel, boolean isCharging) {
        BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);

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