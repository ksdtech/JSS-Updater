package org.kentfieldschools.jss;

import java.util.ArrayList;
import java.util.List;

import com.google.api.client.util.Key;

public class MobileDeviceList {
	@Key
	public int size;
	@Key("mobile_device")
	public List<MobileDeviceMatch> mobileDevices = new ArrayList();
	
	public MobileDeviceMatch first() throws IndexOutOfBoundsException {
		if (mobileDevices == null || mobileDevices.isEmpty()) {
			throw new IndexOutOfBoundsException("no mobile devices");
		}
		return mobileDevices.get(0);
	}
}
