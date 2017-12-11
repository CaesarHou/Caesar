package com.cloudwise.toushibao.ip.datfile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class IpInfo {
	private Set<String> locals = new HashSet<String>();
	private Map<String, String> ipLocalMap = new TreeMap<String, String>();
	public Set<String> getLocals() {
		return locals;
	}
	 
	public Map<String, String> getIpLocalMap() {
		return ipLocalMap;
	}

}
