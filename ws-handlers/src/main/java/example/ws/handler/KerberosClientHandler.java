package example.ws.handler;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import java.security.Key;
import java.security.SecureRandom;

import org.w3c.dom.NodeList;
import pt.ulisboa.tecnico.sdis.kerby.*;
import pt.ulisboa.tecnico.sdis.kerby.cli.KerbyClient;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.Node;
import pt.ulisboa.tecnico.sdis.kerby.cli.KerbyClientException;

/**
 * This SOAPHandler manages client authentication using the Kerberos protocol.
 */
public class KerberosClientHandler implements SOAPHandler<SOAPMessageContext> {

    private static final String WS_URL = "http://sec.sd.rnl.tecnico.ulisboa.pt:8888/kerby";
    private static final String PASSWORD = "fXp5TsK2";
    private static final String CLIENT_EMAIL = "alice@T07.binas.org";
    private static final String SERVER_EMAIL = "binas@T07.binas.org";
    private static final int DURATION = 30;

    //
    // Handler interface implementation
    //

    /**
     * Gets the header blocks that can be processed by this Handler instance. If
     * null, processes all.
     */
    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    /**
     * The handleMessage method is invoked for normal processing of inbound and
     * outbound messages.
     */
    @Override
    public boolean handleMessage(SOAPMessageContext smc) {

        try {

            Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

            if (outboundElement.booleanValue()) {

                CipherClerk clerk = new CipherClerk();
                SecureRandom randomGenerator = new SecureRandom();

                //connect client with Kerby authentication service
                KerbyClient client = new KerbyClient(WS_URL);

                //generate Kc
                Key clientKey = SecurityHelper.generateKeyFromPassword(PASSWORD);

                //generate nounce
                long nounce = randomGenerator.nextLong();

                //Authenticate client with Kerby authentication service
                SessionKeyAndTicketView result = client.requestTicket(CLIENT_EMAIL, SERVER_EMAIL, nounce, DURATION);

                //Receive session key and ticket encrypted with Ksc
                CipheredView cipheredSessionKey = result.getSessionKey();
                CipheredView cipheredTicket = result.getTicket();

                //Decrypt Kcs with Kc
                SessionKey sessionKey = new SessionKey(cipheredSessionKey, clientKey);

                //validate session key with nounce
                if (sessionKey.getNounce() != nounce) {
                    throw new RuntimeException("Nounces don't match");
                }

                smc.put("SESSION_KEY", sessionKey.getKeyXY());

                //Create authenticator and encrypt with Ksc
                Auth auth = new Auth(CLIENT_EMAIL, new Date());
                CipheredView cipheredAuth = auth.cipher(sessionKey.getKeyXY());
                smc.put("AUTH", auth);


                // get SOAP envelope
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();

                // add header
                SOAPHeader sh = se.getHeader();
                if (sh == null)
                    sh = se.addHeader();

                // add ticket header element (name, namespace prefix, namespace)
                Name ticketName = se.createName("ticket", "binas", "http://ws.binas.org/");
                SOAPHeaderElement ticketElement = sh.addHeaderElement(ticketName);

                // add ticket header element value
                Node ticketNode = clerk.cipherToXMLNode(cipheredTicket, ticketName.getLocalName());
                Node importedticketNode = sp.importNode(ticketNode.getFirstChild(), true);
                ticketElement.appendChild(importedticketNode);

                // add auth header element (name, namespace prefix, namespace)
                Name authName = se.createName("auth", "binas", "http://ws.binas.org/");
                SOAPHeaderElement authElement = sh.addHeaderElement(authName);

                // add auth header element value
                Node authNode = clerk.cipherToXMLNode(cipheredAuth, authName.getLocalName());
                Node importedAuthNode = sp.importNode(authNode.getFirstChild(), true);
                authElement.appendChild(importedAuthNode);
            }

            else {

                System.out.println("Reading header from INbound SOAP message...");

                // get SOAP envelope header
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();
                SOAPHeader sh = se.getHeader();

                // check header
                if (sh == null) {
                    throw new RuntimeException("Header not found");
                }

                // get requestTime header element
                Name requestTimeName = se.createName("requestTime", "binas", "http://ws.binas.org/");

                Iterator<?> requestTimeIt = sh.getChildElements(requestTimeName);

                // check requestTime header element
                if (!requestTimeIt.hasNext()) {
                    throw new RuntimeException("Request time header element not found");
                }

                SOAPElement requestTimeElement = (SOAPElement) requestTimeIt.next();

                //get ciphered request time
                NodeList requestTimeList = requestTimeElement.getElementsByTagName("requestTime");
                Node cipheredRequestTimeNode = requestTimeList.item(0);

                CipherClerk clerk = new CipherClerk();

                CipheredView cipheredRequestTime = clerk.cipherFromXMLNode(cipheredRequestTimeNode);

                RequestTime requestTime = new RequestTime(cipheredRequestTime, (Key) smc.get("SESSION_KEY"));

                requestTime.validate();

                Auth auth = (Auth) smc.get("AUTH");
                
                RequestTime authRequestTime = new RequestTime(auth.getTimeRequest());

                if (!requestTime.equals(authRequestTime)) {
                    throw new RuntimeException("Timestamps don't match.");
                }
            }

        } catch (NoSuchAlgorithmException e) {
        	System.out.println("no such algorithm");
            throw new RuntimeException(e.getMessage());
        } catch (InvalidKeySpecException e) {
        	System.out.println("invalid key");
            throw new RuntimeException(e.getMessage());
        } catch (KerbyClientException e) {
        	System.out.println("kerby client exception");
            throw new RuntimeException(e.getMessage());
        } catch (BadTicketRequest_Exception e) {
        	System.out.println("bad ticket request");
            throw new RuntimeException(e.getMessage());
        } catch (KerbyException e) {
        	System.out.println("kerby exception");
            throw new RuntimeException(e.getMessage());
        } catch (JAXBException e) {
        	System.out.println("JAXB exception");
            throw new RuntimeException(e.getMessage());
        } catch (SOAPException e) {
        	System.out.println("SOAP exception");
            throw new RuntimeException(e.getMessage());
        } catch(Exception e) {
        	System.out.println("Error:");
        	throw new RuntimeException(e.getMessage());
        }

        return true;
    }

    /**
     * The handleFault method is invoked for fault message processing.
     */
    @Override
    public boolean handleFault(SOAPMessageContext smc) {

        return true;
    }

    /**
     * Called at the conclusion of a message exchange pattern just prior to the
     * JAX-WS runtime dispatching a message, fault or exception.
     */
    @Override
    public void close(MessageContext messageContext) {
        // nothing to clean up
    }
}