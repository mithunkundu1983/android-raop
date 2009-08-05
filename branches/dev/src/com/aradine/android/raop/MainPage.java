package com.aradine.android.raop;

import java.util.Vector;

import android.app.ListActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainPage extends ListActivity {
    /** Called when the activity is first created. */
    
    boolean refreshing = true;
    boolean paused = false;
    int numItems = 0;
    ArrayAdapter<RAOPDeviceDescription> discoveredDevicesAdapter;
    Vector<RAOPDeviceDescription> discoveredDevicesVector = new Vector<RAOPDeviceDescription>();
    
    private final Handler uiHandler = new Handler();
    private final Runnable listUpdateRunnable = new Runnable() {
    	public void run() {
    		discoveredDevicesAdapter.clear();
    		for (int i = 0; i < discoveredDevicesVector.size(); i++) {
    			discoveredDevicesAdapter.add(discoveredDevicesVector.elementAt(i));
    		}
    	}
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ListView discoveredDevicesList = (ListView)findViewById(android.R.id.list);
        discoveredDevicesAdapter = new ArrayAdapter<RAOPDeviceDescription>(this, R.layout.device_list_element);
        discoveredDevicesList.setAdapter(discoveredDevicesAdapter);
        
        Thread refreshListTimer = new Thread() {
        	public void run() {
        		if (refreshing)
        			Log.d("MainPage", "Starting to refresh devices.");
        		while (refreshing) {
        			try {
        				sleep(1000);
        			} catch (InterruptedException e) {
        				Log.e("MainPage", e.toString());
        			}
        			
        			
        			if (!paused) {
        				Log.d("MainPage", "Adding a new item here.");
        				numItems++;
        				
        				RAOPDeviceDescription rd = new RAOPDeviceDescription();
        				rd.setTitle("I am " + numItems);
        				discoveredDevicesVector.add(rd);
        				uiHandler.post(listUpdateRunnable);
        				
        				if (numItems > 5)
        					break;
        			}
        		}
        		
        		Log.d("MainPage", "Never refreshing again!");
        	}
        };
        refreshListTimer.start();
        
    }
    
    @Override
    protected void onPause() {
    	//paused = true;
    	super.onPause();
    }

    @Override
    protected void onResume() {
    	paused = false;
    	super.onResume();
    }
}