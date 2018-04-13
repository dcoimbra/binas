package org.binas.ws.it;

import org.binas.ws.AlreadyHasBina_Exception;
import org.binas.ws.EmailExists_Exception;
import org.binas.ws.FullStation_Exception;
import org.binas.ws.InvalidEmail_Exception;
import org.binas.ws.InvalidStation_Exception;
import org.binas.ws.NoBinaAvail_Exception;
import org.binas.ws.NoBinaRented_Exception;
import org.binas.ws.NoCredit_Exception;
import org.binas.ws.UserNotExists_Exception;
import org.junit.*;

public class ReturnBinaIT extends BaseIT{

    @BeforeClass
    public static void setUp() throws EmailExists_Exception, InvalidEmail_Exception {
        client.activateUser(EMAIL_DAVID);
    }
    
    @Test
    public void sucessReturnBina() throws UserNotExists_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception, FullStation_Exception, NoBinaRented_Exception {
    	int credit = client.getCredit(EMAIL_DAVID);
        client.rentBina(STATION_ID1, EMAIL_DAVID);
        Assert.assertEquals(credit-1, client.getCredit(EMAIL_DAVID)); 
        
        int totalReturns = client.getInfoStation(STATION_ID1).getTotalReturns();
        client.returnBina(STATION_ID1, EMAIL_DAVID);
        Assert.assertEquals(totalReturns+1, client.getInfoStation(STATION_ID1).getTotalReturns());
    }

    @Test(expected = UserNotExists_Exception.class)
    public void userNotExists() throws NoBinaAvail_Exception, NoCredit_Exception, InvalidStation_Exception, AlreadyHasBina_Exception, UserNotExists_Exception, FullStation_Exception, NoBinaRented_Exception {
        client.returnBina(STATION_ID1, "teste@binas");
    }

    @Test(expected = NoBinaRented_Exception.class)
    public void noBinaRented() throws UserNotExists_Exception, InvalidStation_Exception, NoBinaRented_Exception, FullStation_Exception {

        client.returnBina(STATION_ID1, EMAIL_DAVID);
    }

    @Test(expected = FullStation_Exception.class)
    public void fullStation() throws NoBinaAvail_Exception, NoCredit_Exception, InvalidStation_Exception, AlreadyHasBina_Exception, UserNotExists_Exception, FullStation_Exception, NoBinaRented_Exception {
        client.rentBina(STATION_ID1, EMAIL_DAVID);
        client.returnBina(STATION_ID2, EMAIL_DAVID);
    }

    @Test(expected = InvalidStation_Exception.class)
    public void returnBinaEmptyStationTest() throws UserNotExists_Exception, NoBinaRented_Exception, FullStation_Exception, InvalidStation_Exception {
        client.returnBina("", EMAIL_DAVID);
    }

    @Test(expected = UserNotExists_Exception.class)
    public void returnBinaEmptyEmailTest() throws UserNotExists_Exception, NoBinaRented_Exception, FullStation_Exception, InvalidStation_Exception {
        client.returnBina(STATION_ID1, "");
    }

    @Test(expected = UserNotExists_Exception.class)
    public void returnBinaNullEmailTest() throws UserNotExists_Exception, NoBinaRented_Exception, FullStation_Exception, InvalidStation_Exception {
        client.returnBina(STATION_ID1, null);
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
