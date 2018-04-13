package org.binas.ws.it;

import org.binas.ws.BadInit_Exception;
import org.binas.ws.InvalidStation_Exception;
import org.binas.ws.StationView;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

public class GetInfoStationIT extends BaseIT {

    @Test
    public void success() throws InvalidStation_Exception, BadInit_Exception {
    									// x, y, capacity, returnPrize
       client.testInitStation(STATION_ID1, 30, 40, 200, 20); 

       StationView station = client.getInfoStation(STATION_ID1);

        Assert.assertEquals(STATION_ID1, station.getId());
        Assert.assertEquals(30, (int) station.getCoordinate().getX());
        Assert.assertEquals(40, (int) station.getCoordinate().getY());
        Assert.assertEquals(200, station.getCapacity());

    }

    @Test
    public void getInfoEmptyTest() throws BadInit_Exception, InvalidStation_Exception {
        client.testInitStation(STATION_ID1, 10, 10, 20, 0);
        Assert.assertNotNull(client.getInfoStation(STATION_ID1));
    }


    @Test(expected = BadInit_Exception.class)
    public void negativeCoord() throws BadInit_Exception {
        client.testInitStation(STATION_ID1,-10, -20, 5, 0);
    }

    @Test(expected = BadInit_Exception.class)
    public void outOfBoundsCoord() throws BadInit_Exception {
        client.testInitStation(STATION_ID1,300, 300, 5, 0);
    }

    @Test(expected = BadInit_Exception.class)
    public void zeroCapacity() throws BadInit_Exception {
        client.testInitStation(STATION_ID1,10, 10, 0, 0);
    }

    @Test(expected = BadInit_Exception.class)
    public void negativeCapacity() throws BadInit_Exception {
        client.testInitStation(STATION_ID1,-10, -10, 0, 0);
    }

    @Test(expected = InvalidStation_Exception.class)
    public void getInfoStationEmptyTest() throws InvalidStation_Exception {
        client.getInfoStation("");
    }


    @Test(expected = InvalidStation_Exception.class)
    public void getInfoStationNullTest() throws InvalidStation_Exception {
        client.getInfoStation(null);
    }

    @Test(expected = InvalidStation_Exception.class)
    public void getInfoStationInvalidStationTest() throws InvalidStation_Exception {
        client.getInfoStation("hell");
    }

    @AfterClass
    public static void cleanup() {
        client.testClear();
    }
}
