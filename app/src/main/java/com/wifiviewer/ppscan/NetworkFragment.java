package com.wifiviewer.ppscan;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NetworkFragment extends Fragment {

    //UI
    private RecyclerView network_view;
    private RecyclerView.LayoutManager linerLayoutM;

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.network_layout, null);
        //create view
        //get view and setup before return
        network_view = (RecyclerView)v.findViewById(R.id.recycler_view_network);
        network_view.setHasFixedSize(true);
        // use a linear layout manager
        linerLayoutM = new LinearLayoutManager(getContext().getApplicationContext());
        network_view.setLayoutManager(linerLayoutM);
        // specify an adapter (see also next example)
        network_view.setAdapter(WifiHandler.networkAdapter);
        return v;
    }
}
