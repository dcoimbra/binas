package org.binas.ws.it;

import org.binas.ws.BadInit_Exception;
import org.binas.ws.CoordinatesView;
import org.binas.ws.StationView;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

public class ListStationsIT extends BaseIT {

    private CoordinatesView coordinatesView = new CoordinatesView();

    @BeforeClass
    public static void setUp() throws BadInit_Exception {
        client.testInitStation(STATION_ID1, 22, 7, 6, 2);
        client.testInitStation(STATION_ID2, 80, 20, 12, 1);
        client.testInitStation(STATION_ID3, 50, 50, 20, 0);
        client.testInitStation(STATION_ID4, 2, 3, 10, 0);
        client.testInitStation(STATION_ID5, 10, 10, 10, 0);
        client.testInitStation(STATION_ID6, 2, 4, 10, 0);
        client.testInitStation(STATION_ID7, 80, 80, 10, 0);
        client.testInitStation(STATION_ID8, 90, 90, 10, 0);
    }

    @Test
    public void success1(){
        coordinatesView.setX(3);
        coordinatesView.setY(4);
        List<StationView> list = client.listStations(3, coordinatesView);

        List<String> expected = new ArrayList<>();
        expected.add(STATION_ID6);
        expected.add(STATION_ID4);
        expected.add(STATION_ID5);

        for(int i = 0; i < expected.size(); i++){
            Assert.assertEquals(expected.get(i), list.get(i).getId());
        }

    }

    @Test
    public void success2(){
        coordinatesView.setX(3);
        coordinatesView.setY(4);
        List<StationView> list = client.listStations(5, coordinatesView);

        List<String> expected = new ArrayList<>();
        expected.add(STATION_ID6);
        expected.add(STATION_ID4);
        expected.add(STATION_ID5);
        expected.add(STATION_ID1);
        expected.add(STATION_ID3);

        for(int i = 0; i < expected.size(); i++){
            Assert.assertEquals(expected.get(i), list.get(i).getId());
        }
    }

    @Test
    public void success3(){
        coordinatesView.setX(3);
        coordinatesView.setY(4);
        List<StationView> list = client.listStations(8, coordinatesView);

        List<String> expected = new ArrayList<>();
        expected.add(STATION_ID6);
        expected.add(STATION_ID4);
        expected.add(STATION_ID5);
        expected.add(STATION_ID1);
        expected.add(STATION_ID3);
        expected.add(STATION_ID2);
        expected.add(STATION_ID7);
        expected.add(STATION_ID8);

        for(int i = 0; i < expected.size(); i++){
            Assert.assertEquals(expected.get(i), list.get(i).getId());
        }
    }

    @Test
    public void listStationsNegativeStationNumberTest(){
        coordinatesView.setX(3);
        coordinatesView.setY(4);
        List<StationView> list = client.listStations(-1, coordinatesView);
        assertTrue(list.isEmpty());
    }

    @Test
    public void listStationsNegativeCoordinatesTest(){
        coordinatesView.setX(-2);
        coordinatesView.setY(-5);
        List<StationView> list = client.listStations(3, coordinatesView);
        assertTrue(list.isEmpty());
    }

    @Test
    public void listStationsOutOfBoundsCoordinatesTest(){
        coordinatesView.setX(100);
        coordinatesView.setY(0);
        List<StationView> list = client.listStations(3, coordinatesView);
        assertTrue(list.isEmpty());
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
