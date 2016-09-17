package com.htappsource.batteryoptimizer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.PopupMenu;

/**
 * Created by Hadi on 13/05/2016.
 */

public class PowerSaver extends Activity   {
    public static SharedPreferences mSettings;
    public static boolean wifi;
    public static boolean bt;
    public static String thresh;

    private void SavePreferences(String key, String value){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.power_saver);
        //instantiate sharedprefs object for storing user prefs in this activitity
        mSettings = PowerSaver.this.getSharedPreferences("Settings", 0);
        final SharedPreferences.Editor editor = mSettings.edit();
        //declare buttons and checkboxes
        final Button btn_thresh = (Button) findViewById(R.id.btn_thresh);
        final CheckBox chk_wifi = (CheckBox) findViewById(R.id.chk_wifi);
        final CheckBox chk_bt   = (CheckBox) findViewById(R.id.chk_blutooth);
        // save power saver preferences in the sharedpreferences
        thresh = mSettings.getString("thresh","missing");
        wifi = mSettings.getBoolean("wifi",false);
        chk_wifi.setChecked(wifi);

        bt = mSettings.getBoolean("bt",false);
        chk_bt.setChecked(bt);

        // set the perefernces to the previous user selection
        if(thresh != "missing" )
            btn_thresh.setText(thresh);
        chk_wifi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("wifi",isChecked);
                editor.apply();
            }
        });
        chk_bt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("bt",isChecked);
                editor.apply();
            }
        });




        btn_thresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(PowerSaver.this, btn_thresh);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        CharSequence thresh_ch = item.getTitle();
                        btn_thresh.setText(thresh_ch);
                        editor.putString("thresh",thresh_ch.toString());
                        editor.apply();
                        return true;
                    }
                });

                popup.show();//showing popup menu
            }
        });//closing the setOnClickListener method







    }
}
