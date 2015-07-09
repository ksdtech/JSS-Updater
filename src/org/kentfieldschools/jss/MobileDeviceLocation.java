package org.kentfieldschools.jss;

import com.google.api.client.util.Key;

public class MobileDeviceLocation {
	@Key
	public String location;
	@Key("department")
	public String department;
	@Key("building")
	public String building;
	@Key("room")
	public String room;
}
