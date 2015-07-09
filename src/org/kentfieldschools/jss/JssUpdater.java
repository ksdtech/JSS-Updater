package org.kentfieldschools.jss;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.xml.XmlHttpContent;
import com.google.api.client.xml.XmlNamespaceDictionary;
import com.google.api.client.xml.XmlObjectParser;

public class JssUpdater {
	public final String MOBILE_DEVICES_API_PATH = "/JSSResource/mobiledevices";
	private String authUsername = null;
	private String authPassword = null;
	private String jssServerUrl = null;
	private String fileName = null;
	@SuppressWarnings("unused")
	private CSVFormat csv = null;

	public final HttpTransport transport = new NetHttpTransport();
	public final XmlNamespaceDictionary nsdict = new XmlNamespaceDictionary()
			.set("", "");
	HttpRequestFactory factory = null;
	HttpHeaders headers = null;

	public JssUpdater() {
		try {
			readProperties();
		} catch (Exception ex) {

		}
		headers = new HttpHeaders();
		headers.setBasicAuthentication(authUsername, authPassword);
		headers.setAccept("application/xml");
		headers.setContentType("application/xml");

		factory = transport.createRequestFactory(new HttpRequestInitializer() {
			@Override
			public void initialize(HttpRequest request) {
				request.setParser(new XmlObjectParser(nsdict));
				request.setHeaders(headers);
			}
		});
		

	}

	private void readProperties() throws IOException {
		String propFileName = "config.properties";
		InputStream inputStream = getClass().getClassLoader()
				.getResourceAsStream(propFileName);
		if (inputStream != null) {
			Properties prop = new Properties();
			prop.load(inputStream);
			fileName = prop.getProperty("csvFile");
			jssServerUrl = prop.getProperty("jssServerUrl");
			authUsername = prop.getProperty("authUsername");
			authPassword = prop.getProperty("authPassword");
			if (jssServerUrl == null || authUsername == null
					|| authPassword == null) {
				throw new IOException("incomplete config.properties file");
			}
		} else {
			System.out.println("could not load " + propFileName);
		}
	}

	private String getCsvPath() {
		String homeDir = System.getProperty("user.home");
		String filePath = homeDir + File.separator + "Downloads"
				+ File.separator + fileName;
		return filePath;
	}

	public MobileDeviceMatch findMobileDevice(String serialNumber) {
		// TODO: urlencode string
		String matchUrlString = jssServerUrl + MOBILE_DEVICES_API_PATH
				+ "/match/" + serialNumber;
		GenericUrl matchUrl = new GenericUrl(matchUrlString);
		int status = 500;
		try {
			// JSS REST API - use GET method to query existing objects
			// (read-only)
			HttpRequest matchRequest = factory.buildGetRequest(matchUrl);
			HttpResponse matchResponse = matchRequest.execute();
			status = matchResponse.getStatusCode();
			if (status < 400) {
				MobileDeviceList mobileDevices = matchResponse
						.parseAs(MobileDeviceList.class);
				if (mobileDevices.size == 1) {
					MobileDeviceMatch device = mobileDevices.first();
					System.out.println("match " + serialNumber + ", status "
							+ String.valueOf(status) + ", id " + device.id);
					if (device.id != null && !device.id.isEmpty()) {
						return device;
					}
				} else {
					System.out.println("Match " + serialNumber + ", got "
							+ String.valueOf(mobileDevices.size) + " devices");
				}
			}
		} catch (IndexOutOfBoundsException ex0) {
			System.out.println("Didn't parse any devices. ");
		} catch (Exception ex1) {
			System.out.println("Exception! " + ex1.getMessage());
		}
		// Not found or internal error
		System.out.println("match " + serialNumber + ", status "
				+ String.valueOf(status));
		return null;
	}

	public boolean updateMobileDevice(String serialNumber, String deviceName,
			String assetTag, String building, String room, String department) {
		boolean success = false;
		MobileDeviceMatch match = findMobileDevice(serialNumber);
		if (match != null) {
			// TODO: urlencode string
			String id = match.id;
			String updateUrlString = jssServerUrl + MOBILE_DEVICES_API_PATH
					+ "/id/" + id;
			GenericUrl updateUrl = new GenericUrl(updateUrlString);
			int status = 500;
			try {
				MobileDevice device = new MobileDevice(id);
				device.general.deviceName = deviceName;
				device.general.assetTag = assetTag;
				device.location.building = building;
				device.location.room = room;
				device.location.department = department;

				XmlHttpContent content = new XmlHttpContent(nsdict,
						"mobile_device", device);
				OutputStream out = new ByteArrayOutputStream();
				content.writeTo(out);
				System.out.println("content " + out.toString());

				// JSS REST API - use PUT method to update existing object
				HttpRequest updateRequest = factory.buildPutRequest(updateUrl,
						content);
				HttpResponse updateResponse = updateRequest.execute();
				status = updateResponse.getStatusCode();
				String respStr = updateResponse.parseAsString();
				System.out.println(respStr);
				if (status < 400) {
					success = true;
				}
			} catch (Exception ex1) {
				System.out.println("Exception! " + ex1.getMessage());
			}
			System.out.println("update " + id + ", status "
					+ String.valueOf(status));
		}
		return success;
	}

	public void processCsvFile(String csvFile) throws IOException {
		if (csvFile != null && !csvFile.isEmpty()) {
			fileName = csvFile;
		}
		File f = new File(getCsvPath());
		CSVParser parser = CSVParser.parse(f, Charset.forName("UTF-8"),
				CSVFormat.EXCEL.withHeader());
		for (CSVRecord record : parser) {
			String serialNumber = record.get("Serial Number");
			String assetTag = record.get("Asset Tag");
			String deviceName = record.get("iPad Name");
			String department = record.get("Department");
			String building = record.get("Building");
			String room = record.get("Room");
			if (serialNumber != null && !serialNumber.isEmpty()
					&& assetTag != null && !assetTag.isEmpty()
					&& deviceName != null && !deviceName.isEmpty()) {
				boolean success = updateMobileDevice(serialNumber, deviceName,
						assetTag, building, room, department);
				System.out.println("update " + serialNumber + ", "
						+ String.valueOf(success));
				break;
			} else {
				System.out.println("bad record at line "
						+ String.valueOf(record.getRecordNumber()));
			}
		}
	}

	public static void main(String[] args) {
		JssUpdater updater = new JssUpdater();
		// updater.updateMobileDevice("F4KKQXLKF196", "ipad-1448", "A001448");
		try {
			updater.processCsvFile("ipads.csv");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
