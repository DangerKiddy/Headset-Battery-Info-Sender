package com.dk.headsetbatteryinfosender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

public class BatteryReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int deviceStatus = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED,-1);
        boolean isPlugged = deviceStatus == BatteryManager.BATTERY_PLUGGED_AC || deviceStatus == BatteryManager.BATTERY_PLUGGED_USB;

        Log.i("OSC", "Received new battery info: " + level + "," + isPlugged);
        MainActivity.SendBatteryLevel(level, isPlugged);
    }
}