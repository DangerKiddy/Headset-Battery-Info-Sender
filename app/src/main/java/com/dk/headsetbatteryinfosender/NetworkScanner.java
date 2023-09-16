package com.dk.headsetbatteryinfosender;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.net.InetAddress;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import com.illposed.osc.*;

class NetworkScanTask extends AsyncTask<Void, Void, Void> {
    private WeakReference<Context> mContextRef;

    public NetworkScanTask(Context context) {
        mContextRef = new WeakReference<Context>(context);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            Context context = mContextRef.get();

            if (context != null) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

                WifiInfo connectionInfo = wm.getConnectionInfo();
                int ipAddress = connectionInfo.getIpAddress();
                String ipString = Formatter.formatIpAddress(ipAddress);

                String prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1);

                OSCSerializerAndParserBuilder serializer = MainActivity.GetSerializer();
                for (int i = 1; i < 255; i++) {
                    String testIp = prefix + i;

                    InetAddress address = InetAddress.getByName(testIp);
                    String hostName = address.getCanonicalHostName();

                    if (!testIp.equals(ipString)) {
                        Log.i("NetworkScan", "Trying access " + hostName + "(" + (testIp) + ")");

                        new OSCController(testIp, serializer).TryConnect();
                    }
                    else
                        Log.w("NetworkScan", "Skipping our ip");
                }
            }
        } catch (Throwable t) {
            Log.e("NetworkScan", t.getMessage());
        }

        return null;
    }
}