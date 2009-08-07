package com.aradine.android.raop;

import java.net.InetAddress;
import java.util.Vector;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceListener;

import android.app.ListActivity;
import android.content.Context;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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
    
    
    public static JmDNS jmdns;
	protected ServiceListener listener;
    
	public final static String AIRFOIL_TYPE = "_airfoilspeaker._tcp.local.";
	public final static String HOST_NAME = "netrig1";
    
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
    
    protected void startDeviceDiscovery() {
    	if (MainPage.jmdns != null)
    		stopDeviceDiscovery();
    	
    	discoveredDevicesVector.clear();
    	
    	// figure out our wifi address, otherwise bail
		WifiManager wifi = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
		
		WifiInfo wifiinfo = wifi.getConnectionInfo();
		int intaddr = wifiinfo.getIpAddress();
		
		byte[] byteaddr = new byte[] { (byte)(intaddr & 0xff), (byte)(intaddr >> 8 & 0xff), (byte)(intaddr >> 16 & 0xff), (byte)(intaddr >> 24 & 0xff) };
		InetAddress addr = null;
		try {
			addr = InetAddress.getByAddress(byteaddr);
		} catch (Exception e) {
			Log.e("MainPage", "Can't get my own address: " + e.getMessage());
		}
		
		Log.d("MainPage", String.format("found intaddr=%d, addr=%s", intaddr, addr.toString()));
		
		try {
			MainPage.jmdns = JmDNS.create(addr, HOST_NAME);
		} catch (Exception e) {
			Log.e("MainPage", "Can't create JmDNS object: " + e.getMessage());
		}
		MainPage.jmdns.addServiceListener(AIRFOIL_TYPE, listener);
    
    }
    
    protected void stopDeviceDiscovery() {
    	MainPage.jmdns.removeServiceListener(AIRFOIL_TYPE, listener);
    	MainPage.jmdns.close();
    	MainPage.jmdns = null;
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