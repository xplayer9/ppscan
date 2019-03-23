package com.wifiviewer.ppscan;

import android.graphics.Color;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.net.wifi.WifiInfo;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InfoFragment extends Fragment {
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        //create view
        View v = inflater.inflate(R.layout.activity_info, null);
        updateInfo(v);
        return v;
    }

    private void updateInfo(View v){

        //check wifi status first
        TextView tmp = (TextView)v.findViewById(R.id.info_status);
        if(!WifiHandler.isWifiConnected()) {
            tmp.setText("Disconnect");
            tmp.setTextColor(Color.RED);
            Toast.makeText(getContext(), "WiFi Disconnected", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            tmp.setText("Connected");
            tmp.setTextColor(Color.GREEN);
            //update device ip
            WifiHandler.deviceIP = WifiHandler.formatIP(WifiHandler.wifiM.getConnectionInfo().getIpAddress());
        }

        WifiInfo wifiInfo = WifiHandler.wifiM.getConnectionInfo();

        //ssid
        tmp = (TextView)v.findViewById(R.id.info_ssid);
        String ssid = "";
        if(!wifiInfo.getHiddenSSID())
            ssid = wifiInfo.getSSID().substring(1,wifiInfo.getSSID().length()-1);
        tmp.setText(ssid);
        //if SSID changed, arp table need refresh
        if(WifiHandler.deviceSSID.compareTo(ssid) != 0){
            WifiHandler.deviceSSID = ssid;
            WifiHandler.arpTimer = 0;
        }

        //ch/freq
        tmp = (TextView)v.findViewById(R.id.info_freq);
        int freq = wifiInfo.getFrequency();
        tmp.setText("Ch " + WifiHandler.channelMap.get(freq) + " / " + freq + "MHz");

        //bssid
        tmp = (TextView)v.findViewById(R.id.info_bssid);
        tmp.setText(wifiInfo.getBSSID());

        //device ip
        tmp = (TextView)v.findViewById(R.id.info_ip);
        tmp.setText(WifiHandler.deviceIP);

        //mac
        tmp = (TextView)v.findViewById(R.id.info_mac);
        tmp.setText(WifiHandler.getMacAddr());

        //speed
        tmp = (TextView)v.findViewById(R.id.info_speed);
        tmp.setText(wifiInfo.getLinkSpeed() + " Mbps");

        //rssi
        tmp = (TextView)v.findViewById(R.id.info_rssi);
        tmp.setText(wifiInfo.getRssi() + " dBm");

        //ap capability
        tmp = (TextView)v.findViewById(R.id.info_apcapability);
        for(int i=0; i<WifiHandler.scanList.size(); i++){
            if(WifiHandler.scanList.get(i).SSID.equals(ssid)){
                tmp.setText(WifiHandler.scanList.get(i).capabilities);
                break;
            }
        }

        //Network info
        DhcpInfo dhcpinfo = WifiHandler.wifiM.getDhcpInfo();

        //Gateway
        tmp = (TextView)v.findViewById(R.id.info_gateway);
        tmp.setText(WifiHandler.formatIP(dhcpinfo.gateway));

        //dns
        tmp = (TextView)v.findViewById(R.id.info_dns);
        tmp.setText(WifiHandler.formatIP(dhcpinfo.dns1));

        //dhcp server
        tmp = (TextView)v.findViewById(R.id.info_dhcp);
        tmp.setText(WifiHandler.formatIP(dhcpinfo.serverAddress));

        //dhcp lease
        tmp = (TextView)v.findViewById(R.id.info_dhcplease);
        tmp.setText(dhcpinfo.leaseDuration + " secs");

        //Ping command, use Handler.post thread
            //google
            final TextView googleText = (TextView)v.findViewById(R.id.info_google);
            new Handler(Looper.getMainLooper()).post(new Runnable(){
                @Override
                public void run() {
                    googleText.setText(WifiHandler.getLatency("www.google.com") + " ms");
                }
            });
            //facebook
            final TextView facebookText = (TextView)v.findViewById(R.id.info_facebook);
            new Handler(Looper.getMainLooper()).post(new Runnable(){
                @Override
                public void run() {
                    facebookText.setText(WifiHandler.getLatency("www.facebook.com")+ " ms");
                }
            });

            //amazon
            final TextView amazonText = (TextView)v.findViewById(R.id.info_amazon);
            new Handler(Looper.getMainLooper()).post(new Runnable(){
                @Override
                public void run() {
                    amazonText.setText(WifiHandler.getLatency("www.amazon.com")+ " ms");
                }
            });

            //apple
            final TextView appleText = (TextView)v.findViewById(R.id.info_apple);
            new Handler(Looper.getMainLooper()).post(new Runnable(){
                @Override
                public void run() {
                    appleText.setText(WifiHandler.getLatency("www.apple.com")+ " ms");
                }
            });
    }
}
