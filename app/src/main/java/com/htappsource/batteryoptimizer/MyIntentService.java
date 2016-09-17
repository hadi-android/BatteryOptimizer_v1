package com.htappsource.batteryoptimizer;

/**
 * Created by Hadi on 13/05/2016.
 */
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;


public class MyIntentService extends IntentService {

    public static final String TEXT_INPUT = "inText";
    public static final String TEXT_OUTPUT = "outText";

    public MyIntentService() {
        super("MyIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String inputText = intent.getStringExtra(TEXT_INPUT);
        SystemClock.sleep(5000); // 5 seconds
        String outputText = inputText + " "
                + DateFormat.format("dd/MM/yy h:mm:ss aa",
                System.currentTimeMillis());

        WifiManager wifiManager = (WifiManager)getBaseContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(
                MainActivity.ResponseReceiver.LOCAL_ACTION);
        broadcastIntent.putExtra(TEXT_OUTPUT, outputText);
        LocalBroadcastManager localBroadcastManager
                = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(broadcastIntent);
    }
}
