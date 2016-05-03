package com.htappsource.batteryoptimizer;
import android.app.ListActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.view.View;

public class MainActivity extends Activity {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    private String[] strText = new String[] {"Battery Level", "Status", "Running Apps"};
    private String[] RunningApps;
    private int Voltage = 0;
    private double Power = 0;
    private boolean trun = true;
    private Handler myHandler = new Handler();
    private Runnable myRun = new Runnable() {
        public void run() {
            updateListData();
        }
    };
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // preparing list data
        initializeListData();
        startMonitor();
    }

    private void initializeListData(){
        setContentView(R.layout.activity_main);
        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.lvExp);

        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        // Adding child data
        listDataHeader.add(strText[0]);
        listDataHeader.add(strText[1]);
        listDataHeader.add(strText[2]);

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);

        // Listview Group click listener
        expListView.setOnGroupClickListener(new OnGroupClickListener() {

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
        expListView.setOnGroupExpandListener(new OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        listDataHeader.get(groupPosition) + " Expanded",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Listview Group collasped listener
        expListView.setOnGroupCollapseListener(new OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        listDataHeader.get(groupPosition) + " Collapsed",
                        Toast.LENGTH_SHORT).show();

            }
        });

        // Listview on child click listener
        expListView.setOnChildClickListener(new OnChildClickListener() {

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

    /*
     * Preparing the list data
     */
    private void updateListData() {
        listDataHeader.clear();
        listDataHeader.add(strText[0]);
        listDataHeader.add(strText[1]);
        listDataHeader.add(strText[2]);

        List<String> runningAppsList = new ArrayList<String>();
        runningAppsList.add(RunningApps[0]);
        runningAppsList.add(RunningApps[1]);
        runningAppsList.add(RunningApps[2]);
        runningAppsList.add(RunningApps[3]);

        listDataChild.put(listDataHeader.get(2), runningAppsList);

        listAdapter.notifyDataSetChanged();

        // Listview Group click listener
        expListView.setOnGroupClickListener(new OnGroupClickListener() {

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
        expListView.setOnGroupExpandListener(new OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        listDataHeader.get(groupPosition) + " Expanded",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Listview Group collasped listener
        expListView.setOnGroupCollapseListener(new OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        listDataHeader.get(groupPosition) + " Collapsed",
                        Toast.LENGTH_SHORT).show();

            }
        });

        // Listview on child click listener
        expListView.setOnChildClickListener(new OnChildClickListener() {

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
    private Thread myThread = new Thread() {
        public void run () {
            do {
                batteryLevelUpdate();
                checkRunningApps();

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

    private void startMonitor() {
        myThread.start();
    }
    private void batteryLevelUpdate() {
        BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                context.unregisterReceiver(this);
                int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int level = -1;
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

                strText[0] = "Battery Level: " + Integer.toString(level) + "%";
                //strText[1] = "Power consumption: " + Double.toString(Power) + "W";
                strText[1] = strStatus;
            }
        };
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelReceiver, batteryLevelFilter);
    }
    private void checkRunningApps() {
        ActivityManager actvityManager = (ActivityManager)
                this.getSystemService( ACTIVITY_SERVICE );
        List<ActivityManager.RunningAppProcessInfo> procInfos = actvityManager.getRunningAppProcesses();
        String[] processNames = new String[procInfos.size()];
        //MemoryInfo[] memInfo = new MemoryInfo[procInfos.size()];
        int[] processid = new int[procInfos.size()];

        for(int i = 0; i < procInfos.size(); i++){
            processNames[i]= procInfos.get(i).processName;
            ActivityManager.RunningAppProcessInfo info = procInfos.get(i);
            processid[i] = info.pid;
        }

        //String app_name = packageInfo.applicationInfo.loadLabel(getPackageManager()).toString();

        Debug.MemoryInfo[] memInfo = actvityManager.getProcessMemoryInfo(processid);
        int[] usage = new int[procInfos.size()];
        for(int i = 0; i < procInfos.size(); i++) {
            usage[i] = memInfo[i].getTotalPrivateDirty();
        }
        //Arrays.sort(usage); //this is not helpful since it does not give you the indices
        // bubble sort usage and corresponding app name
        int n = procInfos.size();
        int swap = 0;
        String swap_str = "";
        for (int c = 0; c < ( n - 1 ); c++) {
            for (int d = 0; d < n - c - 1; d++) {
                if (usage[d] < usage[d+1]) /* For descending order use < */
                {
                    swap       = usage[d];
                    usage[d]   = usage[d+1];
                    usage[d+1] = swap;
                    swap_str   = processNames[d];
                    processNames[d] = processNames[d+1];
                    processNames[d+1] = swap_str;
                }
            }
        }
        RunningApps = processNames;
        RunningApps[0] = processNames[0] + " (" + Integer.toString(usage[0]/1000) + "MB)";
        RunningApps[1] = processNames[1] + " (" + Integer.toString(usage[1]/1000) + "MB)";
        RunningApps[2] = processNames[2] + " (" + Integer.toString(usage[2]/1000) + "MB)";
        RunningApps[3] = processNames[3] + " (" + Integer.toString(usage[3]/1000) + "MB)";
    }


}
