package org.binas.ws.it;

import org.binas.ws.BadInit_Exception;
import org.binas.ws.EmailExists_Exception;
import org.binas.ws.InvalidEmail_Exception;
import org.binas.ws.UserNotExists_Exception;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

public class GetCreditIT extends BaseIT {

    @Test
    public void success() throws UserNotExists_Exception, EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception {

        client.testInit(20);

        Assert.assertEquals(20, client.getCredit("david@tecnico.pt"));
    }

    @Test(expected = BadInit_Exception.class)
    public void negativeCredit() throws BadInit_Exception {
        client.testInit(-20);
    }

    @Test(expected = UserNotExists_Exception.class)
    public void getCreditNullTest() throws UserNotExists_Exception {
        client.getCredit(null);
    }

    @Test(expected = UserNotExists_Exception.class)
    public void getCreditEmptyTest() throws UserNotExists_Exception {
        client.getCredit("");
    }

    @Test(expected = UserNotExists_Exception.class)
    public void getCreditInvalidEmailTest() throws UserNotExists_Exception {
        client.getCredit("lucia@@");
    }

    @AfterClass
    public static void cleanup() {
        client.testClear();
    }
}
