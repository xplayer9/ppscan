package com.wifiviewer.ppscan;

import android.graphics.Canvas;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.*;
import android.widget.*;
import android.view.*;
import android.graphics.Paint;

import java.util.ArrayList;

public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ViewHolder> {

    private ArrayList<ScanResult> scanList;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        ImageView imageView_wifilock;
        TextView textView_ssid;
        TextView textView_ch;
        TextView textView_dbm;
        ProgressBar signal_bar;
        ViewHolder(View v) {
            super(v);
            imageView_wifilock = v.findViewById(R.id.imageview_wifilock);
            textView_ssid = v.findViewById(R.id.textView_ssid);
            textView_ch = v.findViewById(R.id.textView_ch);
            textView_dbm = v.findViewById(R.id.textView_dbm);
            signal_bar = v.findViewById(R.id.signal_bar);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    RecycleAdapter(ArrayList<ScanResult> ll) {
        scanList = new ArrayList<>(ll);
    }

    void updateScanResult(ArrayList<ScanResult> ll){
        scanList = new ArrayList<>(ll);
    }
    // Create new views (invoked by the layout manager)
    @Override
    public RecycleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycleview_layout, parent, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        //Check Security
        String wpa = scanList.get(position).capabilities;
        if(!wpa.contains("WPA"))
            holder.imageView_wifilock.setImageResource(R.drawable.wifi_black_30dp);
        else
            holder.imageView_wifilock.setImageResource(R.drawable.wifi_lock_30dp);

        //Check SSID
        holder.textView_ssid.setText(scanList.get(position).SSID);

        //Check channel
        int freq = scanList.get(position).frequency;
        if(WifiHandler.channelMap.containsKey(freq))
            holder.textView_ch.setText("Ch "+ WifiHandler.channelMap.get(freq));
        else
            holder.textView_ch.setText("Freq "+freq);

        //Check RSSI
        int rssi = scanList.get(position).level;
        holder.textView_dbm.setText(rssi+" dBm");
        if(rssi >= -45) {
            //Color dark green
            holder.signal_bar.getProgressDrawable().setColorFilter(
                    Color.parseColor("#3F8341"), android.graphics.PorterDuff.Mode.SRC_IN);
            holder.signal_bar.setProgress(rssi+140);
            holder.textView_dbm.setTextColor(Color.parseColor("#3F8341"));
        }
        else if(rssi > -66) {
            //Color Orange
            holder.signal_bar.getProgressDrawable().setColorFilter(
                    Color.parseColor("#FF9800"), android.graphics.PorterDuff.Mode.SRC_IN);
            holder.signal_bar.setProgress(rssi+115);
            holder.textView_dbm.setTextColor(Color.parseColor("#FF9800"));
        }
        else {
            holder.signal_bar.getProgressDrawable().setColorFilter(
                    Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
            holder.signal_bar.setProgress(rssi+100);
            holder.textView_dbm.setTextColor(Color.RED);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return scanList.size();
    }
}
