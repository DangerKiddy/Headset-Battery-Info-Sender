package com.dk.headsetbatteryinfosender;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;
@SuppressLint("SpecifyJobSchedulerIdRange")
public class BackgroundJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        if (MainActivity.instance != null)
            MainActivity.UpdateBatteryLevelAndSend();

        jobFinished(params, true);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}