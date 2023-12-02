package com.dk.headsetbatteryinfosender;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.app.Service;
import android.content.*;
import android.os.*;
import android.widget.Toast;
import android.util.Log;

public class BackgroundService extends Service {

    public Context context = this;
    public Handler handler = null;
    public static Runnable runnable = null;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        handler = new Handler();
        runnable = new Runnable()
        {
            public void run()
            {
                MainActivity.UpdateBatteryLevelAndSend();

                handler.postDelayed(runnable, 5000);
            }
        };

        handler.postDelayed(runnable, 1500);
    }

    @Override
    public void onDestroy()
    {
        // IF YOU WANT THIS SERVICE KILLED WITH THE APP THEN UNCOMMENT THE FOLLOWING LINE
        // handler.removeCallbacks(runnable)

        MainActivity.instance.StartBackgroundService();
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
    }
}