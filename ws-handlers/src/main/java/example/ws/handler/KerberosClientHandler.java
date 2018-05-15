package example.ws.handler;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.Set;

import java.security.Key;
import java.security.SecureRandom;

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

                //Create authenticator and encrypt with Ksc
                Auth auth = new Auth(CLIENT_EMAIL, new Date());
                CipheredView cipheredAuth = auth.cipher(sessionKey.getKeyXY());

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

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e.getMessage());
        } catch (KerbyClientException e) {
            throw new RuntimeException(e.getMessage());
        } catch (BadTicketRequest_Exception e) {
            throw new RuntimeException(e.getMessage());
        } catch (KerbyException e) {
            throw new RuntimeException(e.getMessage());
        } catch (JAXBException e) {
            throw new RuntimeException(e.getMessage());
        } catch (SOAPException e) {
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