package com.dk.headsetbatteryinfosender;

import android.os.AsyncTask;
import android.util.Log;

import com.illposed.osc.*;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.transport.OSCPortIn;
import com.illposed.osc.transport.OSCPortOut;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Objects;

public class OSCController {
    private static final int port = 28092;
    private final String host;
    private final OSCSerializerAndParserBuilder serializer;
    private OSCPortOut sender;
    public OSCController(String host, OSCSerializerAndParserBuilder serializer)
    {
        this.host = host;
        this.serializer = serializer;
    }
    public void TryConnect()
    {
        Log.i("OSC", "Initializing OSC for " + host);

        CreateSender();
        CreateListener();

        new SendIPTest().execute(host);
    }
    public void CreateSender()
    {
        try {
            InetAddress ipAddress = InetAddress.getByName(host);

            Log.i("OSC", "Creating OSC sender for " + host + "(" + ipAddress + ":" + port + ")");

            sender = new OSCPortOut(serializer,
                    new InetSocketAddress(ipAddress, port));

            sender.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void Send(String address, Object data)
    {
        new SendMessage().execute(address, data);
    }
    public void CreateListener()
    {
        Log.i("OSC", "Creating OSC listener");

        OSCController controller = this;
        try {
            OSCPortIn receiver = new OSCPortIn(port);

            receiver.getDispatcher().addListener(new MessageSelector() {
                @Override
                public boolean isInfoRequired() {
                    return false;
                }

                @Override
                public boolean matches(OSCMessageEvent messageEvent) {
                    return true;
                }
            }, new OSCMessageListener() {
                @Override
                public void acceptMessage(OSCMessageEvent event) {
                    if (Objects.equals(event.getMessage().getAddress(), "/confirmAddress"))
                    {
                        Log.i("OSC", "Found battery streaming device " + sender);

                        MainActivity.SetOSCController((String)event.getMessage().getArguments().get(0));
                    }
                }
            });

            receiver.startListening();

            Log.i("OSC", "Starting to listen...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class SendIPTest extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... hosts) {
            Object[] args = new Object[] { hosts[0] };
            String address = "/netLocalIpAddress";
            OSCMessage message = new OSCMessage(address, Arrays.asList(args));

            Log.d("OSC", "Sending \"" + address + " = " + args[0] + " to " + host);

            try {
                sender.send(message);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
    private class SendMessage extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... data) {
            Object[] args = new Object[] { data[1] };
            String address = (String)data[0];
            OSCMessage message = new OSCMessage(address, Arrays.asList(args));

            Log.d("OSC", "Sending \"" + address + " = " + args[0] + " to " + host);

            try {
                sender.send(message);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}