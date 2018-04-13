package org.binas.ws.it;

import org.junit.Assert;
import org.junit.Test;


/**
 * Test suite
 */
public class PingIT extends BaseIT {

    // tests
    // assertEquals(expected, actual);

    // public String ping(String x)

    @Test
    public void pingEmptyTest() {
		Assert.assertNotNull(client.testPing("test"));
    }

}
