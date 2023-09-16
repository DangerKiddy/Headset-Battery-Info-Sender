package com.dk.headsetbatteryinfosender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class BatteryReceiver extends BroadcastReceiver {
    private MainActivity mainActivity;
    public BatteryReceiver(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        IntentFilter batteryIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        mainActivity.registerReceiver(this, batteryIntentFilter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int deviceStatus = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED,-1);
        boolean isPlugged = deviceStatus == BatteryManager.BATTERY_PLUGGED_AC || deviceStatus == BatteryManager.BATTERY_PLUGGED_USB;

        mainActivity.sendBatteryLevel(level, isPlugged);
    }
}
