package org.binas.station.ws.it;

import org.binas.station.ws.ValTagPair;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SetBalanceIT extends BaseIT {

    @Before
    public void setUp(){
        client.setBalance(EMAIL_DAVID, 10, "1:T07_Binas");
    }

    @Test
    public void success(){
        client.setBalance("lucia@a", 10, "1:T07_Binas");

        ValTagPair val = client.getBalance("lucia@a");

        Assert.assertNotNull(val);
    }

    @Test
    public void updatedBalance (){
        client.setBalance(EMAIL_DAVID, 15, "2:T07_Binas");
        ValTagPair val = client.getBalance(EMAIL_DAVID);

        Assert.assertEquals(15, val.getBalance());
    }

    @Test
    public void notUpdatedBalance() {

        client.setBalance(EMAIL_DAVID, 15, "0:T07_Binas");
        ValTagPair val = client.getBalance(EMAIL_DAVID);

        Assert.assertEquals(10, val.getBalance());
    }

    @Test
    public void negativeBalance(){

        client.setBalance(EMAIL_DAVID, -3, "1:T07_Binas");
        ValTagPair val = client.getBalance(EMAIL_DAVID);
        Assert.assertEquals(10, val.getBalance());
    }

    @Test
    public void emptyTag(){

        client.setBalance("luciaa@a", 4, "");
        ValTagPair val = client.getBalance("luciaa@a");
        Assert.assertNull(val);
    }

    @Test
    public void nullTag(){

        client.setBalance("luciaa@a", 4, null);
        ValTagPair val = client.getBalance("luciaa@a");
        Assert.assertNull(val);
    }

    @AfterClass
    public static void cleanup() {
        client.testClear();
    }


}
