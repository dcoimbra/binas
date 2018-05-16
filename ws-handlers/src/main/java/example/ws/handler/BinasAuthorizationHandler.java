package example.ws.handler;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import pt.ulisboa.tecnico.sdis.kerby.Auth;
import pt.ulisboa.tecnico.sdis.kerby.CipheredView;
import pt.ulisboa.tecnico.sdis.kerby.Ticket;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.security.Key;
import java.util.Set;

public class BinasAuthorizationHandler implements SOAPHandler<SOAPMessageContext> {

    /**
     * Gets the names of the header blocks that can be processed by this Handler instance.
     * If null, processes all.
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

            //Retorna um booleano que indica se é outbound ou inbound, neste caso é inbound
            Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

            if (!outboundElement.booleanValue()) {

                try {
                    CipheredView cipheredView = (CipheredView) smc.get("TIMESTAMP_RESPONSE");
                    Key key = (Key) smc.get("SESSION_KEY");

                    Ticket ticket = (Ticket) smc.get("TICKET");
                    Auth auth = (Auth) smc.get("AUTH");

                    String ticketEmail = ticket.getX();
                    String authEmail = auth.getX();

                    // get SOAP envelope header
                    SOAPMessage msg = smc.getMessage();
                    SOAPPart sp = msg.getSOAPPart();
                    SOAPEnvelope soapEnvelope = sp.getEnvelope();
                    SOAPBody soapBody = soapEnvelope.getBody();
                    Document doc = soapBody.extractContentAsDocument();

                    NodeList emailList = doc.getElementsByTagName("email");
                    Node emailNode = emailList.item(0);

                    if(emailNode.getNodeValue() == null){
                        throw new RuntimeException("Email not found");
                    }

                    String email = emailNode.getNodeValue();

                    if(!(email.equals(ticketEmail) && email.equals(authEmail))){
                        throw new RuntimeException("Email not authenticated");
                    }

                } catch (SOAPException e) {
                    System.out.println("SOAP exception");
                    throw new RuntimeException(e.getMessage());
                }catch (NullPointerException e) {
                    System.out.println("exception");
                    e.printStackTrace();
                }

            }

        return true;
    }

    /** The handleFault method is invoked for fault message processing. */
    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    /**
     * Called at the conclusion of a message exchange pattern just prior to the
     * JAX-WS runtime dispatching a message, fault or exception.
     */
    @Override
    public void close(MessageContext context) {

    }
}