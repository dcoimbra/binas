package org.binas.station.ws.it;

import org.binas.station.ws.BadInit_Exception;
import org.binas.station.ws.cli.StationClient;
import org.binas.station.ws.cli.StationClientException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Class that tests GetInfo operation
 */
public class GetInfoIT extends BaseIT {

	// tests
	// Assert.assertEquals(expected, actual);
	@Before
	public void setUp() throws StationClientException {
		
	}


	@Test
	public void getInfoEmptyTest() throws BadInit_Exception {
		client.testInit(10, 10, 20, 0);
		Assert.assertNotNull(client.getInfo());
	}
	

	@Test(expected = BadInit_Exception.class)
	public void negativeCoord() throws BadInit_Exception {
		client.testInit(-10, -20, 5, 0);
	}
	
	@Test(expected = BadInit_Exception.class)
	public void outOfBoundsCoord() throws BadInit_Exception {
		client.testInit(300, 300, 5, 0);
	}
	
	@Test(expected = BadInit_Exception.class)
	public void zeroCapacity() throws BadInit_Exception {
		client.testInit(10, 10, 0, 0);
	}
	
	@Test(expected = BadInit_Exception.class)
	public void negativeCapacity() throws BadInit_Exception {
		client.testInit(-10, -10, 0, 0);
	}
	
	@AfterClass
	public static void cleanup() {
		client.testClear();
	}

}
