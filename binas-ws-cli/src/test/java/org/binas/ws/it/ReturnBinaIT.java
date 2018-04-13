package org.binas.ws.it;

import org.binas.ws.FullStation_Exception;
import org.binas.ws.InvalidStation_Exception;
import org.binas.ws.NoBinaRented_Exception;
import org.binas.ws.UserNotExists_Exception;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ReturnBinaIT extends BaseIT{

    @BeforeClass
    public static void setUp() throws Exception{
        client.testInit(10);
        client.rentBina("T07_Station1", "david@tecnico.pt");
    }

    @Test(expected = InvalidStation_Exception.class)
    public void returnBinaEmptyStationTest() throws UserNotExists_Exception, NoBinaRented_Exception, FullStation_Exception, InvalidStation_Exception {
        client.returnBina("", "david@tecnico.pt");
    }

    @Test(expected = UserNotExists_Exception.class)
    public void returnBinaEmptyEmailTest() throws UserNotExists_Exception, NoBinaRented_Exception, FullStation_Exception, InvalidStation_Exception {
        client.returnBina("T07_Station1", "");
    }

    @Test(expected = UserNotExists_Exception.class)
    public void returnBinaNullEmailTest() throws UserNotExists_Exception, NoBinaRented_Exception, FullStation_Exception, InvalidStation_Exception {
        client.returnBina("T07_Station1", null);
    }

    @Test(expected = InvalidStation_Exception.class)
    public void returnBinaNullStationTest() throws UserNotExists_Exception, NoBinaRented_Exception, FullStation_Exception, InvalidStation_Exception{
        client.returnBina(null, "lucia@not.com");
    }

    @AfterClass
    public static void cleanup() {
        client.testClear();
    }
}
