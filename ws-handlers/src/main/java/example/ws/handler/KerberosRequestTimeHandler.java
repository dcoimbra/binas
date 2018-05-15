package example.ws.handler;

import org.w3c.dom.Node;
import pt.ulisboa.tecnico.sdis.kerby.CipherClerk;
import pt.ulisboa.tecnico.sdis.kerby.CipheredView;
import pt.ulisboa.tecnico.sdis.kerby.RequestTime;

import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * This SOAPHandler adds a RequestTime instance to a server response.
 */
public class KerberosRequestTimeHandler implements SOAPHandler<SOAPMessageContext> {

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
     * The handleMessage method is invoked for normal processing of in-bound and
     * out-bound messages.
     */
    @Override
    public boolean handleMessage(SOAPMessageContext smc) {

        Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        try {

            if (outboundElement.booleanValue()) {

                CipheredView cipheredTreq = (CipheredView) smc.get("TIMESTAMP_RESPONSE");

                System.out.println(cipheredTreq);

                // get SOAP envelope
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();

                // add header
                SOAPHeader sh = se.getHeader();
                if (sh == null)
                    sh = se.addHeader();

                // add request time header element (name, namespace prefix, namespace)
                Name requestTimeName = se.createName("requestTime", "binas", "http://ws.binas.org/");
                SOAPHeaderElement requestTimeElement = sh.addHeaderElement(requestTimeName);

                // add ticket header element value
                CipherClerk clerk = new CipherClerk();
                org.w3c.dom.Node requestTimeNode = clerk.cipherToXMLNode(cipheredTreq, requestTimeName.getLocalName());
                Node importedRequestTimeNode = sp.importNode(requestTimeNode.getFirstChild(), true);
                requestTimeElement.appendChild(importedRequestTimeNode);
            }
        } catch (SOAPException e) {
            throw new RuntimeException(e.getMessage());
        } catch (JAXBException e) {
            throw new RuntimeException(e.getMessage());
        }
        return true;
    }

    /** The handleFault method is invoked for fault message processing. */
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
