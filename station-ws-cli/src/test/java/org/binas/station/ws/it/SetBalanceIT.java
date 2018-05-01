package org.binas.station.ws.it;

import org.binas.station.ws.InvalidEmail_Exception;
import org.binas.station.ws.ValTagPair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SetBalanceIT extends BaseIT {

    @Before
    public void setUp(){
        client.setBalance(EMAIL_DAVID, 10, "1:T07_Binas");
    }

    @Test
    public void success() throws InvalidEmail_Exception{
        client.setBalance("lucia@a", 10, "1:T07_Binas");

        ValTagPair val = client.getBalance("lucia@a");

        Assert.assertNotNull(val);
    }

    @Test
    public void updatedBalance () throws InvalidEmail_Exception {
        client.setBalance(EMAIL_DAVID, 15, "2:T07_Binas");
        ValTagPair val = client.getBalance(EMAIL_DAVID);

        Assert.assertEquals(15, val.getBalance());
    }

    @Test
    public void notUpdatedBalance() throws InvalidEmail_Exception{

        client.setBalance(EMAIL_DAVID, 15, "0:T07_Binas");
        ValTagPair val = client.getBalance(EMAIL_DAVID);

        Assert.assertEquals(10, val.getBalance());
    }
}
