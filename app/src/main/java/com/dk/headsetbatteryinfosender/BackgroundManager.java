package com.dk.headsetbatteryinfosender;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class BackgroundManager {

    public static void startWork() {
        Constraints constraints = new Constraints.Builder().build();

        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(
                BackgroundWorker.class,
                1, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance().enqueue(periodicWorkRequest);
    }

    public static void cancelWork() {
        WorkManager.getInstance().cancelAllWorkByTag("YOUR_WORK_TAG");
    }
}