package org.binas.station.ws.it;

import org.binas.station.ws.BadInit_Exception;
import org.binas.station.ws.NoBinaAvail_Exception;
import org.binas.station.ws.NoSlotAvail_Exception;
import org.binas.station.ws.StationView;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

/**
 * Class that tests ReturnBina operation
 */
public class ReturnBinaIT extends BaseIT {

	// tests
	// Assert.assertEquals(expected, actual);


	@Test
	public void returnBinaEmptyTest() throws NoSlotAvail_Exception, BadInit_Exception, NoBinaAvail_Exception {
		client.testInit(10, 10, 20, 0);		
		client.getBina();
		int nbinas = client.getInfo().getAvailableBinas();

		Assert.assertNotNull(client.returnBina());
		Assert.assertEquals(nbinas+1, client.getInfo().getAvailableBinas());
	}
	
	@Test (expected = NoSlotAvail_Exception.class)
	public void returnBinaNoSlotException() throws NoSlotAvail_Exception, BadInit_Exception {
		client.testInit(10, 10, 20, 0);		
		int nbinas = client.getInfo().getAvailableBinas();

		Assert.assertEquals(20, nbinas);
		client.returnBina();
	}
	
	@AfterClass
	public static void cleanup() {
		client.testClear();
	}

}
