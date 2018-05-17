package example.ws.handler;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import pt.ulisboa.tecnico.sdis.kerby.Auth;
import pt.ulisboa.tecnico.sdis.kerby.Ticket;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Iterator;
import java.util.Set;

public class MaliciousHandlerMACTest implements SOAPHandler<SOAPMessageContext> {

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

        Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        if (outboundElement.booleanValue()) {
            System.out.println("Attacking this OUTbound message...");
            try {

                // get SOAP envelope
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();
                SOAPBody soapBody = se.getBody();

                //attack soap message
                QName attackName = new QName("attack");
                SOAPBodyElement bodyElement = soapBody.addBodyElement(attackName);

                bodyElement.addTextNode("This is an attack.");

            } catch (SOAPException e) {
                throw new RuntimeException(e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
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