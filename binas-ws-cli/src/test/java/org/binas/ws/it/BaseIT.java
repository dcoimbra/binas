package org.binas.ws.it;

import org.binas.ws.cli.BinasClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;
import java.util.Properties;


/*
 * Base class of tests
 * Loads the properties in the file
 */
public class BaseIT {

	private static final String TEST_PROP_FILE = "/test.properties";
	protected static Properties testProps;

	protected static BinasClient client;
	
	protected final static String STATION_ID1 = "T07_Station1";
	protected final static String STATION_ID2 = "T07_Station2";
	protected final static String STATION_ID3 = "T07_Station3";
	protected final static String STATION_ID4 = "T07_Station4";
	protected final static String STATION_ID5 = "T07_Station5";
	protected final static String STATION_ID6 = "T07_Station6";
	protected final static String STATION_ID7 = "T07_Station7";
	protected final static String STATION_ID8 = "T07_Station8";
	protected final static String EMAIL_DAVID = "david@tecnico.pt";

	@BeforeClass
	public static void oneTimeSetup() throws Exception {
		testProps = new Properties();
		try {
			testProps.load(BaseIT.class.getResourceAsStream(TEST_PROP_FILE));
			System.out.println("Loaded test properties:");
			System.out.println(testProps);
		} catch (IOException e) {
			final String msg = String.format("Could not load properties file {}", TEST_PROP_FILE);
			System.out.println(msg);
			throw e;
		}

		final String uddiEnabled = testProps.getProperty("uddi.enabled");
		final String verboseEnabled = testProps.getProperty("verbose.enabled");

		final String uddiURL = testProps.getProperty("uddi.url");
		final String wsName = testProps.getProperty("ws.name");
		final String wsURL = testProps.getProperty("ws.url");

		if ("true".equalsIgnoreCase(uddiEnabled)) {
			client = new BinasClient(uddiURL, wsName);
		} else {
			client = new BinasClient(wsURL);
		}
		client.setVerbose("true".equalsIgnoreCase(verboseEnabled));

	}

	@AfterClass
	public static void cleanup() {
	}

}
