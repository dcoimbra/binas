package example.ws.handler;


import java.io.StringWriter;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.Node;

import pt.ulisboa.tecnico.sdis.kerby.CipherClerk;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

public class MacClientHandler implements SOAPHandler<SOAPMessageContext> {

	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		Boolean isOutbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		
		if(!isOutbound) {	// if message is inbound there is no need to handle/verify message integrity
			return true;
		}
		
		//for outbound messages:
		try {
			CipherClerk clerk = new CipherClerk();
			
			String hMacKey = getSessionKey(smc).toString();
			
			
			// get SOAP envelope
			SOAPMessage msg = smc.getMessage();
	        SOAPPart sp = msg.getSOAPPart();
	        SOAPEnvelope se = sp.getEnvelope();
	        SOAPBody soapBody = se.getBody();
	        
	        //concatenate msg with key
	        String hMacMsgDig = digestMessage(convertToString(soapBody)+hMacKey);
	        
	        // add header if it doesn't exist
	        SOAPHeader sh = se.getHeader();
	        if (sh == null)
	            sh = se.addHeader();
	
	        // add header MAC as header element (name, namespace prefix, namespace)
	        Name hMac = se.createName("hMac", "binas", "http://ws.binas.org/");
	        SOAPHeaderElement hMacElement = sh.addHeaderElement(hMac);
	        
	        // add ticket header element value
            hMacElement.setValue(hMacMsgDig);

		} catch (SOAPException e) {
			throw new RuntimeException(e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getMessage());
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		} 
		
		return true;
	}
	
	private String convertToString(SOAPBody element) throws TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
        DOMSource source = new DOMSource(element);
        StringWriter stringResult = new StringWriter();
        TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(stringResult));
        String message = stringResult.toString();
        return message;
    }
	
	private Key getSessionKey(SOAPMessageContext smc) {
		
		Key sessionKey = (Key) smc.get("SESSION_KEY");
		return sessionKey;
	}
	
	private String digestMessage(String s) throws NoSuchAlgorithmException {
		MessageDigest msgD = MessageDigest.getInstance("SHA-256");
		msgD.update(s.getBytes());
		byte[] digest = msgD.digest();
		return printHexBinary(digest);
	}

	/**
	 * The handleFault method is invoked for fault message processing.
	 */
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
		// nothing to clean up
	}

	/**
	 * Gets the header blocks that can be processed by this Handler instance. If
	 * null, processes all.
	 */
	@Override
	public Set<QName> getHeaders() {
		return null;
	}

}
