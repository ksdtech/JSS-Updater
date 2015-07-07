package org.kentfieldschools.jss;

import com.google.api.client.util.Key;

public class MobileDevice {
	@Key
	public MobileDeviceGeneral general = new MobileDeviceGeneral();
	
	public MobileDevice(String id) {
		general.id = id;
	}
}
