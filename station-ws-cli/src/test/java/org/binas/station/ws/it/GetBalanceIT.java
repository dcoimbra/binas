package org.binas.station.ws.it;

import org.binas.station.ws.InvalidEmail_Exception;
import org.binas.station.ws.ValTagPair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GetBalanceIT extends BaseIT {

    @Before
    public void setUp(){
        client.setBalance(EMAIL_DAVID, 10, "0:T07_Binas");
    }

    @Test
    public void success() throws InvalidEmail_Exception {
        ValTagPair v = client.getBalance(EMAIL_DAVID);
        int balance = v.getBalance();
        String tag = v.getTag();

        Assert.assertEquals(10, balance);
        Assert.assertEquals("0:T07_Binas", tag);
    }

    @Test()
    public void nullEmail() throws InvalidEmail_Exception {
        client.getBalance("");
    }

    @Test
    public void emptyEmail() throws InvalidEmail_Exception{
        client.getBalance(null);
    }

    @Test
    public void emailDoesNotExist() throws InvalidEmail_Exception{
        ValTagPair v = client.getBalance("lucia@lucia");

        Assert.assertNull(v);
    }
}
