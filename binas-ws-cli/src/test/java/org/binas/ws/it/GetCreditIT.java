package org.binas.ws.it;

import org.binas.ws.UserNotExists_Exception;
import org.junit.AfterClass;
import org.junit.Test;

public class GetCreditIT extends BaseIT {

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
