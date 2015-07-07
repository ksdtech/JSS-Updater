package org.kentfieldschools.jss;

import com.google.api.client.util.Key;

public class MobileDeviceGeneral {
	@Key
	public String id;
	@Key("device_name")
	public String deviceName;
	@Key("asset_tag")
	public String assetTag;
}
