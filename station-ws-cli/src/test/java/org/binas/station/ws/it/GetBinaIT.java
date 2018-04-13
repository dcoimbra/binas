package org.binas.station.ws.it;

import org.binas.station.ws.BadInit_Exception;
import org.binas.station.ws.NoBinaAvail_Exception;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

/**
 * Class that tests GetBina operation
 */
public class GetBinaIT extends BaseIT {

	// tests
	// Assert.assertEquals(expected, actual);

	// public String ping(String x)

	@Test
	public void getBinaEmptyTest() throws NoBinaAvail_Exception, BadInit_Exception {
		client.testInit(22, 7, 6, 2);
		int nbinas = client.getInfo().getAvailableBinas();
		client.getBina();
		Assert.assertEquals(nbinas-1, client.getInfo().getAvailableBinas());
		
	}
	
	@Test (expected = NoBinaAvail_Exception.class)
	public void getBinaNoBinaAvail() throws NoBinaAvail_Exception, BadInit_Exception {
		client.testInit(22, 7, 2, 2);
		client.getBina();
		client.getBina();
		Assert.assertEquals(0, client.getInfo().getAvailableBinas());
		client.getBina();
		
	}
	
	
	@AfterClass
	public static void cleanup() {
		client.testClear();
	}

}
