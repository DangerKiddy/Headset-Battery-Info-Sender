package com.dk.headsetbatteryinfosender;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import com.illposed.osc.*;

class NetworkScanTask extends AsyncTask<Void, Void, Void> {
    private WeakReference<Context> mContextRef;
    private Context context;
    private OSCSerializerAndParserBuilder serializer;

    public NetworkScanTask(Context context) {
        mContextRef = new WeakReference<Context>(context);
    }
    private static Map<String, String> deviceNetworkNames = new HashMap<String, String>();
    private static Map<String, OSCController> oscTestConnections = new HashMap<String, OSCController>();
    public static String GetNetworkName(String ip)
    {
        String name = ip;

        if (deviceNetworkNames.containsKey(ip))
            name = deviceNetworkNames.get(ip);

        return name;
    }

    public static void CloseAllTestConnection()
    {
        for (Map.Entry<String, OSCController> entry : oscTestConnections.entrySet()) {
            OSCController connection = entry.getValue();

            if (connection != null)
                connection.Close();
        }

        oscTestConnections.clear();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        context = mContextRef.get();

        if (context != null) {
            serializer = MainActivity.GetSerializer();
            String lastDeviceName = MainActivity.TryGetLastDeviceIp();
            if (!lastDeviceName.equals(""))
            {
                ConnectToLastDevice(lastDeviceName);
            }
            else
            {
                ScanNetwork();
            }
        }

        return null;
    }

    private void ConnectToLastDevice(String lastDeviceName)
    {
        MainActivity.SetStatusText("Trying to connect to the last device (" + lastDeviceName + ")\nIf it takes too long, please, press \"Scan again\"");
        try {
            InetAddress ipAddress = InetAddress.getByName(lastDeviceName);
            String ip = ipAddress.getHostAddress();

            OSCController testConnection = new OSCController(ip, serializer);
            oscTestConnections.put(ip, testConnection);
            deviceNetworkNames.put(ip, lastDeviceName);

            testConnection.TryConnect();
            MainActivity.FinishNetworkScan();

        } catch (Exception e) {
            MainActivity.SetStatusText("Unable to connect to the last used device, re-scanning network");
            e.printStackTrace();

            MainActivity.SetLastUsedDeviceName("");
            MainActivity.StartNetworkScan();
        }
    }
    private void ScanNetwork()
    {
        try {
            WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

            WifiInfo connectionInfo = wm.getConnectionInfo();
            int ipAddress = connectionInfo.getIpAddress();
            String ipString = Formatter.formatIpAddress(ipAddress);

            String prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1);

            MainActivity.SetStatusText("Searching device...");
            for (int i = 1; i < 255; i++) {
                String testIp = prefix + i;

                InetAddress address = InetAddress.getByName(testIp);
                String hostName = address.getCanonicalHostName();

                if (!testIp.equals(ipString)) {
                    Log.i("NetworkScan", "Trying access " + hostName + "(" + (testIp) + ")");

                    deviceNetworkNames.put(testIp, hostName);

                    MainActivity.SetStatusText("Searching device " + i + "/255...");

                    OSCController testConnection = new OSCController(testIp, serializer);
                    oscTestConnections.put(testIp, testConnection);

                    testConnection.TryConnect();
                } else
                    Log.w("NetworkScan", "Skipping our ip");
            }

            MainActivity.FinishNetworkScan();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}