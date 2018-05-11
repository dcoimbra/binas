package example;

import java.io.IOException;
import java.io.InputStream;

import java.util.Arrays;
import java.util.Properties;
import java.util.Date;

import java.security.Key;
import java.security.SecureRandom;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

import javax.crypto.Mac;

import pt.ulisboa.tecnico.sdis.kerby.cli.KerbyClient;
import pt.ulisboa.tecnico.sdis.kerby.CipheredView;
import pt.ulisboa.tecnico.sdis.kerby.SecurityHelper;
import pt.ulisboa.tecnico.sdis.kerby.SessionKey;
import pt.ulisboa.tecnico.sdis.kerby.SessionKeyAndTicketView;
import pt.ulisboa.tecnico.sdis.kerby.Ticket;
import pt.ulisboa.tecnico.sdis.kerby.Auth;
import pt.ulisboa.tecnico.sdis.kerby.RequestTime;
import pt.ulisboa.tecnico.sdis.kerby.CipherClerk;

public class KerbyExperiment {

    /** Makes a message authentication code. */
    private static byte[] makeMAC(byte[] bytes, Key key) throws Exception {

        Mac cipher = Mac.getInstance("HmacSHA256");
        cipher.init(key);
        byte[] cipherDigest = cipher.doFinal(bytes);

        return cipherDigest;
    }

    /**
     * Calculates new digest from text and compare it to the to deciphered
     * digest.
     */
    private static boolean verifyMAC(byte[] cipherDigest, byte[] bytes, Key key) throws Exception {

        Mac cipher = Mac.getInstance("HmacSHA256");
        cipher.init(key);
        byte[] cipheredBytes = cipher.doFinal(bytes);
        return Arrays.equals(cipherDigest, cipheredBytes);
    }

    public static void main(String[] args) throws Exception {

        String password = "fXp5TsK2";
        String wsUrl = "http://sec.sd.rnl.tecnico.ulisboa.pt:8888/kerby";
        String clientEmail = "alice@T07.binas.org";
        String serverEmail = "binas@T07.binas.org";
        int duration = 30;

        String message = "Message";

        System.out.println("Hi!");

        System.out.println();

        // receive arguments
        System.out.printf("Received %d arguments%n", args.length);

        System.out.println();

        // load configuration properties
        try {
            InputStream inputStream = KerbyExperiment.class.getClassLoader().getResourceAsStream("config.properties");
            // variant for non-static methods:
            // InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties");

            Properties properties = new Properties();
            properties.load(inputStream);

            System.out.printf("Loaded %d properties%n", properties.size());

        } catch (IOException e) {
            System.out.printf("Failed to load configuration: %s%n", e);
        }

        System.out.println();

		// client-side code experiments
        System.out.println("Experiment with Kerberos client-side processing");

        //connect client with Kerby authentication service
        KerbyClient client = new KerbyClient(wsUrl);
        System.out.println("Created client for " + client.getWsURL());

        //generate Kc
        Key clientKey = SecurityHelper.generateKeyFromPassword(password);
        System.out.println("Generated client key from password");

        //generate nounce
        SecureRandom randomGenerator = new SecureRandom();
        long nounce = randomGenerator.nextLong();
        System.out.println("Generated nounce randomly");

        //Authenticate client with Kerby authentication service
        SessionKeyAndTicketView result = client.requestTicket(clientEmail, serverEmail, nounce, duration);
        System.out.println("Sent ticket request for " + clientEmail + " to " + serverEmail + " with nounce " + nounce + " for the duration of " + duration);

        //Receive session key and ticket encrypted with Ksc
        CipheredView cipheredSessionKey = result.getSessionKey();
        CipheredView cipheredTicket = result.getTicket();
        System.out.println("Received encrypted session key and ticket");

        //Decrypt Kcs with Kc
        SessionKey sessionKey = new SessionKey(cipheredSessionKey, clientKey);
        System.out.println("Session key: " + sessionKey);

        // make MAC with Ksc
        byte[] mac = makeMAC(message.getBytes(), sessionKey.getKeyXY());

        //Create authenticator and encrypt with Ksc
        Auth auth = new Auth(clientEmail, new Date());
        CipheredView cipheredAuth = auth.cipher(sessionKey.getKeyXY());

        //Send encrypted ticket, authenticator, message and MAC
        System.out.println("Sent ciphered ticket and authenticator for " + auth.getX() + " to " + serverEmail + " with timestamp " + auth.getTimeRequest());
        System.out.println("Sent message: Message");
        System.out.println("Sent MAC: " + printHexBinary(mac));
        System.out.println();

		// server-side code experiments
        System.out.println("Experiment with Kerberos server-side processing");

        //generate Ks
        Key serverKey = SecurityHelper.generateKeyFromPassword("cpiNanR");

        //Decrypt ticket with Ks
        Ticket ticket = new Ticket(cipheredTicket, serverKey);
        System.out.println("Received ticket from " + ticket.getX());

        //validate ticket
        ticket.validate();
        System.out.println("Ticket is valid.");
        System.out.println("Ticket: " + ticket);

        System.out.println("Received message from " + ticket.getX());

        //verify the MAC with Ksc
        boolean verified = verifyMAC(mac, message.getBytes(), ticket.getKeyXY());

        if (!verified) {
            System.out.println("MAC doesn't match.");
            return;
        }

        System.out.println("MAC matches");

        //decrypt authenticator with Ksc
        Auth decipheredAuth = new Auth(cipheredAuth, ticket.getKeyXY());
        System.out.println("Received authenticator from " + decipheredAuth.getX());

        //validate authenticator
        decipheredAuth.validate();
        System.out.println("Authenticator is valid.");
        System.out.println("Authenticator: " + auth.authToString());

        //check if ticket sender and authenticator sender match
        if (!ticket.getX().equals(auth.getX())) {
            System.out.println("Ticket and authenticator don't match.");
            return;
        }

        System.out.println("User authenticated.");

        System.out.println();

        //send encrypted authenticator timestamp back to client
        RequestTime treq = new RequestTime(decipheredAuth.getTimeRequest());
        CipheredView cipheredRequestTime = treq.cipher(ticket.getKeyXY());
        System.out.println("Sent encrypted timestamp to " + ticket.getX());

        System.out.println();
		
		System.out.println("Bye!");
    }
}
