package org.kentfieldschools.jss;

import com.google.api.client.util.Key;

public class MobileDevice {
	@Key
	public MobileDeviceGeneral general = new MobileDeviceGeneral();
	@Key
	public MobileDeviceLocation location = new MobileDeviceLocation();

	public MobileDevice() {
	}
	public MobileDevice(String id) {
		general.id = id;
	}
}
