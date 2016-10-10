package com.htappsource.batteryoptimizer;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 10/9/2016.
 */
public class BatteryMonitor extends IntentService {
    //define global variables
    private int level = -1;
    private boolean trun = true;
    private Handler myHandler = new Handler();
    private Runnable myRun = new Runnable() {
        public void run() {
            updateListData();
        }
    };

    // define public constructor
    public BatteryMonitor() {
        this(BatteryMonitor.class.getName());
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BatteryMonitor(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        startMonitor();
    }
    /*
     * Preparing the list data
     */
    private void updateListData() {
        listDataHeader.clear();
        listDataHeader.add(strExplist);

        List<String> runningAppsList = new ArrayList<String>();
        runningAppsList.add(RunningApps[0]);
        runningAppsList.add(RunningApps[1]);
        runningAppsList.add(RunningApps[2]);
        runningAppsList.add(RunningApps[3]);
        runningAppsList.add(RunningApps[4]);

        listDataChild.put(listDataHeader.get(0), runningAppsList);

        listAdaptorButton.notifyDataSetChanged();

        // Listview Group click listener
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                // Toast.makeText(getApplicationContext(),
                // "Group Clicked " + listDataHeader.get(groupPosition),
                // Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        // Listview Group expanded listener
        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        listDataHeader.get(groupPosition) + " Expanded",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Listview Group collasped listener
        expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        listDataHeader.get(groupPosition) + " Collapsed",
                        Toast.LENGTH_SHORT).show();

            }
        });

        // Listview on child click listener
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                // TODO Auto-generated method stub
                Toast.makeText(
                        getApplicationContext(),
                        listDataHeader.get(groupPosition)
                                + " : "
                                + listDataChild.get(
                                listDataHeader.get(groupPosition)).get(
                                childPosition), Toast.LENGTH_SHORT)
                        .show();
                return false;
            }
        });
    }

    private void startMonitor() {
        myThread.start();
    }
    private Thread myThread = new Thread() {
        public void run () {
            do {
                batteryLevelUpdate();
                checkPowerSetting();
                myHandler.post(myRun);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } while (trun);
        }
    };
    private void batteryLevelUpdate() {
        BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                context.unregisterReceiver(this);
                int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                if (rawlevel >= 0 && scale > 0) {
                    level = (rawlevel * 100) / scale;
                }
                Voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
                Power = Math.round(Voltage*Voltage*1e-6);
                //String Power_str = Double.toString(Power);
                //Power_str.format("%.1g%n", Power);

                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                int onplug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
                boolean onUSB = onplug == BatteryManager.BATTERY_PLUGGED_USB;
                boolean onAC = onplug == BatteryManager.BATTERY_PLUGGED_AC;
                String strStatus = "Charging on ";
                if (isCharging && onUSB)
                    strStatus += "USB";
                else if (isCharging && onAC)
                    strStatus += "AC Power";
                else
                    strStatus = "Battery Discharging";

                strList[0] = "Battery Level: " + Integer.toString(level) + "%";
                strList[1] = strStatus;
            }
        };
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelReceiver, batteryLevelFilter);
    }
    private void checkPowerSetting(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String  thresh = sharedPreferences.getString("thresh",null);
        Boolean wifi = sharedPreferences.getBoolean("wifi",false);
        Boolean bt = sharedPreferences.getBoolean("bt",false);
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(level != -1 && thresh != null) {
            if (level < Integer.parseInt(thresh)) {
                wifiManager.setWifiEnabled(!wifi);
                if(bt==true) {
                    mBluetoothAdapter.disable();
                }
            }
            else {
                wifiManager.setWifiEnabled(true);
                mBluetoothAdapter.enable();
            }
        }
    }


}
