package org.binas.ws.it;


public class RentBinaIT extends BaseIT {

    /*private final String EMAIL = "a@a.a";

    @Before
    public void setUp() throws EmailExists_Exception, InvalidEmail_Exception {
        client.activateUser(EMAIL);
    }


    @Test(expected = InvalidStation_Exception.class)
    public void rentBinaNullStationTest() throws NoCredit_Exception, AlreadyHasBina_Exception, UserNotExists_Exception, InvalidStation_Exception, NoBinaAvail_Exception{
        String email = "lucia@not.com";

        client.rentBina(null, email);
    }

    @Test(expected = UserNotExists_Exception.class)
    public void rentBinaNullUserTest() throws NoCredit_Exception, AlreadyHasBina_Exception, UserNotExists_Exception, InvalidStation_Exception, NoBinaAvail_Exception{
        client.rentBina("T07_Station01", null);
    }

    @Test(expected = InvalidStation_Exception.class)
    public void rentBinaEmptyStationTest() throws NoCredit_Exception, AlreadyHasBina_Exception, UserNotExists_Exception, InvalidStation_Exception, NoBinaAvail_Exception{
        String email = "lucia@not.com";

        client.rentBina("", email);
    }

    @Test(expected = UserNotExists_Exception.class)
    public void rentBinaEmptyTest() throws NoCredit_Exception, AlreadyHasBina_Exception, UserNotExists_Exception, InvalidStation_Exception, NoBinaAvail_Exception{
        client.rentBina("T07_Station01", "");
    }

    @AfterClass
    public static void cleanup() {
        client.testClear();
    }

    @Test
    public void successRentBina() throws UserNotExists_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception {
        int credit = client.getCredit(EMAIL);
        int before_bikes = client.getInfoStation("T07_Station2").getAvailableBinas();
        client.rentBina("T07_Station2", EMAIL);

        Assert.assertEquals(credit-1,client.getCredit(EMAIL), 0);
        Assert.assertEquals(before_bikes-1, client.getInfoStation("T07_Station2").getAvailableBinas());
    }

    @AfterClass
    public static void tearDown() {
        client.testClear();
    }*/
}

