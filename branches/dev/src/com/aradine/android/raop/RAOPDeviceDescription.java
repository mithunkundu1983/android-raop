package com.aradine.android.raop;

import java.net.InetAddress;

public class RAOPDeviceDescription {
	private String title = "";
	private String addr = null;
	
	public RAOPDeviceDescription(String addr, String title) {
		this.addr = addr;
		this.title = title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String toString() {
		return title;
	}
}
