package org.binas.ws.it;

import org.binas.ws.EmailExists_Exception;
import org.binas.ws.InvalidEmail_Exception;
import org.binas.ws.UserNotExists_Exception;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.Assert;

public class ActivateUserIT extends BaseIT{

    @Test
    public void success() throws EmailExists_Exception, InvalidEmail_Exception, UserNotExists_Exception {

        client.activateUser("david@tecnico.pt");

        Assert.assertEquals(10, client.getCredit("david@tecnico.pt"));
    }

    @Test(expected = EmailExists_Exception.class)
    public void activateUserDuplicateTest() throws EmailExists_Exception, InvalidEmail_Exception {
        String email = "mario@lisboa.com";

        client.activateUser(email);
        client.activateUser(email);
    }

    @Test(expected = InvalidEmail_Exception.class)
    public void activateUserEmptyTest() throws EmailExists_Exception, InvalidEmail_Exception {
        String email = "";

        client.activateUser(email);
    }

    @Test(expected = InvalidEmail_Exception.class)
    public void activateUserNullTest() throws EmailExists_Exception, InvalidEmail_Exception {
        client.activateUser(null);
    }

    @Test
    public void activateUserTest() throws EmailExists_Exception, InvalidEmail_Exception {
        String email = "mariolisboa@lisboa.com";

        client.activateUser(email);
    }

    @AfterClass
    public static void cleanup() {
        client.testClear();
    }
}
