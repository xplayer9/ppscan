package com.wifiviewer.ppscan;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.*;
import android.os.Bundle;
import android.content.*;
import android.net.wifi.*;
import android.view.*;

import java.util.*;
import android.support.design.widget.BottomNavigationView;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity
        implements
        BottomNavigationView.OnNavigationItemSelectedListener,
        PopupMenu.OnMenuItemClickListener {

    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private final int ACCESS_FINE_LOCATION_REQUEST = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init WifiHandler
        WifiHandler.wifiM = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiHandler.connM = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        //Check location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_REQUEST);
        }
        else{
            //force wifi enable and scan
            if(!WifiHandler.wifiM.isWifiEnabled())
                WifiHandler.wifiM.setWifiEnabled(true);
            Toast.makeText(getApplicationContext(), "Scanning....", Toast.LENGTH_LONG).show();
            WifiHandler.wifiM.startScan();
        }

        //update device ip
        if(WifiHandler.isWifiConnected())
            WifiHandler.deviceIP = WifiHandler.formatIP(WifiHandler.wifiM.getConnectionInfo().getIpAddress());

        //Set Intent and Regirster Receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(WifiHandler.wifiScanReceiver, intentFilter);

        //set toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("WiFi List");
        setSupportActionBar(toolbar);

        //load default Fragment
        ScanFragment firstFrag = new ScanFragment();
        loadFragment(firstFrag);

        //bottomNavigation
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == ACCESS_FINE_LOCATION_REQUEST) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //force wifi enable and scan
                if(!WifiHandler.wifiM.isWifiEnabled())
                    WifiHandler.wifiM.setWifiEnabled(true);
                Toast.makeText(getApplicationContext(), "Scanning....", Toast.LENGTH_LONG).show();
                WifiHandler.wifiM.startScan();
            }
            else
                Toast.makeText(this, "Scan fail without location permission", Toast.LENGTH_LONG).show();
        }
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile:
                //popup menu
                View menuItemView = findViewById(R.id.profile); // SAME ID AS MENU ID
                PopupMenu popupMenu = new PopupMenu(this, menuItemView);
                popupMenu.setOnMenuItemClickListener(this);
                popupMenu.inflate(R.menu.popup_menu);
                popupMenu.getMenu().getItem(WifiHandler.scanCriteria).setChecked(true);
                popupMenu.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.check_rssi:
                 item.setChecked(true);
                 WifiHandler.scanCriteria = 0;
                 WifiHandler.sortingScanList();
                 return true;
            case R.id.check_ssid:
                 item.setChecked(true);
                 WifiHandler.scanCriteria = 1;
                 WifiHandler.sortingScanList();
                 return true;
            case R.id.check_ch:
                 item.setChecked(true);
                 WifiHandler.scanCriteria = 2;
                 WifiHandler.sortingScanList();
                 return true;
            default:
                 return false;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        // uncheck the other items.

        Fragment fragment = null;
        switch (item.getItemId()) {
            case R.id.navigation_scan:
                toolbar.setTitle("WiFi List");
                fragment = new ScanFragment();

                //Check location permission
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_REQUEST);
                }
                else{
                    //force wifi enable and scan
                    if(!WifiHandler.wifiM.isWifiEnabled())
                        WifiHandler.wifiM.setWifiEnabled(true);
                    Toast.makeText(getApplicationContext(), "Scanning....", Toast.LENGTH_LONG).show();
                    WifiHandler.wifiM.startScan();
                }
                break;
            case R.id.navigation_info:
                toolbar.setTitle("Device Info");
                fragment = new InfoFragment();
                break;
            case R.id.navigation_network:
                toolbar.setTitle("SubNetwork List");
                fragment = new NetworkFragment();
                if(!WifiHandler.isWifiConnected()) {
                    Toast.makeText(getApplicationContext(), "WiFi Disconnected", Toast.LENGTH_SHORT).show();
                    WifiHandler.arpList = new ArrayList<>();
                    WifiHandler.arpTimer = 0;
                }
                else {
                    long curTime = System.currentTimeMillis();
                    //at least wait for 10 minutes for arp check
                    if((curTime - WifiHandler.arpTimer)/1000 > 600) {
                        WifiHandler.arpTimer = curTime;
                        WifiHandler.runArp(WifiHandler.deviceIP);
                        WifiHandler.parseArp();
                    }
                }
                WifiHandler.networkAdapter.notifyDataSetChanged();
                break;
        }
        return loadFragment(fragment);
    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
