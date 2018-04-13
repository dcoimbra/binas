package org.binas.ws.it;

import org.binas.ws.BadInit_Exception;
import org.binas.ws.CoordinatesView;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ListStationsIT extends BaseIT {

    private CoordinatesView coordinatesView = new CoordinatesView();

    @BeforeClass
    public static void setUp() throws BadInit_Exception {
        client.testInitStation("T07_Station01", 2, 3, 10, 0);
        client.testInitStation("T07_Station01", 10, 10, 10, 0);
        client.testInitStation("T07_Station01", 2, 4, 10, 0);
    }
    @Test
    public void listStationsTest(){
        coordinatesView.setX(3);
        coordinatesView.setY(4);
        client.listStations(3, coordinatesView);
    }

    @Test
    public void listStations2EqualDistancesTest(){
        coordinatesView.setX(3);
        coordinatesView.setY(4);
        client.listStations(3, coordinatesView);
    }

    @Test
    public void listStations3EqualDistancesTest(){
        coordinatesView.setX(3);
        coordinatesView.setY(4);
        client.listStations(3, coordinatesView);
    }

    @AfterClass
    public static void cleanup() {
        client.testClear();

    }
}
