package com.wifiviewer.ppscan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class ScanFragment extends Fragment {

    //UI
    private RecyclerView recyclerView_scan;
    private RecyclerView.LayoutManager linerLayoutM;

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        //create view
        View v = inflater.inflate(R.layout.scan_layout, null);
        //get view and setup before return
        recyclerView_scan = (RecyclerView)v.findViewById(R.id.recycler_view);
        recyclerView_scan.setHasFixedSize(true);
        // use a linear layout manager
        linerLayoutM = new LinearLayoutManager(getContext().getApplicationContext());
        recyclerView_scan.setLayoutManager(linerLayoutM);
        // specify an adapter (see also next example)
        recyclerView_scan.setAdapter(WifiHandler.mAdapter);
        return v;
    }
}
