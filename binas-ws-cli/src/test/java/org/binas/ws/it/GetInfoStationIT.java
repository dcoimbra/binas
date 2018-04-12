package org.binas.ws.it;

import org.binas.ws.InvalidStation_Exception;
import org.junit.AfterClass;
import org.junit.Test;

public class GetInfoStationIT extends BaseIT {

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
