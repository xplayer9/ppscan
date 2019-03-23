package com.wifiviewer.ppscan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.*;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.*;

public class WifiHandler {
    final static HashMap<Integer, Integer> channelMap = new HashMap<>();
    public static WifiManager wifiM;
    public static ConnectivityManager connM;
    public static BroadcastReceiver wifiScanReceiver;
    public static ArrayList<ScanResult> scanList = new ArrayList<>();
    public static ArrayList<String> arpList = new ArrayList<>();
    public static HashMap<String,String> hostmap = new HashMap<>();
    public static RecycleAdapter mAdapter;
    public static NetworkAdapter networkAdapter;
    public static String deviceIP = "";
    public static String deviceSSID = "";
    public static long arpTimer = 0;
    public static int scanCriteria = 0;

    static{
        //2.4G
        channelMap.put(2412, 1);
        channelMap.put(2417, 2);
        channelMap.put(2422, 3);
        channelMap.put(2427, 4);
        channelMap.put(2432, 5);
        channelMap.put(2437, 6);
        channelMap.put(2442, 7);
        channelMap.put(2447, 8);
        channelMap.put(2452, 9);
        channelMap.put(2457, 10);
        channelMap.put(2462, 11);
        channelMap.put(2467, 12);
        channelMap.put(2472, 13);
        channelMap.put(2484, 14);

        //5G
        channelMap.put(5180, 36);
        channelMap.put(5200, 40);
        channelMap.put(5220, 44);
        channelMap.put(5240, 48);
        channelMap.put(5260, 52);
        channelMap.put(5280, 56);
        channelMap.put(5300, 60);
        channelMap.put(5320, 64);
        channelMap.put(5500, 100);
        channelMap.put(5520, 104);
        channelMap.put(5540, 108);
        channelMap.put(5560, 112);
        channelMap.put(5580, 116);
        channelMap.put(5600, 120);
        channelMap.put(5620, 124);
        channelMap.put(5640, 128);
        channelMap.put(5660, 132);
        channelMap.put(5680, 136);
        channelMap.put(5700, 140);
        channelMap.put(5745, 149);
        channelMap.put(5765, 153);
        channelMap.put(5785, 157);
        channelMap.put(5805, 161);
        channelMap.put(5825, 165);

        //init adapter
        mAdapter = new RecycleAdapter(scanList);
        networkAdapter = new NetworkAdapter();

        //init wifi BroadcastReceiver
        WifiHandler.wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    List<ScanResult> cur_list = wifiM.getScanResults();
                    if(cur_list.size() != scanList.size()) {
                        //for RSSI sorting
                        ArrayList<ScanResult> ll = new ArrayList<>();
                        for(ScanResult s:cur_list)
                            ll.add(s);
                        Collections.sort(ll, new
                                Comparator<ScanResult>() {
                                    @Override
                                    public int compare(ScanResult o1, ScanResult o2) {
                                        if(scanCriteria == 0)  //sort RSSI
                                            return Math.abs(o1.level) - Math.abs(o2.level);
                                        else if(scanCriteria == 1) //sort SSID
                                            return o1.SSID.compareTo(o2.SSID);
                                        else   //sort channel
                                            return o1.frequency - o2.frequency;
                                    }
                                });
                        scanList = new ArrayList<>(ll);
                        mAdapter.updateScanResult(scanList);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
        };
    }

    public static void sortingScanList(){
        if(scanList.size() > 0) {
            //for RSSI sorting
            Collections.sort(scanList, new
                    Comparator<ScanResult>() {
                        @Override
                        public int compare(ScanResult o1, ScanResult o2) {
                            if(scanCriteria == 0)  //sort RSSI
                                return Math.abs(o1.level) - Math.abs(o2.level);
                            else if(scanCriteria == 1) //sort SSID
                                return o1.SSID.compareTo(o2.SSID);
                            else   //sort channel
                                return o1.frequency - o2.frequency;
                        }
                    });
            mAdapter.updateScanResult(scanList);
            mAdapter.notifyDataSetChanged();
        }
    }

    public static boolean isWifiConnected() {
        NetworkInfo networkInfo = connM.getActiveNetworkInfo();
        if(networkInfo == null) //both wifi and LTE disabled
            return false;
        //only care WiFi
        if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
                return true;
        return false;
    }

    //public static void runScan(){
        //wifiM.startScan();
    //}

    public static String formatIP(int ip){
        return String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff),
                (ip >> 16 & 0xff), (ip >> 24 & 0xff));
    }

    public static String getLatency(String ipAddress){
        String pingCommand = "/system/bin/ping -c 1" + " " + ipAddress;
        String inputLine = "";
        try {
            // execute the command on the environment interface
            Process process = Runtime.getRuntime().exec(pingCommand);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while (true) {
                String l = bufferedReader.readLine();
                if(l == null)
                    break;
                inputLine = l;
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        String[] sp = inputLine.split("/");
        return sp.length>=5?sp[4]:"";
    }

    public static void runArp(String deviceIP) {
        //discover
        String ipseg = deviceIP.substring(0, deviceIP.lastIndexOf(".") + 1);
        for (int i = 2; i < 255; i++) {
            Thread ut = new UDPThread(ipseg+i);
            ut.start();
        }
    }

    public static void parseArp(){
        ArrayList<String> tmp = new ArrayList<>();
        TreeSet<ArpSortHelper> arpSort = new TreeSet<>(new Comparator<ArpSortHelper>() {
            @Override
            public int compare(ArpSortHelper o1, ArpSortHelper o2) {
                return o1.index-o2.index;
            }
        });
        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
            while (true) {
                String l = null;
                l = br.readLine();
                if (l == null)
                    break;
                tmp.add(l);
            }
        }
        catch(IOException e){
            e.printStackTrace();
            return;
        }
        arpList = new ArrayList<>();
        for(int i=0; i<tmp.size(); i++){
            if( i == 0)     //skip the first line
                continue;
            String[] strArr = tmp.get(i).split("\\s+");
            if(!strArr[3].equalsIgnoreCase("00:00:00:00:00:00")) {
                int index = Integer.parseInt(strArr[0].split("\\.")[3]);
                arpSort.add(new ArpSortHelper(index, strArr[0] + " " + strArr[3]));
                //lookup host name
                new ArpHostLookup(strArr[0]).start();
            }
        }
        //re-arrage list
        Iterator<ArpSortHelper> it = arpSort.iterator();
        while(it.hasNext())
            arpList.add(it.next().str);

        if(arpList.size() > 0)  //means there are new data
            arpTimer = System.currentTimeMillis();
    }
    static class ArpSortHelper{
        public int index = 0;
        public String str = "";
        public ArpSortHelper(int i, String s){
            index = i;
            str = s;
        }
    }

    static class ArpHostLookup extends Thread{
        String ipStr = "";
        public ArpHostLookup(String s){
            ipStr = s;
        }
        public void run(){
            try {
                if(hostmap.containsKey(ipStr))
                    return;
                InetAddress inetAddress = InetAddress.getByName(ipStr);
                String name = inetAddress.getHostName();
                if(!name.equalsIgnoreCase(ipStr))
                    hostmap.put(ipStr, name);

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null)
                    return "";

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes)
                    res1.append(Integer.toHexString(b & 0xFF) + ":");

                if (res1.length() > 0)
                    res1.deleteCharAt(res1.length() - 1);
                return res1.toString();
            }
        } catch (Exception ex) {}
        return "N/A";
    }
}
