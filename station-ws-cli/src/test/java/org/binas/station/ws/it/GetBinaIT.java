package org.binas.station.ws.it;

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
	public void getBinaEmptyTest() throws NoBinaAvail_Exception {
		int nbinas = client.getInfo().getAvailableBinas();
		client.getBina();
		Assert.assertEquals(nbinas-1, client.getInfo().getAvailableBinas());
		
	}
	
	@AfterClass
	public static void cleanup() {
		client.testClear();
	}

}
