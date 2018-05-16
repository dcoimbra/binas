package example.ws.handler;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.Set;
import java.util.Iterator;

import java.lang.RuntimeException;

import java.security.Key;

import pt.ulisboa.tecnico.sdis.kerby.*;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This SOAPHandler manages client authentication using the Kerberos protocol on the server side.
 */
public class KerberosServerHandler implements SOAPHandler<SOAPMessageContext> {


    private static final String PASSWORD = "cpiNanR";
    private static final String SERVER_NAME = "binas@T07.binas.org";


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

            if (!outboundElement.booleanValue()) {

                //generate Ks
                Key serverKey = SecurityHelper.generateKeyFromPassword(PASSWORD);

                CipherClerk clerk = new CipherClerk();

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

                // get ticket header element
                Name ticketName = se.createName("ticket", "binas", "http://ws.binas.org/");

                Iterator<?> ticketHeaderIt = sh.getChildElements(ticketName);

                // check ticket header element
                if (!ticketHeaderIt.hasNext()) {
                    throw new RuntimeException("Ticket header element not found");
                }

                SOAPElement ticketElement = (SOAPElement) ticketHeaderIt.next();

                //get ciphered ticket
                NodeList ticketList = ticketElement.getElementsByTagName("ticket");
                Node cipheredTicketNode = ticketList.item(0);

                CipheredView cipheredTicket = clerk.cipherFromXMLNode(cipheredTicketNode);

                //Decrypt ticket with Ks
                Ticket ticket = new Ticket(cipheredTicket, serverKey);

                //validate ticket
                ticket.validate();
                
                Thread.sleep(2000); //para forcar a data actual a ser anterior ao time1 do ticket
                Date now = new Date();

                if (!(now.after(ticket.getTime1()) && now.before(ticket.getTime2())) ) {
                    throw new RuntimeException("Invalid ticket: now="+now+" t1="+ticket.getTime1()+"; t2="+ticket.getTime2());
                }
                if ((!ticket.getY().equals(SERVER_NAME))) {
                	 throw new RuntimeException("Invalid ticket: ty="+ticket.getY()+"; sn="+SERVER_NAME);
                }

                // get authenticator header element
                Name authName = se.createName("auth", "binas", "http://ws.binas.org/");

                Iterator<?> authHeaderIt = sh.getChildElements(authName);

                // check authenticator header element
                if (!authHeaderIt.hasNext()) {
                    throw new RuntimeException("Authenticator header element not found.");
                }

                SOAPElement authElement = (SOAPElement) authHeaderIt.next();

                //get ciphered authenticator
                NodeList authList = authElement.getElementsByTagName("auth");
                Node cipheredAuthNode = authList.item(0);

                CipheredView cipheredAuth = clerk.cipherFromXMLNode(cipheredAuthNode);

                //Decrypt authenticator with Kcs
                Auth auth = new Auth(cipheredAuth, ticket.getKeyXY());

                //validate authenticator
                auth.validate();

                if (!auth.getX().equals(ticket.getX())) {

                    throw new RuntimeException("Invalid authenticator.");
                }

                RequestTime treq = new RequestTime(auth.getTimeRequest());

                CipheredView cipheredtReq = treq.cipher(ticket.getKeyXY());
                
                // put header in a property context
                smc.put("TIMESTAMP_RESPONSE", cipheredtReq);
                smc.put("SESSION_KEY", ticket.getKeyXY());
                // set property scope to application client/server class can
                // access it
                smc.setScope("TIMESTAMP_RESPONSE", Scope.HANDLER);
                smc.setScope("SESSION_KEY", Scope.HANDLER);

            }

        } catch (NoSuchAlgorithmException e) {
        	System.out.println("no such algorithm");
            throw new RuntimeException(e.getMessage());
        } catch (InvalidKeySpecException e) {
        	System.out.println("invalid key");
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
        } catch (Exception e) {
        	System.out.println("cebola: "+e.getMessage());
        	try {
				throw e;
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
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