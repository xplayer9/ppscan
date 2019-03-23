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

public class NetworkAdapter extends RecyclerView.Adapter<NetworkAdapter.ViewHolder> {

    //public ArrayList<String> arpList;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView peer_host;
        public TextView peer_mac;
        public TextView peer_ip;
        public ViewHolder(View v) {
            super(v);
            peer_host = v.findViewById(R.id.peer_hostname);
            peer_mac = v.findViewById(R.id.peer_mac);
            peer_ip = v.findViewById(R.id.peer_ip);
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public NetworkAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.network_adapter_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String[] str = WifiHandler.arpList.get(position).split(" ");
        if(WifiHandler.hostmap.containsKey(str[0])) {
            holder.peer_host.setText(WifiHandler.hostmap.get(str[0]));
            holder.peer_host.setTextColor(Color.CYAN);
        }
        else
            holder.peer_host.setHeight(1);
        holder.peer_ip.setText("IP: " + str[0]);
        holder.peer_mac.setText("MAC: " +str[1]);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return WifiHandler.arpList.size();
    }
}
