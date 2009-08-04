package com.aradine.android.raop;

import android.app.ListActivity;

import android.os.Bundle;
import android.provider.Contacts.Intents.UI;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainPage extends ListActivity {
    /** Called when the activity is first created. */
    
    boolean refreshing = true;
    boolean paused = false;
    int numItems = 0;
    ArrayAdapter<RAOPDeviceDescription> discoveredDevicesAdapter;
    
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
        			Log.e("MainPage", "Starting to refresh devices.");
        		while (refreshing) {
        			try {
        				sleep(1000);
        			} catch (InterruptedException e) {
        				Log.e("MainPage", e.toString());
        			}
        			
        			
        			if (!paused) {
        				Log.d("MainPage", "Adding a new item here.");
        				numItems++;
        				TextView t = new TextView(MainPage.this);
        				t.setText("I am " + numItems);
        				discoveredDevicesAdapter.add(new RAOPDeviceDescription());
        				
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