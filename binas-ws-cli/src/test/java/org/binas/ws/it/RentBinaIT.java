package org.binas.ws.it;


import org.binas.ws.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class RentBinaIT extends BaseIT {

    private final static String EMAIL = "a@a.a";

    @BeforeClass
    public static void setUp() throws EmailExists_Exception, InvalidEmail_Exception {
        client.activateUser(EMAIL);
    }

    @Test
    public void successRentBina() throws UserNotExists_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception, EmailExists_Exception, InvalidEmail_Exception {
    	int credit = client.getCredit(EMAIL);
        int before_bikes = client.getInfoStation(STATION_ID2).getAvailableBinas();
        client.rentBina(STATION_ID2, EMAIL);

        Assert.assertEquals(credit-1,client.getCredit(EMAIL), 0);
        Assert.assertEquals(before_bikes-1, client.getInfoStation(STATION_ID2).getAvailableBinas());
    }
    
    @Test (expected = UserNotExists_Exception.class)
    public void userNoExistsException() throws UserNotExists_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception {
        client.rentBina(STATION_ID2, "nao@existo.aqui");
    }
    
    @Test (expected = AlreadyHasBina_Exception.class)
    public void userHasBinaException() throws UserNotExists_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception, EmailExists_Exception, InvalidEmail_Exception {
    	client.activateUser("b@a.a");
    	client.rentBina(STATION_ID2, "b@a.a");
    	client.rentBina(STATION_ID2, "b@a.a");
    }
    
    @Test (expected = AlreadyHasBina_Exception.class)
    public void userHasBinaDiffStationException() throws UserNotExists_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception, EmailExists_Exception, InvalidEmail_Exception {
    	client.rentBina(STATION_ID1, EMAIL);
    }
    
    @Test (expected = NoCredit_Exception.class)
    public void userHasNoCreditException() throws UserNotExists_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception, BadInit_Exception, EmailExists_Exception, InvalidEmail_Exception {
        client.testInit(0);
        client.activateUser(EMAIL_DAVID);
    	client.rentBina(STATION_ID1, EMAIL_DAVID);
    }

    // Argument validity tests --------------------------------------------------------------------
    @Test(expected = InvalidStation_Exception.class)
    public void rentBinaNullStationTest() throws NoCredit_Exception, AlreadyHasBina_Exception, UserNotExists_Exception, InvalidStation_Exception, NoBinaAvail_Exception{
        String email = "lucia@not.com";

        client.rentBina(null, email);
    }

    @Test(expected = UserNotExists_Exception.class)
    public void rentBinaNullUserTest() throws NoCredit_Exception, AlreadyHasBina_Exception, UserNotExists_Exception, InvalidStation_Exception, NoBinaAvail_Exception{
        client.rentBina(STATION_ID2, null);
    }

    @Test(expected = InvalidStation_Exception.class)
    public void rentBinaEmptyStationTest() throws NoCredit_Exception, AlreadyHasBina_Exception, UserNotExists_Exception, InvalidStation_Exception, NoBinaAvail_Exception{
        String email = "lucia@not.com";

        client.rentBina("", email);
    }

    @Test(expected = UserNotExists_Exception.class)
    public void rentBinaEmptyTest() throws NoCredit_Exception, AlreadyHasBina_Exception, UserNotExists_Exception, InvalidStation_Exception, NoBinaAvail_Exception{
        client.rentBina(STATION_ID1, "");
    }


    @AfterClass
    public static void tearDown() {
        client.testClear();
    }
}

