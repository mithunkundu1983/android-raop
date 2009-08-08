package com.aradine.android.raop;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import javax.jmdns.impl.DNSConstants;
import javax.jmdns.impl.DNSRecord;
import javax.jmdns.impl.JmDNSImpl;

import android.app.ListActivity;
import android.content.Context;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainPage extends ListActivity {
    /** Called when the activity is first created. */
    
    boolean refreshing = true;
    boolean paused = false;
    int numItems = 0;
    LibraryAdapter discoveredDevicesAdapter;
    
    private final Handler uiUpdateHandler = new Handler() {
		public void handleMessage(Message msg) {
			discoveredDevicesAdapter.notifyFound((String)msg.obj);
			discoveredDevicesAdapter.notifyDataSetChanged();
		}
    };
    
    
    public static JmDNS jmdns;
    public static JmDNSImpl jmimpl;
	protected ServiceListener listener;
    
	public final static String AIRFOIL_TYPE = "_airfoilspeaker._tcp.local.",
							   RAOP_TYPE = "_raop._tcp.local.";
	public final static String HOST_NAME = "netrig1";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ListView discoveredDevicesList = (ListView)findViewById(android.R.id.list);
        discoveredDevicesAdapter = new LibraryAdapter(this);
        discoveredDevicesList.setAdapter(discoveredDevicesAdapter);
        

        this.listener = new ServiceListener() {
        	public void serviceAdded(ServiceEvent e) {
        		String serviceAddress = String.format("%s.%s", e.getName(), e.getType());
        		// Delay by 500ms so if that the JmDNS stuff gets a chance to discover the
        		// DNS info
        		Message msg = Message.obtain(uiUpdateHandler, DeviceDiscoveryMessage.FOUND_DEVICE, serviceAddress);
        		uiUpdateHandler.sendMessageDelayed(msg, 500); 
        		Log.d("MainPage", "Found a service: " + serviceAddress);
        	}
        	public void serviceRemoved(ServiceEvent e) {
        		String serviceAddress = String.format("%s.%s", e.getName(), e.getType());
        		// Delay by 500ms so if that the JmDNS stuff gets a chance to discover the
        		// DNS info
        		Message msg = Message.obtain(uiUpdateHandler, DeviceDiscoveryMessage.REMOVED_DEVICE, serviceAddress);
        		uiUpdateHandler.sendMessage(msg); 
        		Log.d("MainPage", "Lost a service: " + serviceAddress);
        	}
        	public void serviceResolved(ServiceEvent e) { }
        };
        
        this.startDeviceDiscovery();
        
    }
    
    private InetAddress getLocalIp() {
		try {
			try {
				WifiManager wifi = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);

				WifiInfo wifiinfo = wifi.getConnectionInfo();
				int intaddr = wifiinfo.getIpAddress();
				byte[] byteaddr = new byte[] { (byte)(intaddr & 0xff), (byte)(intaddr >> 8 & 0xff), (byte)(intaddr >> 16 & 0xff), (byte)(intaddr >> 24 & 0xff) };
				return InetAddress.getByAddress(byteaddr);
			} catch (SecurityException se) {
				// Oops, can't get wifi state, must be emulated?
				for (Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces(); nis.hasMoreElements();) {
					NetworkInterface ni = nis.nextElement();
					for (Enumeration<InetAddress> ias = ni.getInetAddresses(); ias.hasMoreElements();) {
						InetAddress ia = ias.nextElement();
						if (!ia.isLoopbackAddress()) {
							return ia;
						}
					}
				}
			}
		} catch (SocketException e) {
			Log.e("MainPage", "Can't get list of local network interfaces: " + e.getStackTrace());
		} catch (UnknownHostException e) {
			Log.e("MainPage", "Can't get my own WiFi address: " + e.getStackTrace());
		}
		Log.e("MainPage", "Should have gotten an IP address, but didn't in getLocalIp.");
		return null;
    }
    
    protected void startDeviceDiscovery() {
    	if (MainPage.jmdns != null)
    		stopDeviceDiscovery();
    	    	
    	// figure out our wifi address, otherwise bail
		
    	InetAddress addr = getLocalIp();
		Log.d("MainPage", String.format("found addr=%s", addr.toString()));

		
		try {
			MainPage.jmdns = JmDNS.create(addr, HOST_NAME);
		} catch (Exception e) {
			Log.e("MainPage", "Can't create JmDNS object: " + e.getMessage());
		}
		MainPage.jmdns.addServiceListener(AIRFOIL_TYPE, listener);
		MainPage.jmdns.addServiceListener(RAOP_TYPE, listener);
		MainPage.jmimpl = (JmDNSImpl)MainPage.jmdns;
    
    }
    
    protected void stopDeviceDiscovery() {
    	MainPage.jmdns.removeServiceListener(AIRFOIL_TYPE, listener);
    	MainPage.jmdns.removeServiceListener(RAOP_TYPE, listener);
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
    private final class DeviceDiscoveryMessage {
    	public static final int FOUND_DEVICE = 1;
    	public static final int REMOVED_DEVICE = 2;
    }
    
	public class LibraryAdapter extends BaseAdapter {
		
		protected Context context;
		protected LayoutInflater inflater;
		
		public View footerView;
		
		protected List<String> known = new LinkedList<String>();
		
		public LibraryAdapter(Context context) {
			this.context = context;
			this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			//this.footerView = inflater.inflate(R.layout.item_network, null, false);
			
		}
		
		public void notifyFound(String address) {
			known.add(address);
		}
		
		public Object getItem(int position) {
			return known.get(position);
		}

		public boolean hasStableIds() {
			return true;
		}

		public int getCount() {
			return known.size();
		}
		 
		public long getItemId(int position) {
			return position;
		}
		
		
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if(convertView == null)
				convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
			
			
			try {
			
				// fetch the dns txt record to get library info
				String dnsname = (String)this.getItem(position);
				DNSRecord.Text txtrec = (DNSRecord.Text)MainPage.jmimpl.getCache().get(dnsname, DNSConstants.TYPE_TXT, DNSConstants.CLASS_IN);
				
				// try finding useful txt header strings
				String[] headers = new String(txtrec.text).split("[\u0000-\u001f]");
				String type = "", title = "", library = "";
				for(String header : headers) {
					if(header.startsWith("rast"))
						type = header.substring(5);
					if(header.startsWith("CtlN"))
						title = header.substring(5);
					if(header.startsWith("DbId"))
						library = header.substring(5);
				}
				Log.d("MainPage.DeviceListItemAdapter", String.format("%s, %s, %s", type, title, library));
	
				// find the parent computer running this service
				DNSRecord.Service srvrec = (DNSRecord.Service)MainPage.jmimpl.getCache().get(dnsname, DNSConstants.TYPE_SRV, DNSConstants.CLASS_IN);
				String hostname = srvrec.server;
	
				// finally, resolve A record for parent host computer
				DNSRecord.Address arec = (DNSRecord.Address)MainPage.jmimpl.getCache().get(hostname, DNSConstants.TYPE_A, DNSConstants.CLASS_IN);
				String addr = arec.getAddress().toString().replaceFirst("[^0-9\\.]", "");
				
				
				((TextView)convertView.findViewById(android.R.id.text1)).setText(title);
				((TextView)convertView.findViewById(android.R.id.text2)).setText(String.format("%s - %s", addr, library));
				
			} catch(Exception e) {
				e.printStackTrace();
				((TextView)convertView.findViewById(android.R.id.text1)).setText("");
				((TextView)convertView.findViewById(android.R.id.text2)).setText("");
			}
			
			return convertView;
			
			
		}

	}

}