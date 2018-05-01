package org.binas.station.ws.it;

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
    public void success(){
        ValTagPair v = client.getBalance(EMAIL_DAVID);
        int balance = v.getBalance();
        String tag = v.getTag();

        Assert.assertEquals(10, balance);
        Assert.assertEquals("0:T07_Binas", tag);
    }

    @Test
    public void nullEmail(){
        ValTagPair v = client.getBalance("");

        Assert.assertNull(v);
    }

    @Test
    public void emptyEmail(){
        ValTagPair v = client.getBalance(null);

        Assert.assertNull(v);
    }

    @Test
    public void emailDoesNotExist(){
        ValTagPair v = client.getBalance("lucia@lucia");

        Assert.assertNull(v);
    }
}
